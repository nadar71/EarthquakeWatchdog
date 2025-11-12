package com.indiewalk.watchdog.earthquake.core.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppBottomBar
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.TopLevelRoutes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.TopLevelBaseRoutes
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.baseRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldModel(
    navController: NavHostController,
    topBar: @Composable (() -> Unit)? = {},
    title: String = "",
    navigationIcon: @Composable (() -> Unit)? = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    content: @Composable (PaddingValues) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    // val currentRoute = backStackEntry?.destination?.route
    // val showBottomBar = currentRoute in TopLevelRoutes

    val currentDestination = backStackEntry?.destination

    val showBottomBar = currentDestination
        ?.hierarchy
        ?.any { dest -> baseRoute(dest.route) in TopLevelBaseRoutes } == true

    val canNavigateBack = !showBottomBar // simple heuristic: not top-level -> show back

    Scaffold(
        topBar = topBar ?: {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = navigationIcon ?: {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = actions,
                colors  = colors
            )
        },
        bottomBar = { if (showBottomBar) AppBottomBar(navController) }
    ) { padding -> content(padding) }
}
