package com.kapplication.landsurvey

import android.app.Application
import android.util.Log

class KApplication : Application() {
    private val TAG: String = "KApplication"

    override fun onCreate() {
        Log.i(TAG, "Application onCreate")
        super.onCreate()
    }
}
