package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.feat_details.presentation.ui.DetailsScreen
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui.EarthquakeListScreen
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui.EarthquakeMapScreen
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui.MapScreen
import com.indiewalk.watchdog.earthquake.feat_intro.presentation.IntroScreen_01
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.CreditsScreen
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.SettingsScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String = NavigationRoutes.Intro.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationRoutes.Intro.route) {
            Log.d("NavigationGraph: ", NavigationRoutes.Intro.route)
            IntroScreen_01(navController)
        }
        composable(NavigationRoutes.Home.route) {
            Log.d("NavigationGraph: ", NavigationRoutes.Home.route)
            EarthquakeListScreen(navController)
        }
        composable(NavigationScreenConstants.MAP) {
            MapScreen(navController)
        }
        composable(
            route = NavigationScreenConstants.MAP_ARGS, // "map/{longitude}/{latitude}"
            arguments = listOf(
                navArgument("longitude") { type = NavType.FloatType },
                navArgument("latitude")  { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val lon = backStackEntry.arguments?.getFloat("longitude")?.toDouble()
            val lat = backStackEntry.arguments?.getFloat("latitude")?.toDouble()
            if (lat != null && lon != null) {
                MapScreen(navController, initialLatLng = LatLng(lat, lon))
            } else {
                MapScreen(navController)
            }
        }
        composable(NavigationRoutes.Settings.route) {
            Log.d("NavigationGraph: ", NavigationRoutes.Settings.route)
            SettingsScreen(navController)
        }
        composable(NavigationRoutes.CreditsScreen.route) {
            Log.d("NavigationGraph: ", NavigationRoutes.CreditsScreen.route)
            CreditsScreen(navController)
        }
        composable(
            route = NavigationRoutes.Details.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            Log.d("NavigationGraph: ", "DetailsScreen")
            val id = backStack.arguments?.getString("id").orEmpty()
            DetailsScreen(navController, id)
        }
    }
}
