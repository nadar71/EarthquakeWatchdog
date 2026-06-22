package com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        appPreferencesRepository.settingsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            appPreferencesRepository.setUnitSystem(unitSystem)
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            appPreferencesRepository.setThemeMode(themeMode)
        }
    }

    fun setManualLocation(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setManualLocationOn(enabled)
        }
    }
}
