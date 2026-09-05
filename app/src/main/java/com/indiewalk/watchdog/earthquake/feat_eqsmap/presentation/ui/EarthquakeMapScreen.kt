package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraGreen_dark
import com.indiewalk.watchdog.earthquake.feat_ads.presentation.AdMobBannerView
import androidx.compose.ui.platform.testTag
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components.EarthquakeMapBottomInfoCard

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeMapScreen(
    currentDestination: AppDestination,
    onTopLevelDestinationSelected: (AppDestination) -> Unit,
    initialLatLng: LatLng? = null,
    mapViewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()

    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val hasLocalPermissions by remember(permissions) { derivedStateOf { permissions.allPermissionsGranted } }

    var showOptions by rememberSaveable { mutableStateOf(false) }
    var mapType by rememberSaveable { mutableStateOf(MapType.TERRAIN) }

    LaunchedEffect(hasLocalPermissions) {
        mapViewModel.onLocationPermissionChanged(hasLocalPermissions)
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
                        text = stringResource(R.string.maps_title),
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
                actions = {
                    IconButton(onClick = { showOptions = !showOptions }) {
                        Box {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.maps_settings_content_description),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (uiState.settings.manualLocOn) {
                                Icon(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-2).dp),
                                    painter = painterResource(id = R.drawable.ic_hand),
                                    contentDescription = null,
                                    tint = extraGreen_dark
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
    ) { padding ->
        val bottomInset = padding.calculateBottomPadding()

        when {
            uiState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = bottomInset),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    AdMobBannerView(
                        adUnitId = stringResource(R.string.admob_key_bottom_banner),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    )
                }
            }

            uiState.error != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = bottomInset),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(id = R.string.map_error_loading_earthquakes))
                    AdMobBannerView(
                        adUnitId = stringResource(R.string.admob_key_bottom_banner),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    )
                }
            }

            else -> {
                var composeMap by remember { mutableStateOf(false) }
                var mapRenderReady by remember(uiState.earthquakes) { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    // Let the loading screen render before Google Maps starts its heavier setup.
                    withFrameNanos { }
                    composeMap = true
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = padding.calculateBottomPadding())
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (composeMap) {
                            EarthquakeMapContent(
                                padding = PaddingValues(),
                                eqs = uiState.earthquakes,
                                hasLocationPermissions = uiState.hasLocationPermission,
                                initialLatLng = initialLatLng,
                                mapType = mapType,
                                recenterTarget = uiState.recenterTarget,
                                onRecenterHandled = mapViewModel::onRecenterHandled,
                                settings = uiState.settings,
                                onEarthquakeSelected = mapViewModel::onEarthquakeSelected,
                                onMapTapped = mapViewModel::onEarthquakeSelectionCleared,
                                onRenderReadyChanged = { mapRenderReady = it },
                            )
                        }

                        if (!mapRenderReady) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                                    .testTag("map-loading-indicator"),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        SelectedEarthquakeDetail(
                            earthquake = uiState.selectedEarthquake,
                            unitSystem = uiState.settings.unitSystem,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            onDismiss = mapViewModel::onEarthquakeSelectionCleared
                        )

                        if (showOptions) {
                            MapOptionsOverlayCard(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp),
                                isManualPositionOn = uiState.settings.manualLocOn,
                                hasLocationPermissions = uiState.hasLocationPermission,
                                mapType = mapType,
                                settings = uiState.settings,
                                onManualPositionToggle = { checked ->
                                    if (!checked) {
                                        mapViewModel.onManualLocationCleared()
                                    }
                                },
                                onManualPositionConfirmed = { latLng, locationInfo ->
                                    mapViewModel.onManualLocationConfirmed(latLng, locationInfo)
                                    showOptions = false
                                },
                                onMapTypeChange = { mapType = it },
                                onDismiss = { showOptions = false },
                            )
                        }
                    }

                    AdMobBannerView(
                        adUnitId = stringResource(R.string.admob_key_bottom_banner),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedEarthquakeDetail(
    earthquake: EQEntity?,
    unitSystem: com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    earthquake?.let { selectedEarthquake ->
        EarthquakeMapBottomInfoCard(
            earthquake = selectedEarthquake,
            unitSystem = unitSystem,
            modifier = modifier.testTag("map-selected-earthquake-detail"),
            onDismiss = onDismiss
        )
    }
}
