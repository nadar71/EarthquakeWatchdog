package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.MagnitudeBubble


@Composable
fun EarthquakeMarker(
    state: MarkerState,
    title: String,
    snippet: String,
    eq: EQEntity,
    onClick: () -> Boolean,
) {
    var showInfoWindow by remember { mutableStateOf(false) }

    MarkerComposable(
        state = state,
        title = title,
        onClick = {
            showInfoWindow = !showInfoWindow
            onClick()
        },
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                /*.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                )*/
                .background(Color.White)
                .padding(1.dp),
            contentAlignment = Alignment.Center
        ) {
            MagnitudeBubble(
                mag = eq.mag,
                size = 30.dp
            )
        }
    }

    if (showInfoWindow) {
        AlertDialog(
            onDismissRequest = { showInfoWindow = false },
            title = { Text(title) },
            text = { Text(snippet) },
            confirmButton = {
                TextButton(
                    onClick = { showInfoWindow = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

