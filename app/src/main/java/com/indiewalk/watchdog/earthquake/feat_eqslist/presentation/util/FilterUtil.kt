package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.util

import android.util.Log
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.toDouble
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.toLong
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity

object FilterUtil {
    fun filterList(
        filters: EqsSortOption,
        minMag: MinMagnitude,
        timeInterval: TimeInterval,
        eqsList: List<EQEntity>?
    ): List<EQEntity>? {
        var eqsListFiltered = when (filters.name) {
            EqsSortOption.MAG_DESC.name -> eqsList?.sortedByDescending { it.mag }
            EqsSortOption.MAG_ASC.name -> eqsList?.sortedBy { it.mag ?: 0.0 }
            EqsSortOption.DATE_ASC.name -> eqsList?.sortedBy { it.time }
            EqsSortOption.DATE_DESC.name -> eqsList?.sortedByDescending { it.time }
            EqsSortOption.DIST_ASC.name -> eqsList?.sortedBy { it.distanceFromUser }
            EqsSortOption.DIST_DESC.name -> eqsList?.sortedByDescending { it.distanceFromUser }
            else -> eqsList
        }?.toList()

        eqsListFiltered = eqsListFiltered?.filter { it.mag != null &&  it.mag >= minMag.toDouble() }
        eqsListFiltered = eqsListFiltered?.filter {
            Log.d("FilterUtil", "it.time: ${it.time}, timeInterval.toLong(): ${timeInterval.toLong()}")
            it.time != null &&  it.time >= timeInterval.toLong()
        }

        return eqsListFiltered
    }
}