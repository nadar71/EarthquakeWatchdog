package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.domain.repository.FilterPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.domain.repository.LocationRepository
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.toEarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository.EQRepository
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases.FilterEarthquakesUseCase
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EarthquakeListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EarthquakeListViewModel @Inject constructor(
    private val repository: EQRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val filterPreferencesRepository: FilterPreferencesRepository,
    private val locationRepository: LocationRepository,
    private val filterEarthquakesUseCase: FilterEarthquakesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EarthquakeListUiState())
    val uiState: StateFlow<EarthquakeListUiState> = _uiState.asStateFlow()

    private var hasStarted = false

    init {
        observeSettings()
        observeFilters()
        observeEarthquakes()
    }

    fun onScreenStarted(hasLocationPermission: Boolean) {
        if (hasStarted) return
        hasStarted = true
        onLocationPermissionChanged(hasLocationPermission)
        refreshEarthquakes()
    }

    fun onLocationPermissionChanged(hasLocationPermission: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = hasLocationPermission) }
        if (hasLocationPermission) {
            viewModelScope.launch { syncUserLocation() }
        }
    }

    fun onRefreshRequested() {
        refreshEarthquakes()
    }

    fun onFilterSheetVisibilityChanged(isVisible: Boolean) {
        _uiState.update { it.copy(isFilterSheetVisible = isVisible) }
    }

    fun onFilterConfirmed(
        sortOption: EqsSortOption,
        minMagnitude: MinMagnitude,
        timeInterval: TimeInterval
    ) {
        viewModelScope.launch {
            filterPreferencesRepository.updateFilters(sortOption, minMagnitude, timeInterval)
            _uiState.update { it.copy(isFilterSheetVisible = false) }
        }
    }

    fun onEarthquakeSelected(id: String) {
        val selected = _uiState.value.filteredEarthquakes.firstOrNull { it.id == id }?.toEarthquakeUI()
        _uiState.update { it.copy(selectedEarthquake = selected) }
    }

    fun onEarthquakeDialogDismissed() {
        _uiState.update { it.copy(selectedEarthquake = null) }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appPreferencesRepository.settingsFlow.collect { settings ->
                _uiState.update { current -> current.copy(settings = settings) }
            }
        }
    }

    private fun observeFilters() {
        viewModelScope.launch {
            filterPreferencesRepository.filterSettingsFlow.collect { filterSettings ->
                _uiState.update { current ->
                    current.copy(
                        filterSettings = filterSettings,
                        filteredEarthquakes = filterEarthquakesUseCase(
                            current.allEarthquakes,
                            filterSettings
                        )
                    )
                }
            }
        }
    }

    private fun observeEarthquakes() {
        viewModelScope.launch {
            try {
                repository.observeAll().collect { earthquakes ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            allEarthquakes = earthquakes,
                            filteredEarthquakes = filterEarthquakesUseCase(
                                earthquakes,
                                current.filterSettings
                            ),
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = AppError.Storage(e.message)
                    )
                }
            }
        }
    }

    private fun refreshEarthquakes() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = current.allEarthquakes.isEmpty(),
                    isRefreshing = true,
                    error = null
                )
            }
            try {
                repository.fetchAndSaveDefault()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = AppError.Network(e.message)
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun syncUserLocation() {
        val userLocation = locationRepository.getLastKnownLatLng() ?: return
        appPreferencesRepository.setUserPosition(userLocation)
        appPreferencesRepository.setUserLocationInfo(locationRepository.getLocationInfo(userLocation))
    }
}

