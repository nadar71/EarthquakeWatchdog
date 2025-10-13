package com.indiewalk.watchdog.earthquake.core.util.extensions

import com.indiewalk.watchdog.earthquake.EarthquakeApp
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.Constants.KM_TO_MILES
import com.indiewalk.watchdog.earthquake.core.data.enums.UnitSystem

fun Int.kmToDisplayInt(unit: UnitSystem): Int =
    when (unit) {
        UnitSystem.IMPERIAL -> (this * KM_TO_MILES).toInt()
        UnitSystem.METRIC -> this
    }

fun Int.kmToDisplayString(unit: UnitSystem): String =
    when (unit) {
        UnitSystem.IMPERIAL -> ((this * KM_TO_MILES).toInt().toString() + " " + EarthquakeApp.appContext.getString(R.string.settings_mi_distance_unit_label))
        UnitSystem.METRIC -> (this.toString() + " " + EarthquakeApp.appContext.getString(R.string.settings_km_distance_unit_label))
    }