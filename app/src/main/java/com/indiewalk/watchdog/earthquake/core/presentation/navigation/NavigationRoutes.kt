package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.indiewalk.watchdog.earthquake.R


sealed class NavigationRoutes(
    val label: String,
    val route: String,
    val icon: ImageVector? = null,
    @StringRes val contentDescription: Int
) {

    data object Intro : NavigationRoutes(
        "Intro",
        NavigationScreenConstants.INTRO,
        null,
        R.string.nav_bottom_intro_desc
    )

    data object Home : NavigationRoutes(
        "Home",
        NavigationScreenConstants.HOME,
        Icons.Filled.Home,
        R.string.nav_bottom_home_desc
    )
    data object Map : NavigationRoutes(
        "Map",
        NavigationScreenConstants.MAP,
        Icons.Filled.Map,
        R.string.nav_bottom_eqs_desc
    )
    data object Settings : NavigationRoutes(
        "Settings",
        NavigationScreenConstants.SETTINGS,
        Icons.Filled.Settings,
        R.string.nav_bottom_settings_desc
    )
    data object Details : NavigationRoutes(
        "Details",
        "${NavigationScreenConstants.DETAILS}/{id}",
        null,
        R.string.nav_bottom_eqs_desc
    )

}

val BottomBarDestinations = listOf(NavigationRoutes.Home, NavigationRoutes.Map, NavigationRoutes.Settings)
val TopLevelRoutes = BottomBarDestinations.map { it.route }.toSet()
