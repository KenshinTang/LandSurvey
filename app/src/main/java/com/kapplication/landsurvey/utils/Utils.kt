package com.kapplication.landsurvey.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class Utils {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 99

        fun checkLocationPermission(context: Context) : Boolean {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    val alertBuilder = AlertDialog.Builder(context)
//                    alertBuilder.setCancelable(true)
//                    alertBuilder.setTitle("Permission necessary")
//                    alertBuilder.setMessage("Location permission is necessary")
//                    alertBuilder.setPositiveButton(android.R.string.yes) { _, _ ->
//                        ActivityCompat.requestPermissions(context,
//                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
//                    }
//                    val alert = alertBuilder.create()
//                    alert.show()
//                } else {
                    ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
//                }
                return false
            }
            return true
        }
    }


}