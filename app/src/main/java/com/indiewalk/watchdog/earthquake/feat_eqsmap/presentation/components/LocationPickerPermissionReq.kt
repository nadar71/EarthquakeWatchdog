package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
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
import androidx.compose.runtime.derivedStateOf
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
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale
import com.indiewalk.watchdog.earthquake.R



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPickerPermissionReq(
    initialLat: Double? = null,
    initialLng: Double? = null,
    onLocationSelected: (String, LatLng) -> Unit,
    onDismiss: () -> Unit,
    onLocationChange: (String) -> Unit,
) {
    val TAG = "LocationPicker"
    Log.d(TAG, "LocationPicker started")

    val context = LocalContext.current
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf("") }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    var mapLoaded by remember { mutableStateOf(false) }

    // Snackbar controls
    val showSnackbar = remember { mutableStateOf(false) }
    val snackbarMsg = stringResource(id = R.string.maps_permission_rationale_text_label)


    // Location permissions
    val fineLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarseLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)

    // Determine if permissions are granted
    val isLocationPermissionGranted = remember {
        derivedStateOf {
            fineLocationPermissionState.status.isGranted || coarseLocationPermissionState.status.isGranted
        }
    }

    // Request permissions if not granted
    LaunchedEffect(isLocationPermissionGranted.value) {
        if (!isLocationPermissionGranted.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fineLocationPermissionState.launchPermissionRequest() // Android 12+ requests both FINE and COARSE
            } else {
                fineLocationPermissionState.launchPermissionRequest() // Pre-Android 12 requests only FINE
            }
        }
    }

    // Camera position for the map, with initial position at the starting point or current location
    val cameraPositionState = rememberCameraPositionState {
        Log.d(TAG, "LocationPicker: init camera postion at : " +
                "initialLat: $initialLat, initialLng: $initialLng")
        position = if (initialLat != null && initialLng != null) {
            CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 15f) // Zoom in on starting point
        } else {
            // Default to a placeholder position (e.g., Milan) if permissions aren't granted yet
            CameraPosition.fromLatLngZoom(LatLng(45.46427, 9.18951), 10f)
        }
    }

    // Move camera to user's current location if no starting coordinates are provided
    if (initialLat == null && initialLng == null && isLocationPermissionGranted.value) {
        LaunchedEffect(Unit) {
            moveToCurrentLocationIfPermitted(context, cameraPositionState) { currentLatLng ->
                selectedLocation = currentLatLng // Set initial pin at current location
                updateLocationName(context, currentLatLng, onLocationChange)
            }
        }
    } else if (initialLat != null && initialLng != null) { // Set initial pin at provided starting coordinates
        Log.d(TAG, "LocationPicker: initialLat: $initialLat, initialLng: $initialLng")
        selectedLocation = LatLng(initialLat, initialLng)
        /*locationName = geocoder.getFromLocation(initialLat, initialLng, 1)
            ?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location" */
        val addresses = geocoder.getFromLocation(initialLat, initialLng, 1)
        locationName = if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].getAddressLine(0) ?: "Unknown Location" // Safe access to address line
        } else {
            Log.e(TAG, "LocationPicker: no address found at provided coordinates")
            "Unknown Location" // Handle empty list
        }
    }

    if (isLocationPermissionGranted.value) {
        AlertDialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = true
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp), // Full-width with padding
            containerColor = MaterialTheme.colorScheme.primary,
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(onClick = {
                    selectedLocation?.let {
                        onLocationSelected(locationName, it)
                    }
                    onLocationChange(locationName)
                    onDismiss()
                }) {
                    Text(
                        text = stringResource(R.string.generic_ok).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() }) {
                    Text(
                        text = stringResource(id = R.string.generic_cancel),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        "$locationName",
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMapView(
                            initialLocation = selectedLocation, // Pass initial location for marker
                            cameraPositionState = cameraPositionState,
                            onMapClick = { latLng ->
                                selectedLocation = latLng
                                val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                                locationName = address?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
                                onLocationChange(locationName)
                            },
                            onMapLoaded = { mapLoaded = true }
                        )

                        // Button to go to current location
                        IconButton(
                            onClick = {
                                moveToCurrentLocationIfPermitted(context, cameraPositionState) { currentLatLng ->
                                    selectedLocation = currentLatLng // Set pin at current location
                                    updateLocationName(context, currentLatLng, onLocationChange)
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
        )
    } else {
        // Show a message if location permission is denied
        Log.e(TAG, "LocationPicker: Permission denied")
        SnackbarAlert(
            message = snackbarMsg,
            showSb = true,
            backgroundColorIn = MaterialTheme.colorScheme.inversePrimary,
            messageColorIn = MaterialTheme.colorScheme.onPrimary,
            openSnackbar = { showSnackbar.value = it },
            snackbarMsg = {"Permission denied" }
        )
    }
}


// Function to check location permission and move the camera to the current position
@SuppressLint("MissingPermission") // Use with caution, ensured permission checks are in place
private fun moveToCurrentLocationIfPermitted(
    context: Context,
    cameraPositionState: CameraPositionState,
    onLocationUpdated: (LatLng) -> Unit // Callback for updating location
) {
    // Check if permission is granted for FINE location or COARSE location
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    ) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        // Attempt to get the last known location and update the camera position
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                )
                onLocationUpdated(currentLatLng) // Set marker at current location
            }
        }.addOnFailureListener { e ->
            Log.e("LocationPicker", "Error fetching location: ${e.message}", e)
        }
    } else {
        Log.e("LocationPicker", "Location permission not granted")
    }
}


// Utility to update location name based on coordinates
private fun updateLocationName(
    context: Context,
    latLng: LatLng,
    onLocationChange: (String) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    val locationName = address?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
    onLocationChange(locationName)
}



@Preview(showBackground = true)
@Composable
fun PreviewMultiPurposeTextFieldWithLocation() {
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
            LocationPickerPermissionReq(
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
                }
            )
        }
    }
}
