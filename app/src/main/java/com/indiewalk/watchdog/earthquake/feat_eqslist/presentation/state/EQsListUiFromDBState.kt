package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state

import eu.indiewalkabout.fridgemanager.core.domain.model.ErrorResponse

sealed class EQsListUiFromDBState<out T> {
    object Idle : EQsListUiFromDBState<Nothing>()
    object Loading : EQsListUiFromDBState<Nothing>()
    data class Success<out T>(val data: T) : EQsListUiFromDBState<T>()
    data class Error(val error: ErrorResponse) : EQsListUiFromDBState<Nothing>()
}