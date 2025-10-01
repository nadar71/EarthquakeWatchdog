package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.FeatureDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EarthquakeCard
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeListScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val TAG = "EarthquakeListScreen"
    var eqsCollection by remember { mutableStateOf<EQFeaturesCollectionDTO?>(null) }
    var eqsList by remember { mutableStateOf<List<FeatureDTO>?>(null) }

    val eqsUIState by mainViewModel.eqsUIState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        mainViewModel.refreshEQsList()
    }

    LaunchedEffect(eqsUIState) {
        when (eqsUIState) {
            is EQsListUiState.Idle -> {
                // showProgressBar = false
            }

            is EQsListUiState.Loading -> {
                // showProgressBar = true
            }

            is EQsListUiState.Success -> {
                Log.d(TAG, "EarthquakeListScreen: SUCCESS, eqs loaded")
                // showProgressBar = false
                eqsCollection =
                    (eqsUIState as EQsListUiState.Success<EQFeaturesCollectionDTO>).data
                eqsList = eqsCollection?.features
                Log.d(TAG, "EarthquakeListScreen: eqsList: $eqsList")
            }

            is EQsListUiState.Error -> {
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
            items(eqsList  ?: emptyList()) { eq ->
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    EarthquakeCard(eq = eq)
                    Text(text = eq.properties.place ?: "Unknown")
                    Text(text = "M ${eq.properties.mag ?: 0.0}")
                }
                HorizontalDivider(
                    modifier = Modifier.padding(16.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )
            }
        }
    }
}