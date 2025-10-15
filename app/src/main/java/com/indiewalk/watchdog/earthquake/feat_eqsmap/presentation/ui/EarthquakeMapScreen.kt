package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui


import android.Manifest
import android.R.attr.top
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.maps.android.compose.MapType
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.openAppSettings
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components.PermissionDeniedDialog
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components.PermissionRationaleDialog
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.state.MapUiState


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeMapScreen(
    navController: NavHostController,
    mapViewModel: MapViewModel = hiltViewModel(),
    onManualPositionToggle: (Boolean) -> Unit = {},
    onLocationGranted: () -> Unit = {}, // hooks for future logic
    onLocationDenied: () -> Unit = {},
) {
    val TAG = "EarthquakeMapScreen"
    val context = LocalContext.current


    // 1) Permissions state: ask location permission on 1st composition
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 2) Permissions dialogs states
    var showPrePermissionDialog by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }
    //  persist across recomposition/config changes for this screen
    var hasAskedOnce by rememberSaveable { mutableStateOf(false) }


    // 3) Permissions Derived flags
    val allGranted by remember(permissions) { derivedStateOf { permissions.allPermissionsGranted } }
    val anyShouldShowRationale by remember(permissions) {
        derivedStateOf { permissions.permissions.any { it.status.shouldShowRationale } }
    }
    // Permanently denied = not granted: no rationale, and already asked once
    val anyPermanentlyDenied by remember(permissions, hasAskedOnce) {
        derivedStateOf {
            hasAskedOnce && permissions.permissions.any {
                (!it.status.isGranted && !it.status.shouldShowRationale)
            }
        }
    }

    // Options overlay/state
    var showOptions by rememberSaveable { mutableStateOf(false) }
    var manualPosition by rememberSaveable { mutableStateOf(false) }
    var mapType by rememberSaveable { mutableStateOf(MapType.TERRAIN) } // default Terrain


    // ---------------------------------------- LOGIC ----------------------------------------------
    val settings by mapViewModel.settings.collectAsStateWithLifecycle()

    // 4) Decide when to show dialogs
    // Show the pre-permission rationale ONLY if not granted and NOT permanently denied
    LaunchedEffect(Unit) {
        if (!allGranted && !anyPermanentlyDenied) {
            showPrePermissionDialog = true
        }
    }

    // If we asked already and still not granted:
    // show the denied dialog ONLY if rationale is available (i.e., NOT permanently denied)
    LaunchedEffect(allGranted, hasAskedOnce, anyShouldShowRationale, anyPermanentlyDenied) {
        showDeniedDialog = hasAskedOnce && !allGranted && anyShouldShowRationale && !anyPermanentlyDenied
    }

    // db state collection : get eqs list updated
    val eqsUIFromDBState by mapViewModel.eqsUIFromDBState.collectAsStateWithLifecycle()

    // ---- Dialogs ----
    if (showPrePermissionDialog) {
        PermissionRationaleDialog(
            onDismiss = { showPrePermissionDialog = false }, // optional close
            onContinue = {
                showPrePermissionDialog = false
                hasAskedOnce = true
                permissions.launchMultiplePermissionRequest()
            }
        )
    }

    // This will NOT show when permanently denied (anyPermanentlyDenied == true)
    if (showDeniedDialog) {
        PermissionDeniedDialog(
            onOpenSettings = { openAppSettings(context) },
            onContinue = { showDeniedDialog = false } // continue without location
        )
    }

    // ------------------------------------------- UI ----------------------------------------------
    ScaffoldModel(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = "Map",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {},
                actions = {
                    IconButton(onClick = {
                        // openAppSettings(context)
                        showOptions = !showOptions
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_equalizer),
                            contentDescription = "Map settings",
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
        when (val s = eqsUIFromDBState) {
            is MapUiState.Loading -> {
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is MapUiState.Error -> {
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load earthquakes")
                }
            }
            is MapUiState.Success -> {
                val eqs = s.data.orEmpty()
                Box(Modifier.fillMaxSize()) {
                    EarthquakeMapContent(
                        padding = padding,
                        eqs = eqs,
                        hasLocationPermission = allGranted,
                        mapType = mapType,
                        onLocationGranted = { /* optional */ },
                        onLocationDenied = { /* optional */ }
                    )

                    if (showOptions) {
                        MapOptionsOverlayCard(
                            modifier = Modifier
                                .align (Alignment.Center)
                                .padding(
                                    start = 32.dp,
                                    top = 32.dp, //padding.calculateTopPadding() + 8.dp,
                                    end = 32.dp,
                                    bottom = 32.dp
                                ),
                            manualPosition = manualPosition,
                            onManualPositionChange = {
                                manualPosition = it
                                onManualPositionToggle(it)
                            },
                            mapType = mapType,
                            onMapTypeChange = { mapType = it },
                            settings = settings,
                            onDismiss = { showOptions = false }
                        )
                    }
                }
            }
        }
    }
}






