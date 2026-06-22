package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.state

import com.google.android.gms.maps.model.LatLng
import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import com.indiewalk.watchdog.earthquake.core.model.preferences.AppSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity

data class MapUiState(
    val isLoading: Boolean = true,
    val earthquakes: List<EQEntity> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val hasLocationPermission: Boolean = false,
    val recenterTarget: LatLng? = null,
    val error: AppError? = null
)

