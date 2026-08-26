# Earthquake Statistics MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an accurate, cached Statistics top-level screen showing global M2.5+ counts, strongest today, and nearest today without depending on list filters.

**Architecture:** A dedicated `feat_statistics` feature uses a hybrid USGS repository: four count requests, one strongest-event query, and paged today-event queries for nearest-distance calculation. A single-row Room cache supports stale-while-refresh behavior, while `StatisticsViewModel` exposes one immutable `StatisticsUiState` to a stateless Compose screen integrated into Navigation 3.

**Tech Stack:** Kotlin, Coroutines/Flow, Ktor/Gson, Room 2.7.1, Hilt, Jetpack Compose Material 3, Navigation 3, JUnit 4, kotlinx-coroutines-test, Ktor MockEngine, Compose UI test.

## Global Constraints

- Statistics are global and always use `minmagnitude=2.5`; list filter preferences never affect them.
- `Today` and `This year` use the device's local calendar boundaries converted to UTC.
- `Last 7 days` and `Last 30 days` are rolling durations from one shared refresh instant.
- Statistics cache freshness is exactly 15 minutes.
- Cached values stay visible during refresh and network errors.
- Only complete refreshes replace the persisted cache; partial results are in-memory only.
- Existing list refresh, Room earthquake records, ads, settings, units, themes, and navigation behavior remain unchanged.
- No embedded map, chart, notification change, or configurable statistics threshold belongs in this MVP.
- All user-facing text is localized in English and Italian.

---

## File Structure

### New production files

- `feat_statistics/domain/model/StatisticsModels.kt`: time windows, counts, event summaries, snapshots, sections, and load results.
- `feat_statistics/domain/time/StatisticsTimeProvider.kt`: injectable clock/time-zone boundary calculation.
- `feat_statistics/domain/repository/EarthquakeStatisticsRepository.kt`: feature repository contract.
- `feat_statistics/domain/use_cases/LoadEarthquakeStatisticsUseCase.kt`: business-intent entry point.
- `feat_statistics/data/remote/EarthquakeStatisticsApi.kt`: USGS count and event requests.
- `feat_statistics/data/local/StatisticsCacheEntity.kt`: single-row cache schema and domain mappings.
- `feat_statistics/data/local/StatisticsCacheDao.kt`: cache read/replace operations.
- `feat_statistics/data/repository/EarthquakeStatisticsRepositoryImpl.kt`: concurrency, pagination, nearest calculation, cache policy, and failures.
- `feat_statistics/presentation/state/StatisticsUiState.kt`: the complete render state.
- `feat_statistics/presentation/ui/StatisticsViewModel.kt`: startup/refresh orchestration.
- `feat_statistics/presentation/ui/StatisticsScreen.kt`: route and stateless content.
- `feat_statistics/presentation/components/StatisticsCountStrip.kt`: four accessible count cells.
- `feat_statistics/presentation/components/StatisticsEventCard.kt`: strongest/nearest cards.

### Modified production files

- `feat_eqslist/data/local/db/EarthquakeDatabase.kt`: register cache entity/DAO and migration 4→5.
- `core/di/DatabaseModule.kt`: provide cache DAO.
- `core/di/RepositoryModule.kt`: provide statistics repository and time provider.
- `core/presentation/navigation/AppDestination.kt`: add Statistics top-level destination.
- `core/presentation/navigation/AppBottomBar.kt`: add Statistics item.
- `core/presentation/navigation/AppNavigationHost.kt`: add Statistics entry and Details callback.
- `core/presentation/navigation/AppNavigationScreenFactory.kt`: add Statistics factory method.
- `res/values/strings.xml` and `res/values-it-rIT/strings.xml`: localized screen, state, and accessibility copy.
- `app/build.gradle.kts` and `gradle/libs.versions.toml`: Room schema export and Ktor MockEngine test dependency.

### New tests

- `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/time/StatisticsTimeProviderTest.kt`
- `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/remote/EarthquakeStatisticsApiTest.kt`
- `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/repository/EarthquakeStatisticsRepositoryImplTest.kt`
- `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsViewModelTest.kt`
- `app/src/androidTest/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsMigrationTest.kt`
- `app/src/androidTest/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsScreenTest.kt`

### Modified tests

- `app/src/test/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigatorTest.kt`
- `app/src/test/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/TestDestinations.kt`
- `app/src/androidTest/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigationHostTest.kt`

---

### Task 1: Deterministic Statistics Domain And Time Windows

**Files:**
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/model/StatisticsModels.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/time/StatisticsTimeProvider.kt`
- Test: `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/time/StatisticsTimeProviderTest.kt`

**Interfaces:**
- Produces: `StatisticsPeriod`, `StatisticsWindow`, `StatisticsWindows`, `StatisticsCounts`, `StatisticsEvent`, `StatisticsSnapshot`, `StatisticsSection`, `StatisticsLoadResult`, and `StatisticsTimeProvider.windows()`.
- Consumes: Java `Clock`, `Instant`, and `ZoneId`; no Android types.

- [ ] **Step 1: Write failing boundary tests**

```kotlin
class StatisticsTimeProviderTest {
    private val now = Instant.parse("2026-08-14T10:30:00Z")

    @Test fun rome_calendar_boundaries_are_converted_to_utc() {
        val provider = StatisticsTimeProvider(
            Clock.fixed(now, ZoneOffset.UTC),
            ZoneId.of("Europe/Rome")
        )

        val windows = provider.windows()

        assertEquals(Instant.parse("2026-08-13T22:00:00Z"), windows.today.start)
        assertEquals(Instant.parse("2025-12-31T23:00:00Z"), windows.year.start)
        assertEquals(now, windows.today.end)
        assertEquals(now.minus(7, ChronoUnit.DAYS), windows.last7Days.start)
        assertEquals(now.minus(30, ChronoUnit.DAYS), windows.last30Days.start)
    }

    @Test fun all_windows_share_one_end_instant() {
        val windows = StatisticsTimeProvider(
            Clock.fixed(now, ZoneOffset.UTC),
            ZoneId.of("UTC")
        ).windows()

        assertEquals(setOf(now), windows.all.map { it.end }.toSet())
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*StatisticsTimeProviderTest'`

Expected: FAIL because `StatisticsTimeProvider` and the domain models do not exist.

- [ ] **Step 3: Implement the domain types and boundary provider**

```kotlin
enum class StatisticsPeriod { TODAY, LAST_7_DAYS, LAST_30_DAYS, YEAR }

data class StatisticsWindow(
    val period: StatisticsPeriod,
    val start: Instant,
    val end: Instant
)

data class StatisticsWindows(
    val today: StatisticsWindow,
    val last7Days: StatisticsWindow,
    val last30Days: StatisticsWindow,
    val year: StatisticsWindow
) {
    val all: List<StatisticsWindow>
        get() = listOf(today, last7Days, last30Days, year)
}

data class StatisticsCounts(
    val today: Int?,
    val last7Days: Int?,
    val last30Days: Int?,
    val year: Int?
)

data class StatisticsEvent(
    val id: String,
    val magnitude: Double,
    val place: String,
    val time: Long,
    val depthKm: Double?,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double? = null
)

data class StatisticsSnapshot(
    val counts: StatisticsCounts,
    val strongestToday: StatisticsEvent?,
    val nearestToday: StatisticsEvent?,
    val threshold: Double,
    val retrievedAt: Instant,
    val windows: StatisticsWindows
)

enum class StatisticsSection { TODAY_COUNT, WEEK_COUNT, MONTH_COUNT, YEAR_COUNT, STRONGEST, NEAREST }

data class StatisticsLoadResult(
    val snapshot: StatisticsSnapshot?,
    val unavailableSections: Set<StatisticsSection> = emptySet(),
    val isFromCache: Boolean = false,
    val isStale: Boolean = false
)

class StatisticsTimeProvider(
    private val clock: Clock,
    private val zoneId: ZoneId
) {
    fun windows(): StatisticsWindows {
        val now = clock.instant()
        val todayStart = now.atZone(zoneId).toLocalDate().atStartOfDay(zoneId).toInstant()
        val yearStart = now.atZone(zoneId).toLocalDate().withDayOfYear(1)
            .atStartOfDay(zoneId).toInstant()
        return StatisticsWindows(
            today = StatisticsWindow(StatisticsPeriod.TODAY, todayStart, now),
            last7Days = StatisticsWindow(StatisticsPeriod.LAST_7_DAYS, now.minus(7, ChronoUnit.DAYS), now),
            last30Days = StatisticsWindow(StatisticsPeriod.LAST_30_DAYS, now.minus(30, ChronoUnit.DAYS), now),
            year = StatisticsWindow(StatisticsPeriod.YEAR, yearStart, now)
        )
    }
}
```

Nullable count fields represent unavailable partial sections, never zero. A complete result suitable for Room caching requires all four count fields to be non-null and `unavailableSections` to be empty. A null highlighted event with its section available means USGS returned no qualifying event; a null event whose section is unavailable means its request failed.

- [ ] **Step 4: Run the focused tests**

Run: `./gradlew :app:testDebugUnitTest --tests '*StatisticsTimeProviderTest'`

Expected: PASS.

- [ ] **Step 5: Commit the domain slice**

```bash
git add app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain
git commit -m "feat: define earthquake statistics domain"
```

---

### Task 2: USGS Count And Targeted Event API

**Files:**
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/remote/EarthquakeStatisticsApi.kt`
- Test: `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/remote/EarthquakeStatisticsApiTest.kt`
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Consumes: `StatisticsWindow`, existing `EQFeaturesCollectionDTO`, and `@UsgsClient HttpClient`.
- Produces: `suspend fun count(window, threshold): Int` and `suspend fun events(window, threshold, orderBy, limit, offset): EQFeaturesCollectionDTO`.

- [ ] **Step 1: Add Ktor MockEngine as a test-only dependency**

```toml
ktorClientMock = "2.3.7"
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktorClientMock" }
```

```kotlin
testImplementation(libs.ktor.client.mock)
```

- [ ] **Step 2: Write failing request-contract tests**

```kotlin
@Test fun count_sends_global_magnitude_and_time_parameters() = runTest {
    val requests = mutableListOf<HttpRequestData>()
    val api = EarthquakeStatisticsApi(mockClient(requests, "146"))
    val window = StatisticsWindow(
        StatisticsPeriod.TODAY,
        Instant.parse("2026-08-13T22:00:00Z"),
        Instant.parse("2026-08-14T10:30:00Z")
    )

    assertEquals(146, api.count(window, 2.5))
    assertEquals("/fdsnws/event/1/count", requests.single().url.encodedPath)
    assertEquals("2.5", requests.single().url.parameters["minmagnitude"])
    assertEquals("earthquake", requests.single().url.parameters["eventtype"])
}

@Test fun event_query_preserves_order_limit_and_offset() = runTest {
    val requests = mutableListOf<HttpRequestData>()
    val api = EarthquakeStatisticsApi(mockJsonClient(requests, emptyFeatureCollectionJson))

    api.events(window, 2.5, orderBy = "magnitude", limit = 1, offset = 20_001)

    val parameters = requests.single().url.parameters
    assertEquals("magnitude", parameters["orderby"])
    assertEquals("1", parameters["limit"])
    assertEquals("20001", parameters["offset"])
}
```

- [ ] **Step 3: Run the API tests and confirm they fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*EarthquakeStatisticsApiTest'`

Expected: FAIL because the API does not exist.

- [ ] **Step 4: Implement the dedicated API**

```kotlin
class EarthquakeStatisticsApi @Inject constructor(
    @UsgsClient private val client: HttpClient
) {
    suspend fun count(window: StatisticsWindow, threshold: Double): Int =
        client.get {
            usgsPath("count")
            commonParameters(window, threshold)
        }.bodyAsText().trim().toInt()

    suspend fun events(
        window: StatisticsWindow,
        threshold: Double,
        orderBy: String,
        limit: Int,
        offset: Int = 1
    ): EQFeaturesCollectionDTO = client.get {
        usgsPath("query")
        parameter("format", "geojson")
        commonParameters(window, threshold)
        parameter("orderby", orderBy)
        parameter("limit", limit)
        parameter("offset", offset)
    }.body()
}
```

Implement `usgsPath()` and `commonParameters()` as private `HttpRequestBuilder` extensions using ISO-8601 instants. Do not reuse `EarthquakeQueryParams.limit = 200`.

- [ ] **Step 5: Run API tests**

Run: `./gradlew :app:testDebugUnitTest --tests '*EarthquakeStatisticsApiTest'`

Expected: PASS.

- [ ] **Step 6: Commit the remote slice**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/remote app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/remote
git commit -m "feat: add USGS statistics requests"
```

---

### Task 3: Room Statistics Cache And Migration

**Files:**
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsCacheEntity.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsCacheDao.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/data/local/db/EarthquakeDatabase.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/di/DatabaseModule.kt`
- Modify: `app/build.gradle.kts`
- Create: `app/src/androidTest/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsMigrationTest.kt`
- Generate: `app/schemas/com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDatabase/5.json`

**Interfaces:**
- Produces: `StatisticsCacheDao.get(): StatisticsCacheEntity?`, `replace(entity)`, `StatisticsSnapshot.toCacheEntity()`, and `StatisticsCacheEntity.toSnapshot()`.
- Consumes: Task 1 domain models.

- [ ] **Step 1: Configure Room schema export and capture the current version-4 schema**

Add to the Android configuration:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

Run while `EarthquakeDatabase.version` is still `4`:

`./gradlew :app:kspDebugKotlin`

Expected: `app/schemas/com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDatabase/4.json` exists and represents the pre-feature schema.

- [ ] **Step 2: Write the failing migration test**

Write a migration test that creates schema version 4, runs `MIGRATION_4_5`, and verifies the new table:

```kotlin
@RunWith(AndroidJUnit4::class)
class StatisticsMigrationTest {
    @get:Rule val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EarthquakeDatabase::class.java
    )

    @Test fun migration_4_5_adds_empty_statistics_cache_without_touching_earthquakes() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """INSERT INTO feed_snapshot (
                    generated, url, title, api, count, status,
                    minLon, minLat, minDepth, maxLon, maxLat, maxDepth
                ) VALUES (1, 'url', 'title', '1.0', 1, 200,
                    NULL, NULL, NULL, NULL, NULL, NULL)""".trimIndent()
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5).use { db ->
            assertEquals(1, db.query("SELECT COUNT(*) FROM feed_snapshot").singleInt())
            assertEquals(0, db.query("SELECT COUNT(*) FROM statistics_cache").singleInt())
        }
    }
}
```

- [ ] **Step 3: Run the migration test and confirm it fails**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsMigrationTest`

Expected: FAIL because schema v5, the entity, and `MIGRATION_4_5` do not exist.

- [ ] **Step 4: Add the single-row entity, DAO, and mappings**

```kotlin
@Entity(tableName = "statistics_cache")
data class StatisticsCacheEntity(
    @PrimaryKey val cacheId: Int = 1,
    val todayCount: Int,
    val weekCount: Int,
    val monthCount: Int,
    val yearCount: Int,
    val threshold: Double,
    val retrievedAtEpochMillis: Long,
    val windowsJson: String,
    val strongestEventJson: String?,
    val nearestEventJson: String?
)

@Dao
interface StatisticsCacheDao {
    @Query("SELECT * FROM statistics_cache WHERE cacheId = 1")
    suspend fun get(): StatisticsCacheEntity?

    @Upsert
    suspend fun replace(entity: StatisticsCacheEntity)
}
```

Use the existing Gson dependency in mapping functions for nested windows/events. Keep JSON serialization confined to the data-local package.

- [ ] **Step 5: Register schema version 5 and explicit migration**

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS statistics_cache (
                cacheId INTEGER NOT NULL PRIMARY KEY,
                todayCount INTEGER NOT NULL,
                weekCount INTEGER NOT NULL,
                monthCount INTEGER NOT NULL,
                yearCount INTEGER NOT NULL,
                threshold REAL NOT NULL,
                retrievedAtEpochMillis INTEGER NOT NULL,
                windowsJson TEXT NOT NULL,
                strongestEventJson TEXT,
                nearestEventJson TEXT
            )""".trimIndent()
        )
    }
}
```

Add `StatisticsCacheEntity` to `@Database`, set `version = 5`, expose `statisticsCacheDao()`, register `.addMigrations(MIGRATION_4_5)`, and provide the DAO from `DatabaseModule`.

- [ ] **Step 6: Generate schema and run migration test**

Run: `./gradlew :app:kspDebugKotlin :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsMigrationTest`

Expected: schema `5.json` is generated and the migration test passes.

- [ ] **Step 7: Commit cache and migration**

```bash
git add app/build.gradle.kts app/schemas app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/data/local/db/EarthquakeDatabase.kt app/src/main/java/com/indiewalk/watchdog/earthquake/core/di/DatabaseModule.kt app/src/androidTest/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local
git commit -m "feat: cache earthquake statistics"
```

---

### Task 4: Hybrid Statistics Repository

**Files:**
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/repository/EarthquakeStatisticsRepository.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/domain/use_cases/LoadEarthquakeStatisticsUseCase.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/repository/EarthquakeStatisticsRepositoryImpl.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/di/RepositoryModule.kt`
- Test: `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/repository/EarthquakeStatisticsRepositoryImplTest.kt`

**Interfaces:**
- Consumes: Task 1 models/time provider, Task 2 API, Task 3 cache DAO, and `AppPreferencesRepository.getCurrentSettings()`.
- Produces: `fun load(forceRefresh: Boolean): Flow<StatisticsLoadResult>` and `LoadEarthquakeStatisticsUseCase(forceRefresh)`.

- [ ] **Step 1: Write failing repository policy tests**

Cover these named tests with fakes for API, cache DAO, time provider, and preferences. Each test collects the returned flow with `toList()` and asserts the exact emissions described by its name:

```kotlin
@Test fun fresh_cache_returns_without_network()
@Test fun stale_cache_emits_before_remote_replacement()
@Test fun force_refresh_runs_four_counts_and_targeted_queries()
@Test fun complete_refresh_replaces_cache_once()
@Test fun partial_refresh_does_not_replace_complete_cache()
@Test fun nearest_uses_manual_position_when_enabled()
@Test fun nearest_uses_user_position_when_manual_is_disabled()
@Test fun invalid_effective_position_marks_nearest_unavailable()
@Test fun nearest_pages_past_twenty_thousand_when_required()
@Test fun cancellation_is_rethrown()
```

Assertions: fresh cache emits once and records zero API calls; stale cache emits first with `isFromCache=true` and then emits the network result; forced refresh records exactly four count calls; complete refresh records one DAO replacement; partial refresh records zero replacements and the failed section; manual/user location tests assert the nearest event id and Haversine distance; invalid latitude/longitude produces `StatisticsSection.NEAREST` without an event query; pagination asserts offsets `1` and `20_001`; cancellation asserts `CancellationException` escapes.

- [ ] **Step 2: Run repository tests and confirm they fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*EarthquakeStatisticsRepositoryImplTest'`

Expected: FAIL because the repository does not exist.

- [ ] **Step 3: Define repository and use-case contracts**

```kotlin
interface EarthquakeStatisticsRepository {
    fun load(forceRefresh: Boolean): Flow<StatisticsLoadResult>
}

class LoadEarthquakeStatisticsUseCase @Inject constructor(
    private val repository: EarthquakeStatisticsRepository
) {
    operator fun invoke(forceRefresh: Boolean): Flow<StatisticsLoadResult> =
        repository.load(forceRefresh)
}
```

- [ ] **Step 4: Implement fresh/stale cache policy**

```kotlin
private val cacheTtl = Duration.ofMinutes(15)

override fun load(forceRefresh: Boolean): Flow<StatisticsLoadResult> = flow {
    val cached = cacheDao.get()?.toSnapshot()
    val windows = timeProvider.windows()
    val isFresh = cached != null &&
        Duration.between(cached.retrievedAt, windows.today.end) < cacheTtl

    if (cached != null) {
        emit(StatisticsLoadResult(cached, isFromCache = true, isStale = !isFresh))
    }
    if (!forceRefresh && isFresh) return@flow

    emit(refresh(cached, windows))
}
```

When remote refresh totally fails and cache exists, do not emit the same snapshot twice; throw a typed network failure carrying `hasCachedContent=true`, which lets the ViewModel preserve the already-emitted cache and show stale status. When no cache exists and no section succeeds, throw the same typed failure with `hasCachedContent=false` for the full-screen error state.

- [ ] **Step 5: Implement concurrent counts, strongest, pagination, and partial results**

Use `supervisorScope` so one failed section does not cancel successful sections. Launch four count calls and strongest concurrently. Use the successful today count to request today's event pages in chunks of 20,000, then map valid DTOs and choose `minByOrNull(distanceKm)`. Emit the assembled network result after all section jobs settle.

```kotlin
private fun EQFeatureDTO.toStatisticsEvent(distanceKm: Double? = null): StatisticsEvent? {
    val magnitude = properties.mag ?: return null
    val place = properties.place ?: return null
    val time = properties.time ?: return null
    val latitude = geometry.latitude ?: return null
    val longitude = geometry.longitude ?: return null
    return StatisticsEvent(id, magnitude, place, time, geometry.depthKm, latitude, longitude, distanceKm)
}
```

Create a partial `StatisticsSnapshot` from every successful section, leaving failed count values null and recording failures in `unavailableSections`. Cache only when all four counts are non-null and `unavailableSections` is empty. A legitimate empty strongest/nearest result is successful and stores `null`; transport failures mark the respective section unavailable.

- [ ] **Step 6: Bind repository and production time provider in Hilt**

```kotlin
@Provides @Singleton
fun provideStatisticsTimeProvider(): StatisticsTimeProvider =
    StatisticsTimeProvider(Clock.systemUTC(), ZoneId.systemDefault())

@Provides @Singleton
fun provideEarthquakeStatisticsRepository(
    api: EarthquakeStatisticsApi,
    cacheDao: StatisticsCacheDao,
    appPreferencesRepository: AppPreferencesRepository,
    timeProvider: StatisticsTimeProvider
): EarthquakeStatisticsRepository = EarthquakeStatisticsRepositoryImpl(
    api, cacheDao, appPreferencesRepository, timeProvider
)
```

- [ ] **Step 7: Run repository tests**

Run: `./gradlew :app:testDebugUnitTest --tests '*EarthquakeStatisticsRepositoryImplTest'`

Expected: PASS.

- [ ] **Step 8: Commit repository orchestration**

```bash
git add app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics app/src/main/java/com/indiewalk/watchdog/earthquake/core/di/RepositoryModule.kt app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/repository
git commit -m "feat: load global earthquake statistics"
```

---

### Task 5: Statistics ViewModel State And Refresh Behavior

**Files:**
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/state/StatisticsUiState.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsViewModel.kt`
- Test: `app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsViewModelTest.kt`

**Interfaces:**
- Consumes: `LoadEarthquakeStatisticsUseCase` and `AppPreferencesRepository.settingsFlow`.
- Produces: `StateFlow<StatisticsUiState>`, `onScreenStarted()`, and `onRefreshRequested()`.

- [ ] **Step 1: Write failing ViewModel tests**

```kotlin
@get:Rule val mainDispatcherRule = MainDispatcherRule()

@Test fun startup_loads_once_and_exposes_snapshot()
@Test fun manual_refresh_forces_network_and_keeps_existing_content_visible()
@Test fun cached_result_is_labeled_without_blocking_loading()
@Test fun partial_result_exposes_unavailable_sections()
@Test fun uncached_failure_exposes_full_screen_error()
@Test fun settings_updates_change_only_unit_rendering_input()
```

Assertions: calling `onScreenStarted()` twice records one `forceRefresh=false` load; manual refresh records `true`, sets `isRefreshing`, and retains the previous snapshot; cached emissions set `isFromCache`; partial emissions preserve their `unavailableSections`; uncached failure clears initial loading and sets `AppError.Network`; changing settings changes `uiState.settings` without replacing `uiState.snapshot`.

- [ ] **Step 2: Run tests and confirm they fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*StatisticsViewModelTest'`

Expected: FAIL because ViewModel/state do not exist.

- [ ] **Step 3: Implement the state contract**

```kotlin
data class StatisticsUiState(
    val snapshot: StatisticsSnapshot? = null,
    val settings: AppSettings = AppSettings(),
    val unavailableSections: Set<StatisticsSection> = emptySet(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isFromCache: Boolean = false,
    val isStale: Boolean = false,
    val error: AppError? = null
) {
    val hasContent: Boolean get() = snapshot != null
}
```

- [ ] **Step 4: Implement one-flow ViewModel orchestration**

```kotlin
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val loadStatistics: LoadEarthquakeStatisticsUseCase,
    appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    private var hasStarted = false

    fun onScreenStarted() {
        if (hasStarted) return
        hasStarted = true
        load(forceRefresh = false)
    }

    fun onRefreshRequested() = load(forceRefresh = true)
}
```

Observe settings in `init`. In `load`, retain the current snapshot while refreshing, map repository failures to `AppError.Network`, and always reset loading flags in `finally` without swallowing cancellation.

- [ ] **Step 5: Run ViewModel tests**

Run: `./gradlew :app:testDebugUnitTest --tests '*StatisticsViewModelTest'`

Expected: PASS.

- [ ] **Step 6: Commit ViewModel slice**

```bash
git add app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/state app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsViewModel.kt app/src/test/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui
git commit -m "feat: add statistics screen state"
```

---

### Task 6: Layout B Compose Screen And Accessibility

**Files:**
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/components/StatisticsCountStrip.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/components/StatisticsEventCard.kt`
- Create: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-it-rIT/strings.xml`
- Test: `app/src/androidTest/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsScreenTest.kt`

**Interfaces:**
- Consumes: `StatisticsUiState`, ViewModel intent methods, current `AppDestination`, top-level selection callback, and event-id callback.
- Produces: `StatisticsScreen(currentDestination, onTopLevelDestinationSelected, onOpenDetails, viewModel)` route and `StatisticsContent(uiState, onRefresh, onEventClick, modifier)` stateless renderer.

- [ ] **Step 1: Write failing Compose state tests**

```kotlin
@Test fun data_state_shows_threshold_counts_and_event_cards() {
    setStatisticsContent(populatedState)
    composeRule.onNodeWithText("Global earthquakes · M2.5+").assertExists()
    composeRule.onNodeWithContentDescription("146 earthquakes today").assertExists()
    composeRule.onNodeWithText("Strongest today").assertExists()
    composeRule.onNodeWithText("Nearest today").assertExists()
}

@Test fun strongest_card_forwards_event_id()
@Test fun nearest_card_forwards_event_id()
@Test fun loading_without_cache_shows_progress()
@Test fun stale_cache_and_partial_states_are_announced()
@Test fun empty_highlight_uses_empty_copy_not_fake_values()
@Test fun unavailable_location_explains_why_nearest_is_missing()
@Test fun large_font_layout_keeps_complete_count_semantics()
```

Assertions: card tests click `statistics-strongest`/`statistics-nearest` and compare the captured id; loading asserts `statistics-loading`; stale/partial states assert localized status text; null highlights assert localized empty text and absence of magnitude placeholders; unavailable nearest asserts the localized location explanation; the large-font test uses `fontScale=2f` and still finds all four complete count content descriptions.

- [ ] **Step 2: Run the Compose tests and confirm they fail**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui.StatisticsScreenTest`

Expected: FAIL because the screen does not exist.

- [ ] **Step 3: Add English and Italian copy**

Add strings for Statistics/Statistiche, the global M2.5+ scope, four period labels, strongest/nearest headings, updated/cached/partial states, no-event states, retry, magnitude, depth, distance, and complete count content descriptions. Use positional format arguments instead of string concatenation.

- [ ] **Step 4: Implement Layout B components**

`StatisticsCountStrip` renders four equal cells in one surface. Visual values use locale-aware compact formatting when needed; each cell merges semantics into a complete phrase.

`StatisticsEventCard` reuses `MagnitudeBubble`, current magnitude colors, date/unit format utilities, and a minimum 48 dp click target. It receives only display data and `onClick`.

```kotlin
@Composable
fun StatisticsContent(
    uiState: StatisticsUiState,
    onRefresh: () -> Unit,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

Render in this order: title/app bar, scope + update row, count strip, strongest card, nearest card, optional cache/partial message, and ad banner consistent with the other top-level screens. Preserve content while the pull-refresh indicator runs.

- [ ] **Step 5: Implement the route wrapper**

```kotlin
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
        topBar = { StatisticsTopBar() }
    ) { padding ->
        StatisticsContent(
            uiState = uiState,
            onRefresh = viewModel::onRefreshRequested,
            onEventClick = onOpenDetails,
            modifier = Modifier.padding(padding)
        )
    }
}
```

- [ ] **Step 6: Run Compose tests**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.feat_statistics.presentation.ui.StatisticsScreenTest`

Expected: PASS on the configured emulator/device.

- [ ] **Step 7: Commit the Compose screen**

```bash
git add app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation app/src/main/res/values/strings.xml app/src/main/res/values-it-rIT/strings.xml app/src/androidTest/java/com/indiewalk/watchdog/earthquake/feat_statistics/presentation
git commit -m "feat: add earthquake statistics screen"
```

---

### Task 7: Navigation 3 And Bottom Bar Integration

**Files:**
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppDestination.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppBottomBar.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigationHost.kt`
- Modify: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigationScreenFactory.kt`
- Modify: `app/src/test/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigatorTest.kt`
- Modify: `app/src/test/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/TestDestinations.kt`
- Modify: `app/src/androidTest/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigationHostTest.kt`

**Interfaces:**
- Consumes: `StatisticsScreen` from Task 6.
- Produces: `AppDestination.Statistics`, fourth bottom item, host entry, and `AppNavigationScreenFactory.Statistics(currentDestination, onTopLevelDestinationSelected, onOpenDetails)`.

- [ ] **Step 1: Extend failing navigator tests**

```kotlin
@Test fun statistics_is_an_explicit_top_level_destination() {
    assertTrue(AppDestination.Statistics.isTopLevel)
    assertTrue(AppDestination.Statistics::class in appTopLevelDestinationClasses)
}

@Test fun switching_to_statistics_preserves_top_level_semantics() {
    val navigator = appNavigatorStartingAtHome()
    navigator.switchTopLevel(AppDestination.Statistics)
    assertEquals(AppDestination.Statistics, navigator.currentDestination)
}
```

Add `Statistics` to `TestDestination` and all explicit top-level sets in existing tests.

- [ ] **Step 2: Extend failing host-flow tests**

Add a fake `Statistics` screen containing `statistics-screen`, `statistics-open-details`, and `statistics-open-home` controls. Test Home → Statistics, Statistics → Details → system back, and Statistics → Home.

- [ ] **Step 3: Run navigation tests and confirm they fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*AppNavigatorTest'`

Expected: FAIL because Statistics is not a destination.

- [ ] **Step 4: Add destination and bottom item**

```kotlin
data object Statistics : AppDestination
```

Add it to `isTopLevel`, `appTopLevelDestinationClasses`, and exhaustive `when` expressions. Add a Material analytics/bar-chart icon and localized `Statistics` label between Map and Settings in `AppBottomBar`.

- [ ] **Step 5: Wire host and factory**

Add:

```kotlin
@Composable
fun Statistics(
    currentDestination: AppDestination,
    onTopLevelDestinationSelected: (AppDestination) -> Unit,
    onOpenDetails: (String) -> Unit
)
```

The host entry invokes the factory and maps `onOpenDetails` to `navigator.navigateTo(AppDestination.Details(id))`.

- [ ] **Step 6: Run unit and host navigation tests**

Run: `./gradlew :app:testDebugUnitTest --tests '*AppNavigatorTest'`

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationHostTest`

Expected: PASS.

- [ ] **Step 7: Commit navigation integration**

```bash
git add app/src/main/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation app/src/test/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation app/src/androidTest/java/com/indiewalk/watchdog/earthquake/core/presentation/navigation
git commit -m "feat: add statistics navigation destination"
```

---

### Task 8: Full Regression Verification And Cleanup

**Files:**
- Modify only files already touched if verification reveals feature-specific defects.

**Interfaces:**
- Consumes: all previous tasks.
- Produces: a verified MVP with no known regression in existing features.

- [ ] **Step 1: Run all JVM unit tests**

Run: `./gradlew :app:testDebugUnitTest`

Expected: BUILD SUCCESSFUL with all existing and statistics tests passing.

- [ ] **Step 2: Run all Compose/instrumentation tests**

Run: `./gradlew :app:connectedDebugAndroidTest`

Expected: BUILD SUCCESSFUL on a connected emulator/device.

- [ ] **Step 3: Run lint and debug assembly**

Run: `./gradlew :app:lintDebug :app:assembleDebug`

Expected: BUILD SUCCESSFUL; no new statistics or navigation errors.

- [ ] **Step 4: Smoke-test on a device**

Verify these exact flows:

1. Open Statistics from Home, Map, and Settings.
2. Confirm the visible scope is global M2.5+ and independent of list filters.
3. Confirm counts and cards render after refresh.
4. Tap strongest and nearest cards and return from Details.
5. Disable network and confirm cached content remains visible.
6. Switch metric/imperial settings and confirm nearest distance changes unit.
7. Switch light/dark/system themes and confirm readable contrast.
8. Increase system font size and confirm the count strip and cards remain usable.

- [ ] **Step 5: Inspect final diff and database schema**

Run: `git diff --check`

Run: `git status --short`

Confirm only intended statistics, navigation, tests, localization, dependency, and Room schema files are changed.

- [ ] **Step 6: Commit verification fixes if any were required**

```bash
git add app app/schemas gradle/libs.versions.toml
git commit -m "test: verify earthquake statistics MVP"
```

Skip this commit when verification required no code changes.
