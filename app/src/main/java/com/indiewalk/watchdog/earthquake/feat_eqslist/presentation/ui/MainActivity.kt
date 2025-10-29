package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.indiewalk.watchdog.earthquake.core.data.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.BottomBarDestinations
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationGraph
import com.indiewalk.watchdog.earthquake.core.presentation.theme.ThemeViewModel
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.EarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.previews.ThemeDemoScreen
import dagger.hilt.android.AndroidEntryPoint
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme


@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by themeViewModel.settings.collectAsStateWithLifecycle()

            // switch between System / Light / Dark
            // var themeMode by remember { mutableStateOf(ThemeMode.System) }

            /*val isDark = when (settings.mode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark  -> true
            }*/
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