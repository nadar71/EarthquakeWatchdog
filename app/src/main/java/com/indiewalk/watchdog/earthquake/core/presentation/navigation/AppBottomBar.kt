package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.R

private data class BottomBarItem(
    val destination: AppDestination,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun AppBottomBar(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit
) {
    val items = listOf(
        BottomBarItem(
            destination = AppDestination.Home,
            label = stringResource(R.string.nav_bottom_home_desc),
            icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.nav_bottom_home_desc)) }
        ),
        BottomBarItem(
            destination = AppDestination.Map(),
            label = stringResource(R.string.maps_title),
            icon = { Icon(Icons.Filled.Map, contentDescription = stringResource(R.string.nav_bottom_eqs_desc)) }
        ),
        BottomBarItem(
            destination = AppDestination.Settings,
            label = stringResource(R.string.settings_title),
            icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_bottom_settings_desc)) }
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentDestination.matches(item.destination)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onDestinationSelected(item.destination)
                    }
                },
                icon = item.icon,
                label = { Text(item.label) },
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
