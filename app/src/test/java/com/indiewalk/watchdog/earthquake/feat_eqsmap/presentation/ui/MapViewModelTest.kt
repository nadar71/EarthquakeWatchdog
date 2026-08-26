package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeEQRepository
import com.indiewalk.watchdog.earthquake.FakeLocationRepository
import com.indiewalk.watchdog.earthquake.sampleEqEntity
import com.indiewalk.watchdog.earthquake.sampleLatLng
import com.indiewalk.watchdog.earthquake.sampleLocationInfo
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LAT
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.DEFAULT_LNG
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticsTestRule
import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.feat_eqsmap.domain.use_cases.ObserveEarthquakesUseCase
import com.google.android.gms.maps.model.LatLng
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    internal val diagnosticsTestRule = DiagnosticsTestRule()

    @Test
    fun `sets recenter target when manual location is confirmed`() = runViewModelTest {
        val viewModel = MapViewModel(
            observeEarthquakesUseCase = ObserveEarthquakesUseCase(FakeEQRepository(listOf(sampleEqEntity()))),
            appPreferencesRepository = FakeAppPreferencesRepository(),
            locationRepository = FakeLocationRepository()
        )

        viewModel.onManualLocationConfirmed(sampleLatLng, sampleLocationInfo)
        advanceUntilIdle()

        assertEquals(sampleLatLng, viewModel.uiState.value.recenterTarget)
    }

    @Test
    fun `clears manual location and recenters to fallback`() = runViewModelTest {
        val appPrefs = FakeAppPreferencesRepository()
        val viewModel = MapViewModel(
            observeEarthquakesUseCase = ObserveEarthquakesUseCase(FakeEQRepository(listOf(sampleEqEntity()))),
            appPreferencesRepository = appPrefs,
            locationRepository = FakeLocationRepository(lastKnownLatLng = sampleLatLng)
        )

        viewModel.onLocationPermissionChanged(true)
        viewModel.onManualLocationCleared()
        advanceUntilIdle()

        assertEquals(sampleLatLng, viewModel.uiState.value.recenterTarget)
        assertEquals(false, appPrefs.getCurrentSettings().manualLocOn)
    }

    @Test
    fun `permission change syncs user location into settings`() = runViewModelTest {
        val appPrefs = FakeAppPreferencesRepository()
        val viewModel = MapViewModel(
            observeEarthquakesUseCase = ObserveEarthquakesUseCase(
                FakeEQRepository(listOf(sampleEqEntity()))
            ),
            appPreferencesRepository = appPrefs,
            locationRepository = FakeLocationRepository(
                lastKnownLatLng = sampleLatLng,
                locationInfo = sampleLocationInfo
            )
        )

        viewModel.onLocationPermissionChanged(true)
        advanceUntilIdle()

        assertEquals(sampleLatLng, appPrefs.getCurrentSettings().userPosition)
        assertEquals(sampleLocationInfo, appPrefs.getCurrentSettings().userLocationInfo)
    }

    @Test
    fun `clearing manual location without permission uses default fallback`() = runViewModelTest {
        val viewModel = MapViewModel(
            observeEarthquakesUseCase = ObserveEarthquakesUseCase(
                FakeEQRepository(listOf(sampleEqEntity()))
            ),
            appPreferencesRepository = FakeAppPreferencesRepository(),
            locationRepository = FakeLocationRepository(lastKnownLatLng = null)
        )

        viewModel.onLocationPermissionChanged(false)
        viewModel.onManualLocationCleared()
        advanceUntilIdle()

        assertEquals(
            LatLng(DEFAULT_LAT, DEFAULT_LNG),
            viewModel.uiState.value.recenterTarget
        )
    }

    @Test
    fun `settings updates propagate into ui state`() = runViewModelTest {
        val appPrefs = FakeAppPreferencesRepository()
        val viewModel = MapViewModel(
            observeEarthquakesUseCase = ObserveEarthquakesUseCase(
                FakeEQRepository(listOf(sampleEqEntity()))
            ),
            appPreferencesRepository = appPrefs,
            locationRepository = FakeLocationRepository()
        )

        appPrefs.setThemeMode(ThemeMode.Dark)
        advanceUntilIdle()

        assertEquals(ThemeMode.Dark, viewModel.uiState.value.settings.mode)
    }

    @Test
    fun `observe failure exposes storage error`() = runViewModelTest {
        val viewModel = MapViewModel(
            observeEarthquakesUseCase = ObserveEarthquakesUseCase(
                FakeEQRepository(observeError = IllegalStateException("db"))
            ),
            appPreferencesRepository = FakeAppPreferencesRepository(),
            locationRepository = FakeLocationRepository()
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
