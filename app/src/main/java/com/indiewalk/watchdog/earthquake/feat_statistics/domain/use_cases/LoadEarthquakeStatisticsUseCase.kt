package com.indiewalk.watchdog.earthquake.feat_statistics.domain.use_cases

import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsLoadResult
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.repository.EarthquakeStatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadEarthquakeStatisticsUseCase @Inject constructor(
    private val repository: EarthquakeStatisticsRepository
) {
    operator fun invoke(forceRefresh: Boolean): Flow<StatisticsLoadResult> =
        repository.load(forceRefresh)
}
