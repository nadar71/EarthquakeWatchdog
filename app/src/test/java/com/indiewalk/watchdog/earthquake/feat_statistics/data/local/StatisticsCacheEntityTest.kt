package com.indiewalk.watchdog.earthquake.feat_statistics.data.local

import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.ActiveRegion
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.DistributionBucket
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsInsights
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.TrendPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class StatisticsCacheEntityTest {

    @Test
    fun completeSnapshotRoundTripsWithoutLosingTimesOrEvents() {
        val now = Instant.parse("2026-08-14T10:30:00Z")
        val snapshot = StatisticsSnapshot(
            counts = StatisticsCounts(146, 892, 3_761, 41_208),
            strongestToday = StatisticsEvent(
                id = "strongest",
                magnitude = 6.1,
                place = "South of Fiji",
                time = 1_786_700_000_000,
                depthKm = 28.0,
                latitude = -24.0,
                longitude = 178.0,
                distanceKm = 132.0
            ),
            nearestToday = null,
            insights = StatisticsInsights(
                globalTrend = listOf(TrendPoint(now.minusSeconds(86_400), now, 12)),
                magnitudeDistribution = listOf(DistributionBucket("m2_5_2_9", 8)),
                depthDistribution = listOf(DistributionBucket("shallow", 10)),
                activeRegions = listOf(ActiveRegion("Italy", 4)),
                nearbyTrend = null
            ),
            threshold = 2.5,
            retrievedAt = now,
            windows = StatisticsWindows(
                today = StatisticsWindow(StatisticsPeriod.TODAY, Instant.parse("2026-08-13T22:00:00Z"), now),
                last7Days = StatisticsWindow(StatisticsPeriod.LAST_7_DAYS, Instant.parse("2026-08-07T10:30:00Z"), now),
                last30Days = StatisticsWindow(StatisticsPeriod.LAST_30_DAYS, Instant.parse("2026-07-15T10:30:00Z"), now),
                year = StatisticsWindow(StatisticsPeriod.YEAR, Instant.parse("2025-12-31T23:00:00Z"), now)
            )
        )

        val cached = snapshot.toCacheEntity()
        val restored = cached.toSnapshot()

        assertEquals(snapshot, restored)
        assertTrue(cached.windowsJson.contains("\"last7Days\""))
        assertTrue(requireNotNull(cached.strongestEventJson).contains("\"distanceKm\""))
        assertTrue(requireNotNull(cached.insightsJson).contains("\"globalTrend\""))
    }

    @Test
    fun persistedCacheJsonWithStableFieldNamesRestoresAfterAnUpgrade() {
        val cached = StatisticsCacheEntity(
            todayCount = 1,
            weekCount = 2,
            monthCount = 3,
            yearCount = 4,
            threshold = 2.5,
            retrievedAtEpochMillis = 1_720_000_000_000,
            windowsJson = """{"today":{"startEpochMillis":1,"endEpochMillis":2},"last7Days":{"startEpochMillis":3,"endEpochMillis":4},"last30Days":{"startEpochMillis":5,"endEpochMillis":6},"year":{"startEpochMillis":7,"endEpochMillis":8}}""",
            strongestEventJson = """{"id":"strongest","magnitude":6.1,"place":"Test","time":9,"depthKm":10.0,"latitude":11.0,"longitude":12.0,"distanceKm":13.0}""",
            nearestEventJson = null,
            insightsJson = """{"globalTrend":[{"startEpochMillis":1,"endEpochMillis":2,"count":3}],"magnitudeDistribution":[{"id":"magnitude","count":4}],"depthDistribution":[{"id":"depth","count":5}],"activeRegions":[{"name":"Italy","count":6}],"nearbyTrend":null}"""
        )

        val restored = cached.toSnapshot()

        assertEquals(3L, restored.windows.last7Days.start.toEpochMilli())
        assertEquals(13.0, restored.strongestToday?.distanceKm)
        assertEquals(3, restored.insights?.globalTrend?.single()?.count)
        assertEquals("Italy", restored.insights?.activeRegions?.single()?.name)
    }
}
