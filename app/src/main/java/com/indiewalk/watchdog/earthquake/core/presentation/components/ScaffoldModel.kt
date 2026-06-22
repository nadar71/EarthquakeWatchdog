package com.indiewalk.watchdog.earthquake.core.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppBottomBar
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.isTopLevel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppBottomBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldModel(
    currentDestination: AppDestination,
    onBack: (() -> Unit)? = null,
    onTopLevelDestinationSelected: ((AppDestination) -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = {},
    title: String = "",
    navigationIcon: @Composable (() -> Unit)? = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    content: @Composable (PaddingValues) -> Unit
) {
    val showBottomBar = currentDestination.isTopLevel
    val showBackButton = !showBottomBar &&
        currentDestination != AppDestination.Intro &&
        onBack != null

    Scaffold(
        topBar = topBar ?: {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = navigationIcon ?: {
                    if (showBackButton) {
                        IconButton(onClick = { onBack?.invoke() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = actions,
                colors  = colors
            )
        },
        bottomBar = {
            if (showBottomBar && onTopLevelDestinationSelected != null) {
                AppBottomBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = onTopLevelDestinationSelected
                )
            }
        }
    ) { padding -> content(padding) }
}
