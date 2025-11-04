package com.indiewalk.watchdog.earthquake.core.data.local.enums

import com.indiewalk.watchdog.earthquake.EarthquakeApp.Companion.appContext
import com.indiewalk.watchdog.earthquake.R

enum class MinMagnitude(val value: String) {
    MAG_0_0(appContext.getString(R.string.filter_LT_0_min_magnitude_label)),
    MAG_1_0(appContext.getString(R.string.filter_1_0_min_magnitude_label)),
    MAG_2_0(appContext.getString(R.string.filter_2_0_min_magnitude_label)),
    MAG_3_0(appContext.getString(R.string.filter_3_0_min_magnitude_label)),
    MAG_4_0(appContext.getString(R.string.filter_4_0_min_magnitude_label)),
    MAG_4_5(appContext.getString(R.string.filter_4_5_min_magnitude_label)),
    MAG_5_0(appContext.getString(R.string.filter_5_0_min_magnitude_label)),
    MAG_5_5(appContext.getString(R.string.filter_5_5_min_magnitude_label)),
    MAG_6_0(appContext.getString(R.string.filter_6_0_min_magnitude_label)),
    MAG_6_5(appContext.getString(R.string.filter_6_5_min_magnitude_label));

    companion object {
        // TODO: use runcatch like in OrderType
        fun fromStringValue(value: String): MinMagnitude? {
            return MinMagnitude.entries.find { it.value == value }
        }

    }
}