# Verified Android Release Runbook

This runbook creates one immutable, signed, checksummed AAB for manual Google
Play internal testing. It does not upload to Play or promote any track.

## 1. Preflight

1. Confirm the release commit is on the intended protected branch and the
   required `repository-hygiene`, `unit-lint-build`, and `instrumentation`
   checks are green.
2. Complete the pending physical-device, Maps, location, ads/consent, Room
   upgrade, Crashlytics delivery, and representative-device performance checks
   in the production-readiness checklist.
3. Update `versionCode` and `versionName` in `app/build.gradle.kts`. A tag build
   is accepted only when the tag is exactly `v<versionName>`.
4. Confirm the `production-release` environment approval and credentials in
   `docs/release/github-secrets.md`. In Play Console, compare the upload-key
   fingerprint with `RELEASE_CERT_SHA256`.
5. Review third-party SDK and Data Safety declarations before producing a
   candidate intended for store review.

## 2. Generate the Candidate

Use one of these protected entry points:

- Push an exact version tag such as `v3.0.0`. Tag runs always keep
  Crashlytics mapping upload disabled.
- Manually dispatch `Android Release Bundle` while selecting the exact
  `v<versionName>` tag. Leave mapping upload disabled for a packaging rehearsal.
  Set `upload_crashlytics_mapping=true` only for an approved Firebase-symbol
  upload to the production project.

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
checksum, and mapping-upload decision in the release record. Treat any mismatch
as a rejected candidate; do not re-sign or edit the AAB.

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

Do not rebuild between internal, closed, and production tracks. Task 10 must
promote this same Play artifact by version code and recorded SHA-256.

## 5. Reject or Escalate

Reject the candidate for any failed check, unexpected SDK initialization,
wrong package/version/certificate, checksum drift, missing profile or mapping,
Firebase project mismatch, Data Safety mismatch, or smoke-test regression.
Preserve logs that contain no credentials, revoke exposed credentials if any
secret handling failed, and create a new version code for a corrected AAB.

Production rollout and rollback decisions are intentionally deferred to the
Task 10 compliance and staged-rollout process.

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
