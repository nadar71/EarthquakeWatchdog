package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.indiewalk.watchdog.earthquake.R

@Composable
fun PermissionDeniedDialog(
    onOpenSettings: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onContinue,
        title = {
            Text(
                text = stringResource(R.string.maps_permission_denied_title_label)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.maps_permission_denied_text_label)
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(
                    text = stringResource(R.string.maps_permission_settings_label)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onContinue) {
                Text(
                    text = stringResource(R.string.generic_continue_label)
                )
            }
        }
    )
}