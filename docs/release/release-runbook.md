# Verified Android Release Runbook

This runbook creates one immutable, signed, checksummed AAB for manual Google
Play internal testing and controls its later promotion. Automation does not
upload to Play or promote any track.

Use these records together:

- `play-compliance-checklist.md`: policy/declaration and external configuration gates.
- `data-safety-inventory.md`: code/SDK data flow, retention, backup, and deletion evidence.
- `rollout-record.md`: immutable artifact identity, internal/closed gates, and
  5%, 20%, 50%, and 100% decisions.
- `incident-template.md`: containment, evidence preservation, and hotfix process.
- `production-readiness-checklist.md`: repository verification ledger.

## 1. Preflight

1. Confirm the release commit is on the intended protected branch and the
   required `repository-hygiene`, `unit-lint-build`, and `instrumentation`
   checks are green.
2. Resolve every `BLOCKED` item in `play-compliance-checklist.md`. As reviewed
   on 2026-08-27, this includes target API 36 for updates submitted on/after
   2026-08-31, a public in-app/store privacy-policy link, clean-checkout
   production AdMob resource provenance, and removal of the UMP test-reset path.
3. Keep physical-device, Maps, location, ads/consent, Room upgrade, Crashlytics
   delivery, and representative-device performance checks `PENDING` until the
   owner completes them and attaches evidence. Do not convert code inspection
   into external verification.
4. Update `versionCode` and `versionName` in `app/build.gradle.kts`. A tag build
   is accepted only when the tag is exactly `v<versionName>`.
5. Confirm the `production-release` environment approval and credentials in
   `docs/release/github-secrets.md`. In Play Console, compare the upload-key
   fingerprint with `RELEASE_CERT_SHA256`.
6. Reconcile the exact resolved third-party SDK graph and signed artifact with
   `data-safety-inventory.md`, the privacy policy, Data safety, Contains ads,
   content rating, target audience, and localized store listing.
7. Create a release-specific copy of `rollout-record.md`; assign all owners and
   leave every unexecuted external field `PENDING`.

## 2. Generate the Candidate

Use one of these protected entry points:

- Push an exact version tag such as `v3.0.0`. Tag runs always keep
  Crashlytics mapping upload disabled.
- Manually dispatch the exact `v<versionName>` tag with GitHub CLI. The workflow
  file must already exist on the repository default branch:

```bash
gh workflow run android-release.yml \
  --ref v3.0.0 \
  -f upload_crashlytics_mapping=false
```

Replace `v3.0.0` with the tag that exactly matches `versionName`. Set the input
to `true` only for an approved Firebase-symbol upload to the production project.
The equivalent workflow-dispatch REST request must provide the same tag as its
`ref` and the same boolean input.

Credential-free jobs run repository contracts, unit tests, lint, a debug build,
and the API 35 connected instrumentation suite first. Only after both jobs pass
does GitHub request environment approval and expose credentials to the signed
`:app:bundleRelease` job. That job downloads bundletool `1.18.3` from the
official release URL and requires SHA-256
`a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29`
before execution.

The release must also come from a commit whose normal protected-branch checks
are green; the dedicated release instrumentation run is additional evidence,
not a replacement for branch protection.

## 3. Inspect the Verified Artifact

Download `verified-signed-release-aab`. It contains only publishable evidence:

- `earthquake-watchdog-release.aab`
- `mapping.txt`
- `native-debug-symbols.zip` when the Android build produced it
- `release-provenance.json`
- `crashlytics-mapping-upload-receipt.json` only after a requested upload completed
- `SHA256SUMS`

The verifier requires a valid JAR signature, the protected certificate
fingerprint, bundletool validation, package/version identity, both Baseline
Profile metadata files, and byte-for-byte equality between `mapping.txt` and
the mapping embedded in the AAB. Verify download integrity from inside the
artifact directory:

```bash
shasum -a 256 -c SHA256SUMS
```

Read `release-provenance.json` and record its commit SHA, source ref, app
identity, certificate fingerprint, AAB checksum, mapping checksum, bundletool
checksum, and mapping-upload request in `rollout-record.md`. Provenance records
that upload is not completed at verification time. Only the separate receipt,
written after the explicit Gradle upload task succeeds, records completion and
is then included in `SHA256SUMS`. Treat any mismatch as a rejected candidate;
do not re-sign or edit the AAB.

The separate `signed-release-quality-reports` and
`signed-release-instrumentation-reports` artifacts contain unit/lint and
connected-test evidence and expire after seven days. The verified release
artifact expires after fourteen days, so archive it only in the repository
owner's approved release evidence store.

## 4. Upload Manually to Internal Testing

1. Create or select the intended Google Play internal-testing release.
2. Upload the exact `earthquake-watchdog-release.aab` whose hash was recorded.
3. Upload native debug symbols if produced. Retain `mapping.txt`; Crashlytics
   mapping delivery is controlled separately by the approved workflow input.
4. Record Play's accepted version code and confirm it matches provenance.
5. Install through the Play internal-testing link, not a locally generated APK.
6. Smoke-test startup, list/refresh/filter, earthquake details, Navigation 3
   restoration, statistics, Maps/location, settings, consent/ads, offline
   cached data, and a historical database upgrade.
7. Confirm Crashlytics collection/deobfuscation in the intended Firebase
   project without committing or retaining a test-crash trigger.
8. Confirm production Maps key restrictions use package
   `com.indiewalk.watchdog.earthquake` and the Play App Signing SHA-1, not only
   the upload key; verify Maps from the Play-installed build.
9. Confirm the production AdMob app ID in the signed manifest and banner ID in
   signed resources after resolving their clean-checkout provenance.
10. Execute UMP EEA, non-EEA, regulated-US, withdrawal, relaunch, and offline
    scenarios without recording consent payload values.
11. Complete fresh-install/current-public-upgrade, API 26/current API,
    low/mid-tier/current physical device, and physical performance rows.

Do not rebuild between internal, closed, and production tracks. Task 10 must
promote this same Play artifact by version code and recorded SHA-256.

## 5. Closed Testing And Production Promotion

1. Promote the same Play-accepted version to closed testing. Do not rebuild.
2. Record representative tester/device/locale coverage, defects, Crashlytics
   and Android-vitals metrics, support signals, and Maps/network/consent health.
3. Require zero open release blockers and explicit Android, privacy/ads,
   product/store, and release-owner approval.
4. Promote the same artifact through 5%, 20%, 50%, and 100%, completing every
   field in `rollout-record.md`. Do not promote on elapsed time alone.
5. At each stage apply the documented Google Play ANR/crash thresholds and the
   stricter project stop conditions. Low sample volume means extend/observe;
   it does not mean healthy.
6. After 100%, monitor for the recorded 48-hour project window. Close only when
   no stop condition exists and all evidence/signatures are attached.

## 6. Reject, Halt, Or Escalate

Reject the candidate for any failed check, unexpected SDK initialization,
wrong package/version/certificate, checksum drift, missing profile or mapping,
Firebase project mismatch, Data Safety mismatch, or smoke-test regression.
Preserve logs that contain no credentials or unnecessary personal data, revoke
exposed credentials if any secret handling failed, and create a new version
code for a corrected AAB. During production, halt for the stop conditions in
`rollout-record.md`, open `incident-template.md`, preserve artifact/mapping/CI/
vitals evidence before cleanup, and use a separately verified higher-version-
code hotfix. Never mutate or replace the accepted artifact.

## Local Disposable Dry Run

Local verification must use a disposable keystore, synthetic Firebase config,
and a non-production Maps placeholder. Prepare credentials with
`scripts/prepare_release_secrets.sh`, keep a shell `EXIT` trap active, build
with the generated `release.properties` and
`-PcrashlyticsMappingUploadEnabled=false`, then run `scripts/verify_aab.sh`
against the pinned/checksummed bundletool JAR. Run
`scripts/tests/verify_aab_test.sh` to prove fingerprint, version, checksum,
mapping, and signed-content tampering is rejected. Never use a real keystore or
Firebase configuration for this dry run.
