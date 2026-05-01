package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DS_NAME = "eq_filters"
val Context.eqFilterDataStore by preferencesDataStore(DS_NAME)

// default value filter
// sort_option: DATE_DESC
// min_mag: MAG_3_0
// time_interval: LAST_30_DAYS
object FilterPrefs {
    private val KEY_SORT = stringPreferencesKey("sort_option")
    private val KEY_MIN_MAG = stringPreferencesKey("min_mag")
    private val KEY_START_DATE = stringPreferencesKey("start_date")
    private val KEY_END_DATE = stringPreferencesKey("end_date")
    private val KEY_TIME_INTERVAL = stringPreferencesKey("time_interval")
    private val KEY_FILTER_ACTIVE_COUNTS = intPreferencesKey("filter_active_counts")

    fun filterSettingsFlow(context: Context): Flow<FilterSettings> =
        context.eqFilterDataStore.data.map { prefs ->
            FilterSettings(
                sortOption = EqsSortOption.fromNameString(prefs[KEY_SORT]),
                minMag = MinMagnitude.fromNameString(prefs[KEY_MIN_MAG]),
                timeInterval = TimeInterval.fromNameString(prefs[KEY_TIME_INTERVAL]),
                startDate = prefs[KEY_START_DATE] ?: "",
                endDate = prefs[KEY_END_DATE] ?: "",
                activeCounts = prefs[KEY_FILTER_ACTIVE_COUNTS] ?: 0
            )
        }


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

    suspend fun setMinMag(context: Context, mag: MinMagnitude) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_MIN_MAG] = mag.name
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


    suspend fun setTimeInterval(context: Context, interval: TimeInterval) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_TIME_INTERVAL] = interval.name
        }
    }

    // -- filter active counts --
    fun filterActiveCountsFlow(context: Context): Flow<Int> =
        context.eqFilterDataStore.data.map { prefs ->
            prefs[KEY_FILTER_ACTIVE_COUNTS] ?: 0
        }

    suspend fun setFilterActiveCounts(context: Context, count: Int) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_FILTER_ACTIVE_COUNTS] = count
        }
    }


    // Debug
    suspend fun debugPrintEqFilterDataStore(context: Context): String {
        val data = context.eqFilterDataStore.data.first()
        val stringBuilder = StringBuilder("=== FilterPrefs DataStore Contents ===\n")

        data.asMap().forEach { (key, value) ->
            stringBuilder.append("${key.name}: $value\n")
        }

        return stringBuilder.toString()
    }
}
