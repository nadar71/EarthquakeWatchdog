package com.indiewalk.watchdog.earthquake.feat_statistics.domain.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class StatisticsTimeProviderTest {

    private val now = Instant.parse("2026-08-14T10:30:00Z")

    @Test
    fun romeCalendarBoundariesAreConvertedToUtc() {
        val provider = StatisticsTimeProvider(
            clock = Clock.fixed(now, ZoneOffset.UTC),
            zoneId = ZoneId.of("Europe/Rome")
        )

        val windows = provider.windows()

        assertEquals(Instant.parse("2026-08-13T22:00:00Z"), windows.today.start)
        assertEquals(Instant.parse("2025-12-31T23:00:00Z"), windows.year.start)
        assertEquals(Instant.parse("2026-08-07T10:30:00Z"), windows.last7Days.start)
        assertEquals(Instant.parse("2026-07-15T10:30:00Z"), windows.last30Days.start)
    }

    @Test
    fun allWindowsShareTheSameClockSnapshot() {
        val windows = StatisticsTimeProvider(
            clock = Clock.fixed(now, ZoneOffset.UTC),
            zoneId = ZoneId.of("UTC")
        ).windows()

        assertEquals(listOf(now, now, now, now), windows.all.map { it.end })
    }
}
