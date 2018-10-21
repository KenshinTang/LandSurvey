package com.kapplication.landsurvey

import android.app.Application
import android.util.Log

private const val TAG = "KApplication"

class KApplication : Application() {

    override fun onCreate() {
        Log.i(TAG, "Application onCreate")
        super.onCreate()
    }
}
