package com.kapplication.landsurvey.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.kapplication.landsurvey.MainActivity
import com.kapplication.landsurvey.eviltransform.WGSPointer

private const val TAG = "LocationService"

class LocationService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    companion object {
        const val ACTION_LOCATION = "ACTION_LOCATION"
        const val KEY_LAST_LOCATION = "KEY_LAST_LOCATION"
        const val KEY_UPDATED_LOCATION = "KEY_UPDATED_LOCATION"
        const val KEY_SATELLITE_STATUS = "KEY_SATELLITE_STATUS"
    }

    private var mIsLocationUpdating = false
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationManager: LocationManager? = null

    private var mBinder: IBinder = LocationBinder()

    inner class LocationBinder : Binder() {
        fun getService() : LocationService {
            return this@LocationService
        }
    }

    private val mGnssCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus?) {
            super.onSatelliteStatusChanged(status)
            val intent = Intent(ACTION_LOCATION).putExtra(KEY_SATELLITE_STATUS, status?.satelliteCount ?: 0)
            LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = LocationRequest().apply {
            interval = 1000 * 2
            fastestInterval = 1000 * 2
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, object:android.location.LocationListener {
//            override fun onLocationChanged(p0: Location?) {
//                this@LocationService.onLocationChanged(p0)
//            }
//
//            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
//                Log.i(TAG, "onStatusChanged($p0: String?, $p1: Int, $p2: Bundle?)")
//            }
//
//            override fun onProviderEnabled(p0: String?) {
//                Log.i(TAG, "onProviderEnabled($p0: String?)")
//            }
//
//            override fun onProviderDisabled(p0: String?) {
//                Log.i(TAG, "onProviderEnabled($p0: String?)")
//            }
//
//        })
        mGoogleApiClient?.connect()
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        mLocationManager?.unregisterGnssStatusCallback(mGnssCallback)
        stopLocationUpdates()
        mGoogleApiClient?.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "google play service connected.")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
//
//        //get my location by request last location.
//        val lastLocation = mFusedLocationClient!!.lastLocation
//        lastLocation.addOnSuccessListener { location: Location? ->
//            Log.i(TAG, "get last location(${location?.latitude}, ${location?.longitude})")
//            val intent = Intent(ACTION_LOCATION).putExtra(KEY_LAST_LOCATION, location)
//            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//        }
//
//        lastLocation.addOnFailureListener {
//            Log.w(TAG, "get last location failed.)")
//        }

        mLocationManager?.registerGnssStatusCallback(mGnssCallback)
        startLocationUpdate()
    }

    fun startLocationUpdate() {
        //check if the gps setting is on, if not, popup a dialog to switch it on
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest!!)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { _ ->
            requestLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
//                val intent = Intent(ACTION_LOCATION).putExtra(KEY_LOCATION_EXCEPTION, exception)
//                LocalBroadcastManager.getInstance(this).
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(mActivity, 2)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    //TODO test location data. this should be a interface callback
    private var mActivity: MainActivity? = null
    fun setMainActivity(activity: MainActivity) {
        mActivity = activity
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        if (!mIsLocationUpdating) {
            val task = mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            Log.d(TAG, "requestLocation task.isSuccessful: ${task.isSuccessful}")
            mIsLocationUpdating = true
        }
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d(TAG, "onLocationResult")
            onLocationChanged(locationResult.lastLocation)
        }
    }

    //register for location updates with onLocationChanged:
    fun onLocationChanged(location: Location?) {
        // New location has now been determined
        val msg = "Updated Location: Latlng(${location!!.latitude}, ${location.longitude})," +
                " accuracy(${location.accuracy}), altitude(${location.altitude})," +
                " satellites(${location.extras?.getInt("satellites")})," +
                " extras(${location.extras?.toString()})"
        Log.d(TAG, "onLocationChanged: $msg")

        // convert wgs pointer the gcj pointer
        val wgsPointer = WGSPointer(location.latitude, location.longitude)
        val gcjPointer = wgsPointer.toGCJPointer()

        location.latitude = gcjPointer.latitude
        location.longitude = gcjPointer.longitude

        Log.e(TAG, "WGS($wgsPointer) --> GCJ($gcjPointer)")

        val intent = Intent(ACTION_LOCATION).putExtra(KEY_UPDATED_LOCATION, location)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun stopLocationUpdates() {
        if (mIsLocationUpdating) {
            Log.d(TAG, "stopLocationUpdates")
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            mIsLocationUpdating = false
        }
    }


    override fun onConnectionSuspended(i: Int) {
        Log.i(TAG, "google play service suspended.")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.w(TAG, "google play service connect failed($connectionResult).")
    }
}
