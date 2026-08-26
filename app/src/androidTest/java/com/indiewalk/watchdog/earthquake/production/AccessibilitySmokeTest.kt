package com.indiewalk.watchdog.earthquake.production

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppBottomBar
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences.FilterSheet
import com.indiewalk.watchdog.earthquake.feat_intro.presentation.IntroLocationPermissionContent
import org.junit.Rule
import org.junit.Test

class AccessibilitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomNavigationLabelsAreClickableAndMeetMinimumTouchTarget() {
        composeRule.setContent {
            EQWatchdogTheme {
                AppBottomBar(
                    currentDestination = AppDestination.Home,
                    onDestinationSelected = {}
                )
            }
        }

        val context = composeRule.activity
        listOf(
            context.getString(R.string.nav_bottom_home_desc),
            context.getString(R.string.maps_title_bottom_nav),
            context.getString(R.string.statistics_title),
            context.getString(R.string.settings_title)
        ).forEach { label ->
            composeRule.onNodeWithText(label)
                .assertHasClickAction()
                .assertHeightIsAtLeast(48.dp)
        }
    }

    @Test
    fun compactLargeFontIntroKeepsBothActionsVisibleAndTouchable() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                EQWatchdogTheme {
                    Box(modifier = Modifier.size(width = 320.dp, height = 520.dp)) {
                        IntroLocationPermissionContent(
                            isPermissionRequestStarted = false,
                            onRequestLocation = {},
                            onContinueToHome = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("intro-location-permission-content")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("intro-enable-location"))
        composeRule.onNodeWithTag("intro-enable-location")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("intro-location-permission-content")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("intro-continue"))
        composeRule.onNodeWithTag("intro-continue")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun filterSheetContentIsScrollableSoActionsRemainReachableOnSmallScreens() {
        composeRule.setContent {
            EQWatchdogTheme {
                FilterSheet(
                    eqsCount = 200,
                    lastRefreshTime = "Aug 26, 2026",
                    startDate = "Jul 27, 2026",
                    filterSettings = FilterSettings(),
                    onConfirm = { _, _, _ -> },
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithTag("filter-sheet-content").assert(hasScrollAction())
    }
}
