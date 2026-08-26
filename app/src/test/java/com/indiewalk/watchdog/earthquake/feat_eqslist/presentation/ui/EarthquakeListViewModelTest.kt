package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeEQRepository
import com.indiewalk.watchdog.earthquake.FakeFilterPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeLocationRepository
import com.indiewalk.watchdog.earthquake.sampleEqEntity
import com.indiewalk.watchdog.earthquake.sampleLatLng
import com.indiewalk.watchdog.earthquake.sampleLocationInfo
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticsTestRule
import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases.ApplySortUseCase
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases.FilterEarthquakesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EarthquakeListViewModelTest {

    @get:Rule
    val diagnosticsTestRule = DiagnosticsTestRule()

    @Test
    fun `updates filtered list when filters change`() = runViewModelTest {
        val repository = FakeEQRepository(
            initialEarthquakes = listOf(
                sampleEqEntity(id = "small", mag = 2.0),
                sampleEqEntity(id = "large", mag = 5.0)
            ),
            fetchBlock = { succeedFetch() }
        )
        val viewModel = EarthquakeListViewModel(
            repository = repository,
            appPreferencesRepository = FakeAppPreferencesRepository(),
            filterPreferencesRepository = FakeFilterPreferencesRepository(),
            locationRepository = FakeLocationRepository(),
            filterEarthquakesUseCase = FilterEarthquakesUseCase(ApplySortUseCase())
        )

        viewModel.onFilterConfirmed(EqsSortOption.MAG_DESC, MinMagnitude.MAG_4_0, TimeInterval.LAST_30_DAYS)
        advanceUntilIdle()

        assertEquals(listOf("large"), viewModel.uiState.value.filteredEarthquakes.map { it.id })
    }

    @Test
    fun `selects earthquake for dialog state`() = runViewModelTest {
        val repository = FakeEQRepository(
            initialEarthquakes = listOf(sampleEqEntity(id = "selected")),
            fetchBlock = { succeedFetch() }
        )
        val viewModel = EarthquakeListViewModel(
            repository = repository,
            appPreferencesRepository = FakeAppPreferencesRepository(),
            filterPreferencesRepository = FakeFilterPreferencesRepository(),
            locationRepository = FakeLocationRepository(),
            filterEarthquakesUseCase = FilterEarthquakesUseCase(ApplySortUseCase())
        )

        advanceUntilIdle()
        viewModel.onEarthquakeSelected("selected")

        assertNotNull(viewModel.uiState.value.selectedEarthquake)
    }

    @Test
    fun `screen start refreshes once and syncs user location when permission granted`() = runViewModelTest {
        val repository = FakeEQRepository(fetchBlock = { /* no-op */ })
        val appPrefs = FakeAppPreferencesRepository()
        val viewModel = EarthquakeListViewModel(
            repository = repository,
            appPreferencesRepository = appPrefs,
            filterPreferencesRepository = FakeFilterPreferencesRepository(),
            locationRepository = FakeLocationRepository(
                lastKnownLatLng = sampleLatLng,
                locationInfo = sampleLocationInfo
            ),
            filterEarthquakesUseCase = FilterEarthquakesUseCase(ApplySortUseCase())
        )

        viewModel.onScreenStarted(true)
        viewModel.onScreenStarted(true)
        advanceUntilIdle()

        assertEquals(1, repository.fetchCount)
        assertEquals(sampleLatLng, appPrefs.getCurrentSettings().userPosition)
        assertEquals(sampleLocationInfo, appPrefs.getCurrentSettings().userLocationInfo)
    }

    @Test
    fun `refresh failure exposes network error and stops refresh`() = runViewModelTest {
        val repository = FakeEQRepository(fetchBlock = { error("boom") })
        val viewModel = EarthquakeListViewModel(
            repository = repository,
            appPreferencesRepository = FakeAppPreferencesRepository(),
            filterPreferencesRepository = FakeFilterPreferencesRepository(),
            locationRepository = FakeLocationRepository(),
            filterEarthquakesUseCase = FilterEarthquakesUseCase(ApplySortUseCase())
        )

        viewModel.onRefreshRequested()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isRefreshing)
        assertTrue(viewModel.uiState.value.error is AppError.Network)
    }

    @Test
    fun `settings updates propagate into ui state`() = runViewModelTest {
        val appPrefs = FakeAppPreferencesRepository()
        val viewModel = EarthquakeListViewModel(
            repository = FakeEQRepository(fetchBlock = { /* no-op */ }),
            appPreferencesRepository = appPrefs,
            filterPreferencesRepository = FakeFilterPreferencesRepository(),
            locationRepository = FakeLocationRepository(),
            filterEarthquakesUseCase = FilterEarthquakesUseCase(ApplySortUseCase())
        )

        appPrefs.setThemeMode(ThemeMode.Dark)
        advanceUntilIdle()

        assertEquals(ThemeMode.Dark, viewModel.uiState.value.settings.mode)
    }

    @Test
    fun `observe failure exposes storage error`() = runViewModelTest {
        val viewModel = EarthquakeListViewModel(
            repository = FakeEQRepository(observeError = IllegalStateException("db")),
            appPreferencesRepository = FakeAppPreferencesRepository(),
            filterPreferencesRepository = FakeFilterPreferencesRepository(),
            locationRepository = FakeLocationRepository(),
            filterEarthquakesUseCase = FilterEarthquakesUseCase(ApplySortUseCase())
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error is AppError.Storage)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun runViewModelTest(block: suspend TestScope.() -> Unit) =
    runTest(StandardTestDispatcher(TestCoroutineScheduler())) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
