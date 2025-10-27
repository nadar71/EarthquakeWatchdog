package com.indiewalk.watchdog.earthquake.feat_intro.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.AppPrefs
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        AppPrefs.settingsFlow(context)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val askedOnce: StateFlow<Boolean> =
        AppPrefs.askedLocationOnceFlow(context)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setAskedOnce() {
        viewModelScope.launch { AppPrefs.setAskedLocationOnce(context, true) }
    }

    fun setUserPosition(latLng: LatLng?) {
        if (latLng == null) return
        viewModelScope.launch {
            AppPrefs.setUserPosition(context, latLng.latitude, latLng.longitude)
            // AppPrefs.setManualLocation(context, false) // autoloc by default after grant
        }
    }

    fun setUserAddress(address: String) {
        viewModelScope.launch {
            AppPrefs.setUserAddress(context, address)
        }
    }

    fun setUserCity(city: String) {
        viewModelScope.launch {
            AppPrefs.setUserCity(context, city)
        }
    }

    fun setUserCountryCode(country: String) {
        viewModelScope.launch {
            AppPrefs.setUserCountryCode(context, country)
        }
    }

    fun keepDefaultLocation() {
        viewModelScope.launch {
            // force defaults (even if already there)
            AppPrefs.setUserPosition(context, DEFAULT_LAT, DEFAULT_LNG)
            // AppPrefs.setManualLocation(context, false)
        }
    }
}
