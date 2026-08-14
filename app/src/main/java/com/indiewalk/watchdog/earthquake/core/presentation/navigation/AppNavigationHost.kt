package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.feat_details.presentation.ui.DetailsScreen
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui.EarthquakeListScreen
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui.MapScreen
import com.indiewalk.watchdog.earthquake.feat_intro.presentation.IntroScreen_01
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.CreditsScreen
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.SettingsScreen

@Composable
fun AppNavigationHost(
    navigator: AppNavigator<AppDestination>,
    screenFactory: AppNavigationScreenFactory = DefaultAppNavigationScreenFactory
) {
    val currentDestination = navigator.currentDestination
    val navigationEventDispatcherOwner =
        rememberNavigationEventDispatcherOwner(parent = null)

    BackHandler(
        enabled = navigator.canNavigateBack &&
            !currentDestination.isTopLevel &&
            currentDestination != AppDestination.Intro
    ) {
        navigator.navigateBack()
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navigationEventDispatcherOwner
    ) {
        NavDisplay(
            backStack = navigator.backStack,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            onBack = navigator::navigateBack,
            entryProvider = entryProvider {
                entry<AppDestination.Intro> {
                    screenFactory.Intro(
                        onContinueToHome = { navigator.replaceWith(AppDestination.Home) }
                    )
                }
                entry<AppDestination.Home> {
                    screenFactory.Home(
                        currentDestination = AppDestination.Home,
                        onTopLevelDestinationSelected = navigator::switchTopLevel,
                        onOpenDetails = { navigator.navigateTo(AppDestination.Details(it)) },
                        onOpenMap = { latitude, longitude ->
                            navigator.switchTopLevel(AppDestination.Map(latitude, longitude))
                        }
                    )
                }
                entry<AppDestination.Map> { destination ->
                    screenFactory.Map(
                        currentDestination = destination,
                        onTopLevelDestinationSelected = navigator::switchTopLevel,
                        initialLatLng = destination.toLatLng()
                    )
                }
                entry<AppDestination.Settings> {
                    screenFactory.Settings(
                        currentDestination = AppDestination.Settings,
                        onTopLevelDestinationSelected = navigator::switchTopLevel,
                        onOpenCredits = { navigator.navigateTo(AppDestination.Credits) }
                    )
                }
                entry<AppDestination.Credits> {
                    screenFactory.Credits(
                        currentDestination = AppDestination.Credits,
                        onBack = navigator::navigateBack
                    )
                }
                entry<AppDestination.Details> { destination ->
                    screenFactory.Details(
                        currentDestination = destination,
                        id = destination.id,
                        onBack = navigator::navigateBack
                    )
                }
            }
        )
    }
}

private fun AppDestination.Map.toLatLng(): LatLng? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return LatLng(lat, lon)
}
