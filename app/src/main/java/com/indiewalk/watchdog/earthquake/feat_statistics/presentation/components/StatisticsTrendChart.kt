package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.TrendPoint

@Composable
fun StatisticsTrendChart(
    title: String,
    trend: List<TrendPoint>,
    semanticDescription: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clearAndSetSemantics { contentDescription = semanticDescription },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TrendBars(
                counts = trend.map(TrendPoint::count),
                color = MaterialTheme.colorScheme.primary,
                baselineColor = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun TrendBars(
    counts: List<Int>,
    color: Color,
    baselineColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
    ) {
        drawLine(
            color = baselineColor,
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = 2f
        )
        if (counts.isEmpty()) return@Canvas
        val peak = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
        val slotWidth = size.width / counts.size
        val barWidth = (slotWidth * 0.62f).coerceAtLeast(2f)
        counts.forEachIndexed { index, count ->
            val height = size.height * count / peak.toFloat()
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = index * slotWidth + (slotWidth - barWidth) / 2,
                    y = size.height - height
                ),
                size = androidx.compose.ui.geometry.Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
