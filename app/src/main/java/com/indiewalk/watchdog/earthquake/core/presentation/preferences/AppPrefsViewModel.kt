package com.indiewalk.watchdog.earthquake.core.presentation.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.local.preferences.AppPrefs
import com.indiewalk.watchdog.earthquake.core.data.local.Constants
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppPrefsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        AppPrefs.settingsFlow(context)
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), AppSettings())

    val askedOnce: StateFlow<Boolean> =
        AppPrefs.askedLocationOnceFlow(context)
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), false)

    fun setAskedOnce() {
        viewModelScope.launch { AppPrefs.setAskedLocationOnce(context, true) }
    }

    fun setManualLocOn(enabled: Boolean) {
        viewModelScope.launch {
            AppPrefs.setManualLocationOn(context, enabled)
        }
    }

    fun setManualPosition(latLng: LatLng) {
        viewModelScope.launch {
            AppPrefs.setManualPosition(context, latLng.latitude, latLng.longitude)
        }
    }

    fun setManualLocationInfo(locationInfo: LocationInfo) {
        viewModelScope.launch {
            AppPrefs.setManualLocationInfo(context, locationInfo)
        }
    }

    fun setUserPosition(latLng: LatLng?) {
        if (latLng == null) return
        viewModelScope.launch {
            AppPrefs.setUserPosition(context, latLng.latitude, latLng.longitude)
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

    fun setUserLocationInfo(locationInfo: LocationInfo) {
        viewModelScope.launch {
            AppPrefs.setUserLocationInfo(context, locationInfo)
        }
    }


    fun keepDefaultLocation() {
        viewModelScope.launch {
            // force defaults (even if already there)
            AppPrefs.setUserPosition(context, Constants.DEFAULT_LAT, Constants.DEFAULT_LNG)
        }
    }
}