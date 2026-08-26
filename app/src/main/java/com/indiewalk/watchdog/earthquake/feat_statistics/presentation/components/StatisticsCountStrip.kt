package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsCounts
import java.text.NumberFormat

@Composable
fun StatisticsCountStrip(
    counts: StatisticsCounts,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CountCell(counts.today, R.string.statistics_today, R.string.statistics_count_today_cd)
            CountCell(counts.last7Days, R.string.statistics_last_7_days, R.string.statistics_count_week_cd)
            CountCell(counts.last30Days, R.string.statistics_last_30_days, R.string.statistics_count_month_cd)
            CountCell(counts.year, R.string.statistics_this_year, R.string.statistics_count_year_cd)
        }
    }
}

@Composable
private fun RowScope.CountCell(
    count: Int?,
    @StringRes labelRes: Int,
    @StringRes descriptionRes: Int
) {
    val formatted = count?.let(NumberFormat.getIntegerInstance()::format)
        ?: stringResource(R.string.statistics_count_unavailable)
    val description = if (count == null) {
        "${stringResource(labelRes)}: ${stringResource(R.string.statistics_count_unavailable)}"
    } else {
        stringResource(descriptionRes, formatted)
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatted,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
