package com.indiewalk.watchdog.earthquake.core.presentation.navigation

sealed interface AppDestination {
    data object Intro : AppDestination
    data object Home : AppDestination
    data class Map(
        val latitude: Double? = null,
        val longitude: Double? = null
    ) : AppDestination

    data object Statistics : AppDestination
    data object Settings : AppDestination
    data object Credits : AppDestination
    data class Details(val id: String) : AppDestination
}

val AppDestination.isTopLevel: Boolean
    get() = when (this) {
        AppDestination.Home,
        is AppDestination.Map,
        AppDestination.Statistics,
        AppDestination.Settings -> true
        AppDestination.Intro,
        AppDestination.Credits,
        is AppDestination.Details -> false
    }

fun AppDestination.matches(other: AppDestination): Boolean =
    when {
        this is AppDestination.Map && other is AppDestination.Map -> true
        else -> this::class == other::class
    }

val appTopLevelDestinationClasses = setOf(
    AppDestination.Home::class,
    AppDestination.Map::class,
    AppDestination.Statistics::class,
    AppDestination.Settings::class
)
