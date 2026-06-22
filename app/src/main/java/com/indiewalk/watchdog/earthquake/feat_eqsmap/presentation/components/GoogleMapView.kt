package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import android.annotation.SuppressLint
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
import com.indiewalk.watchdog.earthquake.core.util.extensions.toLocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getAddress


@SuppressLint("MissingPermission")
@Composable
fun GoogleMapView(
    cameraPositionState: CameraPositionState,
    initialLocation: LatLng?, // Added initial location parameter
    userPosition: LatLng,     // user position (real or default)
    hasLocationPermissions: Boolean,
    isManualOn: Boolean,
    onMapClick: (LatLng) -> Unit,
    onMapLoaded: () -> Unit
) {
    val TAG = "GoogleMapView"
    Log.d(TAG, "GoogleMapView Opened")
    val context = LocalContext.current

    // State to keep track of the marker position
    var markerPosition by remember(initialLocation) {
        mutableStateOf(initialLocation)
    }

    var justOpened by remember { mutableStateOf(true) } // at start

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
        // with granted permissions display the user marker fixed
        if (hasLocationPermissions){
            Marker(
                state = MarkerState(position = userPosition),
                title = getAddress(context, userPosition).toLocationInfo(context).concatString(context),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        }

        // if manual position on, display the marker, then change it at clicking on map
        markerPosition?.let { position ->
                Marker(
                    state = MarkerState(position = position),
                    title = getAddress(context, position).toLocationInfo(context).concatString(context),
                    icon =
                        if (isManualOn && !hasLocationPermissions) // in manual always green at start and next
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        else if (!isManualOn && hasLocationPermissions && justOpened) // with granted always blue at start, next green
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        else
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )

        }
    }
}

