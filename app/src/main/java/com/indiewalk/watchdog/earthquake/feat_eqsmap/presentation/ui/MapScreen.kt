package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng


@Composable
fun MapScreen(navController: NavHostController, initialLatLng: LatLng? = null) {
    EarthquakeMapScreen(
        navController = navController,
        initialLatLng = initialLatLng,
    )
}
