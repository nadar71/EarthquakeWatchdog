package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui

import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.MainDispatcherRule
import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.feat_statistics.data.repository.EarthquakeStatisticsLoadException
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsLoadResult
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSection
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.repository.EarthquakeStatisticsRepository
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.use_cases.LoadEarthquakeStatisticsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startupLoadsOnceAndExposesSnapshot() = runTest {
        val repository = FakeStatisticsRepository { flowOf(StatisticsLoadResult(snapshot)) }
        val viewModel = viewModel(repository)

        viewModel.onScreenStarted()
        viewModel.onScreenStarted()
        advanceUntilIdle()

        assertEquals(listOf(false), repository.loadCalls)
        assertEquals(snapshot, viewModel.uiState.value.snapshot)
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun cachedPartialResultKeepsItsStatusFlags() = runTest {
        val repository = FakeStatisticsRepository {
            flowOf(
                StatisticsLoadResult(
                    snapshot = snapshot,
                    unavailableSections = setOf(StatisticsSection.YEAR_COUNT),
                    isFromCache = true,
                    isStale = true
                )
            )
        }
        val viewModel = viewModel(repository)

        viewModel.onScreenStarted()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFromCache)
        assertTrue(state.isStale)
        assertEquals(setOf(StatisticsSection.YEAR_COUNT), state.unavailableSections)
    }

    @Test
    fun refreshFailurePreservesPreviouslyRenderedSnapshot() = runTest {
        val repository = FakeStatisticsRepository { forceRefresh ->
            if (!forceRefresh) {
                flowOf(StatisticsLoadResult(snapshot))
            } else {
                flow { throw EarthquakeStatisticsLoadException("offline") }
            }
        }
        val viewModel = viewModel(repository)
        viewModel.onScreenStarted()
        advanceUntilIdle()

        viewModel.onRefreshRequested()
        advanceUntilIdle()

        assertEquals(snapshot, viewModel.uiState.value.snapshot)
        assertTrue(viewModel.uiState.value.error is AppError.Network)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    private fun viewModel(repository: EarthquakeStatisticsRepository) = StatisticsViewModel(
        loadStatistics = LoadEarthquakeStatisticsUseCase(repository),
        appPreferencesRepository = FakeAppPreferencesRepository()
    )

    private val now = Instant.parse("2026-08-14T10:30:00Z")
    private val snapshot = StatisticsSnapshot(
        counts = StatisticsCounts(146, 892, 3_761, 41_208),
        strongestToday = null,
        nearestToday = null,
        threshold = 2.5,
        retrievedAt = now,
        windows = StatisticsWindows(
            StatisticsWindow(StatisticsPeriod.TODAY, now.minusSeconds(1), now),
            StatisticsWindow(StatisticsPeriod.LAST_7_DAYS, now.minusSeconds(2), now),
            StatisticsWindow(StatisticsPeriod.LAST_30_DAYS, now.minusSeconds(3), now),
            StatisticsWindow(StatisticsPeriod.YEAR, now.minusSeconds(4), now)
        )
    )
}

private class FakeStatisticsRepository(
    private val results: (Boolean) -> Flow<StatisticsLoadResult>
) : EarthquakeStatisticsRepository {
    val loadCalls = mutableListOf<Boolean>()

    override fun load(forceRefresh: Boolean): Flow<StatisticsLoadResult> {
        loadCalls += forceRefresh
        return results(forceRefresh)
    }
}
