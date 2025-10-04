package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indiewalk.watchdog.earthquake.core.data.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.presentation.theme.ThemeViewModel
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.EarthquakeUI
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.previews.ThemeDemoScreen
import dagger.hilt.android.AndroidEntryPoint
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme


@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    private var lastUpdate: String? = ""
    private var earthquakeUIS: List<EarthquakeUI>? = null
    // Preferences value
    private var minMagnitude: String? = null
    private var orderBy: String? = null
    private var lat_s: String? = null
    private var lng_s: String? = null
    private var dateFilter: String? = null
    private lateinit var dateFilterLabel: String
    private var location_address: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by themeViewModel.settings.collectAsStateWithLifecycle()

            // switch between System / Light / Dark
            // var themeMode by remember { mutableStateOf(ThemeMode.System) }

            val isDark = when (settings.mode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark  -> true
            }
            EQWatchdogTheme {
                EarthquakeListScreen()
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