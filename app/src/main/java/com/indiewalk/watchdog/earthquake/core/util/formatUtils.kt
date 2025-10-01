package com.indiewalk.watchdog.earthquake.core.util

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object formatUtils {
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

    fun formatDistanceToInt(distance: Double): String {
        val nf = NumberFormat.getIntegerInstance()
        return nf.format(distance.roundToInt())
    }
}