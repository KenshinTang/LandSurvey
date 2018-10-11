package com.kapplication.landsurvey.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class LocationService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private val TAG = "LocationService"

    companion object {
        val ACTION_LOCATION = "ACTION_LOCATION"
        val KEY_LAST_LOCATION = "KEY_LAST_LOCATION"
        val KEY_UPDATED_LOCATION = "KEY_UPDATED_LOCATION"
    }

    private var mIsLocationUpdating = false
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var mBinder: IBinder = LocationBinder()

    inner class LocationBinder : Binder() {
        fun getService() : LocationService {
            return this@LocationService
        }
    }

    override fun onCreate() {
        super.onCreate()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

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
        stopLocationUpdates()
        mGoogleApiClient?.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "google play service connected.")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //get my location by request last location.
        val lastLocation = mFusedLocationClient!!.lastLocation
        lastLocation.addOnSuccessListener { location: Location? ->
            Log.i(TAG, "last location(${location?.latitude}, ${location?.longitude})")
            val intent = Intent(ACTION_LOCATION).putExtra(KEY_LAST_LOCATION, location)
            sendBroadcast(intent)
        }

        //check if the gps setting is on, if not, popup a dialog to switch it on
        checkLocationSettings()
    }

    private fun checkLocationSettings() {
        mLocationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest!!)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { _ ->
            startLocationUpdate()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
//                val intent = Intent(ACTION_LOCATION).putExtra(KEY_LOCATION_EXCEPTION, exception)
//                sendBroadcast(intent)
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

    private var mActivity: Activity? = null
    public fun setMainActivity(activity: Activity) {
        mActivity = activity
    }

    @SuppressLint("MissingPermission")
    public fun startLocationUpdate() {
        val task = mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        Log.d(TAG, "startLocationUpdate task.isSuccessful: ${task.isSuccessful}")
        mIsLocationUpdating = true
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
        val msg = "Updated Location: " +
                java.lang.Double.toString(location!!.latitude) + "," +
                java.lang.Double.toString(location.longitude) + "," +
                location.accuracy + "," +
                location.extras?.getInt("satellites") + "," +
                location.extras?.toString()
        Log.d(TAG, "onLocationChanged: $msg")

        val intent = Intent(ACTION_LOCATION).putExtra(KEY_UPDATED_LOCATION, location)
        sendBroadcast(intent)
    }

    public fun stopLocationUpdates() {
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
