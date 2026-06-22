package com.indiewalk.watchdog.earthquake.core.model.preferences

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem

data class AppSettings(
    val mode: ThemeMode = ThemeMode.System,
    val manualLocOn: Boolean = false,
    val userPosition: LatLng = LatLng(DEFAULT_LAT, DEFAULT_LNG),
    val userLocationInfo: LocationInfo = LocationInfo("", "", ""),
    val manualPosition: LatLng = LatLng(DEFAULT_LAT, DEFAULT_LNG),
    val manualLocationInfo: LocationInfo = LocationInfo("", "", ""),
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val lastRefreshTime: String = "",
)

data class LocationInfo(
    val city: String?,
    val countryCode: String?,
    val address: String?
){
    fun concatString(context: Context): String {
        if (city == null || countryCode == null || address == null)
            return context.getString(R.string.generic_unknown_location)
        return "$countryCode, $city, $address"
    }
}




