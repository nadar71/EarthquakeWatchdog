package com.indiewalk.watchdog.earthquake.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.diagnostics.AppDiagnostics
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticCategory

import androidx.core.graphics.createBitmap
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraDeepRed_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraDeepRed_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraGreen_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraGreen_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraOrange_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraOrange_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraRed_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraRed_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraYellow_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraYellow_light

object GenericUtil {

    @Composable
    fun openUrlInBrowser(url: String) {
        val uriHandler = LocalUriHandler.current
        uriHandler.openUri(url)
    }

    fun openUrlInBrowserNotCompose(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun openAppStore(context: Context,appPackageName: String) {
        val context = context
        val marketUri_01 = Uri.parse("market://details?id=$appPackageName")
        val marketUri_02 = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")

        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    marketUri_01
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        marketUri_02
                    )
                )
            } catch (e: ActivityNotFoundException) {
                AppDiagnostics.recordNonFatal(DiagnosticCategory.EXTERNAL_INTENT, e)
            }
        }
    }

    // Extract only the digit with "." from a String
    fun returnDigit(s: String): String {
        return s.replace("[^0-9?!\\.]+".toRegex(), "")
    }

    // char from a String
    fun returnChar(s: String): String {
        return s.replace("[0-9]+".toRegex(), "")
    }


}
