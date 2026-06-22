package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.indiewalk.watchdog.earthquake.R

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.maps_permission_rationale_title_label)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.maps_permission_rationale_text_label)
            )
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(
                    text = stringResource(R.string.generic_continue_label)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.generic_cancel)
                )
            }
        }
    )
}