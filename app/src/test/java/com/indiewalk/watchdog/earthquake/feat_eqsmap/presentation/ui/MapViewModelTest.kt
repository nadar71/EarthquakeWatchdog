package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeLocationRepository
import com.indiewalk.watchdog.earthquake.MainDispatcherRule
import com.indiewalk.watchdog.earthquake.sampleEqEntity
import com.indiewalk.watchdog.earthquake.sampleLatLng
import com.indiewalk.watchdog.earthquake.sampleLocationInfo
import com.indiewalk.watchdog.earthquake.feat_eqsmap.domain.use_cases.ObserveEarthquakesUseCase
import com.indiewalk.watchdog.earthquake.FakeEQRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `sets recenter target when manual location is confirmed`() = runTest {
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
    fun `clears manual location and recenters to fallback`() = runTest {
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
}
