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
        /*composable(NavigationRoutes.Map.route) {
            Log.d("NavigationGraph: ", NavigationRoutes.Map.route)
            MapScreen(navController)
        }*/
        composable(
            // route = NavigationRoutes.MapWithParams.route,
            route = NavigationRoutes.Map.route,
            arguments = listOf(
                navArgument("longitude") {type = NavType.FloatType},
                navArgument("latitude") {type = NavType.FloatType}
            )
        ) { backStackEntry ->
            val longitude = backStackEntry.arguments?.getFloat("longitude")?.toDouble()
            val latitude = backStackEntry.arguments?.getFloat("latitude")?.toDouble()

            if (latitude != null && longitude != null){
                MapScreen(navController, initialLatLng = LatLng(latitude, longitude))
            } else {
                MapScreen(navController)
            }
        }
        composable(NavigationRoutes.Settings.route) {
            Log.d("NavigationGraph: ", NavigationRoutes.Settings.route)
            SettingsScreen(navController)
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
