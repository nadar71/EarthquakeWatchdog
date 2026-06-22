package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.MinMagnitude
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.TimeInterval
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.enums.EqsSortOption
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.util.label


// default : DATE_DESC : newest -> oldest
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    modifier: Modifier = Modifier,
    value: EqsSortOption?,
    onChange: (EqsSortOption?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val items = EqsSortOption.entries.toList()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = value?.label() ?: EqsSortOption.DATE_DESC.label(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            /*DropdownMenuItem(
                text = { Text(labelNone) },
                onClick = { onChange(null); expanded = false }
            )*/
            items.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.label()) },
                    onClick = { onChange(opt); expanded = false }
                )
            }
        }
    }
}

// default : MAG_3_0
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinMagDropdown(
    modifier: Modifier = Modifier,
    value: MinMagnitude?,
    onChange: (MinMagnitude?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val items = MinMagnitude.entries.toList()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = value?.label() ?: MinMagnitude.MAG_3_0.label(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.label()) },
                    onClick = { onChange(opt); expanded = false }
                )
            }
        }
    }
}

// default: LAST_30_DAYS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodDropdown(
    modifier: Modifier = Modifier,
    value: TimeInterval,
    onChange: (TimeInterval) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val items = TimeInterval.entries.toList()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = value.label(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.label()) },
                    onClick = { onChange(opt); expanded = false }
                )
            }
        }
    }
}
