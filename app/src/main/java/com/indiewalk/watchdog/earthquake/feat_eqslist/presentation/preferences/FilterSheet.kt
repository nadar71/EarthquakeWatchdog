package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.FilterSettings
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.MinMagDropdown
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.PeriodDropdown
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.SortDropdown
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    eqsCount: Int = 0,
    lastRefreshTime: String = "",
    startDate: String = "",
    filterSettings: FilterSettings,
    onConfirm: (EqsSortOption, MinMagnitude, TimeInterval) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSort by remember(filterSettings.sortOption) { mutableStateOf(filterSettings.sortOption) }
    var selectedMinMag by remember(filterSettings.minMag) { mutableStateOf(filterSettings.minMag) }
    var selectedInterval by remember(filterSettings.timeInterval) {
        mutableStateOf(filterSettings.timeInterval)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .testTag("filter-sheet-content")
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
        ) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.filter_sort_by),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SortDropdown(
                modifier = Modifier.fillMaxWidth(),
                value = selectedSort,
                onChange = { selectedSort = it ?: EqsSortOption.DATE_DESC }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.filter_min_magnitude_label),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            MinMagDropdown(
                modifier = Modifier.fillMaxWidth(),
                value = selectedMinMag,
                onChange = { selectedMinMag = it ?: MinMagnitude.MAG_3_0 }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.filter_time_period_filter_label),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            PeriodDropdown(
                modifier = Modifier.fillMaxWidth(),
                value = selectedInterval,
                onChange = { selectedInterval = it }
            )

            Spacer(modifier = Modifier.height(12.dp))
            SettingsItem(
                title = stringResource(id = R.string.filter_last_refresh) + " :",
                subtitle = lastRefreshTime,
                isIconVisible = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            SettingsItem(
                title = stringResource(id = R.string.filter_start_date) + " :",
                subtitle = startDate,
                isIconVisible = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            SettingsItem(
                title = stringResource(id = R.string.filter_eq_count_from_start_date) + " :",
                subtitle = eqsCount.toString(),
                isIconVisible = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.generic_cancel))
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onConfirm(selectedSort, selectedMinMag, selectedInterval) }) {
                    Text(stringResource(R.string.generic_ok))
                }
            }
        }
    }
}
