# Production Readiness Checklist

This checklist records reproducible production-readiness work. Historical
results remain as evidence; current external gates stay `PENDING` until the
named owner executes them and attaches evidence.

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

## Task 9 Protected Signed AAB

The `Android Release Bundle` workflow accepts exact `v*` version tags and
manual dispatches that select that same exact version tag. Its
`signed-release-aab` job is bound to the protected
`production-release` environment, has read-only repository permission, never
runs for pull requests, and never uploads to Google Play. Concurrent release
runs do not cancel one another.

The release transport and Gradle names are documented in
`docs/release/github-secrets.md`. Secret preparation fails before writing files
when any required value is absent, validates the disposable keystore and
Firebase package, writes mode-`600` material under a mode-`700` directory, and
uses guarded explicit/trap cleanup. Only the credential-bearing build step can
read environment secrets.

Before `:app:bundleRelease`, credential-free prerequisite jobs run
repository/security contracts, unit tests, lint, a debug build, and the API 35
connected instrumentation suite. Crashlytics mapping upload defaults off and can
be enabled only by the explicit boolean input on a protected manual run. The
workflow downloads bundletool `1.18.3` from its fixed official URL and verifies
its pinned SHA-256 before use.

`scripts/verify_aab.sh` checks the AAB signature, expected upload certificate,
bundle structure, manifest package/version, Baseline Profile metadata, and
embedded R8 mapping. It produces a fixed-name AAB, mapping, optional native
symbols, JSON provenance, and `SHA256SUMS` for manual internal-track upload.

### Task 9 Local Evidence

| Verification | Command | Result |
| --- | --- | --- |
| Secret lifecycle | `bash scripts/tests/prepare_release_secrets_test.sh` | Passed for every missing variable, mode restrictions, explicit cleanup, and failure-trap cleanup using disposable credentials. |
| Release workflow contract | `python3 scripts/tests/verify_release_pipeline_test.py` | Passed for triggers, protected environment, permissions, concurrency, exact secret names, quality ordering, pins, verification controls, retention, and no Play upload. |
| AAB negative/positive contract | `BUNDLETOOL_FIXTURE=<verified-jar> bash scripts/tests/verify_aab_test.sh` | Passed for a signed synthetic AAB and rejected certificate, version-code, version-name, bundletool-checksum, mapping, and signed-content tampering. |
| JVM/static quality | `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` | Passed: 70 JVM tests, lint, and debug APK; 60 Gradle tasks, 21 executed. |
| Connected release gate | `ANDROID_SERIAL=emulator-5556 ./gradlew :app:connectedDebugAndroidTest` | Passed: all 29 tests on the API 35 `MathBrainer_7in` emulator. |
| Disposable signed release | `BUNDLETOOL_FIXTURE=<verified-jar> bash scripts/tests/dry_run_signed_release_test.sh` | Passed: fresh JKS credentials and synthetic Firebase/Maps inputs produced a signed/minified AAB with mapping upload disabled; bundletool/signature/identity/profile/mapping/provenance and tamper checks passed, then all temporary credentials were removed. |
| Real credential run | Protected GitHub environment plus Play internal testing | Pending repository owner; this task did not access real credentials, push a tag, create an environment, upload mapping, or contact Play. |

### Task 9 Pending Repository-Owner Actions

- Create and protect the `production-release` environment, configure required
  reviewers/tag restrictions, and add the values in `github-secrets.md`.
- Verify `RELEASE_CERT_SHA256` against the Play upload certificate before the
  first run.
- Run a packaging rehearsal with mapping upload disabled, inspect provenance,
  then run an explicitly approved mapping upload only when Firebase delivery is
  being verified.
- Upload the verified AAB manually to Play internal testing and complete every
  device/external-service check in `release-runbook.md` before Task 10 rollout.

Task 9 final implementation commits are `58ad19e` and `694a0d9`; the latter
hardened verified mapping-delivery provenance after review. No protected
environment, real credential, tag, Firebase upload, or Play upload was changed.

## Task 10 Production Operations And Compliance

Repository documentation status: `CODE-VERIFIED`. Production readiness and all
external execution: `PENDING`. Known implementation/policy mismatches:
`BLOCKED`. Inapplicable reviewed requirements: `NOT APPLICABLE` with rationale.

Task 10 adds:

- `play-compliance-checklist.md` for Play policy/declaration, Maps/app-signing,
  AdMob/UMP, Crashlytics, store-content, internal, closed, and physical gates.
- `data-safety-inventory.md` for location, Room/DataStore, backup/deletion,
  USGS/network/geocoding, sharing/browser, diagnostics, and every production
  SDK family.
- `rollout-record.md` for Task 9 artifact identity and 5%, 20%, 50%, and 100%
  promotion decisions with quantitative stop criteria.
- `incident-template.md` for rollout containment, sanitized evidence
  preservation, and a higher-version-code hotfix.
- `scripts/tests/verify_production_operations_docs_test.py` to prevent missing
  gates/fields or an external claim being represented as code-verified.

### Task 10 Code-Verified Findings

| Area | Status | Evidence |
| --- | --- | --- |
| Foreground location scope | `CODE-VERIFIED` | Fine/coarse permissions exist; no background permission/service exists; intro disclosure precedes the permission launcher. Approximate-only device behavior remains pending. |
| Local retention/backups | `CODE-VERIFIED` | Exact user/manual position and reverse-geocoded text persist in DataStore; earthquake/statistics data persist in Room; database/preferences/shared preferences/cache are excluded from backup/transfer and clear storage/uninstall deletes them. |
| First-party network | `CODE-VERIFIED` | USGS HTTPS queries do not include selected user/manual coordinates; Maps, Geocoder, Ads/UMP, Crashlytics, and Play Services retain separate SDK disclosure obligations. |
| Diagnostics sanitization | `CODE-VERIFIED` | App-added categories/types exclude messages, causes, caller frames, coordinates, addresses, consent values, and ad IDs; automatic SDK metadata still requires declaration. |
| Immutable release interface | `CODE-VERIFIED` | Task 9 artifact/provenance/checksum/mapping identity is required unchanged across internal, closed, and production stages. |

### Task 10 Code-Level Resolution

- `CODE-VERIFIED`: `targetSdk` is 36 and release instrumentation targets API 36.
- `CODE-VERIFIED`: Settings opens a dedicated bilingual privacy screen; release
  builds require an HTTPS `PRIVACY_POLICY_URL_RELEASE` while debug safely omits
  the external link.
- `CODE-VERIFIED`: clean debug builds use generated Google test ad IDs; protected
  release builds require validated AdMob app/banner variables and the signed-AAB
  verifier checks their embedded values.
- `CODE-VERIFIED`: UMP eligibility defaults false, Mobile Ads auto-init is
  disabled, initialization occurs once only after `canRequestAds()`, and
  Settings uses the production privacy-options form rather than `reset()`.

### Task 10 Owner-Only Remaining Gates

- Privacy/product owner: host the public policy, configure the protected URL,
  enter the same Play Console public policy URL, and reconcile and
  submit Data safety, Contains ads, content rating, target audience, and EN/IT
  store listing for the exact artifact.
- Android owner: resolve approximate-only permission behavior and review the
  final merged target/API, consent, privacy-link, and ad-resource configuration.
- Cloud/release owner: verify Maps package plus Play App Signing SHA-1/API
  restrictions and upload-certificate SHA-256 without exposing credentials.
- QA/privacy owner: execute EEA/non-EEA/regulated-US consent, withdrawal,
  relaunch/offline, Maps/network/location, fresh/current-public upgrade, API 26
  and current API, low/mid/current physical-device, and performance scenarios.
- Firebase owner: verify internal-build Crashlytics fatal/nonfatal delivery,
  version, deobfuscation, and matching mapping receipt.
- Release owner: upload/promote the exact Task 9 artifact through internal,
  closed, 5%, 20%, 50%, and 100%; attach every metric/evidence/approval and keep
  the initiative `PENDING` until post-100% monitoring closes cleanly.
