package com.indiewalk.watchdog.earthquake.core.model

import com.indiewalk.watchdog.earthquake.core.data.enums.ThemeMode

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.System,
    // val dynamicColor: Boolean = true,
)