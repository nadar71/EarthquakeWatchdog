package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

// @file:Suppress("MissingPermission")

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.state.MapUiState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeMapScreen(
    navController: NavHostController,
    mapViewModel: MapViewModel = hiltViewModel(),
    onLocationGranted: () -> Unit = {}, // hooks for future logic
    onLocationDenied: () -> Unit = {},
) {
    val TAG = "EarthquakeMapScreen"
    val context = LocalContext.current
    // UI state for dialogs
    var showPrePermissionDialog by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }
    var askedOnce by remember { mutableStateOf(false) }

    // Ask location permission on first composition
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Whether we can enable "my location" layer/button
    val hasLocationPermission by remember(permissions) {
        derivedStateOf { permissions.allPermissionsGranted }
    }

    // Decide when to show dialogs
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            showPrePermissionDialog = true
        }
    }

    // If user was asked and still not granted, show the denied dialog
    LaunchedEffect(hasLocationPermission, askedOnce) {
        if (askedOnce && !hasLocationPermission) {
            showDeniedDialog = true
        } else {
            showDeniedDialog = false
        }
    }

    /*LaunchedEffect(Unit) {
        permissions.launchMultiplePermissionRequest()
    }
*/
    val eqsUIFromDBState by mapViewModel.eqsUIFromDBState.collectAsStateWithLifecycle()

    // ---- Dialogs ----
    if (showPrePermissionDialog) {
        PermissionRationaleDialog(
            onDismiss = { showPrePermissionDialog = false }, // optional close
            onContinue = {
                showPrePermissionDialog = false
                askedOnce = true
                permissions.launchMultiplePermissionRequest()
            }
        )
    }

    if (showDeniedDialog && !hasLocationPermission) {
        PermissionDeniedDialog(
            onOpenSettings = { openAppSettings(context) },
            onContinue = { showDeniedDialog = false } // continue without location
        )
    }

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
                    IconButton(onClick = {  /*Handle icon click*/  }) {
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
        when (val s = eqsUIFromDBState) {
            is MapUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is MapUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load earthquakes")
                }
            }
            is MapUiState.Success -> {
                val eqs = s.data.orEmpty()
                // Render your GoogleMap with markers using 'eqs'
                EarthquakeMapContent(
                    padding = padding,
                    eqs = eqs,
                    hasLocationPermission = hasLocationPermission,
                    onLocationGranted = { /* optional */ },
                    onLocationDenied = { /* optional */ }
                )
            }
        }
    }
}

/* ---------------- Dialogs ---------------- */

@Composable
private fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allow location access") },
        text = {
            Text(
                "We use your location to center the map near you. " +
                        "Earthquakes will be shown regardless of your choice."
            )
        },
        confirmButton = {
            TextButton(onClick = onContinue) { Text("Continue") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PermissionDeniedDialog(
    onOpenSettings: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onContinue,
        title = { Text("Location permission denied") },
        text = {
            Text(
                "You can still browse the map, but we can’t show your blue dot or center on your position.\n\n" +
                        "To enable location:\n• Open system Settings → App permissions → Location\n" +
                        "• Or continue without it and set a location manually later."
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text("Open Settings") }
        },
        dismissButton = {
            TextButton(onClick = onContinue) { Text("Continue") }
        }
    )
}

/* ------------- Helpers ------------- */

private fun openAppSettings(context: Context) {
    val uri = Uri.fromParts("package", context.packageName, null)
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}


