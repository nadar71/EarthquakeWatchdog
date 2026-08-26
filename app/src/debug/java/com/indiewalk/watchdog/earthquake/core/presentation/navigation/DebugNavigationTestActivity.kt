package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.google.android.gms.maps.model.LatLng

/** Debug-only host used to verify Navigation 3 state restoration without feature dependencies. */
class DebugNavigationTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigationHost(
                navigator = rememberAppNavigator(),
                screenFactory = DebugNavigationScreenFactory
            )
        }
    }
}

private object DebugNavigationScreenFactory : AppNavigationScreenFactory {
    @Composable
    override fun Intro(onContinueToHome: () -> Unit) = NavigationButton(
        tag = "intro-continue",
        label = "continue",
        onClick = onContinueToHome
    )

    @Composable
    override fun Home(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit,
        onOpenMap: (Double, Double) -> Unit
    ) = Column {
        Text("home-screen")
        NavigationButton("home-open-details", "details") { onOpenDetails("eq-42") }
        NavigationButton("home-open-settings", "settings") {
            onTopLevelDestinationSelected(AppDestination.Settings)
        }
    }

    @Composable
    override fun Map(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        initialLatLng: LatLng?
    ) = Text("map-screen")

    @Composable
    override fun Statistics(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit
    ) = Text("statistics-screen")

    @Composable
    override fun Settings(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenCredits: () -> Unit
    ) = Text("settings-screen")

    @Composable
    override fun Credits(currentDestination: AppDestination, onBack: () -> Unit) = Text("credits-screen")

    @Composable
    override fun Details(currentDestination: AppDestination, id: String, onBack: () -> Unit) =
        Text("details:$id")
}

@Composable
private fun NavigationButton(tag: String, label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.testTag(tag)) {
        Text(label)
    }
}
