package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DS_NAME = "eq_filters"
val Context.eqFilterDataStore by preferencesDataStore(DS_NAME)

object FilterPrefs {
    private val KEY_SORT = stringPreferencesKey("sort_option")
    private val KEY_MIN_MAG = stringPreferencesKey("min_mag")
    private val KEY_START_DATE = stringPreferencesKey("start_date")
    private val KEY_END_DATE = stringPreferencesKey("end_date")
    private val KEY_TIME_INTERVAL = stringPreferencesKey("time_interval")

    // -- sort type --
    fun sortFlow(context: Context): Flow<EqsSortOption> =
        context.eqFilterDataStore.data.map { prefs ->
            EqsSortOption.fromNameString(prefs[KEY_SORT])
        }

    suspend fun setSort(context: Context, option: EqsSortOption) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_SORT] = option.name
        }
    }

    // -- min magnitude --
    fun minMagFlow(context: Context): Flow<MinMagnitude> =
        context.eqFilterDataStore.data.map { prefs ->
            MinMagnitude.fromNameString(prefs[KEY_MIN_MAG])
        }

    suspend fun setMinMag(context: Context, mag: Double) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_MIN_MAG] = mag.toString()
        }
    }

    // -- start/end date --
    fun startDateFlow(context: Context): Flow<String> =
        context.eqFilterDataStore.data.map { prefs ->
            prefs[KEY_START_DATE] ?: ""
        }

    suspend fun setStartDate(context: Context, date: String) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_START_DATE] = date
        }
    }

    fun endDateFlow(context: Context): Flow<String> =
        context.eqFilterDataStore.data.map { prefs ->
            prefs[KEY_END_DATE] ?: ""
        }

    suspend fun setEndDate(context: Context, date: String) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_END_DATE] = date
        }
    }

    // -- time interval --
    fun timeIntervalFlow(context: Context): Flow<TimeInterval> =
        context.eqFilterDataStore.data.map { prefs ->
            TimeInterval.fromNameString(prefs[KEY_TIME_INTERVAL])
        }


    suspend fun setTimeInterval(context: Context, interval: String) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_TIME_INTERVAL] = interval
        }
    }


    // Debug
    suspend fun debugPrintEqFilterDataStore(context: Context): String {
        val data = context.eqFilterDataStore.data.first()
        val stringBuilder = StringBuilder("=== FilterPrefs DataStore Contents ===\n")

        data.asMap().forEach { (key, value) ->
            stringBuilder.append("${key.name}: $value\n")
        }

        val result = stringBuilder.toString()
        println(result)
        return result
    }
}
