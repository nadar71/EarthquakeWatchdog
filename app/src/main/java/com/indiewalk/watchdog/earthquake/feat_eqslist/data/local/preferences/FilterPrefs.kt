package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.preferences.EqsSortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "eq_filters"
val Context.eqFilterDataStore by preferencesDataStore(DS_NAME)

object FilterPrefs {
    private val KEY_SORT = stringPreferencesKey("sort_option")

    fun sortFlow(context: Context): Flow<EqsSortOption> =
        context.eqFilterDataStore.data.map { prefs ->
            EqsSortOption.fromString(prefs[KEY_SORT])
        }

    suspend fun setSort(context: Context, option: EqsSortOption) {
        context.eqFilterDataStore.edit { prefs ->
            prefs[KEY_SORT] = option.name
        }
    }
}
