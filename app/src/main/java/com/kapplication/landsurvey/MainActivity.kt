package com.kapplication.landsurvey

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
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
    private val CD = LatLng(30.542434, 104.073449)

    private var mPermissionDenied = false
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mContext: Context
    private lateinit var mLocationReceiver: LocationReceiver
    //    private var mLocationManager: LocationManager? = null
    private var mCurrentLatLng: LatLng = CD

//    private val mLocationListener: LocationListener = object: LocationListener{
//        override fun onLocationChanged(location: Location) {
//            mCurrentLatLng = LatLng(location.latitude, location.longitude)
//            Log.i(TAG, "current location: ${mCurrentLatLng.toString()}")
//
//        }
//
//        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//        override fun onProviderEnabled(provider: String) {}
//        override fun onProviderDisabled(provider: String) {}
//    }

    private inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "LocationReceiver(${intent})")
            val extras = intent.extras
            if (extras.containsKey(LocationService.KEY_MY_LOCATION)) {
                val location = intent.getParcelableExtra<Location>(LocationService.KEY_MY_LOCATION)
                mCurrentLatLng = LatLng(location.latitude, location.longitude)
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)

//        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        try {
//            // Request location updates
//            mLocationManager?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0L, 0f, mLocationListener)
//        } catch(ex: SecurityException) {
//            Log.d(TAG, "Security Exception, no location available")
//        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?) {
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

    override fun onResume() {
        super.onResume()
        startLocationService()
    }

    override fun onPause() {
        super.onPause()
        stopLocationService()
    }


    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true)
        } else {
            // Access to the location has been granted to the app.
            mGoogleMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (mPermissionDenied) {
            PermissionUtils.PermissionDeniedDialog.newInstance(true).show(supportFragmentManager, "dialog")
            mPermissionDenied = false
        }
    }

    private fun startLocationService() {
        startService(Intent(mContext, LocationService::class.java))

        mLocationReceiver = LocationReceiver()
        registerReceiver(mLocationReceiver, IntentFilter(LocationService.ACTION_MY_LOCATION))
    }

    private fun stopLocationService() {
        stopService(Intent(mContext, LocationService::class.java))
        unregisterReceiver(mLocationReceiver)
    }
}
