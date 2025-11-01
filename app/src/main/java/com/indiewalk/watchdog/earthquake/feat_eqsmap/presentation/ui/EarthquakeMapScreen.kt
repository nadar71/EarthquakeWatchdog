package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui


import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.util.extensions.toLocationInfo
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getAddress
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.getLastKnownLatLng
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.state.MapUiState
import com.indiewalk.watchdog.earthquake.core.presentation.preferences.PreferencesViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeMapScreen(
    navController: NavHostController,
    onManualPositionToggle: (Boolean) -> Unit = {},
    mapViewModel: MapViewModel = hiltViewModel(),
    preferencesViewModel: PreferencesViewModel = hiltViewModel(),
) {
    val TAG = "EarthquakeMapScreen"
    Log.d(TAG, "EarthquakeMapScreen Opened")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by mapViewModel.settings.collectAsStateWithLifecycle()


    Log.d(TAG, "settings manualLocOn : ${settings.manualLocOn}")
    // Check current location permission state
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val hasLocalPermissions by remember(permissions) { derivedStateOf { permissions.allPermissionsGranted } }

    // Options overlay/state
    var showOptions by rememberSaveable { mutableStateOf(false) }
    val isManualPositionOn by remember(settings.manualLocOn) {
        mutableStateOf(settings.manualLocOn)
    }
    var mapType by rememberSaveable { mutableStateOf(MapType.TERRAIN) } // default Terrain

    // one-shot camera target
    // TODO: use this to center map in a particular eq location from home list
    var recenterTo by remember { mutableStateOf<LatLng?>(null) }
    Log.d("EarthquakeMapScreen", "isManualPositionOn: $isManualPositionOn")

    // ---------------------------------------- LOGIC ----------------------------------------------
    // get eqs list updated from db
    val eqsUIFromDBState by mapViewModel.eqsUIFromDBState.collectAsStateWithLifecycle()

    // Update user position and address in prefs in case location permissions changed meanwhile/later
    LaunchedEffect(Unit) {
        if (hasLocalPermissions) {
            val userLocation = getLastKnownLatLng(context = context)
            preferencesViewModel.setUserPosition(userLocation )
            if (userLocation != null) {
                val address = getAddress(context = context, latLng = userLocation)
                address?.let { preferencesViewModel.setUserLocationInfo(address.toLocationInfo(context)) }
            }
        }
    }



    // ------------------------------------------- UI ----------------------------------------------
    ScaffoldModel(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = "Map",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {},
                actions = {
                    IconButton(onClick = {
                        showOptions = !showOptions
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_equalizer),
                            contentDescription = "Map settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,       // background
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,  // title text
                )

            )
        },
    ) { padding ->
        when (val s = eqsUIFromDBState) {
            is MapUiState.Loading -> {
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is MapUiState.Error -> {
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load earthquakes")
                }
            }
            is MapUiState.Success -> {
                val eqs = s.data.orEmpty()
                Box(Modifier.fillMaxSize()) {
                    EarthquakeMapContent(
                        padding = padding,
                        eqs = eqs,
                        hasLocationPermissions = hasLocalPermissions,
                        mapType = mapType,
                        recenterTarget = recenterTo,
                        onRecenterHandled = { recenterTo = null },
                        settings = settings,
                    )

                    if (showOptions) {
                        MapOptionsOverlayCard(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp, 32.dp, 32.dp, 32.dp),
                            isManualPositionOn = isManualPositionOn,
                            hasLocationPermissions = hasLocalPermissions,
                            mapType = mapType,
                            settings = settings,
                            onManualPositionToggle = { checked ->
                                if (checked) {
                                    Log.d(TAG, "EarthquakeMapScreen: manual position toggle: $checked")
                                    // just open picker; updating storage on OK pressed -> onManualPositionConfirmed
                                } else { // uncheck
                                    Log.d(TAG, "EarthquakeMapScreen: UNCHECKED manual position toggle: $checked")
                                    // restore user position coordinates and address info and recenter
                                    scope.launch {
                                        val lastKnownUserPosition =
                                            if (hasLocalPermissions) getLastKnownLatLng(context) else null
                                        val fallback = lastKnownUserPosition ?: LatLng(DEFAULT_LAT, DEFAULT_LNG)
                                        val userAddress = getAddress(context, fallback)
                                        preferencesViewModel.setUserPosition(fallback)
                                        userAddress?.let { preferencesViewModel
                                            .setUserLocationInfo(userAddress.toLocationInfo(context)) }

                                        preferencesViewModel.setManualLocOn(false)
                                        onManualPositionToggle(false)
                                        recenterTo = fallback
                                    }
                                }
                            },
                            onManualPositionConfirmed = { latLng, address -> // only after OK in picker
                                Log.d(TAG, "EarthquakeMapScreen: manual position confirmed: $latLng")
                                Log.d(TAG, "EarthquakeMapScreen: manual address confirmed: $address")
                                // save manual position coordinates and address info and recenter
                                preferencesViewModel.setManualPosition(latLng)
                                preferencesViewModel.setManualLocationInfo(address.toLocationInfo(context))

                                preferencesViewModel.setManualLocOn(true)
                                onManualPositionToggle(true)
                                recenterTo = latLng
                                showOptions = false
                            },
                            onMapTypeChange = { mapType = it },
                            onDismiss = { showOptions = false },
                        )
                    }
                }
            }
        }
    }
}






