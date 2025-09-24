package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.FeatureDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.PropertiesDTO
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EarthquakeCard(
    eq: FeatureDTO,
    distanceKm: Double?,                 // pass precomputed distance if you have it; else null to hide
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    bubbleSize: Dp = 44.dp
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MagnitudeBubble(
                mag = eq.properties.mag,
                size = bubbleSize
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Date/time
                Text(
                    text = formatEpoch(eq.properties.time),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF5F6A7A) // subtle gray-blue
                    )
                )

                // Place line: prefix + name
                val (prefix, placeName) = splitPlace(eq.properties.place)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (prefix.isNotEmpty()) {
                        Text(
                            text = prefix,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF19232F)
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = placeName.ifEmpty { "Unknown" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF0D1B2A),
                            fontSize = 22.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right column: distance
            if (distanceKm != null) {
                Spacer(Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${formatKm(distanceKm)} km",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF0D1B2A),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "from you",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF8A96A3)
                        )
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            thickness = 1.dp,
            color = Color(0xFFEAECEF)
        )
    }
}

@Composable
private fun MagnitudeBubble(
    mag: Double?,
    size: Dp,
) {
    val m = (mag ?: 0.0).coerceAtLeast(0.0)
    val colors = magnitudeColors(m)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(colors.first, colors.second),
                    center = androidx.compose.ui.geometry.Offset.Zero,
                    radius = 120f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatMag(m),
            style = MaterialTheme.typography.labelLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

/* ---------- helpers ---------- */

private fun formatMag(mag: Double): String {
    // One decimal like "5.0"
    return String.format(Locale.US, "%.1f", mag)
}

@SuppressLint("NewApi")
private fun formatEpoch(epochMs: Long?): String {
    if (epochMs == null) return ""
    val instant = Instant.ofEpochMilli(epochMs)
    val dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
    // To mimic lowercase "am/pm" in your screenshot:
    return dtf.format(instant).replace("AM", "am").replace("PM", "pm")
}

/**
 * USGS 'place' strings are typically:
 *  - "76 km WSW of Anderson Springs, CA"
 *  - "Near the coast of Nicaragua"
 *  - "Macquarie Island region"
 * We try to split a readable prefix (bold small) + place name (large).
 */
private fun splitPlace(place: String?): Pair<String, String> {
    val p = place.orEmpty().trim()
    if (p.isEmpty()) return "" to ""

    val lower = p.lowercase(Locale.getDefault())

    // "Near the ..." case
    if (lower.startsWith("near")) {
        // Keep "Near the" or "Near" as prefix, rest as name
        val parts = p.split(" ", limit = 3)
        return if (parts.size >= 3) {
            "${parts[0]} ${parts[1]}" to parts[2] // "Near the" + rest
        } else {
            "Near" to p.removePrefix(parts[0]).trim()
        }
    }

    // "xx km <dir> of <Place>" case
    val ofIdx = lower.indexOf(" of ")
    if (ofIdx != -1) {
        val prefix = p.substring(0, ofIdx + 3).trim() // includes " of"
        val name = p.substring(ofIdx + 4).trim()
        return prefix to name
    }

    // Fallback: no prefix
    return "" to p
}

private fun formatKm(distanceKm: Double): String {
    val nf = NumberFormat.getIntegerInstance()
    return nf.format(distanceKm.roundToInt())
}

private fun magnitudeColors(mag: Double): Pair<Color, Color> {
    // Simple scale; tweak to taste
    return when {
        mag < 2.5 -> Color(0xFF2E7D32) to Color(0xFF66BB6A) // green
        mag < 4.5 -> Color(0xFFF9A825) to Color(0xFFFFD54F) // yellow
        mag < 6.0 -> Color(0xFFF57C00) to Color(0xFFFFB74D) // orange
        mag < 7.0 -> Color(0xFFD32F2F) to Color(0xFFEF5350) // red
        else      -> Color(0xFFB71C1C) to Color(0xFFE57373) // deep red
    }
}

// create a preview
/*
@Preview(showBackground = true)
@Composable
fun EarthquakeCardPreview() {
    EarthquakeCard(
        eq = FeatureDTO(
            properties = PropertiesDTO(
                mag = 5.0,
                place = "76 km WSW of Anderson Springs, CA",
                time = 1696728000000L
            )
        ),
        distanceKm = 76.0,
        onClick = {}
    )
}
*/
