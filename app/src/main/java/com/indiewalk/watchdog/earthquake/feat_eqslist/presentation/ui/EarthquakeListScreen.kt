package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.toEarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.toEQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EarthquakeCard
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromDBState
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromRemoteState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeListScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val TAG = "EarthquakeListScreen"
    var eqsCollection by remember { mutableStateOf<EQFeaturesCollectionDTO?>(null) }
    var eqsList by remember { mutableStateOf<List<EQEntity>?>(null) }
    var eqListLoadedFromDb by remember { mutableStateOf(false) }

    val eqsUIFromRemoteState by mainViewModel.eqsUIFromRemoteState.collectAsStateWithLifecycle()
    val eqsUIFromDBState by mainViewModel.eqsUIFromDBState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        mainViewModel.refreshEQsList()
    }

    LaunchedEffect(eqsUIFromRemoteState) {
        when (eqsUIFromRemoteState) {
            is EQsListUiFromRemoteState.Idle -> {
                // TODO :
                // showProgressBar = false
            }

            is EQsListUiFromRemoteState.Loading -> {
                // TODO :
                // showProgressBar = true
            }

            is EQsListUiFromRemoteState.Success -> {
                Log.d(TAG, "EarthquakeListScreen: SUCCESS, eqs loaded")
                // TODO :
                // showProgressBar = false
                eqsCollection =
                    (eqsUIFromRemoteState as EQsListUiFromRemoteState.Success<EQFeaturesCollectionDTO>).data
                // eqsList = eqsCollection?.features
                mainViewModel.loadAllEQsDB()
                Log.d(TAG, "EarthquakeListScreen: eqsList: $eqsList")
            }

            is EQsListUiFromRemoteState.Error -> {
                Log.d(TAG, "EarthquakeListScreen: ERROR!")

                // showProgressBar = false

                // TODO: rewrite error handling
                /*val errorResponse = (userSubscriptionsUiState as EQsListUiState.Error).error
                Log.e(TAG, "Error Object  : $errorResponse")
                if (errorResponse.statusCode == 0 && errorResponse.message.isEmpty()) {
                    errorMessage = errorResponse.error
                } else {
                    errorMessage = errorResponse.message.joinToString("\n")
                }
                showError = true*/
            }

            else -> {}
        }
    }



    LaunchedEffect(eqsUIFromDBState) {
        when (eqsUIFromDBState) {
            is EQsListUiFromDBState.Idle -> {
                // showProgressBar = false
            }
            is EQsListUiFromDBState.Loading -> {
                // showProgressBar = true
            }
            is EQsListUiFromDBState.Success -> {
                // showProgressBar = false
                eqsList = (eqsUIFromDBState as EQsListUiFromDBState.Success<List<EQEntity>?>).data
                Log.d(TAG, "Eq list loaded from db : $eqsList")
                eqListLoadedFromDb = true
            }
            is EQsListUiFromDBState.Error -> {
                // showProgressBar = false
                Log.e(TAG, "Error recovering foodList from db")
                eqListLoadedFromDb = true
            }
        }
    }


    // TODO: adding recovering eqs data from db all the time in main screen after refresh db is success

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Earthquakes",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                        },
                navigationIcon = {
                    LogoAnimationForward(
                        // modifier = Modifier.padding(start = 8.dp),
                        size = 50.dp,                // tweak to taste (24–32dp works well)
                        frameDurationMs = 90L
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,       // background
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,  // title text
                )

            )
        }
    ) { padding ->
        if (eqListLoadedFromDb && !eqsList.isNullOrEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(eqsCollection?.features ?: emptyList()) { eq ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        val generated = eqsCollection?.metadata?.generated
                        EarthquakeCard(
                            eq = eq.toEQEntity(generated).toEarthquakeUI(),
                        )
                    }
                }
            }
        }
        else {
            Text("No earthquakes found")
        }
    }
}