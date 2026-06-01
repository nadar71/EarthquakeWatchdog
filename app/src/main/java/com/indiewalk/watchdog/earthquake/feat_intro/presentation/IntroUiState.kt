package com.indiewalk.watchdog.earthquake.feat_intro.presentation

data class IntroUiState(
    val askedOnce: Boolean = false,
    val isPermissionRequestStarted: Boolean = false,
    val showDeniedDialog: Boolean = false
)

sealed interface IntroEffect {
    data object NavigateHome : IntroEffect
    data object OpenAppSettings : IntroEffect
}

