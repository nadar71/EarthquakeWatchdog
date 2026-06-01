package com.indiewalk.watchdog.earthquake.core.data.repository

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.data.local.preferences.AppPrefs
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferencesRepository {

    override val settingsFlow: Flow<AppSettings> = AppPrefs.settingsFlow(context)
    override val askedLocationOnceFlow: Flow<Boolean> = AppPrefs.askedLocationOnceFlow(context)

    override suspend fun getCurrentSettings(): AppSettings = AppPrefs.getCurrentSettings(context)

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        AppPrefs.setMode(context, themeMode)
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        AppPrefs.setUnitSystem(context, unitSystem)
    }

    override suspend fun setAskedLocationOnce(value: Boolean) {
        AppPrefs.setAskedLocationOnce(context, value)
    }

    override suspend fun setManualLocationOn(enabled: Boolean) {
        AppPrefs.setManualLocationOn(context, enabled)
    }

    override suspend fun setManualPosition(latLng: LatLng) {
        AppPrefs.setManualPosition(context, latLng.latitude, latLng.longitude)
    }

    override suspend fun setManualLocationInfo(locationInfo: LocationInfo) {
        AppPrefs.setManualLocationInfo(context, locationInfo)
    }

    override suspend fun setUserPosition(latLng: LatLng) {
        AppPrefs.setUserPosition(context, latLng.latitude, latLng.longitude)
    }

    override suspend fun setUserLocationInfo(locationInfo: LocationInfo) {
        AppPrefs.setUserLocationInfo(context, locationInfo)
    }

    override suspend fun setLastRefreshTime(lastRefreshTime: String) {
        AppPrefs.setLastRefreshTime(context, lastRefreshTime)
    }
}

