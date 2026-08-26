package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.core.diagnostics.AppDiagnostics
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticCategory
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticEvent
import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.use_cases.LoadEarthquakeStatisticsUseCase
import com.indiewalk.watchdog.earthquake.feat_statistics.presentation.state.StatisticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val loadStatistics: LoadEarthquakeStatisticsUseCase,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var hasStarted = false
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            appPreferencesRepository.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun onScreenStarted() {
        if (hasStarted) return
        hasStarted = true
        load(forceRefresh = false)
    }

    fun onRefreshRequested() {
        load(forceRefresh = true)
    }

    private fun load(forceRefresh: Boolean) {
        AppDiagnostics.breadcrumb(DiagnosticEvent.STATISTICS_LOAD_REQUESTED)
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isInitialLoading = current.snapshot == null,
                    isRefreshing = forceRefresh || current.snapshot != null,
                    error = null
                )
            }
            try {
                loadStatistics(forceRefresh).collect { result ->
                    _uiState.update { current ->
                        current.copy(
                            snapshot = result.snapshot ?: current.snapshot,
                            unavailableSections = result.unavailableSections,
                            isInitialLoading = false,
                            isFromCache = result.isFromCache,
                            isStale = result.isStale,
                            error = null
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AppDiagnostics.recordNonFatal(DiagnosticCategory.STATISTICS, error)
                _uiState.update { current ->
                    current.copy(
                        isInitialLoading = false,
                        isStale = current.snapshot != null,
                        error = AppError.Network(error.message)
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isInitialLoading = false, isRefreshing = false)
                }
            }
        }
    }
}
