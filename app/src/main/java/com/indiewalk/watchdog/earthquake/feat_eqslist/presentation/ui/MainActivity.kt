package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.MobileAds
import com.indiewalk.watchdog.earthquake.EarthquakeApp
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationHost
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.rememberAppNavigator
import com.indiewalk.watchdog.earthquake.core.performance.BenchmarkRuntime
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme
import com.indiewalk.watchdog.earthquake.feat_ads.util.ConsentManager
import com.indiewalk.watchdog.earthquake.feat_ads.util.RequestConfigurationUtils


@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BenchmarkRuntime.screenFactoryOrNull()?.let { benchmarkScreenFactory ->
            setContent {
                EQWatchdogTheme(darkTheme = false) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.semantics { testTagsAsResourceId = true }
                    ) {
                        val navigator = rememberAppNavigator()
                        AppNavigationHost(
                            navigator = navigator,
                            screenFactory = benchmarkScreenFactory
                        )
                    }
                }
            }
            return
        }

        // Set your test devices.
        RequestConfigurationUtils.setTestDeviceIds()

        // Check consent
        ConsentManager.requestConsent(this, this@MainActivity){ canRequestAds ->
            MobileAds.initialize(this)
            setContent {
                EarthquakeApp.canRequestAdsFlag = canRequestAds
                val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

                // switch between System / Light / Dark
                // var themeMode by remember { mutableStateOf(ThemeMode.System) }

                val darkTheme = when (settings.mode) {
                    ThemeMode.System -> isSystemInDarkTheme()
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                    else -> isSystemInDarkTheme()
                }
                EQWatchdogTheme(darkTheme = darkTheme) {
                    val navigator = rememberAppNavigator()
                    AppNavigationHost(navigator = navigator)
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
