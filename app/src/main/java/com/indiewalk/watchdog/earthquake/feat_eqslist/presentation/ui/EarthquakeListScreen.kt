package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.toEarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.toEQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EarthquakeCard
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromDBState
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromRemoteState
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.model.toMappingSettings
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EarthquakeListScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val TAG = "EarthquakeListScreen"
    Log.d(TAG, "EarthquakeListScreen on")
    var eqsCollection by remember { mutableStateOf<EQFeaturesCollectionDTO?>(null) }
    var eqsList by remember { mutableStateOf<List<EQEntity>?>(null) }
    var eqListLoadedFromDb by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for FAB visibility
    val isFabVisible by remember {
        derivedStateOf {
            !listState.isScrollInProgress
        }
    }

    // Example badge count - replace with your actual data
    val notificationCount = remember { mutableStateOf(3) }

    // Pull to refresh state
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                mainViewModel.refreshEQsList()
                isRefreshing = false
            }
        }
    )

    // ------------------------------------- LOGIC -------------------------------------------------
    val settings by mainViewModel.settings.collectAsStateWithLifecycle()
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


    // ------------------------------------- UI ----------------------------------------------------
    ScaffoldModel(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    LogoAnimationForward(
                        modifier = Modifier
                            .padding(start = 5.dp),
                        size = 50.dp,
                        frameDurationMs = 90L
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle icon click */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,       // background
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,  // title text
                )

            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()){
            if (eqListLoadedFromDb && !eqsList.isNullOrEmpty()) {
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .padding(top = 4.dp),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

                LazyColumn(
                    state = listState,
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
                                eq = eq.toEQEntity(generated,settings.toMappingSettings()).toEarthquakeUI(),
                                settings = settings
                            )
                        }
                    }
                }

                // Add FAB
                AnimatedVisibility(
                    visible = isFabVisible,
                    enter = fadeIn() + slideIn(initialOffset = { IntOffset(0, 100) }),
                    exit = fadeOut() + slideOut(targetOffset = { IntOffset(0, 100) }),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .wrapContentSize()
                    ){
                        FloatingActionButton(
                            shape = CircleShape,
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "FAB clicked!",
                                        withDismissAction = true
                                    )
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Notifications"
                            )
                        }


                        if (notificationCount.value > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .zIndex(1f)
                                    .offset(x = 2.dp, y = (-2).dp)
                            ) {
                                Text(notificationCount.value.toString())
                            }
                        }
                    }
                }

            } else {
                Text("No earthquakes found")
            }
            // Add Snackbar host
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}