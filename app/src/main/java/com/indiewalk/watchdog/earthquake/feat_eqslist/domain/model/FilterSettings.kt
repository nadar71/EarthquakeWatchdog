package com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model

import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval

data class FilterSettings(
    val sortOption: EqsSortOption = EqsSortOption.DATE_DESC,
    val minMag: MinMagnitude = MinMagnitude.MAG_3_0,
    val timeInterval: TimeInterval = TimeInterval.LAST_30_DAYS,
    val startDate: String = "",
    val endDate: String = "",
    val activeCounts: Int = 0
)
