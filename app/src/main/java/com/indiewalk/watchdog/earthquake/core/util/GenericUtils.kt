package com.indiewalk.watchdog.earthquake.core.util

import android.app.Activity
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
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.indiewalk.watchdog.earthquake.R

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Calendar
import java.util.Locale
import androidx.core.graphics.createBitmap

object GenericUtils {


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
            Log.d("openAppStore", "store uri: $marketUri_01")
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    marketUri_01
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            try {
                Log.d("openAppStore", "store uri: $marketUri_02")
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        marketUri_02
                    )
                )
            } catch (e: ActivityNotFoundException){
                Log.e("OpenAppStore", "Error opening app store", e)
            }
        }
    }

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

    fun formatMag(mag: Double): String {
        return String.format(Locale.US, "%.1f", mag)
    }

    // @SuppressLint("NewApi")
    fun formatDate(epochMs: Long?): String {
        if (epochMs == null) return ""
        val instant = Instant.ofEpochMilli(epochMs)
        val dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return dtf.format(instant).replace("AM", "am").replace("PM", "pm")
    }


    // Format date in a specific way and millisec  format
    fun formatDateFromMsec(dateMillisec: Long): String {
        // Date
        val date = Date(dateMillisec)
        println("date : $date")
        // Format Date
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy")
        return dateFormatter.format(date)
    }


    // Format time in a specific way and millisec  format
    fun formatTimeFromMsec(dateMillisec: Long): String {
        // Time
        val time = Date(dateMillisec)
        println("time : $time")
        // Format Time
        val timeFormatter = SimpleDateFormat("h:mm a")
        return timeFormatter.format(time)
    }


    // Extract only the digit with "." from a String
    fun returnDigit(s: String): String {
        return s.replace("[^0-9?!\\.]+".toRegex(), "")
    }


    // Extract only the char from a String
    fun returnChar(s: String): String {
        return s.replace("[0-9]+".toRegex(), "")
    }

    // Return Past date by daysOffset
    fun oldDate(daysOffset: Int): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd ")
        val calReturn = Calendar.getInstance()
        calReturn.add(Calendar.DATE, -daysOffset)
        return dateFormat.format(calReturn.time)
    }

    // Restart current activity
    fun restartActivity(activity: Activity) {
        val mIntent = activity.intent
        activity.finish()
        activity.startActivity(mIntent)
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


    // Add a day in ol Date format
    fun addDays(date: Date, numDays: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DATE, numDays)
        return cal.time
    }


}
