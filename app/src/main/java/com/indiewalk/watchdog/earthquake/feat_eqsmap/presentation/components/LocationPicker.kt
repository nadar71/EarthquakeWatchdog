package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import android.location.Address
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.model.preferences.LocationInfo
import com.indiewalk.watchdog.earthquake.core.util.extensions.toLocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getAddress
import java.util.Locale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPicker(
    initialFallback: LatLng,         // starting position
    userPosition: LatLng,            // user position: maybe Default position if permissions not granted
    hasLocationPermissions: Boolean,
    isManualOn: Boolean,
    onLocationSelected: (LatLng, LocationInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var isMapManualPositionSet by remember { mutableStateOf(false) }
    var selectedCoordinates by remember { mutableStateOf<LatLng?>(null) }
    var selectedLocationAddress by remember { mutableStateOf<Address?>(null) }
    var selectedLocationAddressString by remember { mutableStateOf("") }


    // Init camera: go to initialFallback position ( manual, user, default position)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialFallback, 8f)
    }

    // Init starting marker attributes from fallback
    LaunchedEffect(initialFallback) {
        selectedCoordinates = initialFallback
        selectedLocationAddress = getAddress(context, initialFallback)
        selectedLocationAddressString =
            selectedLocationAddress.toLocationInfo(context).concatString(context)
    }

    AlertDialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 0.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (isMapManualPositionSet){
                    selectedCoordinates?.let { coordinates ->
                        val selectedLocationAddress = getAddress(context, coordinates)
                        val safeAddress =
                            selectedLocationAddress ?: Address(Locale.getDefault()).apply {
                                setAddressLine(
                                    0,
                                    context.getString(R.string.generic_unknown_location)
                                )
                            }
                        onLocationSelected(
                            coordinates,
                            (selectedLocationAddress ?: safeAddress).toLocationInfo(context)
                        )
                    }
                }
                onDismiss()
            }) {
                Text(
                    text = stringResource(R.string.generic_ok).uppercase(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.generic_cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(selectedLocationAddressString, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxSize()) {
                    GoogleMapView(
                        initialLocation = selectedCoordinates, //  user position (real or default)
                        userPosition = userPosition, // user position (real or default)
                        cameraPositionState = cameraPositionState,
                        hasLocationPermissions = hasLocationPermissions,
                        isManualOn = isManualOn,
                        onMapClick = { latLng ->
                            isMapManualPositionSet = true
                            selectedCoordinates = latLng
                            selectedLocationAddress = getAddress(context, latLng)
                            selectedLocationAddressString =
                                selectedLocationAddress.toLocationInfo(context)
                                    .concatString(context)
                        },
                        onMapLoaded = { /* do nothing */ }
                    )

                    // permissions granted: go to user real/default position if granted/not granted
                    if (hasLocationPermissions) {
                        IconButton(
                            onClick = {
                                if (hasLocationPermissions) {
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngZoom(
                                            userPosition, 8f
                                        )
                                    )

                                } else {
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngZoom(
                                            initialFallback, 8f
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp, 8.dp, 8.dp, 90.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Current Location",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    )
}


// -------------------------------------- Previews --------------------------------------------------

/*@Preview(showBackground = true)
@Composable
fun PreviewMultiPurposeTextFieldWithLocation_noPermissionsReq() {
    var textFieldValue by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedCoordinates by remember { mutableStateOf<LatLng?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }

    Column {
        Text("Selected location: $selectedLocation")

        Button(onClick = { showLocationPicker = true }) {
            Text(
                "Pick Location")
        }

        if (showLocationPicker) {
            LocationPicker(
                initialFallback = LatLng(DEFAULT_LAT, DEFAULT_LNG),
                onLocationSelected = { locationName, latLng ->
                    selectedLocation = locationName
                    selectedCoordinates = latLng
                    showLocationPicker = false
                },
                onDismiss = {
                    showLocationPicker = false
                },
                onLocationChange = { locationName ->
                    // Update textFieldValue when location changes
                    textFieldValue = locationName
                },
                hasLocationPermissions = false,
            )
        }
    }
}*/
