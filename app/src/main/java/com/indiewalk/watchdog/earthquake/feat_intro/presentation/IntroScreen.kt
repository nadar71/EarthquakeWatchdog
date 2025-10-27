package com.indiewalk.watchdog.earthquake.feat_intro.presentation

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationRoutes
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getAddressFromLatLng
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.getLastKnownLatLng
import com.indiewalk.watchdog.earthquake.core.util.MapsUtils.openAppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun IntroScreen_01(
    navController: NavHostController,
    introViewModel: IntroViewModel = hiltViewModel()
) {
    val TAG = "IntroScreen"
    Log.d(TAG, "IntroScreen: shown.")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isReqPermissionBtnPressed by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf(null as LatLng?) }
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val allGranted by remember(permissions) { derivedStateOf { permissions.allPermissionsGranted } }

    // ------------------------------------- LOGIC -------------------------------------------------
    val askedOnce by introViewModel.askedOnce.collectAsStateWithLifecycle()

    val fineLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Foreground location permission granted
                scope.launch {
                    userLocation = getLastKnownLatLng(context)
                    introViewModel.setUserPosition(userLocation )
                    if (userLocation != null) {
                        val address = getAddressFromLatLng(context, userLocation!!)
                        introViewModel.setAddress(address?.getAddressLine(0) ?: "Unknown")
                        introViewModel.setCity(address?.locality ?: "Unknown")
                        introViewModel.setCountryCode(address?.countryCode ?: "Unknown")
                        Log.d(TAG, "User location: ${userLocation?.latitude}, ${userLocation?.longitude}")
                        Log.d(TAG, "User address: ${address?.getAddressLine(0)}")
                        Log.d(TAG, "User city: ${address?.locality}")
                        Log.d(TAG, "User country code: ${address?.countryCode}")
                    }
                    navigateToHome(navController)
                }
            } else {
                // Foreground location permission denied
                // asked one flag set: do not ask again next app opening
                introViewModel.setAskedOnce()
                showLocationPermissionDeniedDialog(context, navController)
            }
        }
    )

    // delay in transition to home screen
    LaunchedEffect(allGranted, askedOnce) {
        if (allGranted || askedOnce) {
            delay(1000)
            navigateToHome(navController)
        }
    }


    // --------------------------------------- UI --------------------------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (!allGranted && !askedOnce) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Localization request",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground

                )
                Spacer(Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_checkmark),
                    contentDescription = null,
                    modifier = Modifier
                        .size(180.dp)
                        .padding(top = 24.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "We use your location to center the map near you. " +
                            "If you don’t allow it, we’ll use a default location (Mountain View) " +
                            "or you can set a manual position later on the map.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                // start request permissions
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isReqPermissionBtnPressed = true
                        fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        Log.d(TAG, "Request permissions shown")
                    },
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Enable Localization") }

                Spacer(Modifier.height(16.dp))

                // ok/not now btn
                Button(
                    onClick = { navigateToHome(navController) },
                    shape = RoundedCornerShape(10.dp),
                ) {
                    if (!isReqPermissionBtnPressed)
                        Text("Skip")
                    else
                        Text("Ok")
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "You can enable after",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground

                )
            }
        } else {

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.primary),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Loading",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    /*// Rationale dialog
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Location permission") },
            text = { Text("Allowing location centers the map near you. You can also skip and set a manual position later.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    vm.setAskedOnce() // mark that we have asked at least once
                    // ✅ snapshot current grants and mark awaiting
                    preRequestGrantMap = permissions.permissions.associate { it.permission to it.status.isGranted }
                    awaitingResult = true // we’re now waiting for the system dialog result
                    permissions.launchMultiplePermissionRequest()
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    // vm.setAskedOnce() // we consider this a completed ask path
                    // Skip request -> go Home; keep defaults or manual
                    navController.navigate(NavigationRoutes.Home.route) {
                        popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) { Text("Skip") }
            }
        )
    }

    // Optional “denied” note (not shown when permanently denied)
    if (showDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showDeniedDialog = false },
            title = { Text("Permission denied") },
            text = { Text("You can keep using the app with a default location or set a manual position later in the Map.") },
            confirmButton = {
                TextButton(onClick = { showDeniedDialog = false }) { Text("OK") }
            }
        )
    }*/


}


// Function to set up location access after permission is granted
fun navigateToHome(navController: NavHostController) {
    // accessing the location or setup GPS tracking
    navController.navigate(NavigationRoutes.Home.route) {
        popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
        launchSingleTop = true
    }
}

// Show dialog for denied location permission
fun showLocationPermissionDeniedDialog(context: Context, navController: NavHostController) {
    AlertDialog.Builder(context)
        .setTitle("Location disables")
        .setMessage("You can enable after or manul position")
        .setPositiveButton("OK") { _, _ ->
            navigateToHome(navController)
        }
        .setNegativeButton("openSettings") { _, _ ->
            openAppSettings(context)
        }
        .show()
}


