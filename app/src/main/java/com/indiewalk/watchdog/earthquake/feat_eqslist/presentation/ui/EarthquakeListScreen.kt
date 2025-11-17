package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.Manifest
import android.net.http.SslCertificate.restoreState
import android.net.http.SslCertificate.saveState
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationRoutes
import com.indiewalk.watchdog.earthquake.core.presentation.preferences.AppPrefsViewModel
import com.indiewalk.watchdog.earthquake.core.util.extensions.toLocationInfo
import com.indiewalk.watchdog.earthquake.feat_ads.presentation.AdBannerPlaceholder
import com.indiewalk.watchdog.earthquake.feat_ads.presentation.AdMobBannerView
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences.FilterPrefs
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.EarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.toEarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EarthquakeCard
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EqItemDialog
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences.FilterSheet
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences.FilterViewModel
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromDBState
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EQsListUiFromRemoteState
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.util.FilterUtil.filterList
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getAddress
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getLastKnownLatLng


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun EarthquakeListScreen(
    navController: NavHostController,
    earthquakeListViewModel: EarthquakeListViewModel = hiltViewModel(),
    filterViewModel: FilterViewModel = hiltViewModel(),
    appPrefsViewModel: AppPrefsViewModel = hiltViewModel()
) {
    val TAG = "EarthquakeListScreen"
    Log.d(TAG, "EarthquakeListScreen on")
    val context = LocalContext.current

    // eqs list & c.
    var isRemoteFetchCompleted by remember { mutableStateOf(false) }
    var eqsCollection by remember { mutableStateOf<EQFeaturesCollectionDTO?>(null) }
    var eqsList by remember { mutableStateOf<List<EQEntity>?>(null) }
    var eqsListFiltered by remember { mutableStateOf<List<EQEntity>?>(null) }
    var isEqListLoadedFromDb by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var showEqItemDialog by remember { mutableStateOf(false) }
    var currentEqItemClicked by remember { mutableStateOf<EarthquakeUI?>(null) }

    // Check current location permission state
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val hasLocalPermissions by remember(permissions) { derivedStateOf { permissions.allPermissionsGranted } }
    // FAB visibility State
    val isFabVisible by remember {
        derivedStateOf {
            !listState.isScrollInProgress
        }
    }
    var showProgressBar by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }


    // ------------------------------------- LOGIC -------------------------------------------------
    val sortOption by filterViewModel.sortOption.collectAsStateWithLifecycle()
    val minMag by filterViewModel.minMagFlow.collectAsStateWithLifecycle()
    val timeInterval by filterViewModel.timeIntervalFlow.collectAsStateWithLifecycle()
    val filterActiveCounts by filterViewModel.filterActiveCountsFlow.collectAsStateWithLifecycle()
    val settings by earthquakeListViewModel.settings.collectAsStateWithLifecycle()

    val eqsUIFromRemoteState by earthquakeListViewModel.eqsUIFromRemoteState.collectAsStateWithLifecycle()
    val eqsUIFromDBState by earthquakeListViewModel.eqsUIFromDBState.collectAsStateWithLifecycle()

    var isRefreshing = eqsUIFromRemoteState is EQsListUiFromRemoteState.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            Log.d(TAG, "EarthquakeListScreen: refreshing pulled, starting")
            earthquakeListViewModel.refreshEQsList()
        }
    )

    // Update user position and address in prefs in case location permissions changed meanwhile/later
    LaunchedEffect(Unit) {
        if (hasLocalPermissions) {
            val userLocation = getLastKnownLatLng(context = context)
            appPrefsViewModel.setUserPosition(userLocation)
            if (userLocation != null) {
                val address = getAddress(context = context, latLng = userLocation)
                address?.let {
                    appPrefsViewModel.setUserLocationInfo(
                        address.toLocationInfo(
                            context
                        )
                    )
                }
                Log.d(TAG, "EarthquakeListScreen: user Location: $userLocation")
                Log.d(TAG, "EarthquakeListScreen: user address: $address")
            }
        }
    }

    // Fetch data from remote at each opening
    LaunchedEffect(Unit) {
        // Debug : filter at screen opening
        println("Filter state on datastore: ${FilterPrefs.debugPrintEqFilterDataStore(context)}")
        earthquakeListViewModel.refreshEQsList()
    }

    // Check remote response
    LaunchedEffect(eqsUIFromRemoteState) {
        when (eqsUIFromRemoteState) {
            is EQsListUiFromRemoteState.Idle -> {
                showProgressBar = false
            }

            is EQsListUiFromRemoteState.Loading -> {
                showProgressBar = true
            }

            is EQsListUiFromRemoteState.Success -> {
                Log.d(TAG, "EarthquakeListScreen: SUCCESS, eqs loaded")
                showProgressBar = false
                eqsCollection =
                    (eqsUIFromRemoteState as EQsListUiFromRemoteState.Success<EQFeaturesCollectionDTO>).data
                // eqsList = eqsCollection?.features
                // earthquakeListViewModel.loadAllEQsDB()
                isRemoteFetchCompleted = true
                Log.d(TAG, "EarthquakeListScreen: eqsList: $eqsListFiltered")
            }

            is EQsListUiFromRemoteState.Error -> {
                Log.d(TAG, "EarthquakeListScreen: ERROR!")
                showProgressBar = false

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


    // load data from db when completed
    LaunchedEffect(isRemoteFetchCompleted) {
        earthquakeListViewModel.loadAllEQsDB()
    }


    // React to db response
    LaunchedEffect(eqsUIFromDBState) {
        when (eqsUIFromDBState) {
            is EQsListUiFromDBState.Idle -> {
                showProgressBar = false
            }

            is EQsListUiFromDBState.Loading -> {
                showProgressBar = true
            }

            is EQsListUiFromDBState.Success -> {
                showProgressBar = false
                eqsList = (eqsUIFromDBState as EQsListUiFromDBState.Success<List<EQEntity>?>).data
                Log.d(TAG, "Eq list loaded from db : $eqsList")
                eqsListFiltered = filterList(sortOption, minMag, timeInterval, eqsList)
                Log.d(TAG, "Eq list filtered : $eqsListFiltered")
                isEqListLoadedFromDb = true
            }

            is EQsListUiFromDBState.Error -> {
                showProgressBar = false
                Log.e(TAG, "Error recovering foodList from db")
                isEqListLoadedFromDb = true
            }
        }
    }

    // React to changes in filter options
    LaunchedEffect(sortOption, minMag, timeInterval) {
        eqsListFiltered = filterList(sortOption, minMag, timeInterval, eqsList)
    }


    // ------------------------------------- UI ----------------------------------------------------
    ScaffoldModel(
        navController = navController,
        topBar = {
            TopAppBar(
                modifier = Modifier,
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    // balancing space placeholder as  trail icon to title centering
                    Spacer(
                        modifier = Modifier
                            .width(50.dp)
                            .padding(end = 5.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,       // background
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,  // title text
                )

            )
        },
    ) { padding ->

        val bottomInset = padding.calculateBottomPadding()
        val topInset = padding.calculateTopPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = bottomInset)
                .pullRefresh(pullRefreshState)
        ) {
            Log.d(
                TAG, "Eq list filtered size: " +
                        "${if (eqsListFiltered != null) eqsListFiltered?.size else "null"}"
            )

            if (isEqListLoadedFromDb && !eqsListFiltered.isNullOrEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    //.padding(padding)
                    contentPadding = PaddingValues(bottom = bottomInset)
                ) {
                    items(eqsListFiltered ?: emptyList()) { eq ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            EarthquakeCard(
                                eq = eq.toEarthquakeUI(),
                                hasLocalPermissions = hasLocalPermissions,
                                settings = settings,
                                onClick = {
                                    currentEqItemClicked = eq.toEarthquakeUI()
                                    showEqItemDialog = true
                                }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.home_no_earthquakes_found),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Pull to refresh indicator (positioned at the top of the screen)
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = topInset),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )


            // ProgressBar
            if (showProgressBar) {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // Show filter FAB
            AnimatedVisibility(
                visible = isFabVisible,
                enter = fadeIn() + slideIn(initialOffset = { IntOffset(0, 100) }),
                exit = fadeOut() + slideOut(targetOffset = { IntOffset(0, 100) }),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = 16.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                ) {
                    FloatingActionButton(
                        onClick = {
                            showFilterSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp,
                            focusedElevation = 4.dp,
                            hoveredElevation = 3.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (filterActiveCounts > 0) {
                        Badge(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                        ) {
                            Text(filterActiveCounts.toString())
                        }
                    }
                }
            }

            /*AdBannerPlaceholder(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(60.dp)
            )*/

            AdMobBannerView(
                adUnitId = stringResource(R.string.admob_key_bottom_banner),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }

        // show filter dialog sheet
        if (showFilterSheet) {
            FilterSheet(
                filterViewModel = filterViewModel,
                onDismiss = { showFilterSheet = false }
            )
        }

        if (showEqItemDialog){
            val current = currentEqItemClicked ?: return@ScaffoldModel
            EqItemDialog(
                eq = current,
                onMapClick = {
                    val route = "map/${current.longitude}/${current.latitude}"
                    Log.d("EarthquakeListScreen", "Navigate to $route")
                    navController.navigate(route) {
                        // NB : ensures the bottom nav stays in sync
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onDismiss = { showEqItemDialog = false }
            )
        }
    }
}

