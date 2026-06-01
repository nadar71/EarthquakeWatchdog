package com.indiewalk.watchdog.earthquake.core.util.extensions

import com.indiewalk.watchdog.earthquake.core.data.local.Constants.KM_TO_MILES
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem

fun Int.kmToDisplayInt(unit: UnitSystem): Int =
    when (unit) {
        UnitSystem.IMPERIAL -> (this * KM_TO_MILES).toInt()
        UnitSystem.METRIC -> this
    }
