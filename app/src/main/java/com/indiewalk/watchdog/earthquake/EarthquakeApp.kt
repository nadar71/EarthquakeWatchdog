package com.indiewalk.watchdog.earthquake

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.indiewalk.watchdog.earthquake.core.diagnostics.CrashlyticsStartup
import com.indiewalk.watchdog.earthquake.core.diagnostics.FirebaseCrashlyticsGateway
import com.indiewalk.watchdog.earthquake.core.performance.BenchmarkRuntime
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EarthquakeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BenchmarkRuntime.isEnabled) return
        configureCrashCollection()
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
