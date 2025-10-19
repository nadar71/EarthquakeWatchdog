package com.indiewalk.watchdog.earthquake.feat_intro.presentation

import android.Manifest
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationRoutes
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui.getLastKnownLatLng
import kotlinx.coroutines.withContext




@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IntroScreen(
    navController: NavHostController,
    vm: IntroViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val askedOnce by vm.askedOnce.collectAsStateWithLifecycle()

    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Derived
    val allGranted by remember(permissions) { derivedStateOf { permissions.allPermissionsGranted } }
    val anyShouldShowRationale by remember(permissions) {
        derivedStateOf { permissions.permissions.any { it.status.shouldShowRationale } }
    }
    val anyPermanentlyDenied by remember(permissions, askedOnce) {
        derivedStateOf {
            askedOnce && permissions.permissions.any {
                (!it.status.isGranted && !it.status.shouldShowRationale)
            }
        }
    }
    val manualOn = settings.manualLocOn
    // track when we actually launched the system request permissions dialog
    // and the result is returned ( awaitingResult turn false again)
    var awaitingResult by rememberSaveable { mutableStateOf(false) }
    var preRequestGrantMap by remember { mutableStateOf<Map<String, Boolean>?>(null) }

    // ---- BYPASS: skip Intro if conditions met ----
    LaunchedEffect(allGranted, anyPermanentlyDenied, manualOn) {
        if (allGranted || anyPermanentlyDenied || manualOn) {
            if (allGranted) {
                // Best effort: persist user location
                tryFetchAndPersistUserLocation(context, vm)
            } else {
                // Keep current settings (manual or defaults already in AppSettings)
            }
            navController.navigate(NavigationRoutes.Home.route) {
                popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // UI state
    var showRationale by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }

    // After request completes, decide and go Home (only when we actually requested)
    /*LaunchedEffect(allGranted, anyShouldShowRationale) {
        if (!showRationale && askedOnce) {
            showDeniedDialog = !allGranted && anyShouldShowRationale && !anyPermanentlyDenied
            if (allGranted || anyPermanentlyDenied || (!allGranted && !anyShouldShowRationale)) {
                if (allGranted) {
                    tryFetchAndPersistUserLocation(context, vm)
                }
                navController.navigate(NavigationRoutes.Home.route) {
                    popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }*/

    // ✅ Handle navigation ONLY after the system dialog returns
    /*LaunchedEffect(awaitingResult, allGranted, anyShouldShowRationale, anyPermanentlyDenied) {
        if (!awaitingResult) return@LaunchedEffect

        when {
            allGranted -> {
                tryFetchAndPersistUserLocation(context, vm)
                awaitingResult = false
                navController.navigate(NavigationRoutes.Home.route) {
                    popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            anyPermanentlyDenied -> {
                awaitingResult = false
                navController.navigate(NavigationRoutes.Home.route) {
                    popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            // Denied w/out "don't ask again" -> show a small info and let user proceed
            !anyShouldShowRationale -> {
                // Some OEMs report false here even on first request; treat as “no rationale”:
                awaitingResult = false
                navController.navigate(NavigationRoutes.Home.route) {
                    popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> {
                // Rationale available: user denied (no "don't ask again")
                showDeniedDialog = true
                awaitingResult = false
            }
        }
    }*/

    // ✅ Only proceed AFTER we detect the grant map has actually changed (dialog returned)
    LaunchedEffect(awaitingResult, permissions.permissions) {
        if (!awaitingResult) return@LaunchedEffect

        val currentMap = permissions.permissions.associate { it.permission to it.status.isGranted }
        val preMap = preRequestGrantMap

        // Still waiting for user choice → do nothing
        if (preMap == null || currentMap == preMap) return@LaunchedEffect

        // Dialog resolved → decide
        when {
            allGranted -> {
                tryFetchAndPersistUserLocation(context, vm)
                awaitingResult = false
                navController.navigate(NavigationRoutes.Home.route) {
                    popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            anyPermanentlyDenied -> {
                awaitingResult = false
                navController.navigate(NavigationRoutes.Home.route) {
                    popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            anyShouldShowRationale -> {
                // Denied (no "Don't ask again")
                showDeniedDialog = true
                awaitingResult = false
            }
            else -> {
                // Some OEMs keep shouldShowRationale=false even on deny; proceed anyway
                awaitingResult = false
                navController.navigate(NavigationRoutes.Home.route) {
                    popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // ---------------------------------------- UI -------------------------------------------------
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
        if (!allGranted && !anyPermanentlyDenied && !manualOn){
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showRationale = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continue") }
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

    // Rationale dialog
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
    }
}

private fun tryFetchAndPersistUserLocation(
    context: Context,
    vm: IntroViewModel
) {
    val fused = LocationServices.getFusedLocationProviderClient(context)
    fused.lastLocation
        .addOnSuccessListener { loc ->
            if (loc != null) vm.setUserLocation(LatLng(loc.latitude, loc.longitude))
        }
        .addOnFailureListener { /* ignore; keep existing settings */ }
}


/*@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IntroScreen(
    navController: NavHostController,
    viewModel: IntroViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Permissions state
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Dialog + guards (mirror EarthquakeMapScreen)
    var showRationale by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }
    var hasAskedOnce by rememberSaveable { mutableStateOf(false) }

    val allGranted by remember(permissions) { derivedStateOf { permissions.allPermissionsGranted } }
    val anyShouldShowRationale by remember(permissions) {
        derivedStateOf { permissions.permissions.any { it.status.shouldShowRationale } }
    }
    val anyPermanentlyDenied by remember(permissions, hasAskedOnce) {
        derivedStateOf {
            hasAskedOnce && permissions.permissions.any {
                (!it.status.isGranted && !it.status.shouldShowRationale)
            }
        }
    }

    // When request completed, decide and go Home
    LaunchedEffect(allGranted, hasAskedOnce, anyShouldShowRationale, anyPermanentlyDenied) {
        if (!hasAskedOnce) return@LaunchedEffect

        // show denied dialog only if rationale available and not permanently denied
        showDeniedDialog = !allGranted && anyShouldShowRationale && !anyPermanentlyDenied

        // If granted OR permanently denied (or denied without rationale), finalize and navigate
        if (allGranted || anyPermanentlyDenied || (!allGranted && !anyShouldShowRationale)) {
            // Persist user coords if granted, else keep defaults
            if (allGranted) {
                // Best effort: fetch last known; if null, still navigate
                tryFetchAndPersistUserLocation(context, viewModel)
            } else {
                viewModel.keepDefaultLocation()
            }
            // Navigate to Home, clear Intro from back stack
            navController.navigate(NavigationRoutes.Home.route) {
                popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // UI
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_checkmark), // provide a drawable
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .padding(top = 24.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "We use your location to center the map near you. " +
                        "If you don’t allow it, we’ll use a default location (Mountain View) " +
                        "and you can set a manual position on the map anytime.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    // Show your rationale, then request
                    showRationale = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }

    // Rationale before system prompt
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Location permission") },
            text = { Text("Allowing location centers the map near you. You can also skip and set a manual position later.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    hasAskedOnce = true
                    permissions.launchMultiplePermissionRequest()
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    hasAskedOnce = true // we “asked once” even if user skipped this prompt
                    // No prompt → proceed with defaults
                    viewModel.keepDefaultLocation()
                    navController.navigate(NavigationRoutes.Home.route) {
                        popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) { Text("Skip") }
            }
        )
    }

    // Optional: a simple “denied” explanation before proceeding (not shown if permanently denied)
    if (showDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showDeniedDialog = false },
            title = { Text("Permission denied") },
            text = {
                Text("You can keep using the app with a default location or set a manual position later in the Map.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeniedDialog = false
                    // keep defaults; navigation handled in LaunchedEffect already
                }) { Text("OK") }
            }
        )
    }
}

private fun tryFetchAndPersistUserLocation(
    context: Context,
    vm: IntroViewModel
) {
    // fire-and-forget: best effort (no need to block UI)
    val fused = LocationServices.getFusedLocationProviderClient(context)
    fused.lastLocation
        .addOnSuccessListener { loc ->
            if (loc != null) {
                vm.setUserLocation(LatLng(loc.latitude, loc.longitude))
            } else {
                vm.keepDefaultLocation()
            }
        }
        .addOnFailureListener {
            vm.keepDefaultLocation()
        }
}*/


/*OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IntroScreen(
    navController: NavHostController,
    viewModel: IntroViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    var showRationale by remember { mutableStateOf(false) }

    fun goToHomeClearIntro() {
        navController.navigate(NavigationRoutes.Home.route) {
            popUpTo(NavigationRoutes.Intro.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    @Composable
    fun onPermissionsResultNavigate() {
        // If granted, try to store user coords; else keep defaults
        val granted = permissions.allPermissionsGranted
        if (granted) {
            // Try to fetch last known; if null, still navigate
            onIO {
                val last = runCatching { getLastKnownLatLng(context) }.getOrNull()
                if (last != null) viewModel.setUserLocation(last) else viewModel.keepDefaultLocation()
                onMain { goToHomeClearIntro() }
            }
        } else {
            viewModel.keepDefaultLocation()
            goToHomeClearIntro()
        }
    }

    // UI
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image / Illustration
            Image(
                painter = painterResource(id = R.drawable.ic_xmark_white), // put any drawable
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .padding(top = 24.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "We use your location to center the map near you. " +
                        "If you prefer not to allow it, we’ll use a default location (Mountain View) " +
                        "and you can set a manual position on the map anytime.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { showRationale = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }

    // Rationale dialog shown when user hits Continue
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Location permission") },
            text = { Text("Allowing location centers the map near you. You can also skip and set a manual position later.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    // Launch request; when it completes, navigate appropriately
                    // We need a one-time effect to observe result — simplest is to call request then
                    // check on next recomposition:
                    permissions.launchMultiplePermissionRequest()
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    // Skip asking, go straight to Home with defaults
                    viewModel.keepDefaultLocation()
                    goToHomeClearIntro()
                }) {
                    Text("Skip")
                }
            }
        )
    }

    // Observe permission state change and navigate
    LaunchedEffect(permissions.allPermissionsGranted) {
        // Only react if the rationale has been closed (to avoid double triggering)
        if (!showRationale) {
            // If user just changed grant state (granted or denied), finish flow
            onPermissionsResultNavigate()
        }
    }
}

// little helpers to switch threads quickly
@Composable private fun onIO(block: @Composable
suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { withContext(kotlinx.coroutines.Dispatchers.IO) { block() } }
}
@Composable private fun onMain(block: () -> Unit) { block() }*/
