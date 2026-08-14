# Earthquake Statistics Insights Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend Statistics with global 30-day activity, magnitude/depth distributions, active regions, and a 1,000 km nearby trend.

**Architecture:** The repository downloads one paged M2.5+ rolling-30-day event dataset and delegates all aggregation to a pure domain analyzer. The resulting immutable insights are stored in the existing Room cache and rendered from the existing single `StatisticsUiState` using dependency-free Compose charts.

**Tech Stack:** Kotlin, coroutines/Flow, Ktor, Room, Hilt, Jetpack Compose Material 3, Navigation 3, JUnit 4, Compose UI Test.

## Global Constraints

- Statistics remain global and fixed at magnitude 2.5+, independent from list filters.
- The analysis window is the existing rolling 30-day window split into exactly 30 ordered 24-hour buckets.
- Nearby means Haversine distance less than or equal to 1,000 km from the effective manual/user position.
- Existing summary cards, 15-minute cache freshness, navigation, themes, units, and refresh behavior remain unchanged.
- No chart dependency is added; charts use Compose Canvas plus accessible summaries.
- Missing location affects only nearby insights; malformed individual events are skipped.

---

### Task 1: Pure Insights Domain Analyzer

**Files:**
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/model/StatisticsModels.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/analysis/StatisticsInsightsAnalyzer.kt`
- Create: `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/analysis/StatisticsInsightsAnalyzerTest.kt`

**Interfaces:**
- Consumes: `StatisticsEvent`, `StatisticsWindow`, and optional app-owned `StatisticsLocation` origin.
- Produces: `StatisticsLocation(latitude: Double, longitude: Double)`, `TrendPoint(start: Instant, end: Instant, count: Int)`, `DistributionBucket(id: String, count: Int)`, `ActiveRegion(name: String, count: Int)`, `StatisticsInsights`, and `StatisticsInsightsAnalyzer.analyze(events, window, origin)`.

- [ ] **Step 1: Write failing analyzer tests**

Cover exactly 30 zero-filled ordered buckets; lower-inclusive magnitude boundaries; depth boundaries where 70 belongs to intermediate and 300 belongs to intermediate; exclusion of invalid depth; deterministic region normalization/tie sorting; 1,000 km radius inclusion; invalid/null origin producing `nearbyTrend = null`; and empty input producing valid zero global trends.

```kotlin
val result = analyzer.analyze(events, rollingWindow, StatisticsLocation(0.0, 0.0))
assertEquals(30, result.globalTrend.size)
assertEquals(listOf(1, 1, 1, 1, 1), result.magnitudeDistribution.map { it.count })
assertEquals(listOf("Italy", "Japan"), result.activeRegions.map { it.name })
assertNotNull(result.nearbyTrend)
```

- [ ] **Step 2: Run the analyzer test and verify RED**

Run: `./gradlew :app:testDebugUnitTest --tests '*StatisticsInsightsAnalyzerTest'`

Expected: compilation fails because insight models and analyzer do not exist.

- [ ] **Step 3: Implement immutable models and analyzer**

Use constants `BUCKET_COUNT = 30`, `NEARBY_RADIUS_KM = 1_000.0`, and `BUCKET_DURATION = Duration.ofDays(1)`. Filter events to `[window.start, window.end]`, calculate bucket index from epoch duration, and cap an event exactly at `window.end` into bucket 29. Normalize a region only when the place has a nonblank final comma-separated segment. Group case-insensitively and sort by count descending then label ascending.

```kotlin
data class StatisticsInsights(
    val globalTrend: List<TrendPoint>,
    val magnitudeDistribution: List<DistributionBucket>,
    val depthDistribution: List<DistributionBucket>,
    val activeRegions: List<ActiveRegion>,
    val nearbyTrend: List<TrendPoint>?
)

fun analyze(
    events: List<StatisticsEvent>,
    window: StatisticsWindow,
    origin: StatisticsLocation?
): StatisticsInsights
```

- [ ] **Step 4: Run analyzer tests and the full JVM suite**

Run: `./gradlew :app:testDebugUnitTest --tests '*StatisticsInsightsAnalyzerTest'`

Expected: PASS.

- [ ] **Step 5: Commit the domain checkpoint**

```bash
git add app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain
git commit -m "feat: analyze earthquake insight trends"
```

### Task 2: Persist Insights in Room

**Files:**
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/model/StatisticsModels.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsCacheEntity.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/data/local/db/EarthquakeDatabase.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/di/DatabaseModule.kt`
- Modify: `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsCacheEntityTest.kt`
- Modify: `app/src/androidTest/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsMigrationTest.kt`
- Generate: `app/schemas/com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDatabase/6.json`

**Interfaces:**
- Consumes: `StatisticsInsights` from Task 1.
- Produces: nullable `StatisticsSnapshot.insights`, `StatisticsCacheEntity.insightsJson`, Room database v6, and `MIGRATION_5_6`.

- [ ] **Step 1: Extend cache tests before production code**

Add round-trip assertions for all insight lists and null nearby trend. Add migration assertions that v5→v6 adds nullable `insightsJson`, while old rows map safely to `insights = null`.

```kotlin
assertEquals(snapshot.insights, snapshot.toCacheEntity().toSnapshot().insights)
helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)
```

- [ ] **Step 2: Run cache and migration compilation to verify RED**

Run: `./gradlew :app:testDebugUnitTest --tests '*StatisticsCacheEntityTest' :app:compileDebugAndroidTestKotlin`

Expected: FAIL because `insights`, `insightsJson`, and `MIGRATION_5_6` are missing.

- [ ] **Step 3: Implement the v6 cache migration**

Add nullable `insightsJson: String?` to the entity, serialize `StatisticsInsights` with Gson, and deserialize missing/blank/invalid legacy JSON as null. Migration SQL:

```sql
ALTER TABLE statistics_cache ADD COLUMN insightsJson TEXT
```

Increment Room to version 6 and register both `MIGRATION_4_5` and `MIGRATION_5_6`.

- [ ] **Step 4: Verify cache and migration tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests '*StatisticsCacheEntityTest'
ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsMigrationTest
```

Expected: PASS and schema `6.json` generated.

- [ ] **Step 5: Commit the persistence checkpoint**

```bash
git add app/src/main app/src/test app/src/androidTest app/schemas
git commit -m "feat: cache earthquake insights"
```

### Task 3: Reuse the 30-Day Dataset in the Repository

**Files:**
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/model/StatisticsModels.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/repository/EarthquakeStatisticsRepositoryImpl.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/di/RepositoryModule.kt`
- Modify: `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/repository/EarthquakeStatisticsRepositoryImplTest.kt`

**Interfaces:**
- Consumes: `StatisticsInsightsAnalyzer` and `LAST_30_DAYS` count.
- Produces: snapshot insights, `StatisticsSection.INSIGHTS`, `StatisticsSection.NEARBY_TREND`, and one paged event fetch shared by insights and nearest-today.

- [ ] **Step 1: Write repository tests for reuse and partial behavior**

Verify offsets follow the last-30-days count, only one set of `orderBy=time` pages is requested, nearest today ignores older events, insights include all 30 days, invalid location marks only `NEARBY_TREND`, a page failure marks `INSIGHTS` and `NEARBY_TREND` while retaining summary data, and only complete global insights are cached.

```kotlin
assertEquals(listOf(1, 20_001), remote.eventCalls.filter { it.orderBy == "time" }.map { it.offset })
assertNotNull(result.snapshot?.insights?.globalTrend)
assertTrue(StatisticsSection.NEARBY_TREND in result.unavailableSections)
```

- [ ] **Step 2: Run repository tests and verify RED**

Run: `./gradlew :app:testDebugUnitTest --tests '*EarthquakeStatisticsRepositoryImplTest'`

Expected: FAIL because insights and new section values are not produced.

- [ ] **Step 3: Implement one shared paged load**

Fetch pages using `windows.last30Days` and its count. Convert valid DTOs once, use events inside `windows.today` for nearest today, and call the analyzer with all converted events. Read effective location once. Keep count/strongest requests concurrent and preserve cancellation propagation.

- [ ] **Step 4: Verify repository and ViewModel suites**

Run: `./gradlew :app:testDebugUnitTest --tests '*EarthquakeStatisticsRepositoryImplTest' --tests '*StatisticsViewModelTest'`

Expected: PASS.

- [ ] **Step 5: Commit the repository checkpoint**

```bash
git add app/src/main app/src/test
git commit -m "feat: load earthquake insight data"
```

### Task 4: Render Accessible Insights

**Files:**
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/components/StatisticsTrendChart.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/components/StatisticsDistributionCard.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/components/StatisticsActiveRegionsCard.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-it-rIT/strings.xml`
- Modify: `app/src/androidTest/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsScreenTest.kt`

**Interfaces:**
- Consumes: `StatisticsSnapshot.insights` and insight availability sections.
- Produces: four themed insight cards with stable test tags and accessibility descriptions.

- [ ] **Step 1: Add failing Compose UI assertions**

Assert titles and tags `statistics-global-trend`, `statistics-magnitude-distribution`, `statistics-depth-distribution`, `statistics-active-regions`, and `statistics-nearby-trend`; assert content descriptions summarize total and peak counts; assert region names/counts; assert nearby-unavailable copy; and assert zero trends render without an error.

- [ ] **Step 2: Compile and run the UI test to verify RED**

Run:

```bash
./gradlew :app:compileDebugAndroidTestKotlin
ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui.StatisticsScreenTest
```

Expected: FAIL because insight components are absent.

- [ ] **Step 3: Implement the insight cards**

Use `Canvas` for a simple filled line/bar trend with Material colors and `clearAndSetSemantics` for a localized summary. Distribution cards render proportional horizontal bars plus text counts. Active regions render a numbered top-five list. Place all insight cards after nearest-today and show section-specific unavailable text instead of hiding the complete screen.

- [ ] **Step 4: Run UI instrumentation tests**

Run: `ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui.StatisticsScreenTest`

Expected: PASS.

- [ ] **Step 5: Commit the presentation checkpoint**

```bash
git add app/src/main app/src/androidTest
git commit -m "feat: present earthquake insights"
```

### Task 5: Full Regression Verification

**Files:**
- Review: all files changed by Tasks 1-4.

**Interfaces:**
- Consumes: the completed Insights feature.
- Produces: verified build and test evidence.

- [ ] **Step 1: Check the final diff and resource consistency**

Run: `git diff --check && git status --short`

Expected: no whitespace errors and only intended changes.

- [ ] **Step 2: Run JVM tests, lint, and debug assembly**

Run: `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`

Expected: BUILD SUCCESSFUL with zero test or lint errors.

- [ ] **Step 3: Run all instrumentation tests on the emulator**

Run: `ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest`

Expected: BUILD SUCCESSFUL with zero failed instrumentation tests.

- [ ] **Step 4: Review requirements against the approved design**

Confirm fixed M2.5+, 30 buckets, all distribution boundaries, deterministic top five, 1,000 km nearby behavior, partial states, cache migration, accessibility, and unchanged MVP rendering.

- [ ] **Step 5: Commit any verification-only corrections**

```bash
git add app/src app/schemas
git commit -m "test: verify earthquake insights"
```
