package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences.FilterPrefs
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.preferences.EqsSortOption
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

    fun setSort(option: EqsSortOption) {
        viewModelScope.launch {
            FilterPrefs.setSort(context, option)
        }
    }
}
