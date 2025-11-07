package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums

import com.indiewalk.watchdog.earthquake.EarthquakeApp.Companion.appContext
import com.indiewalk.watchdog.earthquake.R
import java.util.Calendar

enum class TimeInterval(val value: String) {
    TODAY(appContext.getString(R.string.filter_date_period_today_label)),
    HOURS_24(appContext.getString(R.string.filter_date_period_24h_label)),
    HOURS_48(appContext.getString(R.string.filter_date_period_48h_label)),
    LAST_WEEK(appContext.getString(R.string.filter_date_period_week_label)),
    LAST_2_WEEKS(appContext.getString(R.string.filter_date_period_2_week_label)),
    LAST_30_DAYS(appContext.getString(R.string.filter_date_period_30_days_label));

    companion object {
        // from "LAST_30_DAYS" -> TimePeriod.LAST_30_DAYS item
        fun fromNameString(s: String?): TimeInterval =
            runCatching { TimeInterval.valueOf(s ?: "") }.getOrDefault(LAST_30_DAYS)
    }
}

fun TimeInterval.toLong(): Long  {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 1)  // 00:00:01
        set(Calendar.MILLISECOND, 0)
    }
    return when (this) {
        TimeInterval.TODAY -> calendar.timeInMillis
        TimeInterval.HOURS_24 -> System.currentTimeMillis() - 24L * 60 * 60 * 1000
        TimeInterval.HOURS_48 -> System.currentTimeMillis() - 48L * 60 * 60 * 1000
        TimeInterval.LAST_WEEK -> System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        TimeInterval.LAST_2_WEEKS -> System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000
        TimeInterval.LAST_30_DAYS -> System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
    }
}