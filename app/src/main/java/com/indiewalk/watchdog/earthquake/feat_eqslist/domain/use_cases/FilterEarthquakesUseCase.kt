package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.use_cases

import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.toDouble
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.toLong
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import javax.inject.Inject

class FilterEarthquakesUseCase @Inject constructor(
    private val applySortUseCase: ApplySortUseCase
) {
    operator fun invoke(
        items: List<EQEntity>,
        filterSettings: FilterSettings
    ): List<EQEntity> {
        return applySortUseCase.invoke(items, filterSettings.sortOption)
            .filterByMagnitude(filterSettings.minMag)
            .filterByTimeInterval(filterSettings.timeInterval)
    }

    private fun List<EQEntity>.filterByMagnitude(minMagnitude: MinMagnitude): List<EQEntity> {
        val threshold = minMagnitude.toDouble()
        return filter { it.mag != null && it.mag >= threshold }
    }

    private fun List<EQEntity>.filterByTimeInterval(timeInterval: TimeInterval): List<EQEntity> {
        val cutoff = timeInterval.toLong()
        return filter { it.time != null && it.time >= cutoff }
    }
}

