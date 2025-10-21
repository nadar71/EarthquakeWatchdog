package com.indiewalk.watchdog.earthquake.core.model

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.data.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.enums.UnitSystem

data class AppSettings(
    val mode: ThemeMode = ThemeMode.System,
    val manualLocOn: Boolean = false,
    val position: LatLng = LatLng(DEFAULT_LAT, DEFAULT_LNG),
    val city: String = "",
    val country: String = "",
    val address: String = "",
    val unitSystem: UnitSystem = UnitSystem.METRIC
)


fun AppSettings.toMappingSettings(): MappingSettings {
    return MappingSettings(
        userLat = position.latitude,
        userLng = position.longitude,
    )
}


