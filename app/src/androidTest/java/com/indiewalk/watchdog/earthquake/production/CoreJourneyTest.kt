package com.indiewalk.watchdog.earthquake.production

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationHost
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationScreenFactory
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigator
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.appTopLevelDestinationClasses
import org.junit.Rule
import org.junit.Test

/** Routing-only coverage. Production feature content is tested separately. */
class AppNavigationHostRoutingJourneyTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun routesAcrossTopLevelAndDetailDestinationsThenReturns() {
        setHostContent()

        composeRule.onNodeWithTag("routing-intro-continue").performClick()
        composeRule.onNodeWithText("home").assertExists()

        composeRule.onNodeWithTag("routing-home-map").performClick()
        composeRule.onNodeWithText("map:41.9,12.5").assertExists()

        composeRule.onNodeWithTag("routing-map-statistics").performClick()
        composeRule.onNodeWithText("statistics").assertExists()
        composeRule.onNodeWithTag("routing-statistics-details").performClick()
        composeRule.onNodeWithText("details:eq-statistics").assertExists()
        composeRule.onNodeWithTag("routing-details-back").performClick()
        composeRule.onNodeWithText("statistics").assertExists()

        composeRule.onNodeWithTag("routing-statistics-settings").performClick()
        composeRule.onNodeWithText("settings").assertExists()
        composeRule.onNodeWithTag("routing-settings-credits").performClick()
        composeRule.onNodeWithText("credits").assertExists()
        composeRule.onNodeWithTag("routing-credits-back").performClick()
        composeRule.onNodeWithText("settings").assertExists()
        composeRule.onNodeWithTag("routing-settings-privacy").performClick()
        composeRule.onNodeWithText("privacy-policy").assertExists()
        composeRule.onNodeWithTag("routing-privacy-back").performClick()
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
            AppNavigationHost(navigator = navigator, screenFactory = RoutingScreenFactory)
        }
    }
}

private object RoutingScreenFactory : AppNavigationScreenFactory {
    @Composable
    override fun Intro(onContinueToHome: () -> Unit) = RoutingButton(
        tag = "routing-intro-continue",
        label = "intro",
        onClick = onContinueToHome
    )

    @Composable
    override fun Home(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit,
        onOpenMap: (Double, Double) -> Unit
    ) = Column {
        Text("home")
        RoutingButton("routing-home-map", "map") { onOpenMap(41.9, 12.5) }
    }

    @Composable
    override fun Map(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        initialLatLng: LatLng?
    ) = Column {
        Text("map:${initialLatLng?.latitude},${initialLatLng?.longitude}")
        RoutingButton("routing-map-statistics", "statistics") {
            onTopLevelDestinationSelected(AppDestination.Statistics)
        }
    }

    @Composable
    override fun Statistics(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit
    ) = Column {
        Text("statistics")
        RoutingButton("routing-statistics-details", "details") {
            onOpenDetails("eq-statistics")
        }
        RoutingButton("routing-statistics-settings", "settings") {
            onTopLevelDestinationSelected(AppDestination.Settings)
        }
    }

    @Composable
    override fun Settings(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenCredits: () -> Unit,
        onOpenPrivacyPolicy: () -> Unit,
        onManageAdPrivacy: () -> Unit
    ) = Column {
        Text("settings")
        RoutingButton("routing-settings-credits", "credits", onOpenCredits)
        RoutingButton("routing-settings-privacy", "privacy", onOpenPrivacyPolicy)
    }

    @Composable
    override fun Credits(currentDestination: AppDestination, onBack: () -> Unit) = Column {
        Text("credits")
        RoutingButton("routing-credits-back", "back", onBack)
    }

    @Composable
    override fun PrivacyPolicy(currentDestination: AppDestination, onBack: () -> Unit) = Column {
        Text("privacy-policy")
        RoutingButton("routing-privacy-back", "back", onBack)
    }

    @Composable
    override fun Details(currentDestination: AppDestination, id: String, onBack: () -> Unit) = Column {
        Text("details:$id")
        RoutingButton("routing-details-back", "back", onBack)
    }
}

@Composable
private fun RoutingButton(tag: String, label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.testTag(tag)) {
        Text(label)
    }
}
