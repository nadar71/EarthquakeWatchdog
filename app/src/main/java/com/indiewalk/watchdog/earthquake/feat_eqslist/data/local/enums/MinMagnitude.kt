package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums

import com.indiewalk.watchdog.earthquake.EarthquakeApp.Companion.appContext
import com.indiewalk.watchdog.earthquake.R

enum class MinMagnitude(val value: String) {
    MAG_0_0(appContext.getString(R.string.filter_0_0_min_magnitude_label)),
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
        // from "0.0+" -> MinMagnitude.MAG_0_0 item
        fun fromNameString(s: String?): MinMagnitude =
            runCatching { valueOf(s ?: "") }.getOrDefault(MAG_3_0)
    }
}

// MinMagnitude -> Double mapping
fun MinMagnitude.toDouble(): Double = when (this) {
    MinMagnitude.MAG_0_0 -> 0.0
    MinMagnitude.MAG_1_0 -> 1.0
    MinMagnitude.MAG_2_0 -> 2.0
    MinMagnitude.MAG_3_0 -> 3.0
    MinMagnitude.MAG_4_0 -> 4.0
    MinMagnitude.MAG_4_5 -> 4.5
    MinMagnitude.MAG_5_0 -> 5.0
    MinMagnitude.MAG_5_5 -> 5.5
    MinMagnitude.MAG_6_0 -> 6.0
    MinMagnitude.MAG_6_5 -> 6.5
}

// Double -> MinMagnitude mapping
fun minMagFromDouble(d: Double?): MinMagnitude? = when (d) {
    0.0 -> MinMagnitude.MAG_0_0
    1.0 -> MinMagnitude.MAG_1_0
    2.0 -> MinMagnitude.MAG_2_0
    3.0 -> MinMagnitude.MAG_3_0
    4.0 -> MinMagnitude.MAG_4_0
    4.5 -> MinMagnitude.MAG_4_5
    5.0 -> MinMagnitude.MAG_5_0
    5.5 -> MinMagnitude.MAG_5_5
    6.0 -> MinMagnitude.MAG_6_0
    6.5 -> MinMagnitude.MAG_6_5
    else -> null
}