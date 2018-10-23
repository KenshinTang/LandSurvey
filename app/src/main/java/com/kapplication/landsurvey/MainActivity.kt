package com.kapplication.landsurvey

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.app.DialogFragment
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.kapplication.landsurvey.fragments.DetailDrawerFragment
import com.kapplication.landsurvey.fragments.ListDrawerFragment
import com.kapplication.landsurvey.fragments.SaveDialogFragment
import com.kapplication.landsurvey.model.Mode
import com.kapplication.landsurvey.model.Path
import com.kapplication.landsurvey.model.Record
import com.kapplication.landsurvey.service.LocationService
import com.kapplication.landsurvey.utils.PermissionUtils
import com.kapplication.landsurvey.utils.Utils
import mehdi.sakout.fancybuttons.FancyButton
import java.util.*
import kotlin.collections.ArrayList

private const val TAG: String = "MainActivity"
private const val PERMISSION_REQUEST_CODE = 1
private const val REQUEST_CHECK_SETTINGS = 2


class MainActivity : AppCompatActivity(),
        OnMapReadyCallback,
        Path.OnPathChangeListener,
        GoogleMap.OnMarkerClickListener,
        SaveDialogFragment.SaveDialogListener,
        DetailDrawerFragment.OnShowSurveyDetailListener {

    private val CD = LatLng(30.542434, 104.073449)
    private val COLOR_LINE = Color.rgb(56,148,255)
    private val COLOR_AREA = Color.argb(100, 56,148,255)

    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mContext: Context

    private var mMarker: BitmapDescriptor? = null
    private var mFirstMarker: BitmapDescriptor? = null
    private var mLocationService: LocationService? = null
    private lateinit var mLocationReceiver: LocationReceiver

    private var mCurrentLatLng: LatLng = CD
    private var mCurrentLocation: Location? = null
    private var mCurrentMode: Mode = Mode.AUTOMATIC
    private var mStartTime: String = ""
    private var mEndTime: String = ""
    private var mPerimeter: Double = 0.0
    private var mArea: Double = 0.0
    private var mAltitudes = ArrayList<Double>()

    private var mPilingButton: FancyButton? = null
    private var mStartStopButton: FancyButton? = null
    private var mSatelliteTextView: TextView? = null
    private var mPrecisionTextView: TextView? = null
    private var mPointsTextView: TextView? = null
    private var mAreaTextView: TextView? = null
    private var mPerimeterTextView: TextView? = null
    private var mLatLngTextView: TextView? = null
    private var mPolyline: Polyline? = null
    private var mPolygon: Polygon? = null

    private var mBoundOnLocationService = false
    private var mIsDrawerShowing = true
    private var mPermissionDenied = false
    private var mIsMeasuring = false
    private var mIsGetLocationFirstTime = true

    private val mPath: Path = Path()

    private inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "LocationReceiver($intent)")
            val extras = intent.extras
            when {
//                extras.containsKey(LocationService.KEY_LAST_LOCATION) -> {
//                    mCurrentLocation = intent.getParcelableExtra(LocationService.KEY_LAST_LOCATION)
//                    if (mCurrentLocation != null) {
//                        updateGPSInfo(mCurrentLocation, true)
//                        mCurrentLatLng = LatLng(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
//                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 18f))
//                    }
//                }
                extras.containsKey(LocationService.KEY_UPDATED_LOCATION) -> {
                    mCurrentLocation = intent.getParcelableExtra(LocationService.KEY_UPDATED_LOCATION)
                    mCurrentLatLng = LatLng(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
                    updateGPSInfo(mCurrentLocation)
                    if (mCurrentMode == Mode.AUTOMATIC && mIsMeasuring) {
                        mPath.add(mCurrentLatLng, 2)
                        val markerOption = MarkerOptions().position(mCurrentLatLng).icon(if (mPath.size() == 1) mFirstMarker else mMarker)
                        mGoogleMap.addMarker(markerOption)
                    }
                }
                extras.containsKey(LocationService.KEY_SATELLITE_STATUS) -> {
                    val satelliteCount = intent.getIntExtra(LocationService.KEY_SATELLITE_STATUS, 0)
                    mSatelliteTextView?.text = satelliteCount.toString()
                }
            }
        }

    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocationService.LocationBinder
            mLocationService = binder.getService()
            mBoundOnLocationService = true
            mLocationService?.setMainActivity(mContext as MainActivity)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBoundOnLocationService = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        hideVirtualKey()

        setContentView(R.layout.activity_main)
        mContext = this
        mPath.setOnPathChangeListener(this)

        initView()

        PermissionUtils.checkPermissionAndRequest(this, PERMISSION_REQUEST_CODE, true)

        mLocationReceiver = LocationReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver, IntentFilter(LocationService.ACTION_LOCATION))

        bindService(Intent(this, LocationService::class.java), mConnection, Context.BIND_AUTO_CREATE)
    }

    private fun hideVirtualKey() {
        val decorView: View = window.decorView
        val uiOptions:Int = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION).or(View.SYSTEM_UI_FLAG_FULLSCREEN).or(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }

    private fun initView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        mPilingButton = findViewById(R.id.button_piling)
        mStartStopButton = findViewById(R.id.button_start_stop)
        mSatelliteTextView = findViewById(R.id.textView_satellite_content)
        mPrecisionTextView = findViewById(R.id.textView_precision_content)
        mPointsTextView = findViewById(R.id.textView_points_content)
        mAreaTextView = findViewById(R.id.textView_area_content)
        mPerimeterTextView = findViewById(R.id.textView_perimeter_content)
        mLatLngTextView = findViewById(R.id.textView_latlng)

        mMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker)
        mFirstMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_first)

        val drawerLayout: LinearLayout = findViewById(R.id.layout_drawer)
        val modeLayout: LinearLayout = findViewById(R.id.layout_mode)
        val drawerHandler: ImageView = findViewById(R.id.imageView_drawer_handler)

        val drawerLayoutAnimator = drawerLayout.animate()
        val modeLayoutAnimator = modeLayout.animate()

        drawerLayoutAnimator?.setListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
                drawerHandler.setImageResource(if (mIsDrawerShowing) R.drawable.arrow_left else R.drawable.arrow_right)
            }
        })
        drawerHandler.setOnClickListener {
            drawerLayoutAnimator?.translationX(if (mIsDrawerShowing) -360f else 0f)?.start()
            modeLayoutAnimator?.translationX(if (mIsDrawerShowing) -160f else 0f)?.start()
            mIsDrawerShowing = !mIsDrawerShowing
        }

        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()
    }

    override fun onResumeFragments() {
        Log.i(TAG, "onResumeFragments")
        super.onResumeFragments()
        if (mPermissionDenied) {
            PermissionUtils.PermissionDeniedDialog.newInstance(true).show(supportFragmentManager, "dialog")
            mPermissionDenied = false
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        Log.i(TAG, "onMapReady")
        map ?: return
        mGoogleMap = map

        with(mGoogleMap) {
            setOnMarkerClickListener(this@MainActivity)

            setOnMyLocationButtonClickListener {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 18f))
                true
            }

//            setOnMyLocationClickListener {
//                Toast.makeText(mContext, "Current location: ${it.latitude}, ${it.longitude}", Toast.LENGTH_SHORT).show()
//            }

            setOnMapLongClickListener {
                if (mCurrentMode == Mode.MANUAL && mIsMeasuring) {
                    mPath.add(it)
                    val markerOption = MarkerOptions().position(it).icon(if (mPath.size() == 1) mFirstMarker else mMarker)
                    mGoogleMap.addMarker(markerOption)
                }
            }
        }

        enableMyLocation()
    }

    override fun onPause() {
        Log.i(TAG, "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver)

        if (mBoundOnLocationService) {
            unbindService(mConnection)
            mBoundOnLocationService = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        } else {
            // Access to the location has been granted to the app.
            mGoogleMap.isMyLocationEnabled = true
            mGoogleMap.uiSettings.isMyLocationButtonEnabled = true
            mGoogleMap.uiSettings.isZoomControlsEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionsResult(request=$requestCode)")
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (PermissionUtils.isPermissionGranted(permissions, grantResults)) {
                    // Enable the my location layer if the permission has been granted.
                    enableMyLocation()
                    mLocationService?.startLocationUpdate()
                } else {
                    // Display the missing permission error dialog when the fragments resume.
                    mPermissionDenied = true
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.i(TAG, "User agreed to make required location settings changes")
                        mLocationService?.startLocationUpdate()
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.w(TAG, "User chose not to make required location settings changes")
                        finish()
                    }
                }
            }
        }
    }

    override fun shouldShowRequestPermissionRationale(permission: String?): Boolean {
        return false
    }

    fun onButtonClick(view: View) {
        when (view) {
            is RadioButton -> {
                val checked = view.isChecked

                when (view.id) {
                    R.id.radio_auto -> {
                        Log.d(TAG, "Change to auto mode.")
                        mCurrentMode = Mode.AUTOMATIC
                        mPilingButton?.isEnabled = false
                    }
                    R.id.radio_piling -> {
                        Log.d(TAG, "Change to piling mode.")
                        mCurrentMode = Mode.PILING
                        mPilingButton?.isEnabled = true
                    }
                    R.id.radio_manual -> {
                        Log.d(TAG, "Change to manual mode.")
                        mCurrentMode = Mode.MANUAL
                        mPilingButton?.isEnabled = false
                    }
                }
            }
            is FancyButton -> when (view.id) {
                R.id.button_piling -> {
                    Log.d(TAG, "piling button clicked.")
                    updateGPSInfo(mCurrentLocation)
                    if (mIsMeasuring) {
                        mPath.add(mCurrentLatLng)
                        val markerOption = MarkerOptions().position(mCurrentLatLng).icon(if (mPath.size() == 1) mFirstMarker else mMarker)
                        mGoogleMap.addMarker(markerOption)
                    }
                }
                R.id.button_start_stop -> {
                    Log.d(TAG, "${if (mIsMeasuring) "stop" else "start"} button clicked.")
                    if (!mIsMeasuring) {
                        startMeasuring()
                    } else {
                        stopMeasuring()
                    }
                }
                R.id.button_show_history -> {
                    Log.d(TAG, "history button clicked.")
                    val list = ListDrawerFragment.newInstance("", "")
                    supportFragmentManager.beginTransaction().replace(R.id.drawer_fragment, list)
                            .addToBackStack(null).commit()
                }
            }
            is ImageView -> when (view.id) {
                R.id.imageView_map_switcher -> {
                    if (mGoogleMap.mapType == GoogleMap.MAP_TYPE_SATELLITE) {
                        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    } else if (mGoogleMap.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                        mGoogleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    }
                }
            }
        }
    }

    private fun startMeasuring() {
        mGoogleMap.clear()
        mPath.clear()
        mAltitudes.clear()
        mStartStopButton?.setIconResource(R.drawable.stop)
        mStartStopButton?.setText(resources.getString(R.string.stop_measuring))
        mStartStopButton?.setBackgroundColor(getColor(R.color.stopButtonColor))
        mIsMeasuring = true
        mStartTime = Utils.formatTime(System.currentTimeMillis())
    }

    private fun stopMeasuring() {
        mStartStopButton?.setIconResource(R.drawable.start)
        mStartStopButton?.setText(resources.getString(R.string.start_measuring))
        mStartStopButton?.setBackgroundColor(getColor(R.color.defaultButtonColor))
        mAreaTextView?.text = String.format("%.2f",(SphericalUtil.computeArea(mPath.getList()))) + "㎡"
        mPerimeterTextView?.text = String.format("%.2f",(SphericalUtil.computeLength(mPath.getList()))) + "m"
        if (!mPath.getList().isEmpty()) {
            mPolygon = mGoogleMap.addPolygon(PolygonOptions()
                    .addAll(mPath.getList())
                    .fillColor(COLOR_AREA)
                    .strokeWidth(4f)
                    .strokeColor(COLOR_LINE)
            )
        }
        mPath.print()
        mIsMeasuring = false
        mEndTime = Utils.formatTime(System.currentTimeMillis())

        showSaveDialog()
    }

    private fun updateGPSInfo(location: Location?) {
        location ?: return
        updateAltitudeRange(location.altitude)
        mPrecisionTextView?.text = (location.accuracy.toInt().toString() + "m")
        if (mIsGetLocationFirstTime) {
            mLatLngTextView?.text = String.format("%.6f", location.latitude) + ", " + String.format("%.6f", location.longitude)
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 18f))
            mIsGetLocationFirstTime = false
        }
    }

    private fun updateAltitudeRange(altitude: Double) {
        if (!mIsMeasuring) {
            return
        }

        when (mAltitudes.size) {
            0 -> mAltitudes.add(altitude)
            1 -> {
                if (altitude <= mAltitudes[0]) {
                    mAltitudes.add(mAltitudes[0])
                    mAltitudes[0] = altitude
                } else {
                    mAltitudes.add(altitude)
                }
            }
            2 -> {
                if (altitude < mAltitudes[0]) {
                    mAltitudes[0] = altitude
                } else if (altitude > mAltitudes[1]) {
                    mAltitudes[1] = altitude
                }
            }
        }
    }

    override fun onPathChanged() {
        mPolyline?.remove()
        mPolygon?.remove()
        mPolyline = mGoogleMap.addPolyline(PolylineOptions().addAll(mPath.getList()).width(4f).color(COLOR_LINE))

        mPerimeter = SphericalUtil.computeLength(mPath.getList())
        mArea = SphericalUtil.computeArea(mPath.getList())

        mPerimeterTextView?.text = "${String.format("%.2f",mPerimeter)}m"
        mAreaTextView?.text = "${String.format("%.2f",mArea)}㎡"

        mPointsTextView?.text = mPath.getList().size.toString()
        if (!mPath.getList().isEmpty()) {
            mLatLngTextView?.text = String.format("%.6f", mPath.getList().last?.latitude) + ", " +
                    String.format("%.6f", mPath.getList().last?.longitude)
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (mIsMeasuring) {
            marker?.remove()
            mPath.remove(marker?.position!!)
        } else {
            val detailFragment = supportFragmentManager.findFragmentByTag("DetailDrawerFragment") as DetailDrawerFragment
            detailFragment.onMarkerClick(marker)
        }
        return true
    }

    private fun showSaveDialog() {
        val dialog = SaveDialogFragment()
        dialog.setOnSaveDialogListener(this)
        dialog.show(fragmentManager, "SaveDialogFragment")
    }

    override fun onSaveClick(dialog: DialogFragment, fileName: String) {
        Log.i(TAG, "onSaveClick($fileName)")
        val record = Record().apply {
            name = fileName
            startTime = mStartTime
            endTime = mEndTime
            perimeter = mPerimeter
            area = mArea
            altitudeRange =
                    when (mAltitudes.size) {
                        0 -> "0.0 ~ 0.0"
                        1 -> "${mAltitudes[0]} ~ ${mAltitudes[0]}"
                        else -> "${mAltitudes[0]} ~ ${mAltitudes[1]}"
                    }

            points = mPath.getList()
        }

        if (!record.writeToFile()) {
            Toast.makeText(this, "The file name is invalidate.", Toast.LENGTH_SHORT).show()
        } else {
            dialog.dismiss()
            mGoogleMap.clear()
            mPath.clear()
        }
    }

    override fun onCancelClick(dialog: DialogFragment) {
        Log.i(TAG, "onCancelClick")
    }

    override fun onShowSurveyDetail(record: Record?) {
        record?: return
        mGoogleMap.clear()
        val points: LinkedList<LatLng> = record.points
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.last, 18f))

        for ((index, point) in points.withIndex()) {
            val markerOptions = MarkerOptions().position(point).icon(if (index == 0) mFirstMarker else mMarker)
            mGoogleMap.addMarker(markerOptions)
        }
        mPolygon = mGoogleMap.addPolygon(PolygonOptions()
                .addAll(points)
                .fillColor(COLOR_AREA)
                .strokeWidth(4f)
                .strokeColor(COLOR_LINE)
        )
    }
}
