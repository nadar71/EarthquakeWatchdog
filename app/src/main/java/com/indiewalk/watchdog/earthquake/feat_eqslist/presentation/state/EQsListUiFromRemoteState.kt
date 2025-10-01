package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.state

import eu.indiewalkabout.fridgemanager.core.domain.model.ErrorResponse

sealed class EQsListUiFromRemoteState<out T> {
    object Idle : EQsListUiFromRemoteState<Nothing>()
    object Loading : EQsListUiFromRemoteState<Nothing>()
    data class Success<out T>(val data: T) : EQsListUiFromRemoteState<T>()
    data class Error(val error: ErrorResponse) : EQsListUiFromRemoteState<Nothing>()
}