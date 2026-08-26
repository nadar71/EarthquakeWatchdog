package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationHostTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun intro_continue_navigates_to_home() {
        setHostContent()

        composeRule.onNodeWithTag("intro-continue").performClick()

        composeRule.onNodeWithText("home-screen").assertExists()
    }

    @Test
    fun home_open_details_and_system_back_returns_to_home() {
        setHostContent()

        composeRule.onNodeWithTag("intro-continue").performClick()
        composeRule.onNodeWithTag("home-open-details").performClick()
        composeRule.onNodeWithText("details:eq-42").assertExists()

        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithText("home-screen").assertExists()
    }

    @Test
    fun home_open_map_preserves_typed_coordinates() {
        setHostContent()

        composeRule.onNodeWithTag("intro-continue").performClick()
        composeRule.onNodeWithTag("home-open-map").performClick()

        composeRule.onNodeWithText("map:10.0,20.0").assertExists()
    }

    @Test
    fun settings_open_credits_and_back_returns_to_settings() {
        setHostContent()

        composeRule.onNodeWithTag("intro-continue").performClick()
        composeRule.onNodeWithTag("home-open-settings").performClick()
        composeRule.onNodeWithText("settings-screen").assertExists()

        composeRule.onNodeWithTag("settings-open-credits").performClick()
        composeRule.onNodeWithText("credits-screen").assertExists()

        composeRule.onNodeWithTag("credits-back").performClick()

        composeRule.onNodeWithText("settings-screen").assertExists()
    }

    @Test
    fun statistics_opens_details_and_back_returns_to_statistics() {
        setHostContent()

        composeRule.onNodeWithTag("intro-continue").performClick()
        composeRule.onNodeWithTag("home-open-statistics").performClick()
        composeRule.onNodeWithText("statistics-screen").assertExists()

        composeRule.onNodeWithTag("statistics-open-details").performClick()
        composeRule.onNodeWithText("details:eq-statistics").assertExists()

        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithText("statistics-screen").assertExists()
    }

    private fun setHostContent() {
        composeRule.setContent {
            val navigator = remember {
                AppNavigator(
                    initialBackStack = mutableStateListOf<AppDestination>(AppDestination.Intro),
                    topLevelDestinations = appTopLevelDestinationClasses
                )
            }
            AppNavigationHost(
                navigator = navigator,
                screenFactory = FakeAppNavigationScreenFactory
            )
        }
    }
}

private object FakeAppNavigationScreenFactory : AppNavigationScreenFactory {
    @Composable
    override fun Intro(onContinueToHome: () -> Unit) {
        Column {
            Text("intro-screen")
            Button(
                onClick = onContinueToHome,
                modifier = androidx.compose.ui.Modifier.testTag("intro-continue")
            ) {
                Text("continue")
            }
        }
    }

    @Composable
    override fun Home(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit,
        onOpenMap: (Double, Double) -> Unit
    ) {
        Column {
            Text("home-screen")
            Button(
                onClick = { onOpenDetails("eq-42") },
                modifier = androidx.compose.ui.Modifier.testTag("home-open-details")
            ) {
                Text("details")
            }
            Button(
                onClick = { onOpenMap(10.0, 20.0) },
                modifier = androidx.compose.ui.Modifier.testTag("home-open-map")
            ) {
                Text("map")
            }
            Button(
                onClick = { onTopLevelDestinationSelected(AppDestination.Settings) },
                modifier = androidx.compose.ui.Modifier.testTag("home-open-settings")
            ) {
                Text("settings")
            }
            Button(
                onClick = { onTopLevelDestinationSelected(AppDestination.Statistics) },
                modifier = androidx.compose.ui.Modifier.testTag("home-open-statistics")
            ) {
                Text("statistics")
            }
        }
    }

    @Composable
    override fun Statistics(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit
    ) {
        Column {
            Text("statistics-screen")
            Button(
                onClick = { onOpenDetails("eq-statistics") },
                modifier = androidx.compose.ui.Modifier.testTag("statistics-open-details")
            ) {
                Text("details")
            }
        }
    }

    @Composable
    override fun Map(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        initialLatLng: LatLng?
    ) {
        val label = if (initialLatLng == null) {
            "map:none"
        } else {
            "map:${initialLatLng.latitude},${initialLatLng.longitude}"
        }
        Column {
            Text(label)
            Button(
                onClick = { onTopLevelDestinationSelected(AppDestination.Home) },
                modifier = androidx.compose.ui.Modifier.testTag("map-open-home")
            ) {
                Text("home")
            }
        }
    }

    @Composable
    override fun Settings(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenCredits: () -> Unit
    ) {
        Column {
            Text("settings-screen")
            Button(
                onClick = onOpenCredits,
                modifier = androidx.compose.ui.Modifier.testTag("settings-open-credits")
            ) {
                Text("credits")
            }
        }
    }

    @Composable
    override fun Credits(
        currentDestination: AppDestination,
        onBack: () -> Unit
    ) {
        Column {
            Text("credits-screen")
            Button(
                onClick = onBack,
                modifier = androidx.compose.ui.Modifier.testTag("credits-back")
            ) {
                Text("back")
            }
        }
    }

    @Composable
    override fun Details(
        currentDestination: AppDestination,
        id: String,
        onBack: () -> Unit
    ) {
        Column {
            Text("details:$id")
            Button(
                onClick = onBack,
                modifier = androidx.compose.ui.Modifier.testTag("details-back")
            ) {
                Text("back")
            }
        }
    }
}
