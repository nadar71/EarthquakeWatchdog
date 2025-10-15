package com.indiewalk.watchdog.earthquake

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EarthquakeApp : Application() {
    companion object {
        lateinit var appContext: Application
    }
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}
