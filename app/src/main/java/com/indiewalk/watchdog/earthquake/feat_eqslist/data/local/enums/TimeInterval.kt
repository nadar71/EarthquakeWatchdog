package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums

import java.util.Calendar

enum class TimeInterval {
    TODAY,
    HOURS_24,
    HOURS_48,
    LAST_WEEK,
    LAST_2_WEEKS,
    LAST_30_DAYS;

    companion object {
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
