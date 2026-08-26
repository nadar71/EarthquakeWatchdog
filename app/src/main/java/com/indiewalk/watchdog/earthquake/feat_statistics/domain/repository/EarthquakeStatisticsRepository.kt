package com.indiewalk.watchdog.earthquake.feat_statistics.domain.repository

import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsLoadResult
import kotlinx.coroutines.flow.Flow

interface EarthquakeStatisticsRepository {
    fun load(forceRefresh: Boolean): Flow<StatisticsLoadResult>
}
