package com.indiewalk.watchdog.earthquake.core.data.local.enums

import androidx.annotation.StringRes
import com.indiewalk.watchdog.earthquake.EarthquakeApp.Companion.appContext
import com.indiewalk.watchdog.earthquake.R

enum class MinMagnitude(val value: String) {
    MAG_1_0(appContext.getString(R.string.settings_1_0_min_magnitude_label)),
    MAG_2_0(appContext.getString(R.string.settings_2_0_min_magnitude_label)),
    MAG_3_0(appContext.getString(R.string.settings_3_0_min_magnitude_label)),
    MAG_4_0(appContext.getString(R.string.settings_4_0_min_magnitude_label)),
    MAG_4_5(appContext.getString(R.string.settings_4_5_min_magnitude_label)),
    MAG_5_0(appContext.getString(R.string.settings_5_0_min_magnitude_label)),
    MAG_5_5(appContext.getString(R.string.settings_5_5_min_magnitude_label)),
    MAG_6_0(appContext.getString(R.string.settings_6_0_min_magnitude_label)),
    MAG_6_5(appContext.getString(R.string.settings_6_5_min_magnitude_label));

    companion object {
        fun fromStringValue(value: String): MinMagnitude? {
            return values().find { it.value == value }
        }

    }
}