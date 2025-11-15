package com.indiewalk.watchdog.earthquake.feat_intro.presentation

import android.Manifest
import android.R.attr.top
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.util.extensions.toLocationInfo
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationRoutes
import com.indiewalk.watchdog.earthquake.core.presentation.preferences.AppPrefsViewModel
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getAddress
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getLastKnownLatLng
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.openAppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun IntroScreen_01(
    navController: NavHostController,
    appPrefsViewModel: AppPrefsViewModel = hiltViewModel()
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
    var showDeniedDialog by remember { mutableStateOf(false) }

    // ------------------------------------- LOGIC -------------------------------------------------
    val askedOnce by appPrefsViewModel.askedOnce.collectAsStateWithLifecycle()

    val fineLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Foreground location permission granted
                scope.launch {
                    userLocation = getLastKnownLatLng(context)
                    appPrefsViewModel.setUserPosition(userLocation)
                    if (userLocation != null) { // update in prefs user location and address
                        val address = getAddress(context, userLocation!!)
                        address?.let {
                            appPrefsViewModel.setUserLocationInfo(
                                address.toLocationInfo(
                                    context
                                )
                            )
                        }
                        Log.d(
                            TAG,
                            "User location: ${userLocation?.latitude}, ${userLocation?.longitude}"
                        )
                        Log.d(TAG, "User address: ${address?.getAddressLine(0)}")
                        Log.d(TAG, "User city: ${address?.locality}")
                        Log.d(TAG, "User country code: ${address?.countryCode}")
                    }
                    navigateToHome(navController)
                }
            } else {
                // Foreground location permission denied
                // asked one flag set: do not ask again next app opening
                // appPrefsViewModel.setAskedOnce()
                showDeniedDialog = true
                // showLocationPermissionDeniedDialog(context, navController)
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
                title = {
                    Text(
                        stringResource(R.string.intro_welcome),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    text = stringResource(R.string.intro_location_request_title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(32.dp))
                Image(
                    painter = painterResource(id = R.drawable.map_img_permissions_req),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(top = 24.dp)
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.intro_localization_permission_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(32.dp))

                // start request permissions
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isReqPermissionBtnPressed = true
                        fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        Log.d(TAG, "Request permissions shown")
                    },
                    shape = RoundedCornerShape(10.dp),
                ) { Text(
                    stringResource(R.string.intro_enable_localization)
                ) }

                Spacer(Modifier.height(16.dp))

                // ok/not now btn
                Button(
                    onClick = { navigateToHome(navController) },
                    shape = RoundedCornerShape(10.dp),
                ) {
                    if (!isReqPermissionBtnPressed)
                        Text(stringResource(R.string.intro_skip))
                    else
                        Text(stringResource(R.string.intro_allow))
                }

            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.intro_loading_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
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
    }*/

    // Optional “denied” note (not shown when permanently denied)
    if (showDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showDeniedDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.intro_permission_denied)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.intro_enable_manual_position)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeniedDialog = false
                        openAppSettings(context)
                        appPrefsViewModel.setAskedOnce()
                    }
                ) {
                    Text(stringResource(R.string.intro_open_app_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeniedDialog = false
                    appPrefsViewModel.setAskedOnce()
                    // navigateToHome(navController)
                }) {
                    Text(
                        stringResource(R.string.generic_ok)
                    )
                }
            }


        )
    }


}


fun navigateToHome(navController: NavHostController) {
    navController.navigate(NavigationRoutes.Home.route) {
        popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
        launchSingleTop = true
    }
}

// Show dialog for denied location permission
/*fun showLocationPermissionDeniedDialog(context: Context, navController: NavHostController) {
    AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.intro_permission_denied))
        .setMessage(context.getString(R.string.intro_enable_manual_position))
        .setPositiveButton(context.getString(R.string.generic_ok)) { _, _ ->
            navigateToHome(navController)
        }
        .setNegativeButton(context.getString(R.string.intro_open_app_settings)) { _, _ ->
            openAppSettings(context)
        }
        .show()
}*/


