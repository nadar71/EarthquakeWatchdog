package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.DistributionBucket

data class LabeledDistributionBucket(
    val bucket: DistributionBucket,
    val label: String
)

@Composable
fun StatisticsDistributionCard(
    title: String,
    buckets: List<LabeledDistributionBucket>,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val peak = buckets.maxOfOrNull { it.bucket.count }?.coerceAtLeast(1) ?: 1
    Surface(
        modifier = modifier.fillMaxWidth().testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            buckets.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${item.label} · ${item.bucket.count}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.bucket.count / peak.toFloat())
                            .height(7.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}
