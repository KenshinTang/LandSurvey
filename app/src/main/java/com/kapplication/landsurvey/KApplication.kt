package com.kapplication.landsurvey

import android.app.Application
import android.util.Log
import com.tencent.bugly.crashreport.CrashReport

private const val TAG = "KApplication"

class KApplication : Application() {

    override fun onCreate() {
        Log.i(TAG, "Application onCreate")
        super.onCreate()

        CrashReport.initCrashReport(applicationContext, "a7fda7579b", false)
    }
}
