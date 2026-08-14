package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.util.FormatUtil
import com.indiewalk.watchdog.earthquake.core.util.extensions.kmToDisplayInt
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.MagnitudeBubble
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent
import kotlin.math.roundToInt

@Composable
fun StatisticsEventCard(
    title: String,
    event: StatisticsEvent,
    unitSystem: UnitSystem,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MagnitudeBubble(mag = event.magnitude, size = 46.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.place,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = eventDetails(event, unitSystem),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun eventDetails(event: StatisticsEvent, unitSystem: UnitSystem): String {
    val details = mutableListOf(FormatUtil.formatDateTime(event.time))
    event.depthKm?.let {
        details += stringResource(R.string.statistics_depth, FormatUtil.formatDistanceToInt(it))
    }
    event.distanceKm?.let {
        val value = it.roundToInt().kmToDisplayInt(unitSystem).toString()
        details += stringResource(
            if (unitSystem == UnitSystem.IMPERIAL) {
                R.string.statistics_distance_mi
            } else {
                R.string.statistics_distance_km
            },
            value
        )
    }
    return details.joinToString(" · ")
}
