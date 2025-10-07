package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqsmap.domain.use_cases.ObserveEarthquakesUseCase
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.state.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.indiewalkabout.fridgemanager.core.domain.model.ErrorResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val observeEarthquakesUseCase: ObserveEarthquakesUseCase
) : ViewModel() {

    private val TAG = "MapViewModel"
    private val _eqsUIFromDBState = MutableStateFlow<MapUiState<List<EQEntity>?>>(
        MapUiState.Loading)
    val eqsUIFromDBState: StateFlow<MapUiState<List<EQEntity>?>> =
        _eqsUIFromDBState.asStateFlow()

    init {
        observeEarthquakes()
    }

    fun observeEarthquakes() {
        viewModelScope.launch {
            observeEarthquakesUseCase()
                .onStart { _eqsUIFromDBState.value = MapUiState.Loading }
                .catch { e ->
                    Log.d(TAG, "observeEarthquakes: exception error: ${e.message}")
                    _eqsUIFromDBState.value = MapUiState.Error(
                        ErrorResponse(0, emptyList(), e.message ?: "Unknown error")
                    )
                }
                .collect { list ->
                    _eqsUIFromDBState.value = MapUiState.Success(list)
                }
        }
    }
}