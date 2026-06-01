package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval

@Composable
fun EqsSortOption.label(): String = stringResource(
    when (this) {
        EqsSortOption.MAG_DESC -> R.string.filter_sort_mag_desc
        EqsSortOption.MAG_ASC -> R.string.filter_sort_mag_asc
        EqsSortOption.DATE_ASC -> R.string.filter_sort_date_asc
        EqsSortOption.DATE_DESC -> R.string.filter_sort_date_desc
        EqsSortOption.DIST_ASC -> R.string.filter_sort_dist_asc
        EqsSortOption.DIST_DESC -> R.string.filter_sort_dist_desc
    }
)

@Composable
fun MinMagnitude.label(): String = stringResource(
    when (this) {
        MinMagnitude.MAG_0_0 -> R.string.filter_0_0_min_magnitude_label
        MinMagnitude.MAG_1_0 -> R.string.filter_1_0_min_magnitude_label
        MinMagnitude.MAG_2_0 -> R.string.filter_2_0_min_magnitude_label
        MinMagnitude.MAG_3_0 -> R.string.filter_3_0_min_magnitude_label
        MinMagnitude.MAG_4_0 -> R.string.filter_4_0_min_magnitude_label
        MinMagnitude.MAG_4_5 -> R.string.filter_4_5_min_magnitude_label
        MinMagnitude.MAG_5_0 -> R.string.filter_5_0_min_magnitude_label
        MinMagnitude.MAG_5_5 -> R.string.filter_5_5_min_magnitude_label
        MinMagnitude.MAG_6_0 -> R.string.filter_6_0_min_magnitude_label
        MinMagnitude.MAG_6_5 -> R.string.filter_6_5_min_magnitude_label
    }
)

@Composable
fun TimeInterval.label(): String = stringResource(
    when (this) {
        TimeInterval.TODAY -> R.string.filter_date_period_today_label
        TimeInterval.HOURS_24 -> R.string.filter_date_period_24h_label
        TimeInterval.HOURS_48 -> R.string.filter_date_period_48h_label
        TimeInterval.LAST_WEEK -> R.string.filter_date_period_week_label
        TimeInterval.LAST_2_WEEKS -> R.string.filter_date_period_2_week_label
        TimeInterval.LAST_30_DAYS -> R.string.filter_date_period_30_days_label
    }
)
