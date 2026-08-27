# Production Readiness Checklist

This checklist records the reproducible Task 1 baseline. Results remain pending until the corresponding command is run.

## Expected Artifacts

| Verification | Command | Expected output | Initial result |
| --- | --- | --- | --- |
| Gradle environment | `./gradlew --version` | Gradle and JVM version information in the terminal | Passed: Gradle 8.11.1, JVM 21.0.10 |
| Hygiene fixture harness | `bash scripts/tests/verify_repository_hygiene_test.sh` | Isolated temporary Git repositories and source trees | Passed: 7 fixture cases |
| Debug tests, lint, and APK | `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` | `app/build/reports/tests/testDebugUnitTest/index.html`; `app/build/reports/lint-results-debug.html`; `app/build/outputs/apk/debug/app-debug.apk` | Passed: 59 tasks, 19 executed |
| Connected instrumentation | `ANDROID_SERIAL=<serial> ./gradlew :app:connectedDebugAndroidTest` | `app/build/reports/androidTests/connected/debug/index.html` | Blocked: no connected device with serial `unavailable` |
| Release bundle | `./gradlew :app:bundleRelease` | `app/build/outputs/bundle/release/app-release.aab` | Passed: 56 tasks, 36 executed |

## Task 1 Results

| Check | Result | Notes |
| --- | --- | --- |
| Repository hygiene | Passed | The initial red run found the commented legacy dialog's `TODO(...)` and `printStackTrace()`; after removal, the gate passed. It blocks tracked credentials/artifacts and executable `TODO(...)`, `println(...)`, and `printStackTrace()`; existing INFO-level production diagnostics are deferred to Task 3. |
| Hygiene fixture harness | Passed | `bash scripts/tests/verify_repository_hygiene_test.sh` creates temporary Git repositories only; it covers comments, Kotlin templates, executable calls, credential/artifact tracking, and Room schema preservation. |
| Debug unit tests, lint, and assembly | Passed | All three tasks passed in one Gradle invocation. |
| Connected instrumentation | Blocked | `ANDROID_SERIAL=unavailable ./gradlew :app:connectedDebugAndroidTest` failed because no connected device with that serial exists; `adb` is not installed in this environment. |
| Release bundle | Passed with warnings | The AAB was created. The build warned that `ndk-bundle` lacks `source.properties` and packaged two native libraries unstripped. |

## Task 8 GitHub Actions Quality Gates

The workflows use Java 17, validate the Gradle wrapper, restore Gradle and AVD
caches, and grant only the repository-wide read permission below:

```yaml
permissions:
  contents: read
```

They do not use `pull_request_target`, GitHub environment secrets, release
signing inputs, Maps keys, Firebase configuration, or Play credentials. Forked
pull requests therefore run only against public repository content. Artifact
uploads use GitHub's job-scoped artifact token and do not require a broader
repository permission.

Every Ubuntu emulator job runs the shared local `enable-kvm` composite action
after checkout and before `android-emulator-runner`. The action installs the
runner-recommended udev rule for `/dev/kvm`, reloads the rules, and triggers the
KVM device so hardware acceleration is available consistently without copying
privileged shell commands across jobs.

### Required Pull-Request Checks

| Check | Trigger | Command / coverage | Artifact policy |
| --- | --- | --- | --- |
| `repository-hygiene` | Pull requests and pushes to `develop` | `bash scripts/verify_repository_hygiene.sh` plus the workflow security contract test | No artifact is produced; the complete violation is printed in the job log. |
| `unit-lint-build` | Pull requests and pushes to `develop` | `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` | Unit-test and lint reports upload for seven days only when the job fails. |
| `instrumentation` | Pull requests and pushes to `develop` | API 35, hardware acceleration enabled, AVD/Gradle caching, animations disabled, `./gradlew :app:connectedDebugAndroidTest` | Connected-test reports upload for seven days even when the test process fails. |

The check names above are explicit job names and are the names the repository
owner must select in branch protection. The workflows also support manual
dispatch.

### Scheduled Coverage

The daily `Android Instrumentation` schedule adds API 26 and API 36 connected
test jobs. These compatibility jobs are deliberately excluded from pull
requests to control emulator cost while retaining minimum-SDK and current-SDK
coverage.

The same schedule and manual dispatch run `benchmark-monitoring-non-blocking`
on API 35. It validates Task 7 selectors, regenerates and verifies the Baseline
Profile, runs the startup and core Macrobenchmarks, checks the measured JSON
against `config/performance-budgets.json`, and uploads evidence for 14 days.
The job has `continue-on-error: true`: cloud-emulator timing variance is visible
but cannot become a noisy required pull-request check. A failed structural
profile or benchmark check still appears in that job and its artifact.

Instrumentation concurrency includes the GitHub event type as well as the ref.
Only pull-request and push runs cancel an older run in their own event group;
scheduled and manually dispatched compatibility/benchmark runs are never
canceled by a push to `develop`.

### Immutable Action Pins

Pins were resolved from the official upstream Git repositories with
`git ls-remote` on 2026-08-27. Annotated tags use the peeled commit SHA.

| Action | Version | Immutable commit |
| --- | --- | --- |
| `actions/checkout` | `v4.2.2` | `11bd71901bbe5b1630ceea73d27597364c9af683` |
| `actions/setup-java` | `v4.7.1` | `c5195efecf7bdfc987ee8bae7a71cb8b11521c00` |
| `actions/cache` | `v4.2.4` | `0400d5f644dc74513175e3cd8d07132dd4860809` |
| `actions/upload-artifact` | `v4.6.2` | `ea165f8d65b6e75b540449e92b4886f43607fa02` |
| `gradle/actions` | `v4.4.3` | `ed408507eac070d1f99cc633dbcf757c94c7933a` |
| `reactivecircus/android-emulator-runner` | `v2.34.0` | `1dcd0090116d15e7c562f8db72807de5e036a4ed` |

Every remote `uses:` entry is pinned to a full 40-character SHA and keeps a
version comment for Dependabot and human review. Local composite-action calls
use a repository-relative path.

### Local Validation

| Verification | Command | Result |
| --- | --- | --- |
| Structural workflow contract | `python3 scripts/tests/verify_github_actions_test.py` | Passed: 10 tests verify names, permissions, pins, commands, triggers, API coverage, KVM setup, concurrency isolation, benchmark policy, artifacts, and secret absence. |
| YAML parsing | Ruby `YAML.safe_load` for both workflows and the composite action | Parsed successfully. |
| GitHub Actions semantics | Checksum-verified `actionlint 1.7.12` binary from the official release | Both workflows passed. The binary was used from a temporary directory and was not committed. |
| Action pin provenance | `git ls-remote` against each official upstream repository and version tag | Every configured SHA matched its tag or peeled annotated-tag commit. |
| Fast PR command under Java 17 | `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` | Passed. |
| Current-device instrumentation | `ANDROID_SERIAL=emulator-5556 ./gradlew :app:connectedDebugAndroidTest` | Passed: 29 tests on API 35. |
| Scheduled-current instrumentation | `ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest` | Passed: 29 tests on API 36. API 26 remains pending for the first scheduled/manual workflow run. |
| Benchmark wiring | API 35 selector contract, Baseline Profile verifier, and Gradle dry-run of profile/Macrobenchmark task graph | Passed. Full cloud Macrobenchmark timing remains intentionally non-blocking and pending its first scheduled/manual run. |

### Pending Repository-Owner Actions

- Open a pull request containing these workflows and let GitHub establish the
  three check names. No PR was opened by this task.
- Protect `develop` and release branches, requiring `repository-hygiene`,
  `unit-lint-build`, and `instrumentation` before merge. No branch-protection
  setting was changed by this task.
- Confirm scheduled workflows are enabled for the repository and inspect the
  first API 26/API 36 and benchmark artifacts before treating them as trusted
  monitoring evidence.

To prove branch protection safely, use a disposable test PR and introduce one
failure at a time: add an executable `println(...)` in production source for
`repository-hygiene`, invert one existing JVM assertion for `unit-lint-build`,
and invert one deterministic Compose/navigation instrumentation assertion for
`instrumentation`. Confirm each named check blocks the PR, then revert every
probe commit and require all three checks to turn green before merging. Never
use real credentials or generated release artifacts as failure probes.
