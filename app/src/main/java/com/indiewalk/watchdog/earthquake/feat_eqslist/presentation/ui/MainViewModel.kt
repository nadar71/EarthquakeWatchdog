package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.EarthquakeApp
import com.indiewalk.watchdog.earthquake.core.data.AppPrefs
import com.indiewalk.watchdog.earthquake.core.model.AppSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository.EQRepository
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases.FetchAndSaveDefaultUseCase
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases.LoadAllEQsUseCase
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromDBState
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromRemoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.indiewalkabout.fridgemanager.core.domain.model.ApiResponse
import eu.indiewalkabout.fridgemanager.core.domain.model.DbResponse
import eu.indiewalkabout.fridgemanager.core.domain.model.ErrorResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fetchAndSaveDefaultUseCase: FetchAndSaveDefaultUseCase,
    private val loadAllEQsUseCase: LoadAllEQsUseCase
) : ViewModel() {
    private val TAG = "MainViewModel"

    private val _eqsUIFromRemoteState = MutableStateFlow<EQsListUiFromRemoteState<EQFeaturesCollectionDTO>>(
        EQsListUiFromRemoteState.Idle)
    val eqsUIFromRemoteState: StateFlow<EQsListUiFromRemoteState<EQFeaturesCollectionDTO>> =
        _eqsUIFromRemoteState.asStateFlow()

    private val _eqsUIFromDBState = MutableStateFlow<EQsListUiFromDBState<List<EQEntity>?>>(
        EQsListUiFromDBState.Idle)
    val eqsUIFromDBState: StateFlow<EQsListUiFromDBState<List<EQEntity>?>> =
        _eqsUIFromDBState.asStateFlow()

    val settings: StateFlow<AppSettings> =
        AppPrefs.settingsFlow(EarthquakeApp.appContext)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())


    // request eqs list from remote and save to db
    fun refreshEQsList() {
        viewModelScope.launch {
            Log.d(TAG, "refreshEQsList: called")
            _eqsUIFromRemoteState.value = EQsListUiFromRemoteState.Loading
            try {
                val response = fetchAndSaveDefaultUseCase()
                _eqsUIFromRemoteState.value = when (response) {
                    is ApiResponse.Success -> {
                        Log.d(TAG, "refreshEQsList: success")
                        EQsListUiFromRemoteState.Success(response.data)
                    }
                    is ApiResponse.Error -> {
                        Log.d(TAG, "refreshEQsList: error")
                        EQsListUiFromRemoteState.Error(response.error)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "refreshEQsList: exception error: ${e.message}")
                _eqsUIFromRemoteState.value = EQsListUiFromRemoteState.Error(
                    ErrorResponse(0, emptyList(), e.message ?: "Unknown error")
                )
            }
        }
    }

    // get eqs list from db
    fun loadAllEQsDB() {
        viewModelScope.launch {
            _eqsUIFromDBState.value = EQsListUiFromDBState.Loading
            try {
                val response = loadAllEQsUseCase()
                _eqsUIFromDBState.value = when (response) {
                    is DbResponse.Success -> EQsListUiFromDBState.Success(response.data)
                    is DbResponse.Error -> EQsListUiFromDBState.Error(response.error)
                }
            } catch (e: Exception) {
                Log.d(TAG, "loadAllEQsDB: exception error: ${e.message}")
                _eqsUIFromDBState.value = EQsListUiFromDBState.Error(
                    ErrorResponse(0, emptyList(), e.message ?: "Unknown error")
                )
            }

        }
    }
}
