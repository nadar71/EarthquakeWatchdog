package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.core.presentation.theme.EQWatchdogTheme
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraDeepRed_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraDeepRed_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraGreen_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraGreen_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraOrange_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraOrange_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraRed_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraRed_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraYellow_dark
import com.indiewalk.watchdog.earthquake.core.presentation.theme.extraYellow_light
import com.indiewalk.watchdog.earthquake.core.presentation.theme.onBackgroundLight
import com.indiewalk.watchdog.earthquake.core.util.formatUtils.formatDate
import com.indiewalk.watchdog.earthquake.core.util.formatUtils.formatDistanceToInt
import com.indiewalk.watchdog.earthquake.core.util.formatUtils.formatMag
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.FeatureDTO
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dtos.GeometryDTO
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
            containerColor = MaterialTheme.colorScheme.surface
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
                    text = formatDate(eq.properties.time),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.secondary
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                    }

                }
                Text(
                    text = placeName.ifEmpty { "Unknown" },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right column: distance
            if (distanceKm != null) {
                Spacer(Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${formatDistanceToInt(distanceKm)} km",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "from you",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.secondaryContainer
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
                    center = Offset.Unspecified,
                    radius = 60f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatMag(m),
            style = MaterialTheme.typography.labelLarge.copy(
                color = Color.White,
            )
        )
    }
}





//  USGS 'place' strings are typically:
//   - "76 km WSW of Anderson Springs, CA"
//   - "Near the coast of Nicaragua"
//   - "Macquarie Island region"
//  Split a readable prefix (small) + place name (large).
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




private fun magnitudeColors(mag: Double): Pair<Color, Color> {
    return when {
        mag < 2.5 -> extraGreen_light   to extraGreen_dark   // green
        mag < 4.5 -> extraYellow_light  to extraYellow_dark  // yellow
        mag < 6.0 -> extraOrange_light  to extraOrange_dark  // orange
        mag < 7.0 -> extraRed_light     to extraRed_dark     // red
        else      -> extraDeepRed_light to extraDeepRed_dark // deep red
    }
}

// ----------------------------------------------------------------------------------------- preview


@Preview(
    name = "Earthquake cards – Light",
    showBackground = true,
    backgroundColor = 0xFF181C1F, // val onBackgroundLight = Color(0xFF181C1F)
    widthDp = 360
)
@Composable
fun EarthquakeCardPreviewLight() {
    EQWatchdogTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(onBackgroundLight/*Color(0xFFF7F9FC)*/)
                .padding(vertical = 8.dp)
        ) {
            Column {
                EarthquakeCard(
                    eq = sampleFeature1(),
                    distanceKm = 15236.0,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                EarthquakeCard(
                    eq = sampleFeature2(),
                    distanceKm = 7826.0,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Preview(
    name = "Earthquake cards – Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360
)
@Composable
fun EarthquakeCardPreviewDark() {
    EarthquakeCardPreviewLight()
}

/* ------------------ sample data ------------------ */

private fun sampleFeature1() = FeatureDTO(
    type = "Feature",
    id = "sample-ci-001",
    properties = PropertiesDTO(
        mag = 5.0,
        place = "76 km West of Macquarie Island",
        time = 1583196360000,  // Mar 03, 2020 2:06 am (example)
        updated = 1583199960000,
        tz = null,
        url = "https://earthquake.usgs.gov/earthquakes/eventpage/sample-ci-001",
        detail = null,
        felt = null,
        cdi = null,
        mmi = null,
        alert = null,
        status = "reviewed",
        tsunami = 0,
        sig = 385,
        net = "ci",
        code = "001",
        ids = ",sample-ci-001,",
        sources = "ci",
        types = "origin,phase-data",
        nst = 25,
        dmin = 0.123,
        rms = 0.76,
        gap = 45.0,
        magType = "mb",
        type = "earthquake"
    ),
    geometry = GeometryDTO(
        type = "Point",
        coordinates = listOf(158.95, -54.5, 10.0) // lon, lat, depth(km)
    )
)

private fun sampleFeature2() = FeatureDTO(
    type = "Feature",
    id = "sample-us-002",
    properties = PropertiesDTO(
        mag = 4.9,
        place = "Near the Chagos Archipelago region",
        time = 1583267460000,  // Mar 03, 2020 6:31 pm (example)
        updated = 1583271060000,
        tz = null,
        url = "https://earthquake.usgs.gov/earthquakes/eventpage/sample-us-002",
        detail = null,
        felt = null,
        cdi = null,
        mmi = null,
        alert = null,
        status = "reviewed",
        tsunami = 0,
        sig = 369,
        net = "us",
        code = "002",
        ids = ",sample-us-002,",
        sources = "us",
        types = "origin,phase-data",
        nst = 19,
        dmin = 0.234,
        rms = 0.68,
        gap = 60.0,
        magType = "mb",
        type = "earthquake"
    ),
    geometry = GeometryDTO(
        type = "Point",
        coordinates = listOf(72.0, -6.0, 12.0)
    )
)
