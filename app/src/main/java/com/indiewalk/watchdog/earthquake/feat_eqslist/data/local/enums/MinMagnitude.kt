package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums

enum class MinMagnitude {
    MAG_0_0,
    MAG_1_0,
    MAG_2_0,
    MAG_3_0,
    MAG_4_0,
    MAG_4_5,
    MAG_5_0,
    MAG_5_5,
    MAG_6_0,
    MAG_6_5;

    companion object {
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
