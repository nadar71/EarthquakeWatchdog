package com.indiewalk.watchdog.earthquake.core.data.local.enums

import android.renderscript.Sampler.Value.NEAREST
import androidx.annotation.StringRes
import com.indiewalk.watchdog.earthquake.EarthquakeApp.Companion.appContext
import com.indiewalk.watchdog.earthquake.R

enum class OrderType(val value: String) {
    DESC_MAGNITUDE(appContext.getString(R.string.order_by_desc_magnitude)),
    ASC_MAGNITUDE(appContext.getString(R.string.order_by_asc_magnitude)),
    MOST_RECENT(appContext.getString(R.string.order_by_most_recent)),
    OLDEST(appContext.getString(R.string.order_by_oldest)),
    NEAREST(appContext.getString(R.string.order_by_nearest)),
    FURTHEST(appContext.getString(R.string.order_by_furthest));
}