package com.indiewalk.watchdog.earthquake.feat_statistics.domain.time

import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import java.time.Clock
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class StatisticsTimeProvider(
    private val clock: Clock,
    private val zoneId: ZoneId
) {
    fun windows(): StatisticsWindows {
        val now = clock.instant()
        val localDate = now.atZone(zoneId).toLocalDate()
        val todayStart = localDate.atStartOfDay(zoneId).toInstant()
        val yearStart = localDate.withDayOfYear(1).atStartOfDay(zoneId).toInstant()

        return StatisticsWindows(
            today = StatisticsWindow(StatisticsPeriod.TODAY, todayStart, now),
            last7Days = StatisticsWindow(
                StatisticsPeriod.LAST_7_DAYS,
                now.minus(7, ChronoUnit.DAYS),
                now
            ),
            last30Days = StatisticsWindow(
                StatisticsPeriod.LAST_30_DAYS,
                now.minus(30, ChronoUnit.DAYS),
                now
            ),
            year = StatisticsWindow(StatisticsPeriod.YEAR, yearStart, now)
        )
    }
}
