package com.kapplication.landsurvey

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.MarkerManager
import com.google.maps.android.SphericalUtil
import com.kapplication.landsurvey.eviltransform.WGSPointer
import com.kapplication.landsurvey.fragments.*
import com.kapplication.landsurvey.model.Mode
import com.kapplication.landsurvey.model.Path
import com.kapplication.landsurvey.model.Record
import com.kapplication.landsurvey.utils.PermissionUtils
import com.kapplication.landsurvey.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_drawer.*
import mehdi.sakout.fancybuttons.FancyButton
import java.util.*
import kotlin.collections.ArrayList

private const val TAG: String = "MainActivity"
private const val PERMISSION_REQUEST_CODE = 1
private const val REQUEST_CHECK_SETTINGS = 2


class MainActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMapLoadedCallback,
        Path.OnPathChangeListener,
        GoogleMap.OnMarkerClickListener,
        SaveDialogFragment.SaveDialogListener,
        DetailDrawerFragment.OnShowSurveyDetailListener,
        DeleteDialogFragment.OnDeleteDialogListener {

    private val COLOR_LINE = Color.rgb(56,148,255)
    private val COLOR_AREA = Color.argb(100, 56,148,255)

    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mContext: Context

    private lateinit var mMarker: BitmapDescriptor
    private lateinit var mFirstMarker: BitmapDescriptor
    private lateinit var mMarkerCollection: MarkerManager.Collection

    private var mCurrentLatLng: LatLng = LatLng(0.0, 0.0)
    private lateinit var mCurrentLocation: Location
    private var mCurrentMode: Mode = Mode.AUTOMATIC
    private var mStartTime: String = ""
    private var mEndTime: String = ""
    private var mPerimeter: Double = 0.0
    private var mArea: Double = 0.0

    private var mAltitudes = ArrayList<Double>()
    private var mPolyline: Polyline? = null

    private var mPolygon: Polygon? = null
    private var mIsDrawerShowing = true
    private var mPermissionDenied = false
    private var mIsMeasuring = false

    private var mIsGetLocationFirstTime = true
    private val mPath: Path = Path()

    private lateinit var mLocationManager: LocationManager

    private val mLocationListener = object: LocationListener {
        override fun onLocationChanged(location: Location) {
            val msg = "Updated Location: Latlng(${location.latitude}, ${location.longitude})," +
                    " accuracy(${location.accuracy}), altitude(${location.altitude})," +
                    " satellites(${location.extras?.getInt("satellites")})," +
                    " extras(${location.extras?.toString()})"
            Log.d(TAG, "onLocationChanged: $msg")

            // convert wgs pointer the gcj pointer
            val wgsPointer = WGSPointer(location.latitude, location.longitude)
            val gcjPointer = wgsPointer.toGCJPointer()

            location.latitude = gcjPointer.latitude
            location.longitude = gcjPointer.longitude

            mCurrentLocation = location
            mCurrentLatLng = LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude)
            updateGPSInfo(mCurrentLocation)
            if (mCurrentMode == Mode.AUTOMATIC && mIsMeasuring && mPath.add(mCurrentLatLng, 2)) {
                if (mPath.size() == 1) {
                    val markerOption = MarkerOptions().position(mCurrentLatLng).icon(mFirstMarker)
                    mMarkerCollection.addMarker(markerOption)
                }
            }
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            Log.i(TAG, "onStatusChanged($p0: String?, $p1: Int, $p2: Bundle?)")
        }

        override fun onProviderEnabled(p0: String?) {
            Log.i(TAG, "onProviderEnabled($p0: String?)")
        }

        override fun onProviderDisabled(p0: String?) {
            Log.i(TAG, "onProviderEnabled($p0: String?)")
        }
    }

    private val mGnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus?) {
            super.onSatelliteStatusChanged(status)
            textView_satellite_content.text = status?.satelliteCount.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        hideVirtualKey()
        setContentView(R.layout.activity_main)
        init()

        PermissionUtils.checkPermissionAndRequest(this, PERMISSION_REQUEST_CODE, true)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationManager.registerGnssStatusCallback(mGnssStatusCallback)
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, mLocationListener)
    }

    private fun hideVirtualKey() {
        val decorView: View = window.decorView
        val uiOptions:Int = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION).or(View.SYSTEM_UI_FLAG_FULLSCREEN).or(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }

    private fun init() {
        mContext = this
        mPath.setOnPathChangeListener(this)

        // use custom title bar
        toolbar.title = ""
        setSupportActionBar(toolbar)

        mMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker)
        mFirstMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_first)

        imageView_drawer_handler.setOnClickListener {
            layout_drawer.animate().translationX(if (mIsDrawerShowing) -360f else 0f)?.start()
            layout_mode.animate().translationX(if (mIsDrawerShowing) -160f else 0f)?.start()
            mIsDrawerShowing = !mIsDrawerShowing
        }

        layout_drawer.animate().setListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
                imageView_drawer_handler.setImageResource(if (mIsDrawerShowing) R.drawable.arrow_left else R.drawable.arrow_right)
            }
        })


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
        mMarkerCollection = MarkerManager(mGoogleMap).newCollection()

        with(mGoogleMap) {
            setOnMarkerClickListener(this@MainActivity)

            setOnMyLocationButtonClickListener {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 18f))
                true
            }

            setOnMapLongClickListener {
                if (mCurrentMode == Mode.MANUAL && mIsMeasuring) {
                    mPath.add(it)
                    val markerOption = MarkerOptions().position(it).icon(if (mPath.size() == 1) mFirstMarker else mMarker)
                    mMarkerCollection.addMarker(markerOption)
                }
            }
        }

        enableMyLocation()
    }

    override fun onMapLoaded() {
        Log.i(TAG, "onMapLoaded")
    }

    override fun onPause() {
        Log.i(TAG, "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()

        mLocationManager.removeUpdates(mLocationListener)
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback)
    }

    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed")
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val dialog = SettingDialogFragment()
            dialog.show(fragmentManager, "SettingDialogFragment")
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
                    requestLocationUpdates()
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
                        requestLocationUpdates()
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
                when (view.id) {
                    R.id.radio_auto -> {
                        Log.d(TAG, "Change to auto mode.")
                        mCurrentMode = Mode.AUTOMATIC
                        button_piling.isEnabled = false
                    }
                    R.id.radio_piling -> {
                        Log.d(TAG, "Change to piling mode.")
                        mCurrentMode = Mode.PILING
                        button_piling.isEnabled = true
                    }
                    R.id.radio_manual -> {
                        Log.d(TAG, "Change to manual mode.")
                        mCurrentMode = Mode.MANUAL
                        button_piling.isEnabled = false
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
                        mMarkerCollection?.addMarker(markerOption)
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
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        button_start_stop.setIconResource(R.drawable.stop)
        button_start_stop.setText(resources.getString(R.string.stop_measuring))
        button_start_stop.setBackgroundColor(getColor(R.color.stopButtonColor))
        mIsMeasuring = true
        mStartTime = Utils.formatTime(System.currentTimeMillis())
    }

    private fun stopMeasuring() {
        button_start_stop.setIconResource(R.drawable.start)
        button_start_stop.setText(resources.getString(R.string.start_measuring))
        button_start_stop.setBackgroundColor(getColor(R.color.defaultButtonColor))
        //String.format("%.2f",(SphericalUtil.computeArea(mPath.getList()))) + "㎡"
        textView_area_content_main.text = Utils.convertArea(this, SphericalUtil.computeArea(mPath.getList()), 2)
        textView_perimeter_content_main.text = String.format("%.2f",(SphericalUtil.computeLength(mPath.getList()))) + "m"
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

        if (mPath.size() > 1) {
            showSaveDialog()
        } else {
            Toast.makeText(this, "Not enough points to compute perimeter or area.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateGPSInfo(location: Location?) {
        location ?: return
        updateAltitudeRange(location.altitude)
        textView_precision_content.text = (location.accuracy.toInt().toString() + "m")
        if (mIsGetLocationFirstTime) {
            textView_latlng.text = String.format("%.6f", location.latitude) + ", " + String.format("%.6f", location.longitude)
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 18f))
            button_start_stop.isEnabled = true
            textView_gps_info.setText(R.string.gps_is_ready)
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

        textView_perimeter_content_main.text = "${String.format("%.2f",mPerimeter)}m"
        textView_area_content_main.text = Utils.convertArea(this, mArea, 2)

        textView_points_content.text = mPath.getList().size.toString()
        if (!mPath.getList().isEmpty()) {
            textView_latlng.text = String.format("%.6f", mPath.getList().last?.latitude) + ", " +
                    String.format("%.6f", mPath.getList().last?.longitude)
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (mIsMeasuring) {
            showDeleteDialog(marker)
        } else {
            val detailFragment = supportFragmentManager.findFragmentByTag("DetailDrawerFragment")
            if (detailFragment != null) {
                (detailFragment as DetailDrawerFragment).onMarkerClick(marker)
            }
        }
        return true
    }

    private fun showSaveDialog() {
        val dialog = SaveDialogFragment()
        dialog.setOnSaveDialogListener(this)
        dialog.show(fragmentManager, "SaveDialogFragment")
    }

    private fun showDeleteDialog(marker: Marker?) {
        val dialog = DeleteDialogFragment()
        dialog.setOnDeleteDialogListener(this@MainActivity)
        dialog.setMarker(marker)
        dialog.show(fragmentManager, "DeleteDialogFragment")
    }

    override fun onDeleteClick(dialog: DialogFragment, marker: Marker?) {
        val position = marker?.position
        val index = mPath.getList().indexOf(position)

        mMarkerCollection?.remove(marker)
        mPath.remove(position!!)

        if (index == 0) {
            mMarkerCollection?.markers?.first()?.setIcon(mFirstMarker)
        }

        dialog.dismiss()
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
            val markerOption = MarkerOptions().position(point).icon(if (index == 0) mFirstMarker else mMarker)
            mMarkerCollection?.addMarker(markerOption)
        }
        mPolygon = mGoogleMap.addPolygon(PolygonOptions()
                .addAll(points)
                .fillColor(COLOR_AREA)
                .strokeWidth(4f)
                .strokeColor(COLOR_LINE)
        )
    }
}
