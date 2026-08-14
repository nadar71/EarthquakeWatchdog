package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.feat_details.presentation.ui.DetailsScreen
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui.EarthquakeListScreen
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui.MapScreen
import com.indiewalk.watchdog.earthquake.feat_intro.presentation.IntroScreen_01
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.CreditsScreen
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.SettingsScreen
import com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui.StatisticsScreen

interface AppNavigationScreenFactory {
    @Composable
    fun Intro(onContinueToHome: () -> Unit)

    @Composable
    fun Home(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit,
        onOpenMap: (Double, Double) -> Unit
    )

    @Composable
    fun Map(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        initialLatLng: LatLng?
    )

    @Composable
    fun Statistics(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit
    )

    @Composable
    fun Settings(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenCredits: () -> Unit
    )

    @Composable
    fun Credits(
        currentDestination: AppDestination,
        onBack: () -> Unit
    )

    @Composable
    fun Details(
        currentDestination: AppDestination,
        id: String,
        onBack: () -> Unit
    )
}

object DefaultAppNavigationScreenFactory : AppNavigationScreenFactory {
    @Composable
    override fun Intro(onContinueToHome: () -> Unit) {
        IntroScreen_01(onContinueToHome = onContinueToHome)
    }

    @Composable
    override fun Home(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit,
        onOpenMap: (Double, Double) -> Unit
    ) {
        EarthquakeListScreen(
            currentDestination = currentDestination,
            onTopLevelDestinationSelected = onTopLevelDestinationSelected,
            onOpenDetails = onOpenDetails,
            onOpenMap = onOpenMap
        )
    }

    @Composable
    override fun Map(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        initialLatLng: LatLng?
    ) {
        MapScreen(
            currentDestination = currentDestination,
            onTopLevelDestinationSelected = onTopLevelDestinationSelected,
            initialLatLng = initialLatLng
        )
    }

    @Composable
    override fun Statistics(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit
    ) {
        StatisticsScreen(
            currentDestination = currentDestination,
            onTopLevelDestinationSelected = onTopLevelDestinationSelected,
            onOpenDetails = onOpenDetails
        )
    }

    @Composable
    override fun Settings(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenCredits: () -> Unit
    ) {
        SettingsScreen(
            currentDestination = currentDestination,
            onTopLevelDestinationSelected = onTopLevelDestinationSelected,
            onOpenCredits = onOpenCredits
        )
    }

    @Composable
    override fun Credits(
        currentDestination: AppDestination,
        onBack: () -> Unit
    ) {
        CreditsScreen(
            currentDestination = currentDestination,
            onBack = onBack
        )
    }

    @Composable
    override fun Details(
        currentDestination: AppDestination,
        id: String,
        onBack: () -> Unit
    ) {
        DetailsScreen(
            currentDestination = currentDestination,
            id = id,
            onBack = onBack
        )
    }
}
