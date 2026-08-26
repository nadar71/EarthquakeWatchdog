# Production Readiness Baseline

This checklist records the reproducible Task 1 baseline. Results remain pending until the corresponding command is run.

## Expected Artifacts

| Verification | Command | Expected output | Initial result |
| --- | --- | --- | --- |
| Gradle environment | `./gradlew --version` | Gradle and JVM version information in the terminal | Passed: Gradle 8.11.1, JVM 21.0.10 |
| Debug tests, lint, and APK | `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` | `app/build/reports/tests/testDebugUnitTest/index.html`; `app/build/reports/lint-results-debug.html`; `app/build/outputs/apk/debug/app-debug.apk` | Passed: 59 tasks, 19 executed |
| Connected instrumentation | `ANDROID_SERIAL=<serial> ./gradlew :app:connectedDebugAndroidTest` | `app/build/reports/androidTests/connected/debug/index.html` | Blocked: no connected device with serial `unavailable` |
| Release bundle | `./gradlew :app:bundleRelease` | `app/build/outputs/bundle/release/app-release.aab` | Passed: 56 tasks, 36 executed |

## Task 1 Results

| Check | Result | Notes |
| --- | --- | --- |
| Repository hygiene | Passed | The initial red run found the commented legacy dialog's `TODO(...)` and `printStackTrace()`; after removal, the gate passed. It blocks tracked credentials/artifacts and executable `TODO(...)`, `println(...)`, and `printStackTrace()`; existing INFO-level production diagnostics are deferred to Task 3. |
| Debug unit tests, lint, and assembly | Passed | All three tasks passed in one Gradle invocation. |
| Connected instrumentation | Blocked | `ANDROID_SERIAL=unavailable ./gradlew :app:connectedDebugAndroidTest` failed because no connected device with that serial exists; `adb` is not installed in this environment. |
| Release bundle | Passed with warnings | The AAB was created. The build warned that `ndk-bundle` lacks `source.properties` and packaged two native libraries unstripped. |
