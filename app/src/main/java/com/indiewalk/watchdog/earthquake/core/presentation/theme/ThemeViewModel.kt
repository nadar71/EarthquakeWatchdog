package com.indiewalk.watchdog.earthquake.core.presentation.theme

import dagger.hilt.android.lifecycle.HiltViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.core.data.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.ThemePrefs
import com.indiewalk.watchdog.earthquake.core.model.ThemeSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    val settings: StateFlow<ThemeSettings> =
        ThemePrefs.settingsFlow(app)
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeSettings())

    fun setMode(mode: ThemeMode) {
        viewModelScope.launch {
            ThemePrefs.setMode(getApplication(), mode)
        }
    }

    fun setDynamic(enabled: Boolean) {
        viewModelScope.launch {
            ThemePrefs.setDynamic(getApplication(), enabled)
        }
    }
}
