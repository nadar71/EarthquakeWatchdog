# Immutable Release And Rollout Record

Reviewed: 2026-08-27. Copy this template for one candidate version. Empty fields
are `PENDING`; do not infer completion from a build, upload, or elapsed time.

## Status Vocabulary

- `CODE-VERIFIED`: a repository check proves the stated contract.
- `PENDING`: external execution/evidence or an owner decision is still needed.
- `BLOCKED`: promotion is prohibited until the condition is resolved.
- `NOT APPLICABLE`: reviewed and inapplicable, with a reason/evidence link.

## Release Ownership

| Field | Value |
| --- | --- |
| Record status | `PENDING` |
| Release owner | `PENDING` |
| Android owner | `PENDING` |
| Privacy/ads owner | `PENDING` |
| Product/store owner | `PENDING` |
| Incident commander | `PENDING` |
| Record created/updated (UTC) | `PENDING` |

## Immutable Artifact Identity

All tracks must promote the same Play-accepted artifact. Copy values from
`verified-signed-release-aab/release-provenance.json` and `SHA256SUMS`; do not
retype from Gradle configuration or rebuild after internal testing.

| Field | Recorded value | Evidence |
| --- | --- | --- |
| GitHub workflow run | `PENDING` | `PENDING` |
| Source ref | `PENDING` | `release-provenance.json` |
| Commit SHA | `PENDING` | `release-provenance.json` |
| Package | `PENDING` | Must equal `com.indiewalk.watchdog.earthquake` |
| Version code | `PENDING` | Provenance plus Play acceptance |
| Version name | `PENDING` | Provenance plus signed manifest |
| AAB filename | `earthquake-watchdog-release.aab` | Artifact contract |
| AAB SHA-256 | `PENDING` | `SHA256SUMS` |
| Signing certificate SHA-256 | `PENDING` | Provenance/verifier; this is the upload certificate |
| Mapping SHA-256 | `PENDING` | `SHA256SUMS` |
| Provenance SHA-256 | `PENDING` | `SHA256SUMS` |
| bundletool version/SHA-256 | `PENDING` | `release-provenance.json` |
| Crashlytics mapping upload requested | `PENDING` | `release-provenance.json` |
| Crashlytics mapping receipt | `PENDING` or `NOT APPLICABLE` | Checksummed receipt only when explicitly requested and delivered |
| CI quality/instrumentation artifacts | `PENDING` | Workflow URLs and artifact names |

Identity status: `PENDING`. Any identity/checksum/certificate mismatch is
`BLOCKED`; reject the candidate rather than re-signing or editing it.

## Compliance Gate Record

| Gate | Status | Approver | Date (UTC) | Evidence |
| --- | --- | --- | --- | --- |
| No open blocker in `play-compliance-checklist.md` | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Privacy policy and Data safety reconciled | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Ads/content rating/target audience/store listing reconciled | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Maps package plus app-signing restrictions verified | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Production ad IDs and consent behavior verified | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Target API requirement satisfied on submission date | `PENDING` | `PENDING` | `PENDING` | `PENDING` |

## Internal Testing Gate

Install the exact AAB through the internal-test link. Record device model, OS,
locale, build/version, network/geography setup, result, defect link, tester,
time, and sanitized evidence for every row.

| Scenario | Status | Required coverage | Evidence |
| --- | --- | --- | --- |
| Fresh install/core journeys | `PENDING` | API 26, API 35/36, low/mid-tier physical, current physical; EN/IT; light/dark; large text | `PENDING` |
| Location paths | `PENDING` | Precise, approximate-only, denied, permanent denial, manual, default, restart | `PENDING` |
| Maps/network | `PENDING` | Correct tiles/markers/details/recenter; slow/offline/recovery | `PENDING` |
| EEA consent | `PENDING` | First run, accept/reject, privacy options, withdrawal, relaunch | `PENDING` |
| non-EEA and regulated-US consent | `PENDING` | Configured message/status/ad eligibility paths | `PENDING` |
| Consent offline | `PENDING` | No cached status, cached status, request failure; app usable and fail-closed ads | `PENDING` |
| Ads | `PENDING` | Approved production IDs in artifact; registered test device shows test ads; ordinary install follows production configuration | `PENDING` |
| current-public upgrade | `PENDING` | Install actual public version from Play, preserve real local state, upgrade to candidate | `PENDING` |
| Crashlytics delivery | `PENDING` | Intended Firebase project, version, fatal/nonfatal, deobfuscation, matching receipt | `PENDING` |
| Physical performance baselines | `PENDING` | Cold startup, frame/jank, memory on low/mid-tier and current device | `PENDING` |

Internal decision: `PENDING`. Approver: `PENDING`. Date: `PENDING`.

## Closed Testing Gate

| Field | Value |
| --- | --- |
| Track/release link | `PENDING` |
| Tester population and device/locale coverage | `PENDING` |
| Start time / End time (UTC) | `PENDING` / `PENDING` |
| Defects opened/closed/open blockers | `PENDING` |
| Crash-free users / ANR rate / Crash rate | `PENDING` / `PENDING` / `PENDING` |
| Support signals | `PENDING` |
| Maps health / Network health / Consent health | `PENDING` / `PENDING` / `PENDING` |
| Decision / Approver / Evidence | `PENDING` / `PENDING` / `PENDING` |

Closed promotion requires zero open release blockers, representative coverage,
and explicit Android, privacy/ads, product, and release-owner approval.

## Staged Production Rollout

The project stages are 5%, 20%, 50%, and 100%. Play does not mandate this
project's observation duration. Default project gate: observe each 5/20/50
stage for at least 24 hours and enough active-user volume to make the evidence
meaningful; owners may extend, never shorten without a written risk decision.
At 100%, continue active monitoring for at least 48 hours before closure.

| Stage | Status | Start time | End time | Population | Crash-free users | ANR rate | Crash rate | Support signals | Maps health | Network health | Consent health | Decision | Approver | Evidence |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 5% | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| 20% | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| 50% | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| 100% | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` |

For each decision, record the candidate Version code, AAB SHA-256, comparison
version/window, metric sample size, known defects, new crash clusters, support
volume/themes, approver, and direct evidence links. Promotion by elapsed time
alone is prohibited.

## Quantitative Stop And Rollback Criteria

Halt promotion immediately for any confirmed data loss/corruption, security or
privacy disclosure, incorrect consent/ad request, broken install/startup/core
journey, broad Maps/network failure, wrong package/certificate/version, or
artifact mismatch. These conditions do not require a minimum sample.

Google Play's current bad-behavior thresholds are:

- Overall user-perceived ANR rate at or above **0.47%**.
- Per-device user-perceived ANR rate at or above **8%**.
- Overall user-perceived crash rate at or above **1.09%**.
- Per-device user-perceived crash rate at or above **8%**.

Halt before a threshold when a statistically credible release-specific trend
is materially worse than the stable comparison version. Project operational
stop criteria, distinct from Google's official thresholds, are: any new
untriaged release-blocker crash cluster; crash-free users down by at least one
percentage point over a comparable window/sample; or a reproducible core-flow
failure on two independent supported devices. Low traffic or absent vitals is
not proof of health; extend the stage and use internal/closed/manual evidence.

On a stop: halt the staged or fully rolled release when available, preserve the
evidence listed in `incident-template.md`, open an incident, and either resume
only after written approval or publish a separately verified higher-version-code
hotfix. Never replace or mutate the accepted artifact.

## Closure

Initiative status: `PENDING`. Closure requires a clean-checkout automated gate,
all external records, 100% rollout, completion of the 48-hour monitoring window,
no stop condition, incident disposition, and all four owner signatures.

| Role | Name | Decision | Time (UTC) | Evidence |
| --- | --- | --- | --- | --- |
| Android owner | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Privacy/ads owner | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Product/store owner | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Release owner | `PENDING` | `PENDING` | `PENDING` | `PENDING` |

## Official Sources

- [Google Play internal, closed, and open testing](https://support.google.com/googleplay/android-developer/answer/9845334?hl=en) (Reviewed 2026-08-27).
- [Prepare and roll out a release](https://support.google.com/googleplay/android-developer/answer/9859348?hl=en) (Reviewed 2026-08-27).
- [Google Play staged rollouts](https://support.google.com/googleplay/android-developer/answer/6346149) (Reviewed 2026-08-27).
- [Google Play Android vitals](https://support.google.com/googleplay/android-developer/answer/9844486?hl=en) (Reviewed 2026-08-27).
- [Halt a fully rolled-out release](https://support.google.com/googleplay/android-developer/answer/16285429?hl=en-GB) (Reviewed 2026-08-27).
- [Google Play app update/version behavior](https://support.google.com/googleplay/android-developer/answer/9859350?hl=en-EN) (Reviewed 2026-08-27).
- [Android application versioning](https://developer.android.com/studio/publish/versioning) (Reviewed 2026-08-27).
