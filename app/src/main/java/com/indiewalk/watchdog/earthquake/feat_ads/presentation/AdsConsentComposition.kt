package com.indiewalk.watchdog.earthquake.feat_ads.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.indiewalk.watchdog.earthquake.feat_ads.domain.AdsConsentState

val LocalAdsConsentState = compositionLocalOf { AdsConsentState() }

@Composable
internal fun ConsentAwareAdContent(
    consentState: AdsConsentState,
    content: @Composable () -> Unit
) {
    if (consentState.shouldLoadAds) {
        content()
    }
}
