package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.tasks.await
import java.util.Locale


@Composable
fun EarthquakeMapContent(
    padding: PaddingValues,
    eqs: List<EQEntity>,
    hasLocationPermission: Boolean,
    mapType: MapType,
    recenterTarget: LatLng?,
    onRecenterHandled: () -> Unit,
    manualLatLng: LatLng?
) {
    val context = LocalContext.current

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

    // Resolve a friendly name for the manual marker (reverse geocode once per position)
    var manualTitle by remember(manualLatLng) { mutableStateOf<String?>(null) }
    LaunchedEffect(manualLatLng) {
        manualTitle = manualLatLng?.let { ll -> getPlaceNameOrNull(context, ll) }
    }

    // Decide initial camera target ONCE:
    // 1) manual location
    // 2) user location (if granted & available)
    // 3) bounds fitting all markers
    // 4) world view
    LaunchedEffect(mapLoaded, hasLocationPermission, bounds, manualLatLng) {
        if (!mapLoaded || cameraInitialized) return@LaunchedEffect

        /*val didCenterOnUser = if (hasLocationPermission) {
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
        }*/

        val didCenterOnManual = if (manualLatLng != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(manualLatLng, 12f))
            true
        } else false

        if (!didCenterOnManual) {
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
        }
        cameraInitialized = true
    }

    // Map properties / UI
    val properties = remember(hasLocationPermission, mapType) {
        MapProperties(isMyLocationEnabled = hasLocationPermission, mapType = mapType)
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

    // One-shot recenter when parent requests it
    LaunchedEffect(recenterTarget) {
        recenterTarget?.let { latLng ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
            onRecenterHandled()
        }
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

            // if manual location, single green manual marker
            manualLatLng?.let { ll ->
                Marker(
                    state = rememberMarkerState(position = ll),
                    title = buildString {
                        append(manualTitle ?: "Selected location")
                        append(" (manual selected)")
                    },
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
suspend fun getLastKnownLatLng(context: Context): LatLng? {
    return try {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val loc = fused.lastLocation.await() ?: return null
        LatLng(loc.latitude, loc.longitude)
    } catch (_: Exception) {
        null
    }
}

private fun getPlaceNameOrNull(context: Context, latLng: LatLng): String? = try {
    val geocoder = Geocoder(context, Locale.getDefault())
    val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    list?.firstOrNull()?.getAddressLine(0)
} catch (_: Exception) {
    null
}