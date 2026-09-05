package com.indiewalk.watchdog.earthquake.feat_statistics.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsInsights
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindows
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.ActiveRegion
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.DistributionBucket
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.TrendPoint
import java.time.Instant

@Entity(tableName = "statistics_cache")
data class StatisticsCacheEntity(
    @PrimaryKey val cacheId: Int = CACHE_ID,
    val todayCount: Int,
    val weekCount: Int,
    val monthCount: Int,
    val yearCount: Int,
    val threshold: Double,
    val retrievedAtEpochMillis: Long,
    val windowsJson: String,
    val strongestEventJson: String?,
    val nearestEventJson: String?,
    val insightsJson: String?
) {
    companion object {
        const val CACHE_ID = 1
    }
}

fun StatisticsSnapshot.toCacheEntity(gson: Gson = Gson()): StatisticsCacheEntity =
    StatisticsCacheEntity(
        todayCount = requireNotNull(counts.today),
        weekCount = requireNotNull(counts.last7Days),
        monthCount = requireNotNull(counts.last30Days),
        yearCount = requireNotNull(counts.year),
        threshold = threshold,
        retrievedAtEpochMillis = retrievedAt.toEpochMilli(),
        windowsJson = gson.toJson(windows.toCacheModel()),
        strongestEventJson = strongestToday?.let(gson::toJson),
        nearestEventJson = nearestToday?.let(gson::toJson),
        insightsJson = insights?.toCacheModel()?.let(gson::toJson)
    )

fun StatisticsCacheEntity.toSnapshot(gson: Gson = Gson()): StatisticsSnapshot =
    StatisticsSnapshot(
        counts = StatisticsCounts(todayCount, weekCount, monthCount, yearCount),
        strongestToday = strongestEventJson?.let {
            gson.fromJson(it, StatisticsEvent::class.java)
        },
        nearestToday = nearestEventJson?.let {
            gson.fromJson(it, StatisticsEvent::class.java)
        },
        insights = insightsJson
            ?.takeIf(String::isNotBlank)
            ?.let { json ->
                runCatching {
                    gson.fromJson(json, StatisticsInsightsCache::class.java).toDomain()
                }.getOrNull()
            },
        threshold = threshold,
        retrievedAt = Instant.ofEpochMilli(retrievedAtEpochMillis),
        windows = gson.fromJson(windowsJson, StatisticsWindowsCache::class.java).toDomain()
    )

private data class StatisticsWindowCache(
    val startEpochMillis: Long,
    val endEpochMillis: Long
)

private data class StatisticsWindowsCache(
    val today: StatisticsWindowCache,
    val last7Days: StatisticsWindowCache,
    val last30Days: StatisticsWindowCache,
    val year: StatisticsWindowCache
)

private data class TrendPointCache(
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val count: Int
)

private data class StatisticsInsightsCache(
    val globalTrend: List<TrendPointCache>,
    val magnitudeDistribution: List<DistributionBucket>,
    val depthDistribution: List<DistributionBucket>,
    val activeRegions: List<ActiveRegion>,
    val nearbyTrend: List<TrendPointCache>?
)

private fun StatisticsWindows.toCacheModel() = StatisticsWindowsCache(
    today = today.toCacheModel(),
    last7Days = last7Days.toCacheModel(),
    last30Days = last30Days.toCacheModel(),
    year = year.toCacheModel()
)

private fun StatisticsWindow.toCacheModel() = StatisticsWindowCache(
    startEpochMillis = start.toEpochMilli(),
    endEpochMillis = end.toEpochMilli()
)

private fun StatisticsInsights.toCacheModel() = StatisticsInsightsCache(
    globalTrend = globalTrend.map(TrendPoint::toCacheModel),
    magnitudeDistribution = magnitudeDistribution,
    depthDistribution = depthDistribution,
    activeRegions = activeRegions,
    nearbyTrend = nearbyTrend?.map(TrendPoint::toCacheModel)
)

private fun TrendPoint.toCacheModel() = TrendPointCache(
    startEpochMillis = start.toEpochMilli(),
    endEpochMillis = end.toEpochMilli(),
    count = count
)

private fun StatisticsWindowsCache.toDomain() = StatisticsWindows(
    today = today.toDomain(StatisticsPeriod.TODAY),
    last7Days = last7Days.toDomain(StatisticsPeriod.LAST_7_DAYS),
    last30Days = last30Days.toDomain(StatisticsPeriod.LAST_30_DAYS),
    year = year.toDomain(StatisticsPeriod.YEAR)
)

private fun StatisticsWindowCache.toDomain(period: StatisticsPeriod) = StatisticsWindow(
    period = period,
    start = Instant.ofEpochMilli(startEpochMillis),
    end = Instant.ofEpochMilli(endEpochMillis)
)

private fun StatisticsInsightsCache.toDomain() = StatisticsInsights(
    globalTrend = globalTrend.map(TrendPointCache::toDomain),
    magnitudeDistribution = magnitudeDistribution,
    depthDistribution = depthDistribution,
    activeRegions = activeRegions,
    nearbyTrend = nearbyTrend?.map(TrendPointCache::toDomain)
)

private fun TrendPointCache.toDomain() = TrendPoint(
    start = Instant.ofEpochMilli(startEpochMillis),
    end = Instant.ofEpochMilli(endEpochMillis),
    count = count
)
