package com.indiewalk.watchdog.earthquake.core.data.repository

import android.content.Context
import com.indiewalk.watchdog.earthquake.core.domain.repository.FilterPreferencesRepository
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences.FilterPrefs
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.util.FilterUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FilterPreferencesRepository {

    override val filterSettingsFlow: Flow<FilterSettings> = FilterPrefs.filterSettingsFlow(context)

    override suspend fun updateFilters(
        sortOption: EqsSortOption,
        minMagnitude: MinMagnitude,
        timeInterval: TimeInterval
    ) {
        FilterPrefs.setSort(context, sortOption)
        FilterPrefs.setMinMag(context, minMagnitude)
        FilterPrefs.setTimeInterval(context, timeInterval)
        FilterPrefs.setFilterActiveCounts(
            context,
            FilterUtil.checkFilterActiveCounts(sortOption, minMagnitude, timeInterval)
        )
    }

    override suspend fun setStartDate(date: String) {
        FilterPrefs.setStartDate(context, date)
    }
}

