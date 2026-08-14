package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSection
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import com.indiewalk.watchdog.earthquake.feat_statistics.presentation.state.StatisticsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class StatisticsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dataStateShowsScopeCountsAndEventCards() {
        setContent(populatedState)

        composeRule.onNodeWithText("Global earthquakes · M2.5+").assertExists()
        composeRule.onNodeWithContentDescription("146 earthquakes today").assertExists()
        composeRule.onNodeWithContentDescription("892 earthquakes in the last 7 days").assertExists()
        composeRule.onNodeWithContentDescription("3,761 earthquakes in the last 30 days").assertExists()
        composeRule.onNodeWithContentDescription("41,208 earthquakes this year").assertExists()
        composeRule.onNodeWithText("Strongest today").assertExists()
        composeRule.onNodeWithText("Nearest today").assertExists()
    }

    @Test
    fun eventCardsForwardTheirEventIds() {
        val selected = mutableListOf<String>()
        setContent(populatedState, onEventClick = selected::add)

        composeRule.onNodeWithTag("statistics-strongest").performClick()
        composeRule.onNodeWithTag("statistics-nearest").performClick()

        assertEquals(listOf("strongest", "nearest"), selected)
    }

    @Test
    fun loadingWithoutCacheShowsProgress() {
        setContent(StatisticsUiState())

        composeRule.onNodeWithTag("statistics-loading").assertExists()
    }

    @Test
    fun unavailableNearestSectionExplainsMissingLocationData() {
        setContent(
            populatedState.copy(
                snapshot = populatedState.snapshot?.copy(nearestToday = null),
                unavailableSections = setOf(StatisticsSection.NEAREST)
            )
        )

        composeRule.onNodeWithText("Nearest earthquake is unavailable for the selected location.")
            .assertExists()
    }

    private fun setContent(
        state: StatisticsUiState,
        onEventClick: (String) -> Unit = {}
    ) {
        composeRule.setContent {
            EQWatchdogTheme {
                StatisticsContent(
                    uiState = state,
                    onRefresh = {},
                    onEventClick = onEventClick
                )
            }
        }
    }

    private val now = Instant.parse("2026-08-14T10:30:00Z")
    private val windows = StatisticsWindows(
        StatisticsWindow(StatisticsPeriod.TODAY, now.minusSeconds(1), now),
        StatisticsWindow(StatisticsPeriod.LAST_7_DAYS, now.minusSeconds(2), now),
        StatisticsWindow(StatisticsPeriod.LAST_30_DAYS, now.minusSeconds(3), now),
        StatisticsWindow(StatisticsPeriod.YEAR, now.minusSeconds(4), now)
    )
    private val populatedState = StatisticsUiState(
        isInitialLoading = false,
        snapshot = StatisticsSnapshot(
            counts = StatisticsCounts(146, 892, 3_761, 41_208),
            strongestToday = StatisticsEvent(
                "strongest", 6.1, "South of Fiji", now.toEpochMilli(), 28.0, -24.0, 178.0
            ),
            nearestToday = StatisticsEvent(
                "nearest", 3.2, "Northern Italy", now.toEpochMilli(), 10.0, 45.0, 9.0, 84.0
            ),
            threshold = 2.5,
            retrievedAt = now,
            windows = windows
        )
    )
}
