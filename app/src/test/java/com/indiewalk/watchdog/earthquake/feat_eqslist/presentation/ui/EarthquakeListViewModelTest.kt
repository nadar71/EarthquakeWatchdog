package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeEQRepository
import com.indiewalk.watchdog.earthquake.FakeFilterPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeLocationRepository
import com.indiewalk.watchdog.earthquake.MainDispatcherRule
import com.indiewalk.watchdog.earthquake.sampleEqEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases.ApplySortUseCase
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases.FilterEarthquakesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EarthquakeListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `updates filtered list when filters change`() = runTest {
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
    fun `selects earthquake for dialog state`() = runTest {
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
}

