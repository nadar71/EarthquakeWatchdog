package com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.core.data.AppPrefs
import com.indiewalk.watchdog.earthquake.core.data.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        AppPrefs.settingsFlow(context)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun toggleUnitSystem() {
        viewModelScope.launch {
            val current = settings.value.unitSystem
            val newUnit = if (current == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
            AppPrefs.setUnitSystem(context, newUnit)
        }
    }

    fun setManualLocation(enabled: Boolean) {
        viewModelScope.launch {
            AppPrefs.setManualLocationOn(context, enabled)
        }
    }
}
