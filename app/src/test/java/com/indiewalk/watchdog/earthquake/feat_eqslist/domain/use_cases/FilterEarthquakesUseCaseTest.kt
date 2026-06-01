package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases

import com.indiewalk.watchdog.earthquake.sampleEqEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterEarthquakesUseCaseTest {
    private val useCase = FilterEarthquakesUseCase(ApplySortUseCase())

    @Test
    fun `filters by min magnitude and sorts by descending magnitude`() {
        val items = listOf(
            sampleEqEntity(id = "small", mag = 2.5, time = System.currentTimeMillis()),
            sampleEqEntity(id = "big", mag = 6.0, time = System.currentTimeMillis()),
            sampleEqEntity(id = "mid", mag = 4.0, time = System.currentTimeMillis())
        )

        val result = useCase(
            items,
            FilterSettings(
                sortOption = EqsSortOption.MAG_DESC,
                minMag = MinMagnitude.MAG_3_0,
                timeInterval = TimeInterval.LAST_30_DAYS
            )
        )

        assertEquals(listOf("big", "mid"), result.map { it.id })
    }

    @Test
    fun `filters out earthquakes older than selected interval`() {
        val now = System.currentTimeMillis()
        val items = listOf(
            sampleEqEntity(id = "recent", time = now - 1_000L),
            sampleEqEntity(id = "old", time = now - 40L * 24 * 60 * 60 * 1000)
        )

        val result = useCase(
            items,
            FilterSettings(timeInterval = TimeInterval.LAST_30_DAYS)
        )

        assertEquals(listOf("recent"), result.map { it.id })
    }
}

