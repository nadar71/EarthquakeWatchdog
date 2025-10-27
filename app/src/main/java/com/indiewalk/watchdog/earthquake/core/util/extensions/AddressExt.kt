package com.indiewalk.watchdog.earthquake.core.util.extensions

import android.content.Context
import android.location.Address
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.model.LocationInfo


fun Address?.toLocationInfo(context: Context): LocationInfo = LocationInfo(
    this?.locality ?: context.getString(R.string.generic_unknown_city),
    this?.countryCode ?: context.getString(R.string.generic_unknown_country_code),
    this?.getAddressLine(0) ?: context.getString(R.string.generic_unknown_address)
)