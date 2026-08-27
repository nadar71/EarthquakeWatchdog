package com.indiewalk.watchdog.earthquake.benchmark

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationScreenFactory
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences.FilterSheetContent
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state.EarthquakeListUiState
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui.EarthquakeListContent
import com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui.SelectedEarthquakeDetail
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.SettingsPreferencesContent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.ActiveRegion
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.DistributionBucket
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsInsights
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.TrendPoint
import com.indiewalk.watchdog.earthquake.feat_statistics.presentation.state.StatisticsUiState
import com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui.StatisticsContent
import java.time.Instant
import java.time.temporal.ChronoUnit

/** Deterministic state adapter around production navigation and feature composables. */
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
class BenchmarkAppNavigationScreenFactory : AppNavigationScreenFactory {
    @Composable
    override fun Intro(onContinueToHome: () -> Unit) {
        LaunchedEffect(Unit) { onContinueToHome() }
    }

    @Composable
    override fun Home(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit,
        onOpenMap: (Double, Double) -> Unit
    ) {
        var filterVisible by remember { mutableStateOf(false) }
        val earthquakes = remember { benchmarkEarthquakes() }
        val state = EarthquakeListUiState(
            isLoading = false,
            allEarthquakes = earthquakes,
            filteredEarthquakes = earthquakes,
            settings = BENCHMARK_SETTINGS,
            filterSettings = FilterSettings(),
            isFilterSheetVisible = filterVisible
        )

        ScaffoldModel(
            currentDestination = currentDestination,
            onTopLevelDestinationSelected = onTopLevelDestinationSelected,
            topBar = null,
            title = "Earthquake Watchdog"
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                EarthquakeListContent(
                    uiState = state,
                    onRefresh = {},
                    onFilterRequested = { filterVisible = true },
                    onEarthquakeSelected = onOpenDetails,
                    contentPadding = padding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = padding.calculateBottomPadding())
                )
                if (filterVisible) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("filter-sheet-content")
                    ) {
                        FilterSheetContent(
                            eqsCount = earthquakes.size,
                            lastRefreshTime = "2026-08-27 04:00 UTC",
                            startDate = "2026-07-28 04:00 UTC",
                            filterSettings = state.filterSettings,
                            onConfirm = { _, _, _ -> filterVisible = false },
                            onDismiss = { filterVisible = false }
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Map(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        initialLatLng: LatLng?
    ) {
        val earthquake = remember { benchmarkEarthquakes().first() }
        var selected by remember { mutableStateOf<EQEntity?>(null) }
        ScaffoldModel(
            currentDestination = currentDestination,
            onTopLevelDestinationSelected = onTopLevelDestinationSelected,
            topBar = null,
            title = "Map"
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("map-content")
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("map-earthquake-marker")
                        .clickable { selected = earthquake }
                ) {
                    Text(
                        text = "M 5.2 Central Italy",
                        modifier = Modifier.padding(24.dp)
                    )
                }
                SelectedEarthquakeDetail(
                    earthquake = selected,
                    unitSystem = UnitSystem.METRIC,
                    onDismiss = { selected = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }

    @Composable
    override fun Statistics(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenDetails: (String) -> Unit
    ) {
        ScaffoldModel(
            currentDestination = currentDestination,
            onTopLevelDestinationSelected = onTopLevelDestinationSelected,
            topBar = null,
            title = "Statistics"
        ) { padding ->
            StatisticsContent(
                uiState = StatisticsUiState(
                    snapshot = benchmarkStatistics(),
                    settings = BENCHMARK_SETTINGS,
                    isInitialLoading = false
                ),
                onRefresh = {},
                onEventClick = onOpenDetails,
                modifier = Modifier.padding(padding)
            )
        }
    }

    @Composable
    override fun Settings(
        currentDestination: AppDestination,
        onTopLevelDestinationSelected: (AppDestination) -> Unit,
        onOpenCredits: () -> Unit,
        onOpenPrivacyPolicy: () -> Unit,
        onManageAdPrivacy: () -> Unit
    ) {
        var settings by remember { mutableStateOf(BENCHMARK_SETTINGS) }
        ScaffoldModel(
            currentDestination = currentDestination,
            onTopLevelDestinationSelected = onTopLevelDestinationSelected,
            topBar = null,
            title = "Settings"
        ) { padding ->
            SettingsPreferencesContent(
                settings = settings,
                onUnitSystemSelected = { settings = settings.copy(unitSystem = it) },
                onThemeModeSelected = { settings = settings.copy(mode = it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .testTag("settings-content")
            )
        }
    }

    @Composable
    override fun Credits(currentDestination: AppDestination, onBack: () -> Unit) {
        Text("Credits")
    }

    @Composable
    override fun PrivacyPolicy(currentDestination: AppDestination, onBack: () -> Unit) {
        Text("Privacy policy")
    }

    @Composable
    override fun Details(currentDestination: AppDestination, id: String, onBack: () -> Unit) {
        Text("Earthquake $id")
    }
}

private val BENCHMARK_SETTINGS = AppSettings(
    mode = ThemeMode.Light,
    unitSystem = UnitSystem.METRIC,
    lastRefreshTime = "2026-08-27 04:00 UTC"
)

private fun benchmarkEarthquakes(): List<EQEntity> = List(120) { index ->
    EQEntity(
        id = "benchmark-earthquake-$index",
        feedGenerated = 1L,
        mag = 3.0 + (index % 40) / 10.0,
        place = "Deterministic earthquake ${index + 1}",
        time = 1_787_802_400_000L - index * 60_000L,
        updated = null,
        tz = null,
        url = null,
        detail = null,
        felt = null,
        cdi = null,
        mmi = null,
        alert = null,
        status = "reviewed",
        tsunami = 0,
        sig = 100,
        net = "benchmark",
        code = index.toString(),
        ids = null,
        sources = null,
        types = null,
        nst = null,
        dmin = null,
        rms = null,
        gap = null,
        magType = "mw",
        eventType = "earthquake",
        longitude = 12.5 + index / 100.0,
        latitude = 42.0 + index / 100.0,
        depthKm = 10.0 + index % 30,
        distanceFromUser = 100 + index
    )
}

private fun benchmarkStatistics(): StatisticsSnapshot {
    val now = Instant.parse("2026-08-27T04:00:00Z")
    val day = StatisticsWindow(StatisticsPeriod.TODAY, now.minus(1, ChronoUnit.DAYS), now)
    val week = StatisticsWindow(StatisticsPeriod.LAST_7_DAYS, now.minus(7, ChronoUnit.DAYS), now)
    val month = StatisticsWindow(StatisticsPeriod.LAST_30_DAYS, now.minus(30, ChronoUnit.DAYS), now)
    val year = StatisticsWindow(StatisticsPeriod.YEAR, now.minus(365, ChronoUnit.DAYS), now)
    val event = StatisticsEvent(
        id = "benchmark-earthquake-0",
        magnitude = 5.2,
        place = "Central Italy",
        time = now.toEpochMilli(),
        depthKm = 10.0,
        latitude = 42.0,
        longitude = 12.5,
        distanceKm = 120.0
    )
    val trend = (0 until 12).map { index ->
        val end = now.minus(index.toLong(), ChronoUnit.DAYS)
        TrendPoint(end.minus(1, ChronoUnit.DAYS), end, 8 + index)
    }.reversed()
    return StatisticsSnapshot(
        counts = StatisticsCounts(today = 24, last7Days = 180, last30Days = 720, year = 8_640),
        strongestToday = event,
        nearestToday = event,
        insights = StatisticsInsights(
            globalTrend = trend,
            magnitudeDistribution = listOf(
                DistributionBucket("3_4", 500),
                DistributionBucket("4_5", 180),
                DistributionBucket("5_6", 38),
                DistributionBucket("6_plus", 2)
            ),
            depthDistribution = listOf(
                DistributionBucket("shallow", 410),
                DistributionBucket("intermediate", 250),
                DistributionBucket("deep", 60)
            ),
            activeRegions = listOf(
                ActiveRegion("Pacific Ring of Fire", 220),
                ActiveRegion("Mediterranean", 90),
                ActiveRegion("Mid-Atlantic Ridge", 45)
            ),
            nearbyTrend = trend.map { it.copy(count = it.count / 2) }
        ),
        threshold = 3.0,
        retrievedAt = now,
        windows = StatisticsWindows(day, week, month, year)
    )
}
