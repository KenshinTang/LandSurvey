package com.kapplication.landsurvey.location


import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

class LocationService : Service() {
    private var broadcastIntent: Intent? = null
    private var recordCountStatus = 0
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null

    internal var mLocationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            Log.d(TAG, "onLocationResult")
            onLocationChanged(locationResult.lastLocation)
        }
    }

    override fun onCreate() {    // initialize only once
        super.onCreate()
        broadcastIntent = Intent(MY_LOCATION)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        mSettingsClient = LocationServices.getSettingsClient(this)
        startLocationUpdates()

        // getLastLocation();
    }

    override fun onBind(intent: Intent): IBinder? {
        //Context.bindService() to obtain a persistent connection to a service.  does not call onStartCommand(). The client will receive the IBinder object that the service returns from its onBind(Intent) method,
        //The service will remain running as long as the connection is established
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {  //onStartCommand() can get called multiple times.
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
        //Service is not restarted , START_NOT_STICKY : if this service's process is killed while it is started (after returning from onStartCommand(Intent, int, int)), and there are no new start intents to deliver to it, then take the service out of the started state and don't recreate until a future explicit call to Context.startService(Intent).
        // Service is restarted if it gets terminated , START_STICKY or START_REDELIVER_INTENT are used for services that should only remain running while processing any commands sent to them.
        // if this service's process is killed while it is started (after returning from onStartCommand(Intent, int, int)), then leave it in the started state but don't retain this delivered intent.
    }

    // Trigger new location updates at interval
    protected fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates")
        createLocationRequest()
        buildLocationSettingsRequest()

        //settingsClient.checkLocationSettings(builder.build());
        //or
        // the Task object that validates the location settings
        //Check whether current location settings are satisfied:
        val task = mSettingsClient!!.checkLocationSettings(mLocationSettingsRequest)

        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            initializeLocationSettings(mLocationRequest)
        }

        task.addOnFailureListener { e ->
            val statusCode = (e as ApiException).statusCode
            when (statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    broadcastIntent!!.putExtra(GPS_NOT_ENABLED, mLocationRequest)
                    sendBroadcast(broadcastIntent)
                    Log.e(TAG, "Location settings are not satisfied. Attempting to upgrade location settings ")
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    val errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                    Log.e(TAG, errorMessage)
                }
            }
            /*if (e instanceof ResolvableApiException) {
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(this, 1);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }*/
        }
    }

    private fun createLocationRequest() {
        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = UPDATE_INTERVAL
        mLocationRequest!!.fastestInterval = FASTEST_INTERVAL
        /*
         * PRIORITIES
         * PRIORITY_BALANCED_POWER_ACCURACY -
         * PRIORITY_HIGH_ACCURACY -
         * PRIORITY_LOW_POWER -
         * PRIORITY_NO_POWER -
         * */
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun buildLocationSettingsRequest() {
        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        builder.setAlwaysShow(true)
        mLocationSettingsRequest = builder.build()
    }

    private fun initializeLocationSettings(mLocationRequest: LocationRequest?) {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        //         getLastLocation();
        Log.d(TAG, "requestLocationUpdates")
        val task = mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        Log.w(TAG, "requestLocationUpdates:" + task.isSuccessful)
        Log.w(TAG, "requestLocationUpdates:" + task.isComplete)
    }

    //Last known location
    /*
    The getLastLocation() method returns a Task that you can use to get a Location object with the latitude and longitude coordinates of a geographic location.
    The location object may be null in the following situations:
    Location is turned off in the device settings. The result could be null even if the last location was previously retrieved because disabling location also clears the cache.
    The device never recorded its location, which could be the case of a new device or a device that has been restored to factory settings.
    Google Play services on the device has restarted, and there is no active Fused Location Provider client that has requested location after the services restarted. To avoid this situation you can create a new client and request location updates yourself.
    */

    fun getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        //FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mFusedLocationClient!!.lastLocation
                .addOnSuccessListener { location ->
                    // GPS location can be null if GPS is switched off
                    if (location != null) {
                        onLocationChanged(location)
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Error trying to get Last Known Location")
                    e.printStackTrace()
                }
    }

    //register for location updates with onLocationChanged:
    fun onLocationChanged(location: Location?) {
        // New location has now been determined
        Log.d(TAG, "onLocationChanged")
        val msg = "Updated Location: " +
                java.lang.Double.toString(location!!.latitude) + "," +
                java.lang.Double.toString(location.longitude) + "," +
                location.accuracy + "," +
                location.extras.getInt("satellites") + "," +
                location.extras.toString()
        Log.d("handleNewLocation", msg)

        val mAccuracy = location.accuracy.toDouble() // Get Accuracy
        if (mAccuracy < mGPSAccuracyLevel) {    // Accuracy reached  < 5f. stop the location updates
            if (recordCountStatus == 0) {    // prevent multiple calls
                recordCountStatus += 1
                stopLocationUpdates()
            }
        }
        broadcastIntent!!.putExtra(INTENT_LOCATION_VALUE, location)
        sendBroadcast(broadcastIntent)
    }

    protected fun stopLocationUpdates() {
        //stop location updates when  is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    companion object {

        private val TAG = LocationService::class.java.simpleName
        private val UPDATE_INTERVAL = (10 * 1000).toLong()  /* 10 secs */
        private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
        private val mGPSAccuracyLevel = 5f
        val MY_LOCATION = "MY_CURRENT_LOCATION"
        val INTENT_LOCATION_VALUE = "currentLocation"
        val GPS_NOT_ENABLED = "GPS_NOT_ENABLED"
    }
}