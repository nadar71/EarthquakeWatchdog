package com.indiewalk.watchdog.earthquake.feat_statistics.data.repository

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsCacheDao
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsCacheEntity
import com.indiewalk.watchdog.earthquake.feat_statistics.data.local.toCacheEntity
import com.indiewalk.watchdog.earthquake.feat_statistics.data.remote.EarthquakeStatisticsRemoteDataSource
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSection
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.time.StatisticsTimeProvider
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.analysis.StatisticsInsightsAnalyzer
import com.indiewalk.watchdog.earthquake.sampleEqFeed
import com.indiewalk.watchdog.earthquake.sampleEqFeature
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class EarthquakeStatisticsRepositoryImplTest {

    private val now = Instant.parse("2026-08-14T10:30:00Z")
    private val timeProvider = StatisticsTimeProvider(
        Clock.fixed(now, ZoneOffset.UTC),
        ZoneOffset.UTC
    )

    @Test
    fun freshCacheReturnsWithoutNetwork() = runTest {
        val cache = RecordingCacheDao(cached = completeSnapshot(retrievedAt = now).toCacheEntity())
        val remote = FakeStatisticsRemoteDataSource()
        val repository = repository(remote = remote, cache = cache)

        val emissions = repository.load(forceRefresh = false).toList()

        assertEquals(1, emissions.size)
        assertTrue(emissions.single().isFromCache)
        assertFalse(emissions.single().isStale)
        assertEquals(0, remote.countCalls.size)
        assertEquals(0, remote.eventCalls.size)
    }

    @Test
    fun staleCacheEmitsBeforeRemoteReplacement() = runTest {
        val cachedSnapshot = completeSnapshot(retrievedAt = now.minusSeconds(16 * 60L))
        val cache = RecordingCacheDao(cached = cachedSnapshot.toCacheEntity())
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts,
            strongestFeed = sampleEqFeed(features = emptyList()),
            eventPages = mapOf(1 to sampleEqFeed(features = emptyList()))
        )

        val emissions = repository(remote = remote, cache = cache)
            .load(forceRefresh = false)
            .toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions.first().isFromCache)
        assertTrue(emissions.first().isStale)
        assertFalse(emissions.last().isFromCache)
        assertEquals(now, emissions.last().snapshot?.retrievedAt)
    }

    @Test
    fun completeRefreshFindsNearestEventAndReplacesCache() = runTest {
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts,
            strongestFeed = sampleEqFeed(features = listOf(sampleEqFeature(id = "strongest", mag = 6.1))),
            eventPages = mapOf(
                1 to sampleEqFeed(
                    features = listOf(
                        sampleEqFeature(id = "far", time = now.toEpochMilli(), latitude = 46.0, longitude = 10.0),
                        sampleEqFeature(id = "near", time = now.toEpochMilli(), latitude = 45.01, longitude = 9.01)
                    )
                )
            )
        )
        val cache = RecordingCacheDao()
        val repository = repository(remote = remote, cache = cache)

        val result = repository.load(forceRefresh = true).toList().single()

        assertEquals(146, result.snapshot?.counts?.today)
        assertEquals("strongest", result.snapshot?.strongestToday?.id)
        assertEquals("near", result.snapshot?.nearestToday?.id)
        assertTrue(requireNotNull(result.snapshot?.nearestToday?.distanceKm) < 2.0)
        assertEquals(2, result.snapshot?.insights?.globalTrend?.sumOf { it.count })
        assertEquals(StatisticsPeriod.LAST_30_DAYS, remote.eventCalls.single { it.orderBy == "time" }.period)
        assertTrue(result.unavailableSections.isEmpty())
        assertEquals(1, cache.replacements.size)
    }

    @Test
    fun partialRefreshDoesNotReplaceCompleteCache() = runTest {
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts,
            failedCountPeriod = StatisticsPeriod.YEAR,
            strongestFeed = sampleEqFeed(features = emptyList()),
            eventPages = mapOf(1 to sampleEqFeed(features = emptyList()))
        )
        val cache = RecordingCacheDao()
        val repository = repository(remote = remote, cache = cache)

        val result = repository.load(forceRefresh = true).toList().single()

        assertNull(result.snapshot?.counts?.year)
        assertTrue(StatisticsSection.YEAR_COUNT in result.unavailableSections)
        assertTrue(cache.replacements.isEmpty())
    }

    @Test
    fun insightPagesPastTwentyThousandUsingThirtyDayCount() = runTest {
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts + (StatisticsPeriod.LAST_30_DAYS to 20_001),
            strongestFeed = sampleEqFeed(features = emptyList()),
            eventPages = mapOf(
                1 to sampleEqFeed(
                    features = listOf(sampleEqFeature(id = "first", time = now.toEpochMilli(), latitude = 46.0, longitude = 10.0))
                ),
                20_001 to sampleEqFeed(
                    features = listOf(sampleEqFeature(id = "last", time = now.toEpochMilli(), latitude = 45.001, longitude = 9.001))
                )
            )
        )
        val repository = repository(remote = remote)

        val result = repository.load(forceRefresh = true).toList().single()

        assertEquals(listOf(1, 20_001), remote.eventCalls.filter { it.orderBy == "time" }.map { it.offset })
        assertEquals("last", result.snapshot?.nearestToday?.id)
    }

    @Test
    fun nearestUsesManualPositionWhenEnabled() = runTest {
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts,
            strongestFeed = sampleEqFeed(features = emptyList()),
            eventPages = mapOf(
                1 to sampleEqFeed(
                    features = listOf(
                        sampleEqFeature(id = "near-user", latitude = 45.0, longitude = 9.0),
                        sampleEqFeature(id = "near-manual", time = now.toEpochMilli(), latitude = 46.0, longitude = 10.0)
                    )
                )
            )
        )
        val repository = repository(
            remote = remote,
            settings = AppSettings(
                manualLocOn = true,
                userPosition = LatLng(45.0, 9.0),
                manualPosition = LatLng(46.0, 10.0)
            )
        )

        val result = repository.load(forceRefresh = true).toList().single()

        assertEquals("near-manual", result.snapshot?.nearestToday?.id)
    }

    @Test
    fun invalidEffectivePositionKeepsGlobalInsightsAndMarksLocationSectionsUnavailable() = runTest {
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts,
            strongestFeed = sampleEqFeed(features = emptyList()),
            eventPages = mapOf(1 to sampleEqFeed(features = emptyList()))
        )
        val repository = repository(
            remote = remote,
            settings = AppSettings(userPosition = LatLng(Double.NaN, 9.0))
        )

        val result = repository.load(forceRefresh = true).toList().single()

        assertTrue(StatisticsSection.NEAREST in result.unavailableSections)
        assertTrue(StatisticsSection.NEARBY_TREND in result.unavailableSections)
        assertTrue(StatisticsSection.INSIGHTS !in result.unavailableSections)
        assertNull(result.snapshot?.nearestToday)
        assertEquals(30, result.snapshot?.insights?.globalTrend?.size)
        assertNull(result.snapshot?.insights?.nearbyTrend)
        assertTrue(remote.eventCalls.any { it.orderBy == "time" })
    }

    @Test
    fun nearestTodayIgnoresCloserEventsOutsideToday() = runTest {
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts,
            strongestFeed = sampleEqFeed(features = emptyList()),
            eventPages = mapOf(
                1 to sampleEqFeed(
                    features = listOf(
                        sampleEqFeature(
                            id = "older-near",
                            time = now.minusSeconds(2 * 24 * 60 * 60L).toEpochMilli(),
                            latitude = 45.0,
                            longitude = 9.0
                        ),
                        sampleEqFeature(
                            id = "today-far",
                            time = now.toEpochMilli(),
                            latitude = 46.0,
                            longitude = 10.0
                        )
                    )
                )
            )
        )

        val result = repository(remote = remote).load(forceRefresh = true).toList().single()

        assertEquals("today-far", result.snapshot?.nearestToday?.id)
        assertEquals(2, result.snapshot?.insights?.globalTrend?.sumOf { it.count })
    }

    @Test
    fun failedInsightPageKeepsSummaryAndMarksInsightSectionsUnavailable() = runTest {
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts,
            strongestFeed = sampleEqFeed(features = emptyList()),
            failedEventOffset = 1
        )

        val result = repository(remote = remote).load(forceRefresh = true).toList().single()

        assertEquals(146, result.snapshot?.counts?.today)
        assertNull(result.snapshot?.insights)
        assertTrue(StatisticsSection.INSIGHTS in result.unavailableSections)
        assertTrue(StatisticsSection.NEARBY_TREND in result.unavailableSections)
        assertTrue(StatisticsSection.NEAREST in result.unavailableSections)
    }

    @Test(expected = CancellationException::class)
    fun cancellationIsRethrown() = runTest {
        val remote = FakeStatisticsRemoteDataSource(
            counts = defaultCounts,
            cancelledCountPeriod = StatisticsPeriod.TODAY
        )

        repository(remote = remote).load(forceRefresh = true).toList()
    }

    private fun repository(
        remote: FakeStatisticsRemoteDataSource,
        cache: RecordingCacheDao = RecordingCacheDao(),
        settings: AppSettings = AppSettings(userPosition = LatLng(45.0, 9.0))
    ) = EarthquakeStatisticsRepositoryImpl(
        remote = remote,
        cacheDao = cache,
        appPreferencesRepository = FakeAppPreferencesRepository(settings),
        timeProvider = timeProvider,
        insightsAnalyzer = StatisticsInsightsAnalyzer()
    )

    private fun completeSnapshot(retrievedAt: Instant) = StatisticsSnapshot(
        counts = StatisticsCounts(146, 892, 3_761, 41_208),
        strongestToday = StatisticsEvent("strongest", 6.1, "Fiji", now.toEpochMilli(), 28.0, -24.0, 178.0),
        nearestToday = StatisticsEvent("nearest", 3.2, "Italy", now.toEpochMilli(), 10.0, 45.0, 9.0, 1.0),
        threshold = 2.5,
        retrievedAt = retrievedAt,
        windows = StatisticsWindows(
            StatisticsWindow(StatisticsPeriod.TODAY, now.minusSeconds(10), now),
            StatisticsWindow(StatisticsPeriod.LAST_7_DAYS, now.minusSeconds(20), now),
            StatisticsWindow(StatisticsPeriod.LAST_30_DAYS, now.minusSeconds(30), now),
            StatisticsWindow(StatisticsPeriod.YEAR, now.minusSeconds(40), now)
        )
    )

    private val defaultCounts = mapOf(
        StatisticsPeriod.TODAY to 146,
        StatisticsPeriod.LAST_7_DAYS to 892,
        StatisticsPeriod.LAST_30_DAYS to 3_761,
        StatisticsPeriod.YEAR to 41_208
    )
}

private class RecordingCacheDao(
    private var cached: StatisticsCacheEntity? = null
) : StatisticsCacheDao {
    val replacements = mutableListOf<StatisticsCacheEntity>()

    override suspend fun get(): StatisticsCacheEntity? = cached

    override suspend fun replace(entity: StatisticsCacheEntity) {
        cached = entity
        replacements += entity
    }
}

private data class EventCall(
    val period: StatisticsPeriod,
    val orderBy: String,
    val limit: Int,
    val offset: Int
)

private class FakeStatisticsRemoteDataSource(
    private val counts: Map<StatisticsPeriod, Int> = emptyMap(),
    private val failedCountPeriod: StatisticsPeriod? = null,
    private val cancelledCountPeriod: StatisticsPeriod? = null,
    private val strongestFeed: EQFeaturesCollectionDTO = sampleEqFeed(features = emptyList()),
    private val eventPages: Map<Int, EQFeaturesCollectionDTO> = emptyMap(),
    private val failedEventOffset: Int? = null
) : EarthquakeStatisticsRemoteDataSource {
    val countCalls = mutableListOf<StatisticsPeriod>()
    val eventCalls = mutableListOf<EventCall>()

    override suspend fun count(window: StatisticsWindow, threshold: Double): Int {
        countCalls += window.period
        if (window.period == cancelledCountPeriod) throw CancellationException("cancelled")
        if (window.period == failedCountPeriod) error("count failed")
        return counts.getValue(window.period)
    }

    override suspend fun events(
        window: StatisticsWindow,
        threshold: Double,
        orderBy: String,
        limit: Int,
        offset: Int
    ): EQFeaturesCollectionDTO {
        eventCalls += EventCall(window.period, orderBy, limit, offset)
        if (offset == failedEventOffset) error("event page failed")
        return if (orderBy == "magnitude") strongestFeed else eventPages.getValue(offset)
    }
}
