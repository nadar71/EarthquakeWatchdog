package com.indiewalk.watchdog.earthquake.core.data


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "app_prefs"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object AppPrefs {
    private val THEME_MODE = stringPreferencesKey("mode")
    private val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    private val POSITION_LAT = stringPreferencesKey("position_lat")
    private val POSITION_LNG = stringPreferencesKey("position_lng")
    private val MANUAL_LOC_ON = booleanPreferencesKey("manual_loc_on")

    // Main Flow with defaults
    fun settingsFlow(context: Context): Flow<AppSettings> =
        context.dataStore.data.map { prefs ->
            val mode = when (prefs[THEME_MODE]) {
                ThemeMode.Light.name -> ThemeMode.Light
                ThemeMode.Dark.name -> ThemeMode.Dark
                else -> ThemeMode.System
            }

            val lat = prefs[POSITION_LAT]?.toDoubleOrNull() ?: Constants.DEFAULT_LAT
            val lng = prefs[POSITION_LNG]?.toDoubleOrNull() ?: Constants.DEFAULT_LNG
            val manualLoc = prefs[MANUAL_LOC_ON] ?: false
            val unit = when (prefs[UNIT_SYSTEM]) {
                UnitSystem.IMPERIAL.name -> UnitSystem.IMPERIAL
                else -> UnitSystem.METRIC
            }

            AppSettings(
                mode = mode,
                manualLocOn = manualLoc,
                position = LatLng(lat, lng),
                unitSystem = unit
            )
        }

    suspend fun setMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setUnitSystem(context: Context, unit: UnitSystem) {
        context.dataStore.edit { it[UNIT_SYSTEM] = unit.name }
    }

    suspend fun setManualLocation(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[MANUAL_LOC_ON] = enabled }
    }

    suspend fun setLocation(context: Context, lat: Double, lng: Double) {
        context.dataStore.edit {
            it[POSITION_LAT] = lat.toString()
            it[POSITION_LNG] = lng.toString()
        }
    }

    // Read fun
    suspend fun getCurrentSettings(context: Context): AppSettings =
        settingsFlow(context).first()


    /*private val THEME_MODE = stringPreferencesKey("mode")
    private val UNIT_SYSTEM = stringPreferencesKey("unit_system") // METRIC|IMPERIAL|null
    private val POSITION_LAT = stringPreferencesKey("position_lat")
    private val POSITION_LNG = stringPreferencesKey("position_lng")
    private val MANUAL_LOC_ON = booleanPreferencesKey("manual_loc_on")

    fun settingsFlow(context: Context): Flow<AppSettings> =
        context.dataStore.data.map { prefs ->
            val mode = when (prefs[THEME_MODE]) {
                ThemeMode.Light.name -> ThemeMode.Light
                ThemeMode.Dark.name  -> ThemeMode.Dark
                else -> ThemeMode.System
            }
            AppSettings(mode = mode)
        }

    suspend fun setMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setUnitSystem(context: Context, unitSystem: String) {
        context.dataStore.edit { it[UNIT_SYSTEM] = unitSystem }
    }

    suspend fun getUnitSystem(context: Context): String {
        return context.dataStore.data.first()[UNIT_SYSTEM] ?: "METRIC"
    }

    suspend fun setLocation(context: Context, lat: Double, lng: Double) {
        context.dataStore.edit { it[POSITION_LAT] = lat.toString() }
        context.dataStore.edit { it[POSITION_LNG] = lng.toString() }
    }

    suspend fun getLocation(context: Context): LatLng {
        val lat = context.dataStore.data.first()[POSITION_LAT]?.toDouble() ?: DEFAULT_LAT
        val lng = context.dataStore.data.first()[POSITION_LNG]?.toDouble() ?: DEFAULT_LNG
        return LatLng(lat, lng)
    }

    suspend fun setManualLocation(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[MANUAL_LOC_ON] = enabled }
    }

    suspend fun getManualLocation(context: Context): Boolean {
        return context.dataStore.data.first()[MANUAL_LOC_ON] ?: false
    }*/

}
