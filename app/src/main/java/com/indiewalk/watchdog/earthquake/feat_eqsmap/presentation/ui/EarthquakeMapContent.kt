package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await


@Composable
fun EarthquakeMapContent(
    padding: PaddingValues,
    eqs: List<EQEntity>,
    hasLocationPermission: Boolean,
    onLocationGranted: () -> Unit,
    onLocationDenied: () -> Unit
) {
    val context = LocalContext.current

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


    var mapLoaded by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }
    // Decide initial camera target ONCE:
    // 1) user location (if permitted & available), else
    // 2) fit all markers, else
    // 3) world view
    LaunchedEffect(mapLoaded, hasLocationPermission, bounds) {
        if (!mapLoaded || cameraInitialized) return@LaunchedEffect

        val didCenterOnUser = if (hasLocationPermission) {
            val user = getLastKnownLatLng(context)
            if (user != null) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(user, 6f))
                true
            } else false
        } else false

        if (!didCenterOnUser) {
            when {
                bounds != null -> cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 80)
                )
                else -> cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(worldCenter, 2f)
                )
            }
        }
        cameraInitialized = true
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
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(padding)
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
                val snippet =
                    listOf(magText, depthText).filter { it.isNotBlank() }.joinToString(" • ")

                Marker(
                    state = rememberMarkerState(position = pos),
                    title = title,
                    snippet = snippet
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun getLastKnownLatLng(context: Context): LatLng? {
    return try {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val loc = fused.lastLocation.await() ?: return null
        LatLng(loc.latitude, loc.longitude)
    } catch (_: Exception) {
        null
    }
}