# Earthquake Statistics MVP Design

## Goal

Add a top-level Statistics screen that gives users an accurate, quickly readable overview of global earthquake activity without depending on the earthquake list filters or the list database's current 200-event limit.

The MVP includes:

- global earthquake counts for today, the last 7 days, the last 30 days, and the current year;
- the strongest global earthquake today;
- the nearest global earthquake today relative to the user's effective location;
- a clearly visible fixed magnitude threshold;
- Navigation 3 integration;
- offline cached content, refresh/error handling, localization, and regression tests.

Charts, magnitude/depth distributions, active regions, and nearby trends are explicitly deferred to the Insights phase.

## Product Decisions

- Statistics use global data with a fixed minimum magnitude of `2.5`.
- Statistics are independent of list filters and filter preferences.
- The scope label is always visible as `Global earthquakes · M2.5+` or its localized equivalent.
- Layout B, "Events first," is selected: a compact row of four counts followed by the strongest-today and nearest-today cards.
- The screen is a fourth top-level bottom-navigation destination between Map and Settings.
- Selecting either event card opens the existing earthquake details destination.
- The MVP does not embed maps, avoiding unnecessary map rendering and API cost.

## Time Semantics

All USGS query boundaries are sent as UTC instants.

- `Today`: from the user's local start of day converted to UTC, through the current instant.
- `Last 7 days`: rolling seven 24-hour periods ending at the current instant.
- `Last 30 days`: rolling thirty 24-hour periods ending at the current instant.
- `This year`: from the user's local start of January 1 converted to UTC, through the current instant.

The ViewModel obtains one clock snapshot per refresh so all requests use the same `now`. Time-zone changes apply on the next refresh.

## Data Strategy

Use a hybrid USGS strategy rather than downloading a full annual event catalog.

### Counts

Add support for the USGS `/fdsnws/event/1/count` endpoint. Execute four requests concurrently with these shared parameters:

- `eventtype=earthquake`
- `minmagnitude=2.5`
- the period-specific `starttime`
- a shared `endtime=now`

The count endpoint returns the complete total and avoids the 20,000-event query limit.

### Strongest Today

Query `/fdsnws/event/1/query` with today's boundaries, `minmagnitude=2.5`, `orderby=magnitude`, and `limit=1`.

### Nearest Today

Query today's global M2.5+ events and compute Haversine distance locally from the effective app location. The query uses today's boundaries and pagination when necessary, so it does not silently truncate at the existing `EarthquakeQueryParams.limit = 200` default.

The effective location follows existing app behavior: current user location when available, otherwise the persisted manual/default location. If no effective location is available, the nearest section enters a location-unavailable state while global sections continue to work.

### Isolation From Existing List Refresh

Statistics use a dedicated repository contract. They do not change the current list download, Room earthquake table, list filters, or refresh behavior. This avoids introducing list regressions while correcting statistics accuracy independently.

## Architecture

Create a feature package following the existing MVVM/Clean Architecture conventions:

- `feat_statistics/domain/model`: period counts, statistics snapshot, and highlighted-event models.
- `feat_statistics/domain/repository`: `EarthquakeStatisticsRepository` contract.
- `feat_statistics/domain/use_cases`: a refresh/load use case that expresses the feature operation without Android dependencies.
- `feat_statistics/data/remote`: USGS count and targeted-query implementation.
- `feat_statistics/data/local`: a Room entity and DAO for the cached statistics snapshot.
- `feat_statistics/data/repository`: orchestration of concurrent remote calls, nearest-event calculation, and cache fallback.
- `feat_statistics/presentation/state`: `StatisticsUiState`.
- `feat_statistics/presentation/ui`: `StatisticsViewModel`, route Composable, and stateless content Composable.
- `feat_statistics/presentation/components`: count strip and highlighted-event cards.

The repository receives a clock/time-zone abstraction where needed so boundary calculations are deterministic in tests. Domain and presentation code do not depend on Android `Context`.

## UI State And Events

`StatisticsUiState` contains:

- `counts`: today, seven days, thirty days, and year;
- `strongestToday`;
- `nearestToday`;
- `threshold`, fixed to `2.5` for this version;
- `lastUpdated`;
- `isInitialLoading`;
- `isRefreshing`;
- per-section availability so partial results can render;
- a refresh-level error suitable for UI string mapping;
- whether displayed data came from cache.

The ViewModel exposes one `StateFlow<StatisticsUiState>` and intent methods for screen start and refresh. Event-card clicks are forwarded by the route to the navigation host; navigation is not embedded in the ViewModel.

## Screen Behavior

- On first entry, show a loading state if no cache exists.
- If cached content exists, render it immediately and refresh in the background when stale.
- Pull-to-refresh or an equivalent explicit refresh action requests fresh statistics.
- During refresh, keep existing values visible.
- The count strip uses compact locale-aware number formatting, while accessibility semantics expose the complete values and labels.
- The strongest card shows magnitude, place, local event time, and depth.
- The nearest card shows magnitude, place, local event time, and distance in the configured unit system.
- Cards are omitted only when USGS returns no qualifying event. They are replaced with a clear empty-state message, not a zero-value fake event.
- If location is unavailable, the nearest card explains that a location is required; the screen does not trigger the permission launcher itself.
- The screen follows the existing light/dark/system themes and current app typography, spacing, magnitude colors, and units.

## Caching

Persist the last successful statistics snapshot in a dedicated single-row Room table, separate from the earthquake-list table. Add the explicit Room schema migration required for the new table; existing earthquake records remain untouched. The cached record includes values, highlighted events, threshold, query boundaries, and retrieval timestamp.

- A snapshot is fresh for 15 minutes.
- Fresh cached data is rendered without an automatic network request.
- Stale cached data is rendered immediately while a background refresh runs.
- Manual refresh always attempts the network.
- A successful full refresh replaces the cache atomically.
- A partial remote result may update the in-memory UI but does not replace the last complete cached snapshot.

## Error Handling

- A total network failure with cache available keeps cached data visible and marks it as not updated.
- A total failure without cache shows a full-screen retry state.
- Individual request failures preserve successful sections and show a non-blocking partial-data message.
- Empty USGS results are modeled separately from transport/server errors.
- Error types are domain/app errors mapped to localized strings in presentation.
- Cancellation is propagated and never converted into a user-visible failure.

## Navigation

Add a serializable `AppDestination.Statistics` top-level destination and a corresponding entry in:

- the bottom bar;
- `AppNavigationHost`;
- `AppNavigationScreenFactory` and its default implementation;
- navigator top-level destination handling and matching logic.

The selected-event callback navigates to the existing `AppDestination.Details(eventId)`. Existing Home, Map, Settings, Intro, Credits, and Details behavior remains unchanged.

## Accessibility

- Count cells expose descriptive semantics such as `146 earthquakes today`, not disconnected number and label nodes.
- Event cards have one clear click action and at least a 48 dp touch target.
- Magnitude is communicated through text as well as color.
- Loading, cached, partial, empty, and error states have readable labels.
- Layout supports font scaling and narrow devices without clipping; the four-count strip may use abbreviated visual numbers but never abbreviated accessibility values.

## Tests

### Unit Tests

- local-day and local-year boundary conversion across representative time zones;
- rolling 7-day and 30-day boundaries;
- all four count requests use M2.5+ and the same `now`;
- strongest query uses magnitude ordering and a one-event limit;
- nearest calculation, null coordinates, ties, and empty results;
- pagination prevents today's nearest data from being truncated at 200 events;
- cache freshness, stale-while-refresh behavior, and manual refresh;
- full success, partial success, cached failure, uncached failure, and empty results;
- ViewModel startup, refresh, and location-unavailable state.

### Navigation And Compose Tests

- Statistics is reachable from each top-level destination and is selected in the bottom bar.
- Switching top-level destinations preserves expected Navigation 3 behavior.
- Strongest and nearest cards navigate to the existing Details destination with the correct event id.
- The content Composable renders loading, data, cached, partial, empty, location-unavailable, and error states.
- Count semantics remain understandable independently of visual abbreviation.

### Verification

- Run targeted statistics unit tests.
- Run existing app unit tests and navigation tests.
- Run Compose instrumentation tests on an emulator/device.
- Run `assembleDebug` as a final build smoke test.

## Non-Goals

- No charts or trend visualizations in this phase.
- No changes to the list's filter behavior or 200-event fetch limit.
- No notification or alert changes.
- No annual earthquake-history storage and no transformation of existing earthquake records.
- No embedded static or interactive maps on the Statistics screen.
- No configurable statistics threshold in the MVP.
