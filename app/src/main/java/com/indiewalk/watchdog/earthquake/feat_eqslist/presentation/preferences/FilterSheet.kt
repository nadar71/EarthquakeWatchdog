package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.preferences.FilterPrefs
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.MinMagDropdown
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.PeriodDropdown
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.SortDropdown
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.util.FilterUtil.minMagFromDouble
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.util.FilterUtil.toDouble
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    filterViewModel: FilterViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // get current filter stored values
    val sort by filterViewModel.sortOption.collectAsStateWithLifecycle()
    val minMag by filterViewModel.minMagFlow.collectAsStateWithLifecycle()
    // val startDateStr by filterViewModel.startDate.collectAsStateWithLifecycle()
    val periodStr by filterViewModel.timeIntervalFlow.collectAsStateWithLifecycle()

    // local filter values
    var selectedSort by remember { mutableStateOf<EqsSortOption?>(sort) }
    var selectedMinMag by remember { mutableStateOf<MinMagnitude?>(minMag) }
    var selectedInterval by remember { mutableStateOf<TimeInterval>(periodStr) }

 /*   // Start date: stored as "yyyy-MM-dd" (empty means none)
    val iso = DateTimeFormatter.ISO_LOCAL_DATE
    var selectedStartDate by remember {
        mutableStateOf(
            startDateStr.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it, iso) }
        )
    }

    // Date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedStartDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )
    fun millisToLocalDate(ms: Long?): LocalDate? =
        ms?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
*/
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            // --- Sort by ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.filter_sort_by),
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(end = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                SortDropdown(
                    modifier = Modifier.weight(0.5f),
                    value = selectedSort,
                    onChange = { selectedSort = it }
                )
            }

            Spacer(Modifier.height(12.dp))

            // --- Min mag ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.filter_min_magnitude_label),
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(end = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                MinMagDropdown(
                    modifier = Modifier.weight(0.5f),
                    value = selectedMinMag,
                    onChange = { selectedMinMag = it }
                )
            }

            Spacer(Modifier.height(12.dp))

            // --- Start date ---
            // TODO: later impl: date, paging
            /*Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.filter_start_date_label),
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    modifier = Modifier
                        .weight(0.6f)
                        .clickable { showDatePicker = true },
                    value = selectedStartDate?.format(iso) ?: stringResource(R.string.filter_none),
                    onValueChange = {},
                    enabled = false,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null
                            )
                        }
                    }
                )
            }*/

            Spacer(Modifier.height(12.dp))

            // --- Period ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.filter_time_period_filter_label),
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(end = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                PeriodDropdown(
                    modifier = Modifier.weight(0.5f),
                    value = selectedInterval,
                    onChange = { selectedInterval = it }
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.generic_cancel))
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        // Debug : filter before saving
                        println("Filter before saving: ${FilterPrefs.debugPrintEqFilterDataStore(context)}")

                        // Saving here
                        selectedSort?.let { filterViewModel.setSort(it) }
                        selectedMinMag?.let { filterViewModel.setMinMag(it.toDouble()) }
                        filterViewModel.setTimeInterval(selectedInterval.name)
                        // filterViewModel.setStartDate(selectedStartDate?.format(iso) ?: "")

                        // debug: filter after saving
                        println("Filter after saving: ${FilterPrefs.debugPrintEqFilterDataStore(context)}")

                        onDismiss()
                    }
                }) {
                    Text(stringResource(R.string.generic_ok))
                }
            }
        }
    }

    /*if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedStartDate = millisToLocalDate(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text(stringResource(R.string.generic_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }*/
}








