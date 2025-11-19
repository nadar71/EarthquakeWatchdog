package com.indiewalk.watchdog.earthquake.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object FormatUtil {
    fun formatMag(mag: Double): String {
        return String.format(Locale.US, "%.1f", mag)
    }

    fun formatDistanceToInt(distance: Double): String {
        val nf = NumberFormat.getIntegerInstance()
        return nf.format(distance.roundToInt())
    }

    // Add a day in ol Date format
    fun addDays(date: Date, numDays: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DATE, numDays)
        return cal.time
    }

    // Past date by daysOffset
    fun oldDate(daysOffset: Int): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd ")
        val calReturn = Calendar.getInstance()
        calReturn.add(Calendar.DATE, -daysOffset)
        return dateFormat.format(calReturn.time)
    }

    // Format datetime in MMM dd, yyyy h:mm a from millisec
    fun formatDateTime(epochMs: Long?): String {
        if (epochMs == null) return ""
        val instant = Instant.ofEpochMilli(epochMs)
        val dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return dtf.format(instant).replace("AM", "am").replace("PM", "pm")
    }

    // Format date in MMM dd, yyyy from millisec
    fun formatDateFromMsec(epochMs: Long): String {
        val date = Date(epochMs)
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy")
        return dateFormatter.format(date)
    }


    fun formatInstantToUtcString(instant: Instant?): String {
        if (instant == null) return ""
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a", Locale.getDefault())
            .withZone(ZoneOffset.UTC)
        return formatter.format(instant).replace("AM", "am").replace("PM", "pm")
    }

    // Format time in h:mm a from millisec
    fun formatTimeFromMsec(epochMs: Long): String {
        val time = Date(epochMs)
        val timeFormatter = SimpleDateFormat("h:mm a")
        return timeFormatter.format(time)
    }
}