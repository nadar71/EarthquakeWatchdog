package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationGraph
import com.indiewalk.watchdog.earthquake.core.presentation.preferences.AppPrefsViewModel
import com.indiewalk.watchdog.earthquake.core.presentation.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme


@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {
    private val appPrefsViewModel: AppPrefsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by appPrefsViewModel.settings.collectAsStateWithLifecycle()

            // switch between System / Light / Dark
            var themeMode by remember { mutableStateOf(ThemeMode.System) }

            val isDark = when (settings.mode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark  -> true
            }
            EQWatchdogTheme {
                val navController = rememberNavController()
                NavigationGraph(navController = navController)
                // EarthquakeListScreen(navController)
            }

            /*EQWatchdogTheme(darkTheme = isDark, dynamicColor = settings.dynamicColor) {
                Surface {
                    ThemeDemoScreen(
                        mode = settings.mode,
                        dynamic = settings.dynamicColor,
                        onModeChange = themeViewModel::setMode,
                        onDynamicChange = themeViewModel::setDynamic
                    )
                }
            }*/
        }

    }

}