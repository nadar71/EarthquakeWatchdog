# Device Test Matrix

This matrix separates deterministic automated evidence from checks that require
real devices, installed Play builds, or external SDK services. Do not mark a
pending check as passed without its recorded device and build evidence.

## Automated Results

| Area | Evidence | Result |
| --- | --- | --- |
| Navigation 3 routing and restoration | `AppNavigationHostTest`, `AppNavigationHostRestorationTest`, `AppNavigationHostRoutingJourneyTest` | Covers host routing plus top-level and details-route restoration through `scenario.recreate()`. The debug-only restoration host has no feature dependencies. |
| Production state-driven feature content | `ProductionUiContentTest`, `StatisticsScreenTest` | Exercises real list refresh/filter callbacks, all bottom destinations, selected-map detail dismissal, settings callbacks/state, and statistics event details without live USGS, Maps tiles, ads, consent, or location. |
| Statistics rendering and chart accessibility | `StatisticsScreenTest`, `AccessibilitySmokeTest` | Renders supplied UI state only, including text scale and chart semantics. |
| Room upgrade paths | `StatisticsMigrationTest` | Covers `4 -> 5`, `5 -> 6`, and `4 -> 6`; preserves a seeded earthquake row and validates the destination statistics-cache columns. |
| Map selected-event state | `MapViewModelTest` | Verifies selected marker detail state and dismissal without Google Maps rendering. |

## Available Emulator Results

| Device | API | Build | Checks | Result | Evidence |
| --- | --- | --- | --- | --- | --- |
| `Medium_Phone` | 36 | Debug instrumentation | Focused Navigation 3, production-content, statistics, and accessibility tests: 1.3x/2.0x intro and filter reachability, 48dp actions, icon semantics | Passed: 26 tests | Focused `:app:connectedDebugAndroidTest` run on 2026-08-27 |
| `Medium_Phone` | 36 | Debug instrumentation | Room migrations `4 -> 5`, `5 -> 6`, `4 -> 6` | Passed | `:app:connectedDebugAndroidTest` with `StatisticsMigrationTest` |
| `Medium_Phone` | 36 | Debug instrumentation | Navigation host, production content, statistics UI, accessibility, and Room migrations | Passed: 29 tests, 0 failures | Full `:app:connectedDebugAndroidTest` run on 2026-08-27. |

The emulator-only tests deliberately do not load USGS, Google Maps tiles, ads,
consent, external geocoding, or device location. Those dependencies remain
manual external checks below.

## Pending Physical And External Checks

| Check | Required environment | Status | Evidence to record |
| --- | --- | --- | --- |
| Fresh install | API 26 emulator | Pending | Device/API, debug or release build, launch screenshot/log. |
| Fresh install | Current API emulator | Pending final manual smoke | Device/API, build, intro and list result. |
| Play-version upgrade | Physical device with installed Play production version | Pending | Starting versionCode, installed signed update, database/list/statistics result. |
| Offline first launch and cached launch | API 26 and current API emulator | Pending | Network state, launch behavior, cached data result. |
| Permission grant, deny, and skip | API 26 and physical device | Pending | Permission path and resulting user/manual location behavior. |
| Manual location and restore to user/default | API 26 and physical device | Pending | Selected location, restart behavior, map recenter result. |
| Google Maps rendering and marker detail | Physical device with release Maps key | Pending | Map tiles, marker selection/dismissal, key and package restriction validation. |
| Ads and consent | Physical device with production configuration | Pending | Consent path, no-consent fallback, production ad behavior. |
| Themes and units | API 26 and current API emulator | Pending | Light/dark/system and metric/imperial screenshots. |
| Process death | API 26 and physical device | Pending | Relaunch behavior for each top-level screen and persisted preferences. |

## Schema Evidence

The checked-in Room schema history starts at database version 4:

- `app/schemas/com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDatabase/4.json`
  first appears in commit `651fd06f`.
- Versions 5 and 6 are represented by checked-in schemas and explicit
  migrations in `EarthquakeDatabase.kt`.
- Git history shows earlier Play application version codes (`1` through `10`),
  but contains no exported Room schema before version 4 and no recoverable
  migration chain from those database versions.

Consequently, no pre-v4 migration is fabricated or asserted. A Play upgrade
from a pre-v4 database remains a release-blocking physical upgrade check until
an authentic historical database or shipped schema can be recovered.
