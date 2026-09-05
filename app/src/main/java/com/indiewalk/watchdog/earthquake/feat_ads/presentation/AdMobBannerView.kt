package com.indiewalk.watchdog.earthquake.feat_ads.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBannerView(
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111",
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    ConsentAwareAdContent(consentState = LocalAdsConsentState.current) {
        AdMobBannerContent(adUnitId = adUnitId, modifier = modifier)
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun AdMobBannerContent(adUnitId: String, modifier: Modifier) {
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier) {
        if (maxWidth.value <= 0f) return@BoxWithConstraints

        val adSize = remember(maxWidth) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context,
                maxWidth.value.toInt()
            )
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { viewContext ->
                AdView(viewContext).apply {
                    setAdUnitId(adUnitId)
                    setAdSize(adSize)
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { adView ->
                if (adView.adSize != adSize) {
                    adView.setAdSize(adSize)
                    adView.loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
