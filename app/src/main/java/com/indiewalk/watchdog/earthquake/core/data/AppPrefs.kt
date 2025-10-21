package com.indiewalk.watchdog.earthquake.core.data


import android.content.Context
import android.location.Address
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
    private val POSITION_CITY = stringPreferencesKey("position_city")
    private val POSITION_COUNTRY_CODE = stringPreferencesKey("position_country_code")
    private val POSITION_ADDRESS = stringPreferencesKey("position_address")

    private val MANUAL_LOC_ON = booleanPreferencesKey("manual_loc_on")
    private val MANUAL_LOC_LAT = stringPreferencesKey("manual_loc_lat")
    private val MANUAL_LOC_LNG = stringPreferencesKey("manual_loc_lng")
    private val MANUAL_LOC_CITY = stringPreferencesKey("manual_loc_city")
    private val MANUAL_LOC_COUNTRY_CODE = stringPreferencesKey("manual_loc_country_code")
    private val MANUAL_LOC_ADDRESS = stringPreferencesKey("manual_loc_address")

    private val KEY_ASKED_LOCATION_ONCE = booleanPreferencesKey("asked_location_once")



    // Main Flow with defaults
    fun settingsFlow(context: Context): Flow<AppSettings> =
        context.dataStore.data.map { prefs ->
            val mode = when (prefs[THEME_MODE]) {
                ThemeMode.Light.name -> ThemeMode.Light
                ThemeMode.Dark.name -> ThemeMode.Dark
                else -> ThemeMode.System
            }

            val isManualLocOn = prefs[MANUAL_LOC_ON] ?: false
            val lat = if (isManualLocOn) prefs[MANUAL_LOC_LAT]?.toDoubleOrNull() ?: Constants.DEFAULT_LAT
                      else prefs[POSITION_LAT]?.toDoubleOrNull() ?: Constants.DEFAULT_LAT
            val lng = if (isManualLocOn) prefs[MANUAL_LOC_LNG]?.toDoubleOrNull() ?: Constants.DEFAULT_LNG
                      else prefs[POSITION_LNG]?.toDoubleOrNull() ?: Constants.DEFAULT_LNG
            val city = if (isManualLocOn) prefs[MANUAL_LOC_CITY] ?: ""
                       else prefs[POSITION_CITY] ?: ""
            val country = if (isManualLocOn) prefs[MANUAL_LOC_COUNTRY_CODE] ?: ""
                          else prefs[POSITION_COUNTRY_CODE] ?: ""
            val address = if (isManualLocOn) prefs[MANUAL_LOC_ADDRESS] ?: ""
                          else prefs[POSITION_ADDRESS] ?: ""
            val unit = when (prefs[UNIT_SYSTEM]) {
                UnitSystem.IMPERIAL.name -> UnitSystem.IMPERIAL
                else -> UnitSystem.METRIC
            }


            AppSettings(
                mode = mode,
                manualLocOn = isManualLocOn,
                position = LatLng(lat, lng),
                city = city,
                country = country,
                address = address,
                unitSystem = unit
            )
        }

    // Flow for location permission asked for at least once.
    // Used to detect permanent denial ("Don't ask again").
    fun askedLocationOnceFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_ASKED_LOCATION_ONCE] ?: false }


    // ---------------- Setters (suspend fun) ----------------

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

    suspend fun setCity(context: Context, city: String) {
        context.dataStore.edit { it[POSITION_CITY] = city }
    }

    suspend fun setCountryCode(context: Context, country: String) {
        context.dataStore.edit { it[POSITION_COUNTRY_CODE] = country }
    }

    suspend fun setAddress(context: Context, address: String) {
        context.dataStore.edit { it[POSITION_ADDRESS] = address }
    }


    suspend fun setManualLocation(context: Context, lat: Double, lng: Double) {
        context.dataStore.edit {
            it[MANUAL_LOC_LAT] = lat.toString()
            it[MANUAL_LOC_LNG] = lng.toString()
        }
    }

    suspend fun setManualCity(context: Context, city: String) {
        context.dataStore.edit { it[MANUAL_LOC_CITY] = city }
    }

    suspend fun setManualCountryCode(context: Context, country: String) {
        context.dataStore.edit { it[MANUAL_LOC_COUNTRY_CODE] = country }
    }

    suspend fun setManualAddress(context: Context, address: String) {
        context.dataStore.edit { it[MANUAL_LOC_ADDRESS] = address }
    }

    // Read fun
    suspend fun getCurrentSettings(context: Context): AppSettings =
        settingsFlow(context).first()

    suspend fun setAskedLocationOnce(context: Context, value: Boolean) {
        context.dataStore.edit { it[KEY_ASKED_LOCATION_ONCE] = value }
    }

}
