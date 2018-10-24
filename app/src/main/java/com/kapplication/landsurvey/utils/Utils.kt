package com.kapplication.landsurvey.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.kapplication.landsurvey.model.Units
import java.io.File
import java.text.SimpleDateFormat

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

        @SuppressLint("SimpleDateFormat")
        fun formatTime(time: Long, format: String = "yyyy-MM-dd HH:mm:ss"): String {
            return SimpleDateFormat(format).format(time)
        }

        fun convertArea(context: Context, num: Double, fractionDigits: Int) : String {
            val sharedPre = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
            val unit = sharedPre.getInt("UNIT", Units.ACRE.ordinal)
            return when(unit) {
                Units.ACRE.ordinal -> {
                    String.format("%.${fractionDigits}f ac", num*0.0002471055)
                }
                Units.SQUARE_MILE.ordinal -> {
                    String.format("%.${fractionDigits}f mi²", num*0.0000003861022)
                }
                Units.SQUARE_FOOT.ordinal -> {
                    String.format("%.${fractionDigits}f ft²", num*10.76391)
                }
                else -> {
                    String.format("%.${fractionDigits}f ac", num*0.0002471055)
                }
            }
        }
    }


}