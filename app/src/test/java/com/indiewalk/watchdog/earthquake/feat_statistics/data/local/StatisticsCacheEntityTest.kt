package com.indiewalk.watchdog.earthquake.feat_statistics.data.local

import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import org.junit.Assert.assertEquals
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
                longitude = 178.0
            ),
            nearestToday = null,
            threshold = 2.5,
            retrievedAt = now,
            windows = StatisticsWindows(
                today = StatisticsWindow(StatisticsPeriod.TODAY, Instant.parse("2026-08-13T22:00:00Z"), now),
                last7Days = StatisticsWindow(StatisticsPeriod.LAST_7_DAYS, Instant.parse("2026-08-07T10:30:00Z"), now),
                last30Days = StatisticsWindow(StatisticsPeriod.LAST_30_DAYS, Instant.parse("2026-07-15T10:30:00Z"), now),
                year = StatisticsWindow(StatisticsPeriod.YEAR, Instant.parse("2025-12-31T23:00:00Z"), now)
            )
        )

        val restored = snapshot.toCacheEntity().toSnapshot()

        assertEquals(snapshot, restored)
    }
}
