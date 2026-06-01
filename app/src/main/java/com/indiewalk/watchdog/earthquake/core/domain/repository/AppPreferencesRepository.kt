package com.indiewalk.watchdog.earthquake.core.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val settingsFlow: Flow<AppSettings>
    val askedLocationOnceFlow: Flow<Boolean>

    suspend fun getCurrentSettings(): AppSettings
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setUnitSystem(unitSystem: UnitSystem)
    suspend fun setAskedLocationOnce(value: Boolean)
    suspend fun setManualLocationOn(enabled: Boolean)
    suspend fun setManualPosition(latLng: LatLng)
    suspend fun setManualLocationInfo(locationInfo: LocationInfo)
    suspend fun setUserPosition(latLng: LatLng)
    suspend fun setUserLocationInfo(locationInfo: LocationInfo)
    suspend fun setLastRefreshTime(lastRefreshTime: String)
}

