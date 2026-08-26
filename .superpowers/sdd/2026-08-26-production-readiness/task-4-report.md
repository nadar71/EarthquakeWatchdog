# Task 4 Report: Privacy-Safe Firebase Crashlytics

## Status

Completed. No Firebase event, fatal, non-fatal, mapping upload, or production credential was sent or committed.

## Implemented

- Added the Google Services and Crashlytics Gradle plugins through the version catalog using Google Services `4.5.0`, Crashlytics Gradle plugin `3.0.8`, and Firebase BoM `34.18.0`.
- Added the Crashlytics runtime only; Analytics was intentionally not added. Closed app breadcrumbs are written through `FirebaseCrashlytics.log` in release builds.
- Kept the Task 3 closed `AppDiagnostics` interface and sanitization boundary. The release adapter can pass only the closed diagnostic category, sanitized exception, and closed breadcrumb event to Crashlytics; it has no API for location, address, distance, consent, ad identifiers, URLs, keys, or arbitrary values.
- Added `CrashReportingPolicy`, which keeps debug collection disabled and delegates expected-failure suppression to the existing category-aware Task 3 policy.
- Set Crashlytics collection to false in debug and true in release through manifest placeholders. `EarthquakeApp` defensively disables collection when a debug build has a local Firebase config, and handles a missing config without crashing.
- A release package now fails with a clear missing `app/google-services.json` error after signing/Maps validation. Debug tests, lint, and APK assembly remain usable when the file is absent because Firebase Gradle plugins are applied only when it is present.
- Mapping upload defaults to false. `validateStoreReleaseConfiguration` requires an explicit `-PcrashlyticsMappingUploadEnabled=true` opt-in for the future protected CI store workflow.
- Extended the release verifier to test missing Firebase configuration, mapping-upload opt-in, and a signed/minified synthetic release bundle. It temporarily creates a synthetic `google-services.json`, forces mapping upload off, and restores/removes the file through its cleanup trap.
- Documented protected CI provisioning and the exact pending Firebase Console/device verification procedure. No temporary crash trigger exists in source control.

## RED/GREEN Evidence

- RED: `./gradlew :app:testDebugUnitTest --tests '*CrashReportingPolicyTest' --offline` failed because `CrashReportingPolicy` did not exist.
- GREEN: after adding the closed policy and using it from `AppDiagnostics`, the focused test passed. It proves debug collection is disabled, network/offline, routine location, and permission-denied UI paths do not report, and an unexpected storage ownership failure reports exactly once with a sanitized category.
- RED: the synthetic verifier initially failed because Crashlytics Gradle Plugin `3.0.8` no longer exposes a project-level `CrashlyticsExtension`.
- GREEN: mapping configuration was moved to the plugin's release build-type extension. The synthetic signed/minified bundle then produced both `app/build/outputs/mapping/release/mapping.txt` and `app/build/outputs/bundle/release/app-release.aab` with `-PcrashlyticsMappingUploadEnabled=false`, and the temporary config was cleaned.

## Verification

- `./gradlew --offline :app:testDebugUnitTest --tests '*CrashReportingPolicyTest' --rerun-tasks`: passed.
- `bash scripts/verify_release_configuration.sh`: passed after the Crashlytics Gradle Plugin v3 configuration correction. It verified clear missing-Firebase failure, explicit mapping-upload opt-in validation, a synthetic signed/minified release AAB, R8 mapping generation, and synthetic config cleanup.
- `./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:lintRelease :app:compileReleaseKotlin`: passed.
- `bash scripts/verify_repository_hygiene.sh`: passed.
- `git diff --check`: passed.

## Limitations And Pending External Checks

- `bundletool` and `adb` are unavailable, so bundle validation and physical-device smoke testing remain pending.
- A protected CI environment must provision the real Firebase configuration and run `:app:validateStoreReleaseConfiguration :app:bundleRelease -PcrashlyticsMappingUploadEnabled=true` for a store artifact and mapping upload.
- Firebase Console delivery, project selection, symbol deobfuscation, and one temporary developer-only fatal/non-fatal check remain manual. The precise sequence is in `docs/release/crashlytics-verification.md`; any trigger must be removed before distribution.

## Fix Round 1: Review Remediation

### Implemented

- Replaced the release adapter's session-global Crashlytics custom-key call with the Firebase Crashlytics `20.1.0` atomic `recordException(Throwable, CustomKeysAndValues)` overload. The new closed `CrashlyticsGateway` boundary accepts only a throwable and the internally-created `diagnostic_category` metadata; it has no global custom-key operation or arbitrary-value API.
- Added an adapter-level test proving one report call receives exactly `diagnostic_category` and that metadata collection is not mutated as a side effect.
- Moved Crashlytics collection startup policy behind `CrashlyticsStartup`. Every initialized debug instance is explicitly disabled and every initialized release instance is explicitly enabled, replacing any persisted debug override. Missing local Firebase configuration returns safely without affecting debug builds.
- Added an initialization-boundary test for a debug-to-release transition with a fake gateway, asserting the exact calls `[false, true]`.
- Extended feature-ownership diagnostics tests: an offline list refresh, routine location provider failure, and denied intro permission produce zero reports; an unexpected list repository stream failure produces exactly one `STORAGE` report.
- Updated the synthetic release verifier so every redirected Gradle failure path prints a redacted tail of its captured log before cleanup, including the signed/minified release bundle and final release-secret validation.

### RED/GREEN Evidence

- RED: the new Crashlytics adapter/startup tests initially failed to compile because `CrashlyticsDiagnosticsAdapter`, `CrashlyticsGateway`, and `CrashlyticsStartup` did not yet exist.
- GREEN: `./gradlew --no-daemon :app:testDebugUnitTest --tests '*CrashReportingPolicyTest' --tests '*EarthquakeListViewModelTest' --tests '*IntroViewModelTest'` passed after the minimal adapter/startup implementation.
- GREEN: after adding the real location ownership test and verifier failure-tail coverage, `bash -n scripts/verify_release_configuration.sh && ./gradlew --no-daemon :app:testDebugUnitTest --tests '*CrashReportingPolicyTest' --tests '*EarthquakeListViewModelTest' --tests '*IntroViewModelTest' --tests '*MapsUtilsTest'` passed.

### Fresh Verification

- `./gradlew --no-daemon :app:testDebugUnitTest --rerun-tasks`: completed with 68 tests, 0 failures, and 0 errors.
- `./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:lintRelease :app:compileReleaseKotlin`: passed.
- `bash scripts/verify_release_configuration.sh`: completed with a temporary synthetic Firebase configuration, mapping upload forced to `false`, a generated release mapping file and AAB, and no remaining `app/google-services.json`.
- `bash scripts/verify_repository_hygiene.sh` and `git diff --check`: passed.

### Remaining External Checks

- No Firebase event, fatal/non-fatal, mapping upload, or external service action was triggered. A real store build still requires CI to provide Firebase configuration and use `-PcrashlyticsMappingUploadEnabled=true`.
- Firebase Console receipt, symbol deobfuscation, and physical-device validation remain manual as documented in `docs/release/crashlytics-verification.md`. No temporary crash trigger is present in this branch.
