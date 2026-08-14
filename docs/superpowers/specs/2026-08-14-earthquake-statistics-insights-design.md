# Earthquake Statistics Insights Design

## Goal

Extend the Statistics feature with useful global and nearby trends while preserving the existing fixed M2.5+ scope, 15-minute cache policy, Navigation 3 integration, and MVVM boundaries.

## Scope

The Insights phase analyzes a rolling 30-day global dataset of earthquakes with magnitude 2.5 or greater. It adds:

- Daily global earthquake counts for the last 30 days.
- Magnitude distribution using `2.5-2.9`, `3.0-3.9`, `4.0-4.9`, `5.0-5.9`, and `6.0+` buckets.
- Depth distribution using shallow `<70 km`, intermediate `70-300 km`, and deep `>300 km` buckets.
- The five most active named regions.
- A daily nearby trend for earthquakes within 1,000 km of the effective user or manual location.

The existing summary counts, strongest-today event, nearest-today event, navigation, ads, themes, units, and list filters remain unchanged. Insights remain independent from list filters.

## Architecture

The existing Statistics repository will fetch one paged 30-day event dataset during refresh. A pure domain `StatisticsInsightsAnalyzer` will transform those events into an immutable `StatisticsInsights` model. The analyzer has no Android, network, database, or localization dependencies.

`StatisticsSnapshot` will include the computed insights. The existing Room cache entity will be extended through a schema migration so summary and insight data share one atomic cache record and one 15-minute freshness timestamp. No second repository, cache, or refresh flow will be introduced.

The ViewModel will continue exposing one `StatisticsUiState`. Global insight failures will be represented independently from the existing summary sections. If effective location is unavailable, global insights remain available while only the nearby trend is marked unavailable.

## Data Flow

On a stale or forced refresh, the repository will:

1. Start the existing count and strongest-event requests.
2. Fetch all M2.5+ events for the rolling 30-day window in USGS-compatible pages.
3. Reuse the subset of today's events to determine nearest today, avoiding a second today-event download.
4. Pass the complete 30-day event list and optional effective location to the analyzer.
5. Build one snapshot containing the summary and insights.
6. Cache only a complete global result. A missing nearby trend does not prevent caching because it can legitimately be unavailable when location is invalid.

Pagination remains bounded by the remote count and existing page-size limit. Analysis is linear in the number of downloaded events and runs off the main thread in the repository coroutine context.

## Insight Rules

Daily values divide the existing rolling 30-day window into 30 ordered 24-hour buckets ending at the snapshot time, including zero-count buckets. This keeps the chart dataset and pagination count identical to the existing last-30-days summary. Labels use the device locale and time zone, but bucket membership is calculated from instants so daylight-saving changes cannot create gaps or overlaps.

Magnitude buckets are lower-bound inclusive. Invalid or missing magnitudes are excluded. Depth values below zero or missing are excluded; 70 km belongs to intermediate and values above 300 km belong to deep.

Active regions are derived conservatively from the final comma-separated segment of the USGS place description. Blank or unparseable descriptions are grouped under an internal unknown value and omitted from the top-five UI. Region matching is case-insensitive while preserving a readable display label. Results sort by count descending and then label ascending for deterministic output.

Nearby trends use Haversine distance against the effective location and include events at or within 1,000 km. They contain the same 30 ordered daily points. When no qualifying nearby events exist, the chart displays a valid zero trend rather than an error.

## Presentation

Insights will appear below the existing strongest and nearest cards in the current Statistics `LazyColumn`:

1. A global 30-day activity chart.
2. Compact magnitude and depth distribution cards.
3. A ranked active-regions card.
4. A nearby 1,000 km activity chart.

Charts will use small custom Compose drawing components and accessible text summaries. No third-party chart dependency will be added. Colors will come from the existing Material theme, support light and dark modes, and avoid encoding meaning by color alone.

## Error Handling

The existing cache-first behavior remains. Cached insights render immediately while refresh runs. A failure to load the 30-day event dataset marks all insight sections unavailable without hiding valid summary data. A location failure marks only the nearby trend unavailable. Pull-to-refresh retries all unavailable sections.

Malformed individual remote events are skipped rather than failing the complete analysis. Cancellation continues to propagate and is never converted to a partial result.

## Testing

- Analyzer unit tests cover all magnitude/depth boundaries, zero-filled days, ordering, region normalization and ties, nearby radius inclusion, invalid locations, malformed events, and empty datasets.
- Repository tests verify one paged 30-day fetch feeds all insights, today's nearest is reused from that dataset, partial insight failures preserve summaries, and complete results are cached.
- Room migration and round-trip tests verify the extended snapshot schema.
- ViewModel tests verify insight availability flags survive cache, refresh, and error transitions.
- Compose instrumentation tests verify chart/distribution/region rendering, accessibility summaries, nearby unavailable state, and empty trends.
- Full JVM, lint, assemble, and emulator instrumentation suites remain the completion gate.

## Deferred Work

Interactive chart selection, configurable windows or radius, map-based heatmaps, server-side aggregation, historical comparisons beyond 30 days, alerts, and predictive interpretation are intentionally deferred.
