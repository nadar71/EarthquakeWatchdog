package com.indiewalk.watchdog.earthquake

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EarthquakeApp : Application() {
    companion object {
        // global variables
        lateinit var TEST_DEVICE_ID: String
        var canRequestAdsFlag: Boolean = true
    }
    override fun onCreate() {
        super.onCreate()
        TEST_DEVICE_ID = applicationContext.getString(R.string.admob_key_test_device)
        // init admob ads
        MobileAds.initialize(this) {}
    }
}
