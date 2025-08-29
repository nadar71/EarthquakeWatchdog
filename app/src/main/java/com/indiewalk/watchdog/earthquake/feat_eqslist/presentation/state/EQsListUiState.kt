package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state

import eu.indiewalkabout.fridgemanager.core.domain.model.ErrorResponse

sealed class EQsListUiState<out T> {
    object Idle : EQsListUiState<Nothing>()
    object Loading : EQsListUiState<Nothing>()
    data class Success<out T>(val data: T) : EQsListUiState<T>()
    data class Error(val error: ErrorResponse) : EQsListUiState<Nothing>()
}