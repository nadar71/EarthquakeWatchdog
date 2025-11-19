package com.indiewalk.watchdog.earthquake.feat_ads.presentation

import android.R.attr.maxWidth
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.indiewalk.watchdog.earthquake.EarthquakeApp



@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AdMobBannerView(
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111", // test id
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    if (!EarthquakeApp.canRequestAdsFlag) return

    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier) {
        val maxWidthDp = with(LocalDensity.current) { maxWidth } // width available to the banner
        // val adWidthDp = maxWidthDp / LocalDensity.current.density // convert px -> dp if needed

        // Defensive: if width not known yet, skip for now
        if (maxWidth.value <= 0f) return@BoxWithConstraints

        // Compute adaptive size using available width (in dp, as int)
        val adSize = remember(maxWidth) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context,
                maxWidth.value.toInt() // this is dp already in BoxWithConstraints
            )
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(), // no explicit height: let AdView measure itself
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdUnitId(adUnitId)
                    setAdSize(adSize)
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { adView ->
                // If constraints changed and size differs, update
                if (adView.adSize != adSize) {
                    adView.setAdSize(adSize)
                    adView.loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

/*
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(60.dp),  // Default height as fallback
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // test unit by default
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.roundToPx() }

    val adSize = remember(screenWidthPx) {
        try {
            // Use the screen width in pixels to get the adaptive banner size
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthPx)
                ?: AdSize.BANNER
        } catch (e: Exception) {
            // Fallback to a standard banner size if adaptive fails
            AdSize.BANNER
        }
    }

    // Convert the ad size height from pixels to Dp
    val adHeightDp = with(LocalDensity.current) {
        adSize.getHeightInPixels(context).toDp()
    }

    if (EarthquakeApp.canRequestAdsFlag) {
        AndroidView(
            modifier = modifier.height(adHeightDp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(adSize)
                    setAdUnitId(adUnitId)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    } else {
        AdBannerPlaceholder(
            modifier = modifier
                .fillMaxWidth()
                .height(adHeightDp)
        )
    }
}

private fun AdSize.getHeightInPixels(context: Context): Int {
    return when (this) {
        AdSize.BANNER -> 50
        AdSize.LARGE_BANNER -> 100
        AdSize.MEDIUM_RECTANGLE -> 250
        AdSize.FULL_BANNER -> 60
        AdSize.LEADERBOARD -> 90
        else -> {
            // For adaptive banners, return a reasonable default height in pixels
            100
        }
    }.let { dpValue ->
        // Convert dp to pixels
        (dpValue * context.resources.displayMetrics.density).toInt()
    }
}*/
