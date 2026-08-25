package com.example.twofactorauth

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TwoFactorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashLogger.init(this)
        CrashLogger.log("App", "Application started")
    }
}
