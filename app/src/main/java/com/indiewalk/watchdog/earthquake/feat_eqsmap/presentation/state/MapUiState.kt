package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.state

import eu.indiewalkabout.fridgemanager.core.domain.model.ErrorResponse

sealed class MapUiState<out T> {
    object Loading : MapUiState<Nothing>()
    data class Success<out T>(val data: T) : MapUiState<T>()
    data class Error(val error: ErrorResponse) : MapUiState<Nothing>()
}