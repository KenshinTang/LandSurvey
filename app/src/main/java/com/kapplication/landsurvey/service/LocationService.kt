package com.kapplication.landsurvey.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices

class LocationService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private val TAG = "LocationService"
    private val ACTION_MY_LOCATION = "ACTION_MY_LOCATION"

    companion object {
        val ACTION_MY_LOCATION = "ACTION_MY_LOCATION"
        val KEY_MY_LOCATION = "KEY_MY_LOCATION"
    }

    private var mGoogleApiClient: GoogleApiClient? = null
//    private var mLocationRequest: LocationRequest? = null

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
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mGoogleApiClient?.disconnect()
    }


    @SuppressLint("MissingPermission")
    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "google play service connected.")
        val lastLocation = LocationServices.getFusedLocationProviderClient(this).lastLocation
        lastLocation.addOnSuccessListener { location: Location? ->
            Log.i(TAG, "last location(${location?.latitude}, ${location?.longitude})")
            val intent = Intent(ACTION_MY_LOCATION).putExtra(KEY_MY_LOCATION, location)
            sendBroadcast(intent)
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Log.i(TAG, "google play service suspended.")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.w(TAG, "google play service connect failed($connectionResult).")
    }
}
