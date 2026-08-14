package com.indiewalk.watchdog.earthquake.core.presentation.navigation

sealed interface TestDestination {
    data object Intro : TestDestination
    data object Home : TestDestination
    data object Map : TestDestination
    data object Settings : TestDestination
    data object Credits : TestDestination
    data class Details(val id: String) : TestDestination
}
