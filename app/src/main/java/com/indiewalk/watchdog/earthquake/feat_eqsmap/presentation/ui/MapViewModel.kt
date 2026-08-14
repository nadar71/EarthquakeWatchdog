package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.domain.repository.LocationRepository
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqsmap.domain.use_cases.ObserveEarthquakesUseCase
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.state.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val observeEarthquakesUseCase: ObserveEarthquakesUseCase,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        observeEarthquakes()
    }

    fun onLocationPermissionChanged(hasLocationPermission: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = hasLocationPermission) }
        if (hasLocationPermission) {
            viewModelScope.launch { syncUserLocation() }
        }
    }

    fun onManualLocationCleared() {
        viewModelScope.launch {
            val fallback = resolveUserFallback(_uiState.value.hasLocationPermission)
            appPreferencesRepository.setUserPosition(fallback)
            appPreferencesRepository.setUserLocationInfo(locationRepository.getLocationInfo(fallback))
            appPreferencesRepository.setManualLocationOn(false)
            _uiState.update { it.copy(recenterTarget = fallback) }
        }
    }

    fun onManualLocationConfirmed(latLng: LatLng, locationInfo: LocationInfo) {
        viewModelScope.launch {
            appPreferencesRepository.setManualPosition(latLng)
            appPreferencesRepository.setManualLocationInfo(locationInfo)
            appPreferencesRepository.setManualLocationOn(true)
            _uiState.update { it.copy(recenterTarget = latLng) }
        }
    }

    fun onEarthquakeSelected(earthquake: com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity) {
        _uiState.update { it.copy(selectedEarthquake = earthquake) }
    }

    fun onEarthquakeSelectionCleared() {
        _uiState.update { it.copy(selectedEarthquake = null) }
    }

    fun onRecenterHandled() {
        _uiState.update { it.copy(recenterTarget = null) }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appPreferencesRepository.settingsFlow.collect { settings ->
                _uiState.update { current -> current.copy(settings = settings) }
            }
        }
    }

    private fun observeEarthquakes() {
        viewModelScope.launch {
            try {
                observeEarthquakesUseCase().collect { earthquakes ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            earthquakes = earthquakes,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppError.Storage(e.message)
                    )
                }
            }
        }
    }

    private suspend fun syncUserLocation() {
        val userLocation = locationRepository.getLastKnownLatLng() ?: return
        appPreferencesRepository.setUserPosition(userLocation)
        appPreferencesRepository.setUserLocationInfo(locationRepository.getLocationInfo(userLocation))
    }

    private suspend fun resolveUserFallback(hasLocationPermission: Boolean): LatLng {
        return if (hasLocationPermission) {
            locationRepository.getLastKnownLatLng() ?: LatLng(DEFAULT_LAT, DEFAULT_LNG)
        } else {
            LatLng(DEFAULT_LAT, DEFAULT_LNG)
        }
    }
}
