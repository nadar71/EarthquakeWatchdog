package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.TrendPoint
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

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
            .semantics { contentDescription = semanticDescription },
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
            TrendWithAxes(
                trend = trend,
                barColor = MaterialTheme.colorScheme.primary,
                guideColor = MaterialTheme.colorScheme.outlineVariant,
                testTag = testTag
            )
        }
    }
}

@Composable
private fun TrendWithAxes(
    trend: List<TrendPoint>,
    barColor: Color,
    guideColor: Color,
    testTag: String
) {
    val counts = trend.map(TrendPoint::count)
    val axisMaximum = roundedAxisMaximum(counts.maxOrNull() ?: 0)
    val midpoint = ceil(axisMaximum / 2.0).toInt()
    val dateLabels = trend.axisDateLabels()
    val dateDescription = stringResource(R.string.statistics_axis_dates_cd, dateLabels.joinToString())
    val countDescription = stringResource(R.string.statistics_axis_count_cd, axisMaximum)
    val yAxisTitle = stringResource(R.string.statistics_axis_y_title)

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(112.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = yAxisTitle,
                    modifier = Modifier
                        .rotate(-90f)
                        .testTag("$testTag-y-axis-title"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(3.dp))
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .height(112.dp)
                    .testTag("$testTag-y-axis")
                    .semantics { contentDescription = countDescription },
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(axisMaximum.toString(), fontSize = 10.sp)
                Text(midpoint.toString(), fontSize = 10.sp)
                Text("0", fontSize = 10.sp)
            }
            Spacer(Modifier.width(7.dp))
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp)
            ) {
                listOf(0f, size.height / 2f, size.height).forEach { y ->
                    drawLine(
                        color = guideColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = 2f
                    )
                }
                if (counts.isEmpty()) return@Canvas
                val slotWidth = size.width / counts.size
                val barWidth = (slotWidth * 0.62f).coerceAtLeast(2f)
                counts.forEachIndexed { index, count ->
                    val height = size.height * count / axisMaximum.toFloat()
                    drawRoundRect(
                        color = barColor,
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
        Row(verticalAlignment = Alignment.Top) {
            Spacer(Modifier.width(60.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .testTag("$testTag-x-axis")
                    .semantics { contentDescription = dateDescription },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dateLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun List<TrendPoint>.axisDateLabels(): List<String> {
    if (isEmpty()) return emptyList()
    val configuration = LocalConfiguration.current
    val locale = Locale.forLanguageTag(configuration.locales[0].toLanguageTag())
    val formatter = DateTimeFormatter.ofPattern("d MMM", locale).withZone(ZoneId.systemDefault())
    return List(AXIS_DATE_LABEL_COUNT) { labelIndex ->
        val trendIndex = labelIndex * lastIndex / (AXIS_DATE_LABEL_COUNT - 1)
        formatter.format(this[trendIndex].start)
    }
}

private fun roundedAxisMaximum(peak: Int): Int {
    if (peak <= MINIMUM_AXIS_MAXIMUM) return MINIMUM_AXIS_MAXIMUM
    val magnitude = 10.0.pow(floor(log10(peak.toDouble())))
    val normalized = peak / magnitude
    val step = when {
        normalized <= 1.0 -> 1.0
        normalized <= 2.0 -> 2.0
        normalized <= 5.0 -> 5.0
        else -> 10.0
    }
    return (step * magnitude).toInt()
}

private const val AXIS_DATE_LABEL_COUNT = 5
private const val MINIMUM_AXIS_MAXIMUM = 5
