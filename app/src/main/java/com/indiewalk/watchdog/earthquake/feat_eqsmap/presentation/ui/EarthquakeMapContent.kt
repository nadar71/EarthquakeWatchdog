package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.model.AppSettings
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getAddressFromLatLng
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getLastKnownLatLng
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getPlaceNameOrNull
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
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
    val TAG = "EarthquakeMapContent"
    Log.d(TAG, "EarthquakeMapContent Opened")
    Log.d(TAG, "settings manualLocOn : ${settings.manualLocOn}")
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
        position = CameraPosition.fromLatLngZoom(worldCenter, 7f)
    }


    var mapLoaded by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }

    // Manual localization marker address
    var manualPositionTitle by remember(settings.manualPosition) { mutableStateOf<String?>(null) }
    LaunchedEffect(settings.manualPosition) {
        manualPositionTitle = settings.manualPosition.let { ll ->
            // getPlaceNameOrNull(context, ll)
            val address = getAddressFromLatLng(context, ll)
            address?.getAddressLine(0) + " " + address?.locality + " " + address?.countryCode
        }
    }

    // Init camera target :
    // 1) manual location
    // 2) user location (if granted & available)
    // 3) default location
    LaunchedEffect(mapLoaded, hasLocationPermissions/*, bounds*/, settings.manualLocOn) {
        if (!mapLoaded || cameraInitialized) return@LaunchedEffect

        // if manual is on, center map in manual location
        val didCenterOnManual = if (settings.manualLocOn) {
            Log.d("EarthquakeMapContent", "Centering on manual location: ${settings.manualPosition}")
            cameraPositionState.animate(CameraUpdateFactory
                .newLatLngZoom(settings.manualPosition, 7f))
            true
        } else false

        // if manual is off, center map in user location
        if (!didCenterOnManual) {
            val didCenterOnUser = if (hasLocationPermissions) {
                val userPosition = getLastKnownLatLng(context)
                Log.d("EarthquakeMapContent", "Centering on user location: $userPosition")
                if (userPosition != null) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userPosition, 7f))
                    true
                } else false
            } else false

            // ...else center in bound or in default location
            if (!didCenterOnUser) {
                Log.d("EarthquakeMapContent", "Centering on default location")
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), 7f))
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
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
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
        }


        // Grid
        if (showGrid) {
            TileOverlay(
                tileProvider = provider,
                transparency = 0f, // 0 = opaque, 1 = invisible
                zIndex = 1f        // draw above base map
            )
        }

        // Manual location marker (if enabled)
        if (settings.manualLocOn) {
            Log.d("EarthquakeMapContent", "Manual location on, set marker at : ${settings.manualPosition}")
            val markerState = rememberMarkerState(position = settings.manualPosition)
            
            // Update marker position when it changes
            LaunchedEffect(settings.manualPosition) {
                markerState.position = settings.manualPosition
            }
            
            Marker(
                state = markerState,
                title = (manualPositionTitle ?: stringResource(id = R.string.maps_selected_location)),
                snippet = " ${stringResource(id = R.string.maps_manual_selected)}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }
    }
}


