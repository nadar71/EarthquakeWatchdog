package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences.FilterPrefs
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel
class FilterViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val sortOption: StateFlow<EqsSortOption> =
        FilterPrefs.sortFlow(context)
            .stateIn(viewModelScope, SharingStarted.Eagerly, EqsSortOption.DATE_DESC)

    val minMagFlow: StateFlow<MinMagnitude> = FilterPrefs.minMagFlow(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, MinMagnitude.MAG_3_0)

    val timeIntervalFlow: StateFlow<TimeInterval> = FilterPrefs.timeIntervalFlow(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, TimeInterval.LAST_30_DAYS)

    fun setSort(option: EqsSortOption) {
        viewModelScope.launch {
            FilterPrefs.setSort(context, option)
        }
    }

    fun setMinMag(mag: MinMagnitude) {
        viewModelScope.launch {
            FilterPrefs.setMinMag(context, mag)
        }
    }

    val startDate: StateFlow<String> =
        FilterPrefs.startDateFlow(context)
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun setStartDate(date: String) {
        viewModelScope.launch {
            FilterPrefs.setStartDate(context, date)
        }
    }

    val endDate: StateFlow<String> =
        FilterPrefs.endDateFlow(context)
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun setEndDate(date: String) {
        viewModelScope.launch {
            FilterPrefs.setEndDate(context, date)
        }
    }

    fun setTimeInterval(interval: TimeInterval) {
        viewModelScope.launch {
            FilterPrefs.setTimeInterval(context, interval)
        }
    }
}
