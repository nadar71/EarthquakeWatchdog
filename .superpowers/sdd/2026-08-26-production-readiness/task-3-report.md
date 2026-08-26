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
