package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_ADDRESS
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_CITY
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_COUNTRY_CODE
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_POSITION
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.util.GraphicsUtil.bitmapDescriptorFromVector
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components.EarthquakeMarker
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components.GraticuleTileProvider


@Composable
fun EarthquakeMapContent(
    padding: PaddingValues,
    eqs: List<EQEntity>,
    hasLocationPermissions: Boolean,
    initialLatLng: LatLng? = null,
    mapType: MapType,
    recenterTarget: LatLng?,
    onRecenterHandled: () -> Unit,
    settings: AppSettings,
    onEarthquakeSelected: (EQEntity) -> Unit,
    onMapTapped: () -> Unit,
    onRenderReadyChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    val showGrid by rememberSaveable { mutableStateOf(true) }

    // Create once; tiles are cached by Google Maps
    val provider = remember {
        GraticuleTileProvider(
            stepDegrees = 10,
            lineWidthPx = 1f
        ) as TileProvider
    }

    // Camera init
    val worldCenter = LatLng(0.0, 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(worldCenter, 7f)
    }
    var cameraInitialized by remember { mutableStateOf(false) }
    var mapLoaded by remember { mutableStateOf(false) }
    val earthquakesWithCoordinates = remember(eqs) {
        eqs.filter { it.latitude != null && it.longitude != null }
    }
    val markerProgress = remember(earthquakesWithCoordinates.size) {
        MapMarkerRenderProgress(earthquakesWithCoordinates.size)
    }
    var renderedMarkerCount by remember(earthquakesWithCoordinates) { mutableStateOf(0) }

    LaunchedEffect(mapLoaded, markerProgress, earthquakesWithCoordinates) {
        if (!mapLoaded) return@LaunchedEffect

        while (renderedMarkerCount < earthquakesWithCoordinates.size) {
            withFrameNanos { }
            renderedMarkerCount = markerProgress.nextCount(renderedMarkerCount)
        }
    }

    val renderReady = markerProgress.isComplete(mapLoaded, renderedMarkerCount)
    LaunchedEffect(renderReady) {
        onRenderReadyChanged(renderReady)
    }



    val manualPositionTitle = remember(settings.manualLocationInfo) {
        settings.manualLocationInfo.concatString(context)
    }

    // Init camera target :
    // 1) check if initialLatLng is not null and in case go to eq location
    // 2) manual location
    // 3) user location (if granted & available)
    // 4) default location
    LaunchedEffect(mapLoaded, hasLocationPermissions, settings.manualLocOn) {
        if (!mapLoaded || cameraInitialized) return@LaunchedEffect

        if (initialLatLng != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(initialLatLng, 7f))
            cameraInitialized = true
            return@LaunchedEffect
        }

        // if manual is on, center map in manual location
        val didCenterOnManual = if (settings.manualLocOn) {
            cameraPositionState.animate(CameraUpdateFactory
                .newLatLngZoom(settings.manualPosition, 7f))
            true
        } else false

        // if manual is off, center map in user location
        if (!didCenterOnManual) {
            val didCenterOnUser = if (hasLocationPermissions) {
                val userPosition = settings.userPosition
                if (userPosition.latitude != DEFAULT_LAT || userPosition.longitude != DEFAULT_LNG) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userPosition, 7f))
                    true
                } else {
                    false
                }
            } else false

            // ...else center in bound or in default location
            if (!didCenterOnUser) {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), 7f))
            }
        }
        cameraInitialized = true
    }

    // Map properties / UI
    val properties = remember(hasLocationPermissions, mapType) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermissions, // avoid standard user pin: custom pin
            mapType = mapType
        )
    }
    val uiSettings = remember(hasLocationPermissions) {
        MapUiSettings(
            zoomControlsEnabled = true,
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
        onMapLoaded = { mapLoaded = true },
        onMapClick = { onMapTapped() }
    ) {

        // Build a BitmapDescriptor from a vector/PNG drawable
        val userPosPin = remember(R.drawable.ic_home) {
            bitmapDescriptorFromVector(context, R.drawable.ic_home, 0xFF0105FC.toInt())
        }

        // Custom user pin
        if (hasLocationPermissions ){
            Marker(
                state = rememberMarkerState(position =
                    LatLng(settings.userPosition.latitude, settings.userPosition.longitude)),
                icon = userPosPin,
                anchor = Offset(0.5f, 1.0f),    // center-bottom so tip points to LatLng
                flat = true,                    // allows icon rotation
                title = stringResource(id = R.string.maps_you_are_here)
            )
        }

        // Add marker for each earthquake
        earthquakesWithCoordinates.take(renderedMarkerCount).forEach { eq ->
            val lat = eq.latitude
            val lng = eq.longitude
            if (lat != null && lng != null) {
                key(eq.id) {
                    val pos = LatLng(lat, lng)
                    val title = eq.place ?: eq.id
                    EarthquakeMarker(
                        state = rememberMarkerState(position = pos),
                        title = title,
                        eq = eq,
                        onClick = {
                            onEarthquakeSelected(it)
                            true
                        }
                    )
                }
            }
        }

        // Default location marker
        if (!hasLocationPermissions && !settings.manualLocOn) {
            val markerState = rememberMarkerState(position = DEFAULT_POSITION)

            // Update marker position when it changes
            LaunchedEffect(settings.userPosition) {
                markerState.position = settings.userPosition
            }

            Marker(
                state = markerState,
                title = "$DEFAULT_ADDRESS, $DEFAULT_CITY, $DEFAULT_COUNTRY_CODE ",
                snippet = " ${stringResource(id = R.string.maps_default_location_label)}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )

        }

        // Manual location marker (if enabled)
        if (settings.manualLocOn) {
            val markerState = rememberMarkerState(position = settings.manualPosition)
            
            // Update marker position when it changes
            LaunchedEffect(settings.manualPosition) {
                markerState.position = settings.manualPosition
            }
            
            Marker(
                state = markerState,
                title = manualPositionTitle,
                snippet = " ${stringResource(id = R.string.maps_manual_selected)}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
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
