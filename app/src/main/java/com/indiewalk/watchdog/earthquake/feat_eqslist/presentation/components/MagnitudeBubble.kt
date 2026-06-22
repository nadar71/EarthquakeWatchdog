package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.core.util.FormatUtil
import com.indiewalk.watchdog.earthquake.core.util.GraphicsUtil

@Composable
fun MagnitudeBubble(
    mag: Double?,
    size: Dp,
) {
    val m = (mag ?: 0.0).coerceAtLeast(0.0)
    val colors = GraphicsUtil.magnitudeColors(m)

    Box(
        modifier = Modifier.Companion.size(size + 8.dp), // Outer container to hold all layers
        contentAlignment = Alignment.Companion.Center
    ) {
        Box(
            modifier = Modifier.Companion
                .size(size + 4.dp)
                .clip(CircleShape)
                .background(colors.second),
            contentAlignment = Alignment.Companion.Center
        ) {}

        Box(
            modifier = Modifier.Companion
                .size(size + 2.dp)
                .clip(CircleShape)
                .background(Color.Companion.White),
            contentAlignment = Alignment.Companion.Center
        ) {}

        Box(
            modifier = Modifier.Companion
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.Companion.radialGradient(
                        colors = listOf(colors.first, colors.second),
                        center = Offset.Companion.Unspecified,
                        radius = 60f
                    )
                ),
            contentAlignment = Alignment.Companion.Center
        ) {
            Text(
                text = FormatUtil.formatMag(m),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.Companion.White,
                )
            )
        }
    }
}