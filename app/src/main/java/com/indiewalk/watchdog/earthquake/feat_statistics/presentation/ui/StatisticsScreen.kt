package com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.core.util.FormatUtil
import com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsSection
import com.indiewalk.watchdog.earthquake.feat_statistics.presentation.components.StatisticsCountStrip
import com.indiewalk.watchdog.earthquake.feat_statistics.presentation.components.StatisticsEventCard
import com.indiewalk.watchdog.earthquake.feat_statistics.presentation.state.StatisticsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    currentDestination: AppDestination,
    onTopLevelDestinationSelected: (AppDestination) -> Unit,
    onOpenDetails: (String) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onScreenStarted() }

    ScaffoldModel(
        currentDestination = currentDestination,
        onTopLevelDestinationSelected = onTopLevelDestinationSelected,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.statistics_title),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        StatisticsContent(
            uiState = uiState,
            onRefresh = viewModel::onRefreshRequested,
            onEventClick = onOpenDetails,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatisticsContent(
    uiState: StatisticsUiState,
    onRefresh: () -> Unit,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(uiState.isRefreshing, onRefresh)
    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when {
            uiState.isInitialLoading && !uiState.hasContent -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("statistics-loading")
            )
            !uiState.hasContent && uiState.error != null -> ErrorContent(onRefresh)
            uiState.snapshot != null -> StatisticsDataContent(
                uiState = uiState,
                contentPadding = PaddingValues(16.dp),
                onEventClick = onEventClick
            )
        }

        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StatisticsDataContent(
    uiState: StatisticsUiState,
    contentPadding: PaddingValues,
    onEventClick: (String) -> Unit
) {
    val snapshot = requireNotNull(uiState.snapshot)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(
                        R.string.statistics_scope,
                        FormatUtil.formatMag(snapshot.threshold)
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.statistics_updated,
                        FormatUtil.formatDateTime(snapshot.retrievedAt.toEpochMilli())
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { StatisticsCountStrip(snapshot.counts) }
        item {
            HighlightSection(
                title = stringResource(R.string.statistics_strongest_today),
                emptyText = stringResource(R.string.statistics_no_strongest),
                unavailable = StatisticsSection.STRONGEST in uiState.unavailableSections,
                unavailableText = stringResource(R.string.statistics_partial),
                event = snapshot.strongestToday,
                testTag = "statistics-strongest",
                uiState = uiState,
                onEventClick = onEventClick
            )
        }
        item {
            HighlightSection(
                title = stringResource(R.string.statistics_nearest_today),
                emptyText = stringResource(R.string.statistics_no_nearest),
                unavailable = StatisticsSection.NEAREST in uiState.unavailableSections,
                unavailableText = stringResource(R.string.statistics_nearest_unavailable),
                event = snapshot.nearestToday,
                testTag = "statistics-nearest",
                uiState = uiState,
                onEventClick = onEventClick
            )
        }
        if (uiState.isFromCache || uiState.isStale || uiState.unavailableSections.isNotEmpty()) {
            item {
                Text(
                    text = when {
                        uiState.unavailableSections.isNotEmpty() -> stringResource(R.string.statistics_partial)
                        uiState.isStale -> stringResource(R.string.statistics_stale)
                        else -> stringResource(R.string.statistics_cached)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HighlightSection(
    title: String,
    emptyText: String,
    unavailable: Boolean,
    unavailableText: String,
    event: com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent?,
    testTag: String,
    uiState: StatisticsUiState,
    onEventClick: (String) -> Unit
) {
    when {
        unavailable -> Text(
            text = unavailableText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        event == null -> Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> StatisticsEventCard(
            title = title,
            event = event,
            unitSystem = uiState.settings.unitSystem,
            testTag = testTag,
            onClick = { onEventClick(event.id) }
        )
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.statistics_error),
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.statistics_retry))
        }
    }
}
