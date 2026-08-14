package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.util.FormatUtil.formatDateTime
import com.indiewalk.watchdog.earthquake.core.util.FormatUtil.formatMag
import com.indiewalk.watchdog.earthquake.core.util.extensions.kmToDisplayInt
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity
import com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.MagnitudeBubble
import java.util.Locale

@Composable
fun EarthquakeMapBottomInfoCard(
    earthquake: EQEntity,
    unitSystem: UnitSystem,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val distanceUnitLabel = stringResource(
        if (unitSystem == UnitSystem.IMPERIAL) {
            R.string.settings_mi_distance_unit_label
        } else {
            R.string.settings_km_distance_unit_label
        }
    )
    val magnitudeValue = earthquake.mag?.let { formatMag(it) } ?: "--"
    val magnitudeType = earthquake.magType.toMagnitudeTypeLabel()
    val magnitudeImpact = earthquake.mag.toMagnitudeImpactLabel()
    val formattedDate = formatDateTime(earthquake.time)
    val formattedDepth = earthquake.depthKm?.let { String.format(Locale.getDefault(), "%.1f km", it) } ?: "--"
    val formattedLocation = earthquake.latitude?.let { lat ->
        earthquake.longitude?.let { lng ->
            String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng)
        }
    } ?: "--"
    val formattedDistance = earthquake.distanceFromUser?.let {
        "${it.kmToDisplayInt(unitSystem)} $distanceUnitLabel"
    } ?: "--"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = earthquake.place.orEmpty().ifBlank { earthquake.id },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_xmark),
                        contentDescription = stringResource(R.string.generic_cancel),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MagnitudeBubble(
                        mag = earthquake.mag,
                        size = 38.dp
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.maps_marker_magnitude_label),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                R.string.maps_marker_magnitude_type_format,
                                magnitudeType
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = magnitudeImpact,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailInfoTile(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_calendar,
                    label = stringResource(R.string.maps_marker_date_label),
                    value = formattedDate
                )
                DetailInfoTile(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_deep,
                    label = stringResource(R.string.maps_marker_depth_label),
                    value = formattedDepth
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailInfoTile(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_location,
                    label = stringResource(R.string.maps_marker_location_label),
                    value = formattedLocation
                )
                DetailInfoTile(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_distance,
                    label = stringResource(R.string.maps_marker_distance_label),
                    value = formattedDistance
                )
            }
        }
    }
}

@Composable
private fun DetailInfoTile(
    iconRes: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 92.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun String?.toMagnitudeTypeLabel(): String {
    return when (this?.lowercase(Locale.getDefault())) {
        "mb" -> "Mb - Body-wave magnitude"
        "ml" -> "Ml - Local magnitude"
        "md" -> "Md - Duration magnitude"
        "mw" -> "Mw - Moment magnitude"
        "mwr" -> "Mwr - Regional moment magnitude"
        "mww" -> "Mww - W-phase moment magnitude"
        "mwc" -> "Mwc - Centroid moment magnitude"
        "mwb" -> "Mwb - Broadband moment magnitude"
        "mwp" -> "Mwp - P-wave moment magnitude"
        else -> (this ?: "--").uppercase(Locale.getDefault())
    }
}

@Composable
private fun Double?.toMagnitudeImpactLabel(): String {
    val resId = when {
        this == null -> R.string.maps_marker_magnitude_impact_unknown
        this < 2.0 -> R.string.maps_marker_magnitude_impact_micro
        this < 4.0 -> R.string.maps_marker_magnitude_impact_minor
        this < 5.0 -> R.string.maps_marker_magnitude_impact_light
        this < 6.0 -> R.string.maps_marker_magnitude_impact_moderate
        this < 7.0 -> R.string.maps_marker_magnitude_impact_strong
        this < 8.0 -> R.string.maps_marker_magnitude_impact_major
        else -> R.string.maps_marker_magnitude_impact_great
    }
    return stringResource(resId)
}
