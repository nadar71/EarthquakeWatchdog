package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.feat_ads.presentation.AdMobBannerView
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.toEarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EarthquakeCard
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.EqItemDialog
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences.FilterSheet

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun EarthquakeListScreen(
    currentDestination: AppDestination,
    onTopLevelDestinationSelected: (AppDestination) -> Unit,
    onOpenDetails: (String) -> Unit,
    onOpenMap: (Double, Double) -> Unit,
    earthquakeListViewModel: EarthquakeListViewModel = hiltViewModel()
) {
    val uiState by earthquakeListViewModel.uiState.collectAsStateWithLifecycle()

    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val hasLocalPermissions by remember(permissions) {
        derivedStateOf { permissions.allPermissionsGranted }
    }
    LaunchedEffect(Unit) {
        earthquakeListViewModel.onScreenStarted(hasLocalPermissions)
    }

    LaunchedEffect(hasLocalPermissions) {
        earthquakeListViewModel.onLocationPermissionChanged(hasLocalPermissions)
    }

    ScaffoldModel(
        currentDestination = currentDestination,
        onTopLevelDestinationSelected = onTopLevelDestinationSelected,
        topBar = {
            TopAppBar(
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
                        modifier = Modifier.padding(start = 5.dp),
                        size = 50.dp,
                        frameDurationMs = 90L
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
    ) { padding ->
        val bottomInset = padding.calculateBottomPadding()
        val topInset = padding.calculateTopPadding()

        Box(modifier = Modifier.fillMaxSize()) {
            EarthquakeListContent(
                uiState = uiState,
                onRefresh = earthquakeListViewModel::onRefreshRequested,
                onFilterRequested = { earthquakeListViewModel.onFilterSheetVisibilityChanged(true) },
                onEarthquakeSelected = earthquakeListViewModel::onEarthquakeSelected,
                contentPadding = PaddingValues(top = topInset + 8.dp, bottom = bottomInset),
                refreshIndicatorTopPadding = topInset,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = bottomInset)
            )

            AdMobBannerView(
                adUnitId = stringResource(R.string.admob_key_bottom_banner),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }

        if (uiState.isFilterSheetVisible) {
            FilterSheet(
                eqsCount = uiState.allEarthquakes.size,
                lastRefreshTime = uiState.settings.lastRefreshTime,
                startDate = uiState.filterSettings.startDate,
                filterSettings = uiState.filterSettings,
                onConfirm = earthquakeListViewModel::onFilterConfirmed,
                onDismiss = { earthquakeListViewModel.onFilterSheetVisibilityChanged(false) }
            )
        }

        uiState.selectedEarthquake?.let { current ->
            EqItemDialog(
                eq = current,
                onMapClick = {
                    val latitude = current.latitude
                    val longitude = current.longitude
                    if (latitude != null && longitude != null) {
                        earthquakeListViewModel.onEarthquakeDialogDismissed()
                        onOpenMap(latitude, longitude)
                    }
                },
                onDismiss = earthquakeListViewModel::onEarthquakeDialogDismissed
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EarthquakeListContent(
    uiState: com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EarthquakeListUiState,
    onRefresh: () -> Unit,
    onFilterRequested: () -> Unit,
    onEarthquakeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    refreshIndicatorTopPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val listState = rememberLazyListState()
    val isFabVisible by remember { derivedStateOf { !listState.isScrollInProgress } }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = onRefresh
    )

    Box(
        modifier = modifier
            .testTag("earthquake-list-content")
            .pullRefresh(pullRefreshState)
    ) {
        if (uiState.filteredEarthquakes.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                items(uiState.filteredEarthquakes, key = { it.id }) { eq ->
                    EarthquakeCard(
                        eq = eq.toEarthquakeUI(),
                        hasLocalPermissions = uiState.hasLocationPermission,
                        settings = uiState.settings,
                        onClick = { onEarthquakeSelected(eq.id) }
                    )
                }
            }
        } else if (!uiState.isLoading) {
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

        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = refreshIndicatorTopPadding),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        AnimatedVisibility(
            visible = isFabVisible,
            enter = fadeIn() + slideIn(initialOffset = { IntOffset(0, 100) }),
            exit = fadeOut() + slideOut(targetOffset = { IntOffset(0, 100) }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp + 60.dp)
        ) {
            Box(modifier = Modifier.wrapContentSize()) {
                FloatingActionButton(
                    onClick = onFilterRequested,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("earthquake-list-filter"),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp,
                        focusedElevation = 4.dp,
                        hoveredElevation = 3.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.filter_title),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (uiState.filterSettings.activeCounts > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                    ) {
                        Text(uiState.filterSettings.activeCounts.toString())
                    }
                }
            }
        }
    }
}
