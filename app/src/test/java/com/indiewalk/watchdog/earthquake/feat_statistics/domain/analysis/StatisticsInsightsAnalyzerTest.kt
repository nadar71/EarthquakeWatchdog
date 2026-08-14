package com.indiewalk.watchdog.earthquake.feat_statistics.domain.analysis

import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsLocation
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsPeriod
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant

class StatisticsInsightsAnalyzerTest {

    private val end = Instant.parse("2026-08-14T12:00:00Z")
    private val window = StatisticsWindow(
        period = StatisticsPeriod.LAST_30_DAYS,
        start = end.minus(Duration.ofDays(30)),
        end = end
    )
    private val analyzer = StatisticsInsightsAnalyzer()

    @Test
    fun globalTrendAlwaysContainsThirtyOrderedZeroFilledBuckets() {
        val result = analyzer.analyze(
            events = listOf(
                event("first", time = window.start.toEpochMilli()),
                event("middle", time = window.start.plus(Duration.ofDays(10)).toEpochMilli()),
                event("last", time = window.end.toEpochMilli()),
                event("outside", time = window.start.minusMillis(1).toEpochMilli())
            ),
            window = window,
            origin = null
        )

        assertEquals(30, result.globalTrend.size)
        assertEquals(window.start, result.globalTrend.first().start)
        assertEquals(window.end, result.globalTrend.last().end)
        assertEquals(1, result.globalTrend[0].count)
        assertEquals(1, result.globalTrend[10].count)
        assertEquals(1, result.globalTrend[29].count)
        assertEquals(3, result.globalTrend.sumOf { it.count })
    }

    @Test
    fun magnitudeDistributionUsesLowerInclusiveBucketsAndSkipsInvalidValues() {
        val result = analyzer.analyze(
            events = listOf(
                event("m25", magnitude = 2.5),
                event("m299", magnitude = 2.99),
                event("m30", magnitude = 3.0),
                event("m40", magnitude = 4.0),
                event("m50", magnitude = 5.0),
                event("m60", magnitude = 6.0),
                event("nan", magnitude = Double.NaN),
                event("below", magnitude = 2.49)
            ),
            window = window,
            origin = null
        )

        assertEquals(
            listOf("m2_5_2_9", "m3_0_3_9", "m4_0_4_9", "m5_0_5_9", "m6_plus"),
            result.magnitudeDistribution.map { it.id }
        )
        assertEquals(listOf(2, 1, 1, 1, 1), result.magnitudeDistribution.map { it.count })
    }

    @Test
    fun depthDistributionTreatsSeventyAndThreeHundredAsIntermediate() {
        val result = analyzer.analyze(
            events = listOf(
                event("shallow", depthKm = 69.99),
                event("seventy", depthKm = 70.0),
                event("three-hundred", depthKm = 300.0),
                event("deep", depthKm = 300.01),
                event("negative", depthKm = -1.0),
                event("missing", depthKm = null)
            ),
            window = window,
            origin = null
        )

        assertEquals(
            listOf("shallow", "intermediate", "deep"),
            result.depthDistribution.map { it.id }
        )
        assertEquals(listOf(1, 2, 1), result.depthDistribution.map { it.count })
    }

    @Test
    fun activeRegionsAreNormalizedRankedAndLimitedDeterministically() {
        val result = analyzer.analyze(
            events = listOf(
                event("1", place = "10 km SW of Rome, Italy"),
                event("2", place = "Northern Italy, italy"),
                event("3", place = "Tokyo, Japan"),
                event("4", place = "Osaka, JAPAN"),
                event("5", place = "Athens, Greece"),
                event("6", place = "Lima, Peru"),
                event("7", place = "Santiago, Chile"),
                event("8", place = "Quito, Ecuador"),
                event("unknown", place = "Unparseable place")
            ),
            window = window,
            origin = null
        )

        assertEquals(
            listOf("Italy" to 2, "Japan" to 2, "Chile" to 1, "Ecuador" to 1, "Greece" to 1),
            result.activeRegions.map { it.name to it.count }
        )
    }

    @Test
    fun nearbyTrendIncludesEventsAtOneThousandKilometersAndExcludesFartherEvents() {
        val result = analyzer.analyze(
            events = listOf(
                event("near", latitude = 0.0, longitude = 8.99),
                event("far", latitude = 0.0, longitude = 9.1)
            ),
            window = window,
            origin = StatisticsLocation(0.0, 0.0)
        )

        assertEquals(1, requireNotNull(result.nearbyTrend).sumOf { it.count })
    }

    @Test
    fun missingOrInvalidOriginOnlyMakesNearbyTrendUnavailable() {
        val missing = analyzer.analyze(emptyList(), window, null)
        val invalid = analyzer.analyze(emptyList(), window, StatisticsLocation(Double.NaN, 0.0))

        assertEquals(30, missing.globalTrend.size)
        assertNull(missing.nearbyTrend)
        assertNull(invalid.nearbyTrend)
    }

    private fun event(
        id: String,
        magnitude: Double = 3.0,
        place: String = "Rome, Italy",
        time: Long = end.minus(Duration.ofHours(1)).toEpochMilli(),
        depthKm: Double? = 10.0,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) = StatisticsEvent(
        id = id,
        magnitude = magnitude,
        place = place,
        time = time,
        depthKm = depthKm,
        latitude = latitude,
        longitude = longitude
    )
}
