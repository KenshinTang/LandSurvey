package com.kapplication.landsurvey

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.kapplication.landsurvey.service.LocationService
import com.kapplication.landsurvey.utils.PermissionUtils

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG: String = "MainActivity"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val REQUEST_CHECK_SETTINGS = 2
    private val CD = LatLng(30.542434, 104.073449)

    private var mPermissionDenied = false
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mContext: Context
    private lateinit var mLocationReceiver: LocationReceiver
    private var mCurrentLatLng: LatLng = CD

    private inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "LocationReceiver(${intent})")
            val extras = intent.extras
            when {
                extras.containsKey(LocationService.KEY_LAST_LOCATION) -> {
                    val location = intent.getParcelableExtra<Location>(LocationService.KEY_LAST_LOCATION)
                    mCurrentLatLng = LatLng(location.latitude, location.longitude)
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 16f))
                }
                extras.containsKey(LocationService.KEY_UPDATED_LOCATION) -> {
                    val location = intent.getParcelableExtra<Location>(LocationService.KEY_UPDATED_LOCATION)
                    mCurrentLatLng = LatLng(location.latitude, location.longitude)
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 16f))
                }
                extras.containsKey(LocationService.KEY_LOCATION_EXCEPTION) -> {
                    val exception = intent.getSerializableExtra(LocationService.KEY_LOCATION_EXCEPTION) as ResolvableApiException
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(mContext as Activity, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true)
        }
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()
        startLocationService()
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
            //            moveCamera(CameraUpdateFactory.newLatLngZoom(CD, 16f))
//            addMarker(MarkerOptions().position(CD))
            setOnMyLocationButtonClickListener {
                Toast.makeText(mContext, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 16f))
                true
            }

            setOnMyLocationClickListener {
                Toast.makeText(mContext, "Current location: ${it.latitude}, ${it.longitude}", Toast.LENGTH_SHORT).show()
            }
        }

        enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        stopLocationService()
    }


    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        } else {
            // Access to the location has been granted to the app.
            mGoogleMap.isMyLocationEnabled = true
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
                        //TODO LocationService.requestLocationUpdate()
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

    private fun startLocationService() {
        startService(Intent(mContext, LocationService::class.java))

        mLocationReceiver = LocationReceiver()
        registerReceiver(mLocationReceiver, IntentFilter(LocationService.ACTION_LOCATION))
    }

    private fun stopLocationService() {
        stopService(Intent(mContext, LocationService::class.java))
        unregisterReceiver(mLocationReceiver)
    }
}
