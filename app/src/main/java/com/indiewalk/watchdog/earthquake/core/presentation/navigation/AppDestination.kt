package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestination : NavKey {
    @Serializable
    data object Intro : AppDestination

    @Serializable
    data object Home : AppDestination

    @Serializable
    data class Map(
        val latitude: Double? = null,
        val longitude: Double? = null
    ) : AppDestination

    @Serializable
    data object Statistics : AppDestination

    @Serializable
    data object Settings : AppDestination

    @Serializable
    data object Credits : AppDestination

    @Serializable
    data object PrivacyPolicy : AppDestination

    @Serializable
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
        AppDestination.PrivacyPolicy,
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
