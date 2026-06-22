package com.indiewalk.watchdog.earthquake.core.util.extensions

import android.content.Context
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo

fun LocationInfo.concatString(context: Context): String {
    if (city == null || countryCode == null || address == null)
        return context.getString(R.string.generic_unknown_location)
    return "$countryCode, $city, $address"
}