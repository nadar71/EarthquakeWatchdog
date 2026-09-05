package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {

    @Test
    fun starts_with_intro_destination() {
        val navigator = AppNavigator(
            initialBackStack = mutableListOf(TestDestination.Intro),
            topLevelDestinations = setOf(
                TestDestination.Home::class,
                TestDestination.Map::class,
                TestDestination.Settings::class
            )
        )

        assertEquals(listOf(TestDestination.Intro), navigator.backStack)
        assertEquals(TestDestination.Intro, navigator.currentDestination)
        assertFalse(navigator.canNavigateBack)
    }

    @Test
    fun replace_with_home_removes_intro_from_back_stack() {
        val navigator = AppNavigator(
            initialBackStack = mutableListOf(TestDestination.Intro),
            topLevelDestinations = setOf(
                TestDestination.Home::class,
                TestDestination.Map::class,
                TestDestination.Settings::class
            )
        )

        navigator.replaceWith(TestDestination.Home)

        assertEquals(listOf(TestDestination.Home), navigator.backStack)
        assertEquals(TestDestination.Home, navigator.currentDestination)
        assertFalse(navigator.canNavigateBack)
    }

    @Test
    fun switching_top_level_destinations_does_not_duplicate_selected_tab() {
        val navigator = AppNavigator(
            initialBackStack = mutableListOf(TestDestination.Home),
            topLevelDestinations = setOf(
                TestDestination.Home::class,
                TestDestination.Map::class,
                TestDestination.Settings::class
            )
        )

        navigator.switchTopLevel(TestDestination.Map)
        navigator.switchTopLevel(TestDestination.Map)

        assertEquals(listOf(TestDestination.Home, TestDestination.Map), navigator.backStack)
        assertEquals(TestDestination.Map, navigator.currentDestination)
    }

    @Test
    fun navigate_back_pops_leaf_destination_only() {
        val navigator = AppNavigator(
            initialBackStack = mutableListOf(TestDestination.Home),
            topLevelDestinations = setOf(
                TestDestination.Home::class,
                TestDestination.Map::class,
                TestDestination.Settings::class
            )
        )

        navigator.navigateTo(TestDestination.Details("eq-1"))
        navigator.navigateBack()

        assertEquals(listOf(TestDestination.Home), navigator.backStack)
        assertEquals(TestDestination.Home, navigator.currentDestination)
        assertFalse(navigator.canNavigateBack)
    }

    @Test
    fun switching_top_level_destination_pops_detail_stack_before_switching() {
        val navigator = AppNavigator(
            initialBackStack = mutableListOf(TestDestination.Home),
            topLevelDestinations = setOf(
                TestDestination.Home::class,
                TestDestination.Map::class,
                TestDestination.Settings::class
            )
        )

        navigator.navigateTo(TestDestination.Details("eq-42"))
        navigator.switchTopLevel(TestDestination.Settings)

        assertEquals(
            listOf(TestDestination.Home, TestDestination.Settings),
            navigator.backStack
        )
        assertEquals(TestDestination.Settings, navigator.currentDestination)
        assertTrue(navigator.canNavigateBack)
    }

    @Test
    fun typed_map_destination_keeps_optional_coordinates() {
        val navigator = AppNavigator(
            initialBackStack = mutableListOf<AppDestination>(AppDestination.Home),
            topLevelDestinations = setOf(
                AppDestination.Home::class,
                AppDestination.Map::class,
                AppDestination.Settings::class
            )
        )

        navigator.navigateTo(AppDestination.Map(latitude = 10.0, longitude = 20.0))

        assertEquals(
            AppDestination.Map(latitude = 10.0, longitude = 20.0),
            navigator.currentDestination
        )
    }

    @Test
    fun statistics_is_a_top_level_destination_without_duplicates() {
        val navigator = AppNavigator(
            initialBackStack = mutableListOf<AppDestination>(AppDestination.Home),
            topLevelDestinations = appTopLevelDestinationClasses
        )

        navigator.switchTopLevel(AppDestination.Statistics)
        navigator.switchTopLevel(AppDestination.Statistics)

        assertEquals(
            listOf(AppDestination.Home, AppDestination.Statistics),
            navigator.backStack
        )
        assertEquals(AppDestination.Statistics, navigator.currentDestination)
    }

    @Test
    fun top_level_destinations_are_explicit() {
        assertTrue(AppDestination.Home.isTopLevel)
        assertTrue(AppDestination.Map().isTopLevel)
        assertTrue(AppDestination.Statistics.isTopLevel)
        assertTrue(AppDestination.Settings.isTopLevel)
        assertFalse(AppDestination.Intro.isTopLevel)
        assertFalse(AppDestination.Credits.isTopLevel)
        assertFalse(AppDestination.PrivacyPolicy.isTopLevel)
        assertFalse(AppDestination.Details("eq-1").isTopLevel)
    }
}
