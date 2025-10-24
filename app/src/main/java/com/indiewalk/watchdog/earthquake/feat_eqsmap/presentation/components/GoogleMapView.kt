package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import android.annotation.SuppressLint
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState


@SuppressLint("MissingPermission")
@Composable
fun GoogleMapView(
    cameraPositionState: CameraPositionState,
    initialLocation: LatLng?, // Added initial location parameter
    hasLocationPermissions: Boolean,
    onMapClick: (LatLng) -> Unit,
    onMapLoaded: () -> Unit
) {
    val TAG = "GoogleMapView"
    Log.d(TAG, "GoogleMapView Opened")

    // State to keep track of the marker position
    var markerPosition by remember(initialLocation) {
        mutableStateOf(initialLocation)
    }

    var justOpened by remember { mutableStateOf(true) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = MapProperties(
            isIndoorEnabled = true,
        ),
        uiSettings = MapUiSettings(
            compassEnabled = true,
            indoorLevelPickerEnabled = true,
            myLocationButtonEnabled = true
        ),
        cameraPositionState = cameraPositionState,
        onMapLoaded = { onMapLoaded() },
        onMapClick = { latLng ->
            justOpened = false
            markerPosition = latLng
            onMapClick(latLng)
        }
    ) {
        // Only display the marker if a position is set by the user click
        markerPosition?.let { position ->
            Marker(
                state = MarkerState(position = position),
                title = "${
                    Geocoder(LocalContext.current)
                        .getFromLocation(position.latitude, position.longitude, 1)
                        ?.get(0)
                        ?.getAddressLine(0)
                }",
                icon =
                    if (hasLocationPermissions && justOpened)
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    else
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }
    }
}

