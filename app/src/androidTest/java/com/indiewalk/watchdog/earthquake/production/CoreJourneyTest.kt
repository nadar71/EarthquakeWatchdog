package com.indiewalk.watchdog.earthquake.production

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationHost
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationScreenFactory
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigator
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.appTopLevelDestinationClasses
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreJourneyTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun deterministicCoreJourneyCoversAllDestinationsAndTransientDetailFlows() {
        setHostContent()

        composeRule.onNodeWithTag("journey-intro-continue").performClick()
        composeRule.onNodeWithText("home").assertExists()

        composeRule.onNodeWithTag("journey-home-refresh").performClick()
        composeRule.onNodeWithText("home-refreshes:1").assertExists()
        composeRule.onNodeWithTag("journey-home-filter").performClick()
        composeRule.onNodeWithText("home-filter:open").assertExists()

        composeRule.onNodeWithTag("journey-home-map").performClick()
        composeRule.onNodeWithText("map:41.9,12.5").assertExists()
        composeRule.onNodeWithTag("journey-map-marker").performClick()
        composeRule.onNodeWithText("map-detail:eq-map").assertExists()
        composeRule.onNodeWithTag("journey-map-dismiss").performClick()
        composeRule.onNodeWithText("map-detail:eq-map").assertDoesNotExist()

        composeRule.onNodeWithTag("journey-map-statistics").performClick()
        composeRule.onNodeWithText("statistics").assertExists()
        composeRule.onNodeWithTag("journey-statistics-details").performClick()
        composeRule.onNodeWithText("details:eq-statistics").assertExists()
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.onNodeWithText("statistics").assertExists()

        composeRule.onNodeWithTag("journey-statistics-settings").performClick()
        composeRule.onNodeWithText("settings").assertExists()
        composeRule.onNodeWithTag("journey-settings-theme").performClick()
        composeRule.onNodeWithText("settings-theme:dark").assertExists()
        composeRule.onNodeWithTag("journey-settings-credits").performClick()
        composeRule.onNodeWithText("credits").assertExists()
        composeRule.onNodeWithTag("journey-credits-back").performClick()
        composeRule.onNodeWithText("settings").assertExists()
    }

    private fun setHostContent() {
        composeRule.setContent {
            val navigator = remember {
                AppNavigator(
                    initialBackStack = androidx.compose.runtime.mutableStateListOf<AppDestination>(
                        AppDestination.Intro
                    ),
                    topLevelDestinations = appTopLevelDestinationClasses
                )
            }
            AppNavigationHost(navigator = navigator, screenFactory = JourneyScreenFactory)
        }
    }
}

private object JourneyScreenFactory : AppNavigationScreenFactory {
    @Composable
    override fun Intro(onContinueToHome: () -> Unit) = JourneyButton(
        tag = "journey-intro-continue",
        label = "intro",
        onClick = onContinueToHome
    )

    @Composable
    override fun Home(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit,
        onOpenMap: (Double, Double) -> Unit
    ) {
        var refreshes by remember { mutableStateOf(0) }
        var filterOpen by remember { mutableStateOf(false) }
        Column {
            Text("home")
            Text("home-refreshes:$refreshes")
            Text("home-filter:${if (filterOpen) "open" else "closed"}")
            JourneyButton("journey-home-refresh", "refresh") { refreshes++ }
            JourneyButton("journey-home-filter", "filter") { filterOpen = true }
            JourneyButton("journey-home-map", "map") { onOpenMap(41.9, 12.5) }
        }
    }

    @Composable
    override fun Map(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        initialLatLng: LatLng?
    ) {
        var markerSelected by remember { mutableStateOf(false) }
        Column {
            Text("map:${initialLatLng?.latitude},${initialLatLng?.longitude}")
            if (markerSelected) {
                Text("map-detail:eq-map")
                JourneyButton("journey-map-dismiss", "dismiss") { markerSelected = false }
            }
            JourneyButton("journey-map-marker", "marker") { markerSelected = true }
            JourneyButton("journey-map-statistics", "statistics") {
                onTopLevelDestinationSelected(AppDestination.Statistics)
            }
        }
    }

    @Composable
    override fun Statistics(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit
    ) = Column {
        Text("statistics")
        JourneyButton("journey-statistics-details", "details") {
            onOpenDetails("eq-statistics")
        }
        JourneyButton("journey-statistics-settings", "settings") {
            onTopLevelDestinationSelected(AppDestination.Settings)
        }
    }

    @Composable
    override fun Settings(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenCredits: () -> Unit
    ) {
        var darkTheme by remember { mutableStateOf(false) }
        Column {
            Text("settings")
            Text("settings-theme:${if (darkTheme) "dark" else "light"}")
            JourneyButton("journey-settings-theme", "theme") { darkTheme = !darkTheme }
            JourneyButton("journey-settings-credits", "credits", onOpenCredits)
        }
    }

    @Composable
    override fun Credits(currentDestination: AppDestination, onBack: () -> Unit) = Column {
        Text("credits")
        JourneyButton("journey-credits-back", "back", onBack)
    }

    @Composable
    override fun Details(currentDestination: AppDestination, id: String, onBack: () -> Unit) = Column {
        Text("details:$id")
        JourneyButton("journey-details-back", "back", onBack)
    }
}

@Composable
private fun JourneyButton(tag: String, label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.testTag(tag)) {
        Text(label)
    }
}
