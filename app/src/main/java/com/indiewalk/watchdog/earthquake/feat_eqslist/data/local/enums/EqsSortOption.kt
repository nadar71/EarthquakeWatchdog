package com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums

import com.indiewalk.watchdog.earthquake.EarthquakeApp
import com.indiewalk.watchdog.earthquake.R

enum class EqsSortOption(val value: String) {
    MAG_DESC (EarthquakeApp.Companion.appContext.getString(R.string.filter_sort_mag_desc)),   // greatest → smallest magnitude
    MAG_ASC  (EarthquakeApp.Companion.appContext.getString(R.string.filter_sort_mag_asc)),    // smallest → greatest magnitude
    DATE_ASC (EarthquakeApp.Companion.appContext.getString(R.string.filter_sort_date_asc)),   // oldest → newest
    DATE_DESC(EarthquakeApp.Companion.appContext.getString(R.string.filter_sort_date_desc)),  // newest → oldest
    DIST_ASC (EarthquakeApp.Companion.appContext.getString(R.string.filter_sort_dist_asc)),   // nearest → furthest
    DIST_DESC(EarthquakeApp.Companion.appContext.getString(R.string.filter_sort_dist_desc));  // furthest → nearest

    companion object {
        fun fromString(s: String?): EqsSortOption =
            runCatching { valueOf(s ?: "") }.getOrDefault(DATE_DESC)
    }
}