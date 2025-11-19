package com.indiewalk.watchdog.earthquake.core.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.Constants
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences.eqFilterDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


private const val DATASTORE_NAME = "app_prefs"
val Context.appPrefsDataStore by preferencesDataStore(name = DATASTORE_NAME)

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
    private val LAST_REFRESH_TIME = stringPreferencesKey("last_refresh_time")


    // Main Flow with defaults
    fun settingsFlow(context: Context): Flow<AppSettings> =
        context.appPrefsDataStore.data.map { prefs ->
            val mode = when (prefs[THEME_MODE]) {
                ThemeMode.Light.name -> ThemeMode.Light
                ThemeMode.Dark.name -> ThemeMode.Dark
                else -> ThemeMode.System
            }

            val isManualLocOn = prefs[MANUAL_LOC_ON] ?: false

            val userLat = prefs[POSITION_LAT]?.toDoubleOrNull() ?: Constants.DEFAULT_LAT
            val userLng = prefs[POSITION_LNG]?.toDoubleOrNull() ?: Constants.DEFAULT_LNG
            val userCity = prefs[POSITION_CITY] ?: ""
            val userCountry = prefs[POSITION_COUNTRY_CODE] ?: ""
            val userAddress = prefs[POSITION_ADDRESS] ?: ""

            val manualLat = prefs[MANUAL_LOC_LAT]?.toDoubleOrNull() ?: Constants.DEFAULT_LAT
            val manualLng = prefs[MANUAL_LOC_LNG]?.toDoubleOrNull() ?: Constants.DEFAULT_LNG
            val manualCity = prefs[MANUAL_LOC_CITY] ?: ""
            val manualCountry = prefs[MANUAL_LOC_COUNTRY_CODE] ?: ""
            val manualAddress = prefs[MANUAL_LOC_ADDRESS] ?: ""

            val unit = when (prefs[UNIT_SYSTEM]) {
                UnitSystem.IMPERIAL.name -> UnitSystem.IMPERIAL
                else -> UnitSystem.METRIC
            }

            val lastRefreshTime = prefs[LAST_REFRESH_TIME] ?: ""


            AppSettings(
                mode = mode,
                manualLocOn = isManualLocOn,
                userPosition = LatLng(userLat, userLng),
                userLocationInfo = LocationInfo(userCity, userCountry, userAddress),
                manualPosition = LatLng(manualLat, manualLng),
                manualLocationInfo = LocationInfo(manualCity, manualCountry, manualAddress),
                unitSystem = unit,
                lastRefreshTime = lastRefreshTime
            )
        }

    // Flow for location permission asked for at least once.
    // Used to detect permanent denial ("Don't ask again").
    fun askedLocationOnceFlow(context: Context): Flow<Boolean> =
        context.appPrefsDataStore.data.map { prefs -> prefs[KEY_ASKED_LOCATION_ONCE] ?: false }


    // ---------------- Setters (suspend fun) ----------------

    suspend fun setMode(context: Context, mode: ThemeMode) {
        context.appPrefsDataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setUnitSystem(context: Context, unit: UnitSystem) {
        context.appPrefsDataStore.edit { it[UNIT_SYSTEM] = unit.name }
    }

    suspend fun setManualLocationOn(context: Context, enabled: Boolean) {
        context.appPrefsDataStore.edit { it[MANUAL_LOC_ON] = enabled }
    }

    suspend fun setUserPosition(context: Context, lat: Double, lng: Double) {
        context.appPrefsDataStore.edit {
            it[POSITION_LAT] = lat.toString()
            it[POSITION_LNG] = lng.toString()
        }
    }

    suspend fun setUserCity(context: Context, city: String) {
        context.appPrefsDataStore.edit { it[POSITION_CITY] = city }
    }

    suspend fun setUserCountryCode(context: Context, countryCode: String) {
        context.appPrefsDataStore.edit { it[POSITION_COUNTRY_CODE] = countryCode }
    }

    suspend fun setUserAddress(context: Context, address: String) {
        context.appPrefsDataStore.edit { it[POSITION_ADDRESS] = address }
    }

    suspend fun setUserLocationInfo(context: Context, locationInfo: LocationInfo) {
        context.appPrefsDataStore.edit {
            it[POSITION_CITY] = locationInfo.city ?: "Unknown"
            it[POSITION_COUNTRY_CODE] = locationInfo.countryCode ?: "Unknown"
            it[POSITION_ADDRESS] = locationInfo.address ?: "Unknown"
        }
    }


    suspend fun setManualPosition(context: Context, lat: Double, lng: Double) {
        context.appPrefsDataStore.edit {
            it[MANUAL_LOC_LAT] = lat.toString()
            it[MANUAL_LOC_LNG] = lng.toString()
        }
    }

    suspend fun setManualCity(context: Context, city: String) {
        context.appPrefsDataStore.edit { it[MANUAL_LOC_CITY] = city }
    }

    suspend fun setManualCountryCode(context: Context, country: String) {
        context.appPrefsDataStore.edit { it[MANUAL_LOC_COUNTRY_CODE] = country }
    }

    suspend fun setManualAddress(context: Context, address: String) {
        context.appPrefsDataStore.edit { it[MANUAL_LOC_ADDRESS] = address }
    }

    suspend fun setManualLocationInfo(context: Context, locationInfo: LocationInfo) {
        context.appPrefsDataStore.edit {
            it[MANUAL_LOC_CITY] = locationInfo.city ?: context.getString(R.string.generic_unknown_city)
            it[MANUAL_LOC_COUNTRY_CODE] = locationInfo.countryCode ?: context.getString(R.string.generic_unknown_country_code)
            it[MANUAL_LOC_ADDRESS] = locationInfo.address ?: context.getString(R.string.generic_unknown_address)
        }
    }

    suspend fun getCurrentSettings(context: Context): AppSettings =
        settingsFlow(context).first()

    suspend fun setAskedLocationOnce(context: Context, value: Boolean) {
        context.appPrefsDataStore.edit { it[KEY_ASKED_LOCATION_ONCE] = value }
    }

    suspend fun getLastRefreshTime(context: Context): String =
        context.appPrefsDataStore.data.first()[LAST_REFRESH_TIME] ?: ""

    suspend fun setLastRefreshTime(context: Context, value: String) {
        context.appPrefsDataStore.edit { it[LAST_REFRESH_TIME] = value }
    }



    // Debug
    suspend fun debugPrintAppPrefsDataStore(context: Context): String {
        val data = context.eqFilterDataStore.data.first()
        val stringBuilder = StringBuilder("=== AppPrefs DataStore Contents ===\n")

        data.asMap().forEach { (key, value) ->
            stringBuilder.append("${key.name}: $value\n")
        }

        val result = stringBuilder.toString()
        println(result)
        return result
    }

}