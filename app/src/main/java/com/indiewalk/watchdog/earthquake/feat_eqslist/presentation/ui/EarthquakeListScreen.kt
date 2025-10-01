package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.util.Log
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
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.toEarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeatureDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.toEQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EarthquakeCard
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromRemoteState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeListScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val TAG = "EarthquakeListScreen"
    var eqsCollection by remember { mutableStateOf<EQFeaturesCollectionDTO?>(null) }
    var eqsList by remember { mutableStateOf<List<EQFeatureDTO>?>(null) }

    val eqsUIState by mainViewModel.eqsUIFromRemoteState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        mainViewModel.refreshEQsList()
    }

    LaunchedEffect(eqsUIState) {
        when (eqsUIState) {
            is EQsListUiFromRemoteState.Idle -> {
                // showProgressBar = false
            }

            is EQsListUiFromRemoteState.Loading -> {
                // showProgressBar = true
            }

            is EQsListUiFromRemoteState.Success -> {
                Log.d(TAG, "EarthquakeListScreen: SUCCESS, eqs loaded")
                // showProgressBar = false
                eqsCollection =
                    (eqsUIState as EQsListUiFromRemoteState.Success<EQFeaturesCollectionDTO>).data
                // eqsList = eqsCollection?.features
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

    // TODO: adding recovering eqs data from db all the time in main screen after refresh db is success

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earthquakes") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(eqsCollection?.features ?: emptyList()) { eq ->
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    val generated = eqsCollection?.metadata?.generated
                    EarthquakeCard(
                        eq = eq.toEQEntity(generated).toEarthquakeUI(),
                    )
                    /*Text(text = eq.properties.place ?: "Unknown")
                    Text(text = "M ${eq.properties.mag ?: 0.0}")*/
                }
                /*HorizontalDivider(
                    modifier = Modifier.padding(16.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )*/
            }
        }
    }
}