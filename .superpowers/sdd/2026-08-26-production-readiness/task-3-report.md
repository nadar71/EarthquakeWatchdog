# Task 3 Report: Sanitize Diagnostics and Separate Debug Logging

## Status

Completed pending commit. This report contains no credential, location, address, advertising identifier, or consent payload.

## Implemented

- Added the closed `AppDiagnostics` boundary with fixed `DiagnosticCategory` and `DiagnosticEvent` enums. Its public methods accept only a category/event and a throwable; they do not accept arbitrary metadata, custom keys, or payload values.
- Added source-set `PlatformDiagnostics` adapters. Debug writes only the sanitized report to Logcat. Release is an intentional no-op placeholder for Task 4, so this task adds no Firebase dependency or reporting side effect.
- `recordNonFatal` now forwards a sanitized wrapper containing only `CATEGORY/SAFE_FAILURE_TYPE` and the original stack frames. It drops the original message, cause tree, and suppressed exceptions. The safe failure types are fixed (`IO`, `SECURITY`, `ILLEGAL_ARGUMENT`, `ILLEGAL_STATE`, and `UNEXPECTED`).
- Moved useful diagnostics to repository/ViewModel/platform-error boundaries: earthquake refresh and storage observation, statistics loading, manual-location confirmation, location/geocoder failures, and a failed external Play Store intent. No diagnostics are emitted during Compose rendering.
- Removed direct and commented production `Log` calls that could contain request parameters, timestamps, coordinates, manual location state, addresses, or filter state.
- Ktor request logging is installed only when `BuildConfig.DEBUG` is true. Release compilation selects the release adapter and does not install the Ktor logging plugin.
- Enabled BuildConfig generation for the variant-gated HTTP logging branch and configured JVM unit tests to return Android framework defaults so debug Logcat diagnostics do not crash existing ViewModel tests.

## RED/GREEN Evidence

- RED: `./gradlew :app:testDebugUnitTest --tests '*AppDiagnosticsTest'` failed to compile because `AppDiagnostics`, the enums, and the sink did not yet exist.
- GREEN: after the boundary and source-set adapters were added, the focused test passed.
- RED refinement: the strengthened test failed with `ComparisonFailure` because the first wrapper used `Unexpected failure.` and did not retain a useful stack trace.
- GREEN refinement: the focused test passed after adding fixed safe failure types and copying only the source throwable stack frames.

## Verification

- `./gradlew :app:testDebugUnitTest --tests '*AppDiagnosticsTest'`: passed.
- `./gradlew :app:testDebugUnitTest`: passed.
- `bash scripts/verify_repository_hygiene.sh`: passed.
- `./gradlew :app:lintDebug :app:assembleDebug :app:lintRelease :app:compileReleaseKotlin`: completed with the debug APK and both lint reports generated; no failure was reported. Earlier direct release lint/compile output completed successfully.
- `git diff --check`: passed.
- Production-source scan for `android.util.Log`, `Log.`, `println(`, and `printStackTrace(`: no matches.

## Privacy Decisions

- A diagnostic sink never receives a caller message, original throwable, cause, suppressed exception, location, address, URL/query parameter, API key, or advertising identifier.
- Tests cover coordinates, addresses, USGS-style query parameters, API keys, and advertising IDs. They assert those strings are absent from the sink payload, message, cause, suppressed list, and stack rendering while the original frames remain available.
- Fixed enum names and safe failure types are the only diagnostic context available to the adapters.

## Changed Files

- `app/build.gradle.kts`
- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/diagnostics/AppDiagnostics.kt`
- `app/src/debug/java/com/indiewalk/watchdog/earthquake/core/diagnostics/PlatformDiagnostics.kt`
- `app/src/release/java/com/indiewalk/watchdog/earthquake/core/diagnostics/PlatformDiagnostics.kt`
- `app/src/test/java/com/indiewalk/watchdog/earthquake/core/diagnostics/AppDiagnosticsTest.kt`
- `app/src/main/java/com/indiewalk/watchdog/earthquake/core/di/NetworkModule.kt`
- Production files formerly containing direct or commented `Log` calls, plus the ViewModel/location boundaries listed above.

## Limitations

- `bash scripts/verify_release_configuration.sh` was started with its disposable keystore and reached `:app:minifyReleaseWithR8`, but was intentionally stopped after approximately eight minutes at the user's request to stop internal waiting. It is not claimed as a passed Task 3 verification.
- `bundletool` and `adb` are unavailable, so bundle validation and device smoke testing remain pending.

## Fix Round 1

- Replaced caller-controlled throwable stack copying with a boundary-owned stack. Diagnostic internals are trimmed so the first remaining frame is the app ownership call site; caller-provided class, method, and file-name strings are never read or forwarded.
- Added `DiagnosticReportingPolicy`, a closed policy that suppresses routine `IOException` and `SecurityException` outcomes. This keeps offline requests, permission denial/revocation, and geocoder unavailability out of Task 4 Crashlytics reporting while unexpected failures still produce one sanitized non-fatal.
- Removed location/geocoder non-fatal reporting from `MapsUtils`; those APIs now return their existing null fallback for expected platform unavailability.
- Removed the global `unitTests.isReturnDefaultValues = true` setting. A test-only `DiagnosticsTestRule` injects a no-op sink into the three ViewModel test classes that intentionally exercise diagnostics, so JVM tests remain strict about unmocked Android APIs.

## Fix Round 1 RED/GREEN Evidence

- RED: hostile `StackTraceElement` values containing a coordinate, API key, and advertising ID reached the sink; the new privacy test failed with `ComparisonFailure`.
- GREEN: the hostile-stack test passed after diagnostics captured and trimmed its own stack. It verifies sensitive values are absent from the message, cause, suppressed data, stack rendering, and sink payload.
- RED: the expected-failure policy test observed three non-fatals for offline, permission, and geocoder failures.
- GREEN: it now observes no expected-failure reports and exactly one `STORAGE/ILLEGAL_STATE` report for an unexpected failure.
- RED: removing `unitTests.isReturnDefaultValues` caused eight affected ViewModel tests to fail through the unmocked debug Logcat adapter.
- GREEN: injecting `DiagnosticsTestRule` into only those fixtures restored all affected tests without relaxing framework stubs globally.

## Fix Round 1 Verification

- `./gradlew :app:testDebugUnitTest --tests '*AppDiagnosticsTest'`: passed.
- `./gradlew :app:testDebugUnitTest --tests '*EarthquakeListViewModelTest' --tests '*MapViewModelTest' --tests '*StatisticsViewModelTest'`: passed with strict Android stubs.
- `./gradlew :app:testDebugUnitTest`: passed.
- `bash scripts/verify_repository_hygiene.sh`: passed.
- Debug and release lint reports contain zero errors (`298` and `249` existing warnings respectively); debug APK and release lint report were generated.
- `./gradlew --offline :app:compileDebugKotlin :app:compileReleaseKotlin :app:lintDebug :app:lintRelease :app:assembleDebug`: passed.

## Fix Round 2

- Made `DiagnosticReportingPolicy` category-aware. `NETWORK` suppresses only cause chains containing `IOException`; `LOCATION` suppresses only cause chains containing routine `IOException` or `SecurityException`; and `STATISTICS` suppresses only the known `StatisticsLoadFailure` wrapper when its cause chain contains `IOException`.
- `STORAGE`, `MAP`, and `EXTERNAL_INTENT` always report, as do unexpected category/type combinations such as `NETWORK/SecurityException`, `LOCATION/IllegalStateException`, and direct `STATISTICS/IOException`.
- Added the core-owned `StatisticsLoadFailure` marker to the existing statistics load wrapper, avoiding a core dependency on the statistics feature type while keeping the exception classification explicit.
- Cause-chain inspection uses an identity set, so cyclic causes terminate safely. It performs type classification only; neither cause objects nor their messages are retained in the sanitized diagnostic sent to a sink.
- Restored `MapsUtils` calls through `AppDiagnostics`. Expected location/geocoder failures are suppressed by the location policy, while unexpected platform failures remain sanitized, reportable non-fatals with the existing null fallback.

## Fix Round 2 RED/GREEN Evidence

- RED: the category matrix reported only `NETWORK`, `STATISTICS`, and `LOCATION` where storage/map/external failures should remain visible, and it reported the known wrapped offline statistics failure.
- GREEN: the matrix now suppresses only expected category/type combinations and observes `NETWORK`, `LOCATION`, `STATISTICS`, `STORAGE`, `MAP`, and `EXTERNAL_INTENT` for their unexpected cases.
- RED: `wrappedOfflineFailureDoesNotEmitStatisticsDiagnostic` observed a non-fatal for `EarthquakeStatisticsLoadException` caused by offline `IOException`.
- GREEN: the StatisticsViewModel retains its existing user-facing network error while emitting no statistics diagnostic for that expected wrapped offline failure.
- Cycle regression: a two-node cause cycle terminates and produces the expected single unexpected network diagnostic.

## Fix Round 2 Verification

- `./gradlew :app:testDebugUnitTest --tests '*AppDiagnosticsTest' --tests '*StatisticsViewModelTest'`: passed.
- `./gradlew :app:testDebugUnitTest`: passed.
- `bash scripts/verify_repository_hygiene.sh`: passed.
- `./gradlew --offline :app:compileDebugKotlin :app:compileReleaseKotlin :app:lintDebug :app:lintRelease :app:assembleDebug`: passed.
- Debug and release lint reports contain zero errors (`298` and `249` existing warnings respectively).
