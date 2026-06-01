package com.indiewalk.watchdog.earthquake.feat_intro.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntroUiState())
    val uiState: StateFlow<IntroUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<IntroEffect>()
    val effects: SharedFlow<IntroEffect> = _effects.asSharedFlow()

    private var navigationTriggered = false

    init {
        viewModelScope.launch {
            appPreferencesRepository.askedLocationOnceFlow.collect { askedOnce ->
                _uiState.update { it.copy(askedOnce = askedOnce) }
                if (askedOnce) {
                    navigateHomeOnce()
                }
            }
        }
    }

    fun onPermissionStateChanged(allGranted: Boolean) {
        if (allGranted) {
            viewModelScope.launch {
                syncUserLocation()
                navigateHomeOnce()
            }
        }
    }

    fun onPermissionRequestStarted() {
        _uiState.update { it.copy(isPermissionRequestStarted = true) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            viewModelScope.launch {
                syncUserLocation()
                navigateHomeOnce()
            }
        } else {
            _uiState.update { it.copy(showDeniedDialog = true) }
        }
    }

    fun onDeniedDialogDismissed() {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeniedDialog = false) }
            appPreferencesRepository.setAskedLocationOnce(true)
        }
    }

    fun onOpenSettingsRequested() {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeniedDialog = false) }
            appPreferencesRepository.setAskedLocationOnce(true)
            _effects.emit(IntroEffect.OpenAppSettings)
        }
    }

    private suspend fun syncUserLocation() {
        val userLocation = locationRepository.getLastKnownLatLng() ?: return
        appPreferencesRepository.setUserPosition(userLocation)
        appPreferencesRepository.setUserLocationInfo(locationRepository.getLocationInfo(userLocation))
    }

    private suspend fun navigateHomeOnce() {
        if (navigationTriggered) return
        navigationTriggered = true
        delay(1000)
        _effects.emit(IntroEffect.NavigateHome)
    }
}

