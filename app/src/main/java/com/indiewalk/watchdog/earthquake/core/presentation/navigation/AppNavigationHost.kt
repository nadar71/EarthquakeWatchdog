package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.feat_details.presentation.ui.DetailsScreen
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui.EarthquakeListScreen
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui.MapScreen
import com.indiewalk.watchdog.earthquake.feat_intro.presentation.IntroScreen_01
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.CreditsScreen
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.SettingsScreen

@Composable
fun AppNavigationHost(
    navigator: AppNavigator<AppDestination>
) {
    val currentDestination = navigator.currentDestination

    BackHandler(
        enabled = navigator.canNavigateBack &&
            !currentDestination.isTopLevel &&
            currentDestination != AppDestination.Intro
    ) {
        navigator.navigateBack()
    }

    NavDisplay(
        backStack = navigator.backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        onBack = navigator::navigateBack,
        entryProvider = entryProvider {
            entry<AppDestination.Intro> {
                IntroScreen_01(
                    onContinueToHome = { navigator.replaceWith(AppDestination.Home) }
                )
            }
            entry<AppDestination.Home> {
                EarthquakeListScreen(
                    currentDestination = AppDestination.Home,
                    onTopLevelDestinationSelected = navigator::switchTopLevel,
                    onOpenDetails = { navigator.navigateTo(AppDestination.Details(it)) },
                    onOpenMap = { latitude, longitude ->
                        navigator.switchTopLevel(AppDestination.Map(latitude, longitude))
                    }
                )
            }
            entry<AppDestination.Map> { destination ->
                MapScreen(
                    currentDestination = destination,
                    onTopLevelDestinationSelected = navigator::switchTopLevel,
                    initialLatLng = destination.toLatLng()
                )
            }
            entry<AppDestination.Settings> {
                SettingsScreen(
                    currentDestination = AppDestination.Settings,
                    onTopLevelDestinationSelected = navigator::switchTopLevel,
                    onOpenCredits = { navigator.navigateTo(AppDestination.Credits) }
                )
            }
            entry<AppDestination.Credits> {
                CreditsScreen(
                    currentDestination = AppDestination.Credits,
                    onBack = navigator::navigateBack
                )
            }
            entry<AppDestination.Details> { destination ->
                DetailsScreen(
                    currentDestination = destination,
                    id = destination.id,
                    onBack = navigator::navigateBack
                )
            }
        }
    )
}

private fun AppDestination.Map.toLatLng(): LatLng? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return LatLng(lat, lon)
}
