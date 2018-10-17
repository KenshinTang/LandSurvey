package com.kapplication.landsurvey

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kapplication.landsurvey.model.Mode
import com.kapplication.landsurvey.service.LocationService
import com.kapplication.landsurvey.utils.PermissionUtils
import mehdi.sakout.fancybuttons.FancyButton
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG: String = "MainActivity"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val REQUEST_CHECK_SETTINGS = 2
    private val CD = LatLng(30.542434, 104.073449)

    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mContext: Context

    private var mMarker: BitmapDescriptor? = null
    private var mLocationService: LocationService? = null
    private lateinit var mLocationReceiver: LocationReceiver

    private var mCurrentLatLng: LatLng = CD
    private var mCurrentMode: Mode = Mode.AUTOMATIC

    private var mPilingButton: FancyButton? = null
    private var mStartStopButton: FancyButton? = null
    private var mSatelliteTextView: TextView? = null
    private var mPrecisionTextView: TextView? = null
    private var mPointsTextView: TextView? = null
    private var mAreaTextView: TextView? = null
    private var mPerimeterTextView: TextView? = null
    private var mLatLngTextView: TextView? = null

    private var mBoundOnLocationService = false
    private var mIsDrawerShowing = true
    private var mPermissionDenied = false
    private var mIsMeasuring = false

    private val mPoints: LinkedList<LatLng> = LinkedList()

    private inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "LocationReceiver(${intent})")
            val extras = intent.extras
            when {
                extras.containsKey(LocationService.KEY_LAST_LOCATION) -> {
                    val location = intent.getParcelableExtra<Location>(LocationService.KEY_LAST_LOCATION)
                    if (location != null) {
                        updateUI(location)
                        mCurrentLatLng = LatLng(location.latitude, location.longitude)
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 17f))
                    }
                }
                extras.containsKey(LocationService.KEY_UPDATED_LOCATION) -> {
                    val location = intent.getParcelableExtra<Location>(LocationService.KEY_UPDATED_LOCATION)
                    mCurrentLatLng = LatLng(location.latitude, location.longitude)
                    updateUI(location)
                    if (mCurrentMode == Mode.AUTOMATIC && mIsMeasuring) {
                        mPoints.add(mCurrentLatLng)
                        mGoogleMap.addMarker(MarkerOptions().position(mCurrentLatLng).icon(mMarker))
                    }
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

        initView()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true)
        }

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
            setOnMyLocationButtonClickListener {
                Toast.makeText(mContext, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 16f))
                true
            }

            setOnMyLocationClickListener {
                Toast.makeText(mContext, "Current location: ${it.latitude}, ${it.longitude}", Toast.LENGTH_SHORT).show()
            }

            setOnMapLongClickListener {
                if (mCurrentMode == Mode.MANUAL && mIsMeasuring) {
                    mPoints.add(it)
                    mGoogleMap.addMarker(MarkerOptions().position(it).icon(mMarker))
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
            mGoogleMap.uiSettings.isZoomControlsEnabled = true
            mGoogleMap.uiSettings.isMapToolbarEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionsResult(request=$requestCode)")
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Enable the my location layer if the permission has been granted.
                    enableMyLocation()
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
        if (view is RadioButton) {
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
        } else if (view is FancyButton) {
            when (view.id) {
                R.id.button_piling -> {
                    Log.d(TAG, "piling button clicked.")
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
                }
            }
        }
    }

    private fun startMeasuring() {
        mStartStopButton?.setIconResource(R.drawable.stop)
        mStartStopButton?.setText(resources.getString(R.string.stop_measuring))
        mStartStopButton?.setBackgroundColor(getColor(R.color.stopButtonColor))
        mIsMeasuring = true
    }

    private fun stopMeasuring() {
        mStartStopButton?.setIconResource(R.drawable.start)
        mStartStopButton?.setText(resources.getString(R.string.start_measuring))
        mStartStopButton?.setBackgroundColor(getColor(R.color.defaultButtonColor))
        mIsMeasuring = true
    }

    private fun updateUI(location: Location?) {
        location ?: return
        var satelliteNums = location.extras?.getInt("satellites", 0)
        if (satelliteNums == null) {
            satelliteNums = 0
        }
        mSatelliteTextView?.text = satelliteNums.toString()
        mPrecisionTextView?.text = (location.accuracy.toInt().toString() + "m")
        mLatLngTextView?.text = (location.latitude.toString() + ", " + location.longitude.toString())
    }
}
