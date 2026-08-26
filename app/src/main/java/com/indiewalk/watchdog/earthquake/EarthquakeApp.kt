package com.indiewalk.watchdog.earthquake

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.indiewalk.watchdog.earthquake.core.diagnostics.CrashReportingPolicy
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
        disableDebugCrashCollection()
        TEST_DEVICE_ID = applicationContext.getString(R.string.admob_key_test_device)
        // init admob ads
        MobileAds.initialize(this) {}
    }

    private fun disableDebugCrashCollection() {
        if (!BuildConfig.DEBUG) return

        // A missing local google-services.json returns null, keeping open-source debug builds usable.
        FirebaseApp.initializeApp(this)?.let {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(
                CrashReportingPolicy.isCollectionEnabled(isDebugBuild = true)
            )
        }
    }
}
