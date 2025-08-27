package com.indiewalk.watchdog.earthquake.core.data.enums

import com.indiewalk.watchdog.earthquake.EarthquakeApp.Companion.appContext
import com.indiewalk.watchdog.earthquake.R

enum class TimePeriod(val value: String) {
    TODAY(appContext.getString(R.string.settings_date_period_today_label)),
    HOURS_24(appContext.getString(R.string.settings_date_period_24h_label)),
    HOURS_48(appContext.getString(R.string.settings_date_period_48h_label)),
    LAST_WEEK(appContext.getString(R.string.settings_date_period_week_label)),
    LAST_2_WEEKS(appContext.getString(R.string.settings_date_period_2_week_label));

    companion object {
        fun fromName(name: String?): TimePeriod? {
            return try {
                name?.let { valueOf(it) }
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}