package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.Constants.DEFAULT_LNG


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPickerNoPermissionsReq(
    initialFallback: LatLng,
    onLocationSelected: (String, LatLng) -> Unit,
    hasLocationPermissions: Boolean,
    onDismiss: () -> Unit,
    onLocationChange: (String) -> Unit
) {
    val TAG = "LocationPickerNoPermissionsReq"
    Log.d(TAG, "LocationPickerNoPermissionsReq Opened")

    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    var selectedCoordinates by remember { mutableStateOf<LatLng?>(null) }
    var selectedLocationName by remember { mutableStateOf("") }

    // Check permission (no requesting here)
    /*val isGranted = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }*/

    // Init camera: go to to  initialFallback position ( manual, user, default position)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialFallback, 8f)
    }

    // Init starting marker attributes from fallback
    LaunchedEffect(initialFallback) {
        selectedCoordinates = initialFallback
        val addr = geocoder.getFromLocation(initialFallback.latitude, initialFallback.longitude, 1)
        selectedLocationName = addr?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
        onLocationChange(selectedLocationName)
    }

    // If permission is granted, try to move camera to user once
    // TODO: check if necessary
    LaunchedEffect(hasLocationPermissions) {
        if (hasLocationPermissions) {
            moveToCurrentLocationIfPermitted(context, cameraPositionState) { current ->
                selectedCoordinates = current
                updateLocationName(context, current, onLocationChange).also { name ->
                    selectedLocationName = name
                }
            }
        }
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = true),
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                selectedCoordinates?.let { onLocationSelected(selectedLocationName, it) }
                onLocationChange(selectedLocationName)
                onDismiss()
            }) {
                Text(
                    text = stringResource(R.string.generic_ok).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.generic_cancel),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(selectedLocationName, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxSize()) {
                    GoogleMapView(
                        initialLocation = selectedCoordinates, // marker
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedCoordinates = latLng
                            // TODO : use function in map utils
                            val addr = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                            selectedLocationName = addr?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
                            onLocationChange(selectedLocationName)
                        },
                        onMapLoaded = { /* do nothing */ }
                    )

                    // Go to user real position  ONLY if permissions granted
                    // TODO: change, go to user position if permissions granted or else to fallback if not
                    if (hasLocationPermissions) {
                        IconButton(
                            onClick = {
                                moveToCurrentLocationIfPermitted(context, cameraPositionState) { current ->
                                    selectedCoordinates = current
                                    updateLocationName(context, current, onLocationChange).also { name ->
                                        selectedLocationName = name
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp, 8.dp, 8.dp, 90.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Current Location",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    )
}

// Function to check location permission and move the camera to the current position
// TODO : try to use getFusedLocationProviderClient in map utils
@SuppressLint("MissingPermission")
private fun moveToCurrentLocationIfPermitted(
    context: Context,
    cameraPositionState: CameraPositionState,
    onLocationUpdated: (LatLng) -> Unit  // Callback for updating location
) {
    // Check if permissions is granted for FINE location or COARSE location
    val fine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return

    // Attempt to get the last known location and update the camera position
    val fused = LocationServices.getFusedLocationProviderClient(context)
    fused.lastLocation.addOnSuccessListener { loc: Location? ->
        loc?.let {
            val ll = LatLng(it.latitude, it.longitude)
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(ll, 8f))
            onLocationUpdated(ll)
        }
    }.addOnFailureListener { e ->
        Log.e("LocationPicker", "Error fetching location: ${e.message}", e)
    }
}


private fun updateLocationName(
    context: Context,
    latLng: LatLng,
    onLocationChange: (String) -> Unit
): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addr = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    val name = addr?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
    onLocationChange(name)
    return name
}



// -------------------------------------- Previews --------------------------------------------------

@Preview(showBackground = true)
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
            LocationPickerNoPermissionsReq(
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
}
