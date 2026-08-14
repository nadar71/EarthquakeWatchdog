package com.indiewalk.watchdog.earthquake.feat_statistics.domain.model

import java.time.Instant

enum class StatisticsPeriod {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    YEAR
}

data class StatisticsWindow(
    val period: StatisticsPeriod,
    val start: Instant,
    val end: Instant
)

data class StatisticsWindows(
    val today: StatisticsWindow,
    val last7Days: StatisticsWindow,
    val last30Days: StatisticsWindow,
    val year: StatisticsWindow
) {
    val all: List<StatisticsWindow>
        get() = listOf(today, last7Days, last30Days, year)
}

data class StatisticsCounts(
    val today: Int?,
    val last7Days: Int?,
    val last30Days: Int?,
    val year: Int?
)

data class StatisticsEvent(
    val id: String,
    val magnitude: Double,
    val place: String,
    val time: Long,
    val depthKm: Double?,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double? = null
)

data class StatisticsLocation(
    val latitude: Double,
    val longitude: Double
)

data class TrendPoint(
    val start: Instant,
    val end: Instant,
    val count: Int
)

data class DistributionBucket(
    val id: String,
    val count: Int
)

data class ActiveRegion(
    val name: String,
    val count: Int
)

data class StatisticsInsights(
    val globalTrend: List<TrendPoint>,
    val magnitudeDistribution: List<DistributionBucket>,
    val depthDistribution: List<DistributionBucket>,
    val activeRegions: List<ActiveRegion>,
    val nearbyTrend: List<TrendPoint>?
)

data class StatisticsSnapshot(
    val counts: StatisticsCounts,
    val strongestToday: StatisticsEvent?,
    val nearestToday: StatisticsEvent?,
    val threshold: Double,
    val retrievedAt: Instant,
    val windows: StatisticsWindows
)

enum class StatisticsSection {
    TODAY_COUNT,
    WEEK_COUNT,
    MONTH_COUNT,
    YEAR_COUNT,
    STRONGEST,
    NEAREST
}

data class StatisticsLoadResult(
    val snapshot: StatisticsSnapshot?,
    val unavailableSections: Set<StatisticsSection> = emptySet(),
    val isFromCache: Boolean = false,
    val isStale: Boolean = false
)
