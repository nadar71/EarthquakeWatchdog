package com.indiewalk.watchdog.earthquake.feat_statistics.domain.analysis

import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.ActiveRegion
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.DistributionBucket
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsInsights
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsLocation
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.TrendPoint
import java.time.Duration
import java.util.Locale
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class StatisticsInsightsAnalyzer @Inject constructor() {

    fun analyze(
        events: List<StatisticsEvent>,
        window: StatisticsWindow,
        origin: StatisticsLocation?
    ): StatisticsInsights {
        val includedEvents = events.filter { event ->
            event.magnitude.isFinite() &&
                event.time >= window.start.toEpochMilli() &&
                event.time <= window.end.toEpochMilli()
        }
        val validOrigin = origin?.takeIf { it.isValid() }
        val nearbyEvents = if (validOrigin != null) {
            includedEvents.filter { event ->
                event.hasValidPosition() && haversineDistanceKm(validOrigin, event) <= NEARBY_RADIUS_KM
            }
        } else {
            null
        }

        return StatisticsInsights(
            globalTrend = buildTrend(includedEvents, window),
            magnitudeDistribution = magnitudeDistribution(includedEvents),
            depthDistribution = depthDistribution(includedEvents),
            activeRegions = activeRegions(includedEvents),
            nearbyTrend = nearbyEvents?.let { buildTrend(it, window) }
        )
    }

    private fun buildTrend(
        events: List<StatisticsEvent>,
        window: StatisticsWindow
    ): List<TrendPoint> {
        val bucketMillis = BUCKET_DURATION.toMillis()
        val counts = IntArray(BUCKET_COUNT)
        events.forEach { event ->
            val elapsed = event.time - window.start.toEpochMilli()
            val index = (elapsed / bucketMillis).toInt().coerceIn(0, BUCKET_COUNT - 1)
            counts[index]++
        }
        return counts.mapIndexed { index, count ->
            val start = window.start.plus(BUCKET_DURATION.multipliedBy(index.toLong()))
            TrendPoint(
                start = start,
                end = if (index == BUCKET_COUNT - 1) window.end else start.plus(BUCKET_DURATION),
                count = count
            )
        }
    }

    private fun magnitudeDistribution(events: List<StatisticsEvent>): List<DistributionBucket> {
        val counts = IntArray(5)
        events.forEach { event ->
            when {
                event.magnitude < 2.5 -> Unit
                event.magnitude < 3.0 -> counts[0]++
                event.magnitude < 4.0 -> counts[1]++
                event.magnitude < 5.0 -> counts[2]++
                event.magnitude < 6.0 -> counts[3]++
                else -> counts[4]++
            }
        }
        return MAGNITUDE_BUCKET_IDS.mapIndexed { index, id ->
            DistributionBucket(id, counts[index])
        }
    }

    private fun depthDistribution(events: List<StatisticsEvent>): List<DistributionBucket> {
        val counts = IntArray(3)
        events.forEach { event ->
            val depth = event.depthKm?.takeIf { it.isFinite() && it >= 0.0 } ?: return@forEach
            when {
                depth < 70.0 -> counts[0]++
                depth <= 300.0 -> counts[1]++
                else -> counts[2]++
            }
        }
        return DEPTH_BUCKET_IDS.mapIndexed { index, id ->
            DistributionBucket(id, counts[index])
        }
    }

    private fun activeRegions(events: List<StatisticsEvent>): List<ActiveRegion> {
        data class RegionCount(val displayName: String, var count: Int)

        val counts = linkedMapOf<String, RegionCount>()
        events.forEach { event ->
            val segments = event.place.split(',')
            if (segments.size < 2) return@forEach
            val displayName = segments.last().trim().takeIf(String::isNotEmpty) ?: return@forEach
            val key = displayName.lowercase(Locale.ROOT)
            counts.getOrPut(key) { RegionCount(displayName, 0) }.count++
        }
        return counts.values
            .map { ActiveRegion(it.displayName, it.count) }
            .sortedWith(compareByDescending<ActiveRegion> { it.count }.thenBy { it.name.lowercase(Locale.ROOT) })
            .take(MAX_ACTIVE_REGIONS)
    }

    private fun StatisticsLocation.isValid(): Boolean =
        latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0

    private fun StatisticsEvent.hasValidPosition(): Boolean =
        latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0

    private fun haversineDistanceKm(origin: StatisticsLocation, event: StatisticsEvent): Double {
        val latitudeDelta = Math.toRadians(event.latitude - origin.latitude)
        val longitudeDelta = Math.toRadians(event.longitude - origin.longitude)
        val a = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
            cos(Math.toRadians(origin.latitude)) * cos(Math.toRadians(event.latitude)) *
            sin(longitudeDelta / 2) * sin(longitudeDelta / 2)
        return EARTH_RADIUS_KM * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private companion object {
        const val BUCKET_COUNT = 30
        const val MAX_ACTIVE_REGIONS = 5
        const val NEARBY_RADIUS_KM = 1_000.0
        const val EARTH_RADIUS_KM = 6_371.0
        val BUCKET_DURATION: Duration = Duration.ofDays(1)
        val MAGNITUDE_BUCKET_IDS = listOf(
            "m2_5_2_9", "m3_0_3_9", "m4_0_4_9", "m5_0_5_9", "m6_plus"
        )
        val DEPTH_BUCKET_IDS = listOf("shallow", "intermediate", "deep")
    }
}
