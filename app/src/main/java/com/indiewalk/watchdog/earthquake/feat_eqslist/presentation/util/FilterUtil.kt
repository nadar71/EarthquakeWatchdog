package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.util

import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval

object FilterUtil {
    fun checkFilterActiveCounts(
        selectedSort: EqsSortOption?,
        selectedMinMag: MinMagnitude?,
        selectedInterval: TimeInterval
    ): Int {
        var counts = 0
        if (selectedSort != EqsSortOption.DATE_DESC ) counts++
        if (selectedMinMag != MinMagnitude.MAG_3_0 ) counts++
        if (selectedInterval != TimeInterval.LAST_30_DAYS ) counts++
        return counts
    }
}
