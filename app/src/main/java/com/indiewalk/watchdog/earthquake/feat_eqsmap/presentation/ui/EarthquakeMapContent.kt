package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.maps.android.compose.MapType
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.model.AppSettings
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getLastKnownLatLng
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getPlaceNameOrNull
import com.google.maps.android.compose.Polyline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.compose.TileOverlay
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components.GraticuleTileProvider


@Composable
fun EarthquakeMapContent(
    padding: PaddingValues,
    eqs: List<EQEntity>,
    hasLocationPermissions: Boolean,
    mapType: MapType,
    recenterTarget: LatLng?,
    onRecenterHandled: () -> Unit,
    settings: AppSettings,
) {
    val context = LocalContext.current

    val showGrid by rememberSaveable { mutableStateOf(true) }

    // Create once; tiles are cached by Google Maps
    val provider = remember {
        GraticuleTileProvider(
            stepDegrees = 10,
            // lineColor = 0x95FF5722.toInt(), // orange
            lineWidthPx = 1f
        ) as TileProvider
    }

    // Camera init
    val worldCenter = LatLng(0.0, 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(worldCenter, 8f)
    }

    // Build bounds from earthquakes
    /*val bounds: LatLngBounds? = remember(eqs) {
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
    }*/


    var mapLoaded by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }

    // Manual localization marker address (reverse geocoding manual position)
    var manualTitle by remember(settings.manualPosition) { mutableStateOf<String?>(null) }
    LaunchedEffect(settings.manualPosition) {
        manualTitle = settings.manualPosition.let { ll -> getPlaceNameOrNull(context, ll) }
    }

    // Init camera target :
    // 1) manual location
    // 2) user location (if granted & available)
    // 3) bounds fitting all markers/default location
    LaunchedEffect(mapLoaded, hasLocationPermissions/*, bounds*/, settings.manualLocOn) {
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

        // if manual is on, center map in manual location
        val didCenterOnManual = if (settings.manualLocOn) {
            Log.d("EarthquakeMapContent", "Centering on manual location: ${settings.manualPosition}")
            cameraPositionState.animate(CameraUpdateFactory
                .newLatLngZoom(settings.manualPosition, 8f))
            true
        } else false

        // if manual is off, center map in user location
        if (!didCenterOnManual) {
            val didCenterOnUser = if (hasLocationPermissions) {
                val userPosition = getLastKnownLatLng(context)
                Log.d("EarthquakeMapContent", "Centering on user location: $userPosition")
                if (userPosition != null) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userPosition, 8f))
                    true
                } else false
            } else false

            // ...else center in bound or in default location
            if (!didCenterOnUser) {
                /*when {
                    bounds != null -> {
                        Log.d("EarthquakeMapContent", "Centering on bounds: $bounds")
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 80))
                    }
                    else -> {
                        Log.d("EarthquakeMapContent", "Centering on default location: $bounds")
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), 2f))
                    }
                }*/
                Log.d("EarthquakeMapContent", "Centering on default location")
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), 8f))
            }
        }
        cameraInitialized = true
    }

    // Map properties / UI
    val properties = remember(hasLocationPermissions, mapType) {
        MapProperties(isMyLocationEnabled = hasLocationPermissions, mapType = mapType)
    }
    val uiSettings = remember(hasLocationPermissions) {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = hasLocationPermissions,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true
        )
    }

    // One-shot recenter when parent requests it
    LaunchedEffect(recenterTarget) {
        recenterTarget?.let { latLng ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 8f))
            onRecenterHandled()
        }
    }

    // Draw MAP
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
        // Add marker for each earthquake
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

            // if manual location, draw single green manual marker
            settings.manualPosition.let { ll ->
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

        // Grid
        if (showGrid) {
            TileOverlay(
                tileProvider = provider,
                transparency = 0f, // 0 = opaque, 1 = invisible
                zIndex = 1f        // draw above base map
            )
        }
    }
}


