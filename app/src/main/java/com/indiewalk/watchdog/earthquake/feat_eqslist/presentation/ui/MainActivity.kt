package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationHost
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.rememberAppNavigator
import com.indiewalk.watchdog.earthquake.core.performance.BenchmarkRuntime
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme
import com.indiewalk.watchdog.earthquake.feat_ads.data.ConsentClientFactory
import com.indiewalk.watchdog.earthquake.feat_ads.domain.AdsConsentCoordinator
import com.indiewalk.watchdog.earthquake.feat_ads.presentation.LocalAdsConsentState
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var adsConsentCoordinator: AdsConsentCoordinator

    @Inject
    lateinit var consentClientFactory: ConsentClientFactory

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

        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val adsConsentState by adsConsentCoordinator.state.collectAsStateWithLifecycle()

            val darkTheme = when (settings.mode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                else -> isSystemInDarkTheme()
            }
            EQWatchdogTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalAdsConsentState provides adsConsentState) {
                    val navigator = rememberAppNavigator()
                    AppNavigationHost(
                        navigator = navigator,
                        onManageAdPrivacy = {
                            adsConsentCoordinator.showPrivacyOptions(
                                consentClientFactory.create(this@MainActivity)
                            )
                        }
                    )
                }
            }
        }

        adsConsentCoordinator.requestConsent(consentClientFactory.create(this))
    }
}
