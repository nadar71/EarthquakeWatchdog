package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import android.net.http.SslCertificate.restoreState
import android.net.http.SslCertificate.saveState
import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppBottomBar(
    navController: NavHostController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BottomBarDestinations.forEach { dest ->
            val selected = currentDestination.isRouteInHierarchy(dest.route)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected){
                        var route = dest.route
                        val toBeReplaced = "/{longitude}/{latitude}"
                        if (toBeReplaced in route) route = route.replace("/{longitude}/{latitude}".trim(),"")
                        Log.d("AppBottomBar: ", "Navigate to ${route}")
                        navController.navigate(route) {
                            Log.d("AppBottomBar: ", "Navigate to ${route}")
                            // Pop up to the start destination to avoid building up a large back stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true // avoid multiple copies of the same destination
                            restoreState = true // Restore state when re-selecting a previously selected item
                        }
                    }
                },
                icon = { dest.icon?.let { Icon(it, dest.label) } },
                label = { Text(dest.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
