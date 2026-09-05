package com.indiewalk.watchdog.earthquake.production

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppBottomBar
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EarthquakeListUiState
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui.EarthquakeListContent
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui.SelectedEarthquakeDetail
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.SettingsPreferencesContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProductionUiContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun earthquakeListRefreshAndFilterForwardProductionCallbacks() {
        var refreshes = 0
        var filterRequests = 0

        composeRule.setContent {
            EQWatchdogTheme {
                EarthquakeListContent(
                    uiState = EarthquakeListUiState(
                        isLoading = false,
                        filteredEarthquakes = listOf(sampleEarthquake())
                    ),
                    onRefresh = { refreshes++ },
                    onFilterRequested = { filterRequests++ },
                    onEarthquakeSelected = {}
                )
            }
        }

        composeRule.onNodeWithTag("earthquake-list-content").performTouchInput {
            down(Offset(width / 2f, 1f))
            moveTo(Offset(width / 2f, height * 0.8f), delayMillis = 500)
            up()
        }
        composeRule.onNodeWithTag("earthquake-list-filter").performClick()

        composeRule.runOnIdle {
            assertEquals(1, refreshes)
            assertEquals(1, filterRequests)
        }
    }

    @Test
    fun bottomBarDispatchesEveryTopLevelDestination() {
        val selected = mutableListOf<AppDestination>()
        var currentDestination by mutableStateOf<AppDestination>(AppDestination.Map())

        composeRule.setContent {
            EQWatchdogTheme {
                AppBottomBar(currentDestination = currentDestination) { destination ->
                    selected += destination
                    currentDestination = destination
                }
            }
        }

        composeRule.onNodeWithText(text(R.string.nav_bottom_home_desc)).performClick()
        composeRule.onNodeWithText(text(R.string.maps_title_bottom_nav)).performClick()
        composeRule.onNodeWithText(text(R.string.statistics_title)).performClick()
        composeRule.onNodeWithText(text(R.string.settings_title)).performClick()

        composeRule.runOnIdle {
            assertEquals(
                listOf(
                    AppDestination.Home,
                    AppDestination.Map(),
                    AppDestination.Statistics,
                    AppDestination.Settings
                ),
                selected
            )
        }
    }

    @Test
    fun selectedMapDetailRendersAndDismissesWithoutMapsTiles() {
        var selected: EQEntity? by mutableStateOf(sampleEarthquake())

        composeRule.setContent {
            EQWatchdogTheme {
                Box(Modifier.fillMaxSize()) {
                    SelectedEarthquakeDetail(
                        earthquake = selected,
                        unitSystem = UnitSystem.METRIC,
                        onDismiss = { selected = null }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("map-selected-earthquake-detail").assertExists()
        composeRule.onNodeWithContentDescription(text(R.string.generic_cancel)).performClick()
        composeRule.onAllNodesWithTag("map-selected-earthquake-detail").assertCountEquals(0)
    }

    @Test
    fun settingsSelectionsForwardCallbacksAndRenderPersistedState() {
        val unitUpdates = mutableListOf<UnitSystem>()
        val themeUpdates = mutableListOf<ThemeMode>()
        var settings by mutableStateOf(AppSettings())

        composeRule.setContent {
            EQWatchdogTheme {
                SettingsPreferencesContent(
                    settings = settings,
                    onUnitSystemSelected = {
                        unitUpdates += it
                        settings = settings.copy(unitSystem = it)
                    },
                    onThemeModeSelected = {
                        themeUpdates += it
                        settings = settings.copy(mode = it)
                    }
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.settings_unit_system_imperial)).performClick()
        composeRule.onNodeWithText(text(R.string.theme_mode_dark)).performClick()

        composeRule.onNodeWithTag("settings-unit-IMPERIAL").assertIsSelected()
        composeRule.onNodeWithTag("settings-theme-Dark").assertIsSelected()
        composeRule.runOnIdle {
            assertEquals(listOf(UnitSystem.IMPERIAL), unitUpdates)
            assertEquals(listOf(ThemeMode.Dark), themeUpdates)
        }
    }

    private fun text(@androidx.annotation.StringRes id: Int): String =
        composeRule.activity.getString(id)

    private fun sampleEarthquake() = EQEntity(
        id = "eq-map",
        feedGenerated = 1L,
        mag = 4.5,
        place = "Sample place",
        time = 1_723_621_600_000L,
        updated = null,
        tz = null,
        url = null,
        detail = null,
        felt = null,
        cdi = null,
        mmi = null,
        alert = null,
        status = null,
        tsunami = null,
        sig = null,
        net = null,
        code = null,
        ids = null,
        sources = null,
        types = null,
        nst = null,
        dmin = null,
        rms = null,
        gap = null,
        magType = "ml",
        eventType = "earthquake",
        longitude = 9.0,
        latitude = 45.0,
        depthKm = 10.0,
        distanceFromUser = 10
    )
}
