package com.indiewalk.watchdog.earthquake.core.data


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.indiewalk.watchdog.earthquake.core.model.ThemeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "theme_prefs"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object ThemePrefs {
    private val KEY_MODE = stringPreferencesKey("mode")
    private val KEY_DYNAMIC = booleanPreferencesKey("dynamic")

    fun settingsFlow(context: Context): Flow<ThemeSettings> =
        context.dataStore.data.map { prefs ->
            val mode = when (prefs[KEY_MODE]) {
                ThemeMode.Light.name -> ThemeMode.Light
                ThemeMode.Dark.name  -> ThemeMode.Dark
                else -> ThemeMode.System
            }
            val dynamic = prefs[KEY_DYNAMIC] ?: true
            ThemeSettings(mode = mode, dynamicColor = dynamic)
        }

    suspend fun setMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { it[KEY_MODE] = mode.name }
    }

    suspend fun setDynamic(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC] = enabled }
    }
}
