package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state

import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.EarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity

data class EarthquakeListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val allEarthquakes: List<EQEntity> = emptyList(),
    val filteredEarthquakes: List<EQEntity> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val filterSettings: FilterSettings = FilterSettings(),
    val isFilterSheetVisible: Boolean = false,
    val selectedEarthquake: EarthquakeUI? = null,
    val error: AppError? = null
)

