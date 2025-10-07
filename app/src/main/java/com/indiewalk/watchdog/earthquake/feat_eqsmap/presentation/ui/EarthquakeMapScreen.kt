package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

// @file:Suppress("MissingPermission")

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.state.MapUiState
import kotlin.math.max

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeMapScreen(
    navController: NavHostController,
    mapViewModel: MapViewModel = hiltViewModel(),
    onLocationGranted: () -> Unit = {}, // hooks for future logic
    onLocationDenied: () -> Unit = {},
) {
    // Ask location permission on first composition
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(Unit) {
        permissions.launchMultiplePermissionRequest()
    }

    // Whether we can enable "my location" layer/button
    val hasLocationPermission by remember(permissions) {
        derivedStateOf { permissions.allPermissionsGranted }
    }

    val eqsUIFromDBState by mapViewModel.eqsUIFromDBState.collectAsStateWithLifecycle()

    ScaffoldModel(
        navController = navController,
        title = "Map"
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


@Composable
private fun EarthquakeMapContent(
    padding: PaddingValues,
    eqs: List<EQEntity>,
    hasLocationPermission: Boolean,
    onLocationGranted: () -> Unit,
    onLocationDenied: () -> Unit
) {
    // Notify hooks (optional)
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) onLocationGranted() else onLocationDenied()
    }

    // Camera setup
    val worldCenter = LatLng(0.0, 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(worldCenter, 2f)
    }

    // Build bounds from earthquakes
    val bounds: LatLngBounds? = remember(eqs) {
        val builder = LatLngBounds.Builder()
        var count = 0
        eqs.forEach { e ->
            val lat = e.latitude
            val lng = e.longitude
            if (lat != null && lng != null) {
                builder.include(LatLng(lat, lng))
                count++
            }
        }
        if (count > 0) builder.build() else null
    }

    // Animate camera to fit markers (after map loads)
    var mapLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(mapLoaded, bounds) {
        if (mapLoaded && bounds != null) {
            // Add padding so markers are not at the very edge
            val paddingPx = 80
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
        }
    }

    // Map properties / UI
    val properties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission
        )
    }
    val uiSettings = remember(hasLocationPermission) {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = hasLocationPermission,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true
        )
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        onMapLoaded = { mapLoaded = true }
    ) {
        // Add a marker for each earthquake
        eqs.forEach { eq ->
            val lat = eq.latitude
            val lng = eq.longitude
            if (lat != null && lng != null) {
                val pos = LatLng(lat, lng)
                val title = eq.place ?: eq.id
                val magText = eq.mag?.let { "M %.1f".format(it) } ?: "M ?"
                val depthText = eq.depthKm?.let { "Depth: %.0f km".format(it) } ?: ""
                val snippet = listOf(magText, depthText).filter { it.isNotBlank() }.joinToString(" • ")

                Marker(
                    state = rememberMarkerState(position = pos),
                    title = title,
                    snippet = snippet
                )
            }
        }
    }
}
