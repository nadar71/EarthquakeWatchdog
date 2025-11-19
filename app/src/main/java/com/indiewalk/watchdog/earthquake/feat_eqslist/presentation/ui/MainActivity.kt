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
import com.google.android.gms.ads.MobileAds
import com.indiewalk.watchdog.earthquake.EarthquakeApp
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationGraph
import com.indiewalk.watchdog.earthquake.core.presentation.preferences.AppPrefsViewModel
import com.indiewalk.watchdog.earthquake.core.presentation.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme
import com.indiewalk.watchdog.earthquake.feat_ads.util.ConsentManager
import com.indiewalk.watchdog.earthquake.feat_ads.util.RequestConfigurationUtils


@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {
    private val appPrefsViewModel: AppPrefsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set your test devices.
        RequestConfigurationUtils.setTestDeviceIds()

        // Check consent
        ConsentManager.requestConsent(this, this@MainActivity){ canRequestAds ->
            MobileAds.initialize(this)
            setContent {
                EarthquakeApp.canRequestAdsFlag = canRequestAds
                val settings by appPrefsViewModel.settings.collectAsStateWithLifecycle()

                // switch between System / Light / Dark
                // var themeMode by remember { mutableStateOf(ThemeMode.System) }

                val darkTheme = when (settings.mode) {
                    ThemeMode.System -> isSystemInDarkTheme()
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                }
                EQWatchdogTheme(darkTheme = darkTheme) {
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

}