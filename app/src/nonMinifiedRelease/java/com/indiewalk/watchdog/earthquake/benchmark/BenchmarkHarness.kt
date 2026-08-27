package com.indiewalk.watchdog.earthquake.benchmark

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.core.performance.BenchmarkScreen

/** A fixture-only UI for repeatable macrobenchmarks; it has no production dependencies. */
class BenchmarkHarness : BenchmarkScreen {
    @Composable
    override fun Content() {
        var destination by rememberSaveable { mutableStateOf(BenchmarkDestination.HOME) }
        var filterVisible by rememberSaveable { mutableStateOf(false) }
        var markerSelected by rememberSaveable { mutableStateOf(false) }

        MaterialTheme {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTagsAsResourceId = true }
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    BenchmarkNavigation(onDestinationSelected = {
                        destination = it
                        filterVisible = false
                        markerSelected = false
                    })
                    when (destination) {
                        BenchmarkDestination.HOME -> BenchmarkHome(
                            filterVisible = filterVisible,
                            onFilterClicked = { filterVisible = true }
                        )
                        BenchmarkDestination.MAP -> BenchmarkMap(
                            markerSelected = markerSelected,
                            onMarkerClicked = { markerSelected = true }
                        )
                        BenchmarkDestination.STATISTICS -> BenchmarkStatistics()
                        BenchmarkDestination.SETTINGS -> BenchmarkSettings()
                    }
                }
            }
        }
    }
}

private enum class BenchmarkDestination { HOME, MAP, STATISTICS, SETTINGS }

@Composable
private fun BenchmarkNavigation(onDestinationSelected: (BenchmarkDestination) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BenchmarkNavButton("benchmark-nav-home", "Home") { onDestinationSelected(BenchmarkDestination.HOME) }
        BenchmarkNavButton("benchmark-nav-map", "Map") { onDestinationSelected(BenchmarkDestination.MAP) }
        BenchmarkNavButton("benchmark-nav-statistics", "Statistics") {
            onDestinationSelected(BenchmarkDestination.STATISTICS)
        }
        BenchmarkNavButton("benchmark-nav-settings", "Settings") {
            onDestinationSelected(BenchmarkDestination.SETTINGS)
        }
    }
}

@Composable
private fun BenchmarkNavButton(tag: String, label: String, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .testTag(tag)
            .clickable(onClick = onClick)
            .padding(8.dp)
    )
}

@Composable
private fun BenchmarkHome(filterVisible: Boolean, onFilterClicked: () -> Unit) {
    Button(onClick = onFilterClicked, modifier = Modifier.testTag("benchmark-filter-open")) {
        Text("Filter")
    }
    if (filterVisible) {
        Card(modifier = Modifier.fillMaxWidth().testTag("benchmark-filter-sheet")) {
            Text("Date: newest first", modifier = Modifier.padding(16.dp))
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("benchmark-home-list")
    ) {
        items(BENCHMARK_EARTHQUAKES) { earthquake ->
            Text(earthquake, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp))
        }
    }
}

@Composable
private fun BenchmarkMap(markerSelected: Boolean, onMarkerClicked: () -> Unit) {
    Card(modifier = Modifier.fillMaxSize().testTag("benchmark-map-content")) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Deterministic map fixture")
            Text(
                text = "M 5.2 Central Italy",
                modifier = Modifier
                    .testTag("benchmark-map-marker")
                    .clickable(onClick = onMarkerClicked)
                    .padding(vertical = 24.dp)
            )
            if (markerSelected) {
                Text("M 5.2, 10 km depth", modifier = Modifier.testTag("benchmark-map-marker-detail"))
            }
        }
    }
}

@Composable
private fun BenchmarkStatistics() {
    LazyColumn(modifier = Modifier.fillMaxSize().testTag("benchmark-statistics-content")) {
        items((1..24).map { "Earthquakes today: $it" }) { value ->
            Text(value, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp))
        }
    }
}

@Composable
private fun BenchmarkSettings() {
    Column(modifier = Modifier.fillMaxSize().testTag("benchmark-settings-content")) {
        Text("Theme", modifier = Modifier.padding(vertical = 20.dp))
        Text("Units", modifier = Modifier.padding(vertical = 20.dp))
    }
}

private val BENCHMARK_EARTHQUAKES = List(120) { index ->
    "M ${"%.1f".format(3.0 + (index % 40) / 10.0)} Deterministic earthquake ${index + 1}"
}
