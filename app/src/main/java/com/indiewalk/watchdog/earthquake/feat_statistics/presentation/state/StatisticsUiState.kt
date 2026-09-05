package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.state

import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSection
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSnapshot

data class StatisticsUiState(
    val snapshot: StatisticsSnapshot? = null,
    val settings: AppSettings = AppSettings(),
    val unavailableSections: Set<StatisticsSection> = emptySet(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isFromCache: Boolean = false,
    val isStale: Boolean = false,
    val error: AppError? = null
) {
    val hasContent: Boolean
        get() = snapshot != null
}
