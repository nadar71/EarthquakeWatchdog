package com.indiewalk.watchdog.earthquake.core.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraAzure_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraAzure_light
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

object GraphicsUtil {


    // Converts a vector or PNG drawable to a BitmapDescriptor
    fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes drawableRes: Int,
        @ColorInt tintArgb: Int? = null
    ): BitmapDescriptor {
        val drawable: Drawable = requireNotNull(ContextCompat.getDrawable(context, drawableRes)) {
            "Drawable $drawableRes not found"
        }
        if (tintArgb != null) {
            DrawableCompat.setTint(drawable, tintArgb)
            // drawable.alpha = (alphaFloat * 255).roundToInt()
        }

        val bitmap = createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1)
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // Return specific color value for specific magnitude values
    fun getMagnitudeColor(magnitude: Double, context: Context): Int {
        val mag = magnitude.toInt()
        Log.i("getMagnitudeColor", "Color: $mag")
        when (mag) {
            1, 0 -> return ContextCompat.getColor(context, R.color.magnitude1)
            2 -> return ContextCompat.getColor(context, R.color.magnitude2)
            3 -> return ContextCompat.getColor(context, R.color.magnitude3)
            4 -> return ContextCompat.getColor(context, R.color.magnitude4)
            5 -> return ContextCompat.getColor(context, R.color.magnitude5)
            6 -> return ContextCompat.getColor(context, R.color.magnitude6)
            7 -> return ContextCompat.getColor(context, R.color.magnitude7)
            8 -> return ContextCompat.getColor(context, R.color.magnitude8)
            9 -> return ContextCompat.getColor(context, R.color.magnitude9)
            10 -> return ContextCompat.getColor(context, R.color.magnitude10plus)
            else -> {
            }
        }
        return -1
    }


    // Return specific vector image for specific magnitude values
    fun getMagnitudeImg(magnitude: Double, context: Context): Drawable? {
        val mag = magnitude.toInt()
        Log.i("getMagnitudeColor", "Color: $mag")
        when (mag) {
            1, 0 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_1)
            2 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_2)
            3 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_3)
            4 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_4)
            5 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_5)
            6 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_6)
            7 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_7)
            8 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_8)
            9 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_9)
            10 -> return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer_10)
            else -> {
            }
        }
        return ContextCompat.getDrawable(context, R.drawable.ic_earthquake_pointer)
    }

    fun magnitudeColors(mag: Double): Pair<Color, Color> {
        return when {
            mag < 1.5 -> extraAzure_light to extraAzure_dark     // azure
            mag < 2.5 -> extraGreen_light   to extraGreen_dark   // green
            mag < 4.5 -> extraYellow_light  to extraYellow_dark  // yellow
            mag < 6.0 -> extraOrange_light  to extraOrange_dark  // orange
            mag < 7.0 -> extraRed_light     to extraRed_dark     // red
            else      -> extraDeepRed_light to extraDeepRed_dark // deep red
        }
    }


    fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    fun Dp.toPx(context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (this.value * density).toInt()
    }

}