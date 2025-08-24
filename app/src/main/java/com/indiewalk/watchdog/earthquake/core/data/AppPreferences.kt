package com.indiewalk.watchdog.earthquake.core.data

import com.chibatching.kotpref.KotprefModel
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LNG


object AppPreferences : KotprefModel() {
    var appOpenedCounter by intPref(0)
    var unitSystem by stringPref("METRIC")  // METRIC|IMPERIAL|null
    var device_lat by stringPref(DEFAULT_LAT.toString())
    var device_lng by stringPref(DEFAULT_LNG.toString())




}