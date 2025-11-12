package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationScreenConstants.DETAILS
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationScreenConstants.HOME
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationScreenConstants.INTRO
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationScreenConstants.MAP
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationScreenConstants.SETTINGS


sealed class NavigationRoutes(
    val label: String,
    val route: String,
    val icon: ImageVector? = null,
    @StringRes val contentDescription: Int
) {
    data object Intro : NavigationRoutes("Intro", INTRO, null, R.string.nav_bottom_intro_desc)
    data object Home : NavigationRoutes("Home", HOME, Icons.Filled.Home, R.string.nav_bottom_home_desc)
    data object Map : NavigationRoutes("Map", MAP, Icons.Filled.Map, R.string.nav_bottom_eqs_desc)
    data object MapWithParams : NavigationRoutes("MapWithParams", "${MAP}/{longitude}/{latitude}", Icons.Filled.Map, R.string.nav_bottom_eqs_desc)
    data object Settings : NavigationRoutes("Settings", SETTINGS, Icons.Filled.Settings, R.string.nav_bottom_settings_desc)
    data object Details : NavigationRoutes("Details", "${DETAILS}/{id}", null, R.string.nav_bottom_eqs_desc)
}

val BottomBarDestinations = listOf(NavigationRoutes.Home, /*NavigationRoutes.Map,*/
    NavigationRoutes.MapWithParams, NavigationRoutes.Settings)
val TopLevelRoutes = BottomBarDestinations.map { it.route }.toSet()
