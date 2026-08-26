package com.indiewalk.watchdog.earthquake

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.indiewalk.watchdog.earthquake.core.diagnostics.CrashlyticsStartup
import com.indiewalk.watchdog.earthquake.core.diagnostics.FirebaseCrashlyticsGateway
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
        configureCrashCollection()
        TEST_DEVICE_ID = applicationContext.getString(R.string.admob_key_test_device)
        // init admob ads
        MobileAds.initialize(this) {}
    }

    private fun configureCrashCollection() {
        // A missing local google-services.json returns null, keeping open-source debug builds usable.
        CrashlyticsStartup.configure(isDebugBuild = BuildConfig.DEBUG) {
            FirebaseApp.initializeApp(this)?.let {
                FirebaseCrashlyticsGateway(FirebaseCrashlytics.getInstance())
            }
        }
    }
}
