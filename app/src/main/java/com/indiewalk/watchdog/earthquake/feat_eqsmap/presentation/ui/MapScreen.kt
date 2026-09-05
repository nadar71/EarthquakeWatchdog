package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination


@Composable
fun MapScreen(
    currentDestination: AppDestination,
    onTopLevelDestinationSelected: (AppDestination) -> Unit,
    initialLatLng: LatLng? = null
) {
    EarthquakeMapScreen(
        currentDestination = currentDestination,
        onTopLevelDestinationSelected = onTopLevelDestinationSelected,
        initialLatLng = initialLatLng,
    )
}
