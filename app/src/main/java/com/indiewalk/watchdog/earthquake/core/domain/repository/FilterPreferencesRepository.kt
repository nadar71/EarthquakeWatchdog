package com.indiewalk.watchdog.earthquake.core.domain.repository

import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import kotlinx.coroutines.flow.Flow

interface FilterPreferencesRepository {
    val filterSettingsFlow: Flow<FilterSettings>

    suspend fun updateFilters(
        sortOption: EqsSortOption,
        minMagnitude: MinMagnitude,
        timeInterval: TimeInterval
    )

    suspend fun setStartDate(date: String)
}

