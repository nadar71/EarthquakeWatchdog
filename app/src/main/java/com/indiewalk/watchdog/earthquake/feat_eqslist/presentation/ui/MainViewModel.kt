package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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



    // var context: EarthquakeApp? = null
    // var eqList: LiveData<List<EarthquakeUI>>? = null
        // private set

    // Livedata var on Earthquake obj to populate through ViewModel
    // ** not used for the moment
    // private val earthquakeUISingleEntry: LiveData<EarthquakeUI>? = null

    // repository ref
    // private var eqRepository: EarthquakeRepository? = null

    // Preferences value
    // private var minMagnitude: String? = null

    // SharePreferences ref
    // private var sharedPreferences: SharedPreferences? = null

    // min mgnitudine value
    // private var dMinMagnitude: Double = 0.0


    /*constructor() {
        context = EarthquakeApp.getsContext() as EarthquakeApp?

        // init repository
        eqRepository = (EarthquakeApp.getsContext() as EarthquakeApp).repository
        eqList = eqRepository!!.earthquakesList

        // init shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }*/


    /*constructor(listType: String) {
        Log.d(TAG, "Actively retrieving the collections from repository")

        context = EarthquakeApp.getsContext() as EarthquakeApp?

        // get repository instance
        // eqRepository = ((AppEarthquake) AppEarthquake.getsContext()).getRepository();
        eqRepository = (EarthquakeApp.getsContext() as EarthquakeApp).repositoryWithDataSource

        // init shared preferences and get value
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        checkPreferences()
        dMinMagnitude = java.lang.Double.parseDouble(minMagnitude)


        if (listType == MainActivity.ORDER_BY_DESC_MAGNITUDE) {
            Log.d(TAG, "setupAdapter: ORDER_BY_DESC_MAGNITUDE : $listType")
            eqList = eqRepository!!.loadAll_orderby_desc_mag(dMinMagnitude)

        }
        if (listType == MainActivity.ORDER_BY_ASC_MAGNITUDE) {
            Log.d(TAG, "setupAdapter: ORDER_BY_ASC_MAGNITUDE : $listType")
            eqList = eqRepository!!.loadAll_orderby_asc_mag(dMinMagnitude)

        } else if (listType == MainActivity.ORDER_BY_MOST_RECENT) {
            Log.d(TAG, "setupAdapter: ORDER_BY_MOST_RECENT : $listType")
            eqList = eqRepository!!.loadAll_orderby_most_recent(dMinMagnitude)

        } else if (listType == MainActivity.ORDER_BY_OLDEST) {
            Log.d(TAG, "setupAdapter: ORDER_BY_OLDEST : $listType")
            eqList = eqRepository!!.loadAll_orderby_oldest(dMinMagnitude)

        } else if (listType == MainActivity.ORDER_BY_NEAREST) {
            Log.d(TAG, "setupAdapter: ORDER_BY_NEAREST : $listType")
            eqList = eqRepository!!.loadAll_orderby_nearest(dMinMagnitude)

        } else if (listType == MainActivity.ORDER_BY_FURTHEST) {
            Log.d(TAG, "setupAdapter: ORDER_BY_FURTHEST : $listType")
            eqList = eqRepository!!.loadAll_orderby_furthest(dMinMagnitude)

        } else if (listType == MainActivity.LOAD_ALL_NO_ORDER) {
            Log.d(TAG, "setupAdapter: LOAD_ALL_NO_ORDER : $listType")
            eqList = eqRepository!!.loadAll()
        }

    }*/


    // Set and check location coordinates from shared preferences.If not set, put default value
    /*private fun checkPreferences() {
        // recover min magnitude value from prefs or set a default from string value
        minMagnitude = sharedPreferences!!.getString(
            context!!.getString(R.string.settings_min_magnitude_key),
            context!!.getString(R.string.settings_min_magnitude_default)
        )

        // check preferences safety
        safePreferencesValue()

    }*/


    // making code more robust checking if for same reasons the default value stored are null or
    // not equals to none of the preferences stored values (e.g.  in case of key value change on code
    // but user saved with the previous one with previous app version )
    /*private fun safePreferencesValue() {

        val editor = sharedPreferences!!.edit()

        // minMagnitude safe
        if (minMagnitude!!.isEmpty() || minMagnitude == null) {
            setMinMagDefault(editor)
        }

        if (minMagnitude != context!!.getString(R.string.settings_1_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_2_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_3_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_4_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_4_5_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_5_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_5_5_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_6_0_min_magnitude_value) &&
            minMagnitude != context!!.getString(R.string.settings_6_5_min_magnitude_value)
        ) {
            setMinMagDefault(editor)
        }

    }*/

    // Set min_magnitude to default
    /*private fun setMinMagDefault(editor: SharedPreferences.Editor) {
        minMagnitude = context!!.getString(R.string.settings_min_magnitude_default)
        editor.putString(
            context!!.getString(R.string.settings_min_magnitude_key),
            context!!.getString(R.string.settings_min_magnitude_default)
        )
    }*/


}
