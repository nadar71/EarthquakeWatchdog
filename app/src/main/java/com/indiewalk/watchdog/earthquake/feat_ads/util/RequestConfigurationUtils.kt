package com.indiewalk.watchdog.earthquake.feat_ads.util

import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.indiewalk.watchdog.earthquake.EarthquakeApp.Companion.TEST_DEVICE_ID


class RequestConfigurationUtils {

    companion object {
        fun setTestDeviceIds() {
            val testDeviceIds = listOf(TEST_DEVICE_ID)
            val configuration =
                RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }
}