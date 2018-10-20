package com.kapplication.landsurvey.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import java.io.File

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

        /* Checks if external storage is available for read and write */
        fun isExternalStorageWritable(): Boolean {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        }

        /* Checks if external storage is available to at least read */
        fun isExternalStorageReadable(): Boolean {
            return Environment.getExternalStorageState() in
                    setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
        }

        fun getLandSurveyDir(): File? {
            val dir = File(Environment.getExternalStorageDirectory(), "LandSurvey")
            if (!dir.exists()) {
                Log.i("Utils", "Dir creation=${dir.mkdir()}")
            }
            Log.i("Utils", "Dir=${dir.absolutePath}")
            return dir
        }
    }


}