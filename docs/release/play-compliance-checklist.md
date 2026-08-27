# Google Play Compliance Checklist

Reviewed: 2026-08-27. This is an evidence record, not a declaration that any
Google Play, Firebase, AdMob, Google Cloud, or physical-device check has run.

## Status Vocabulary

- `CODE-VERIFIED`: proven from tracked source, build configuration, or an
  automated repository check. It does not prove an external configuration.
- `PENDING`: an owner must inspect or execute an external or physical check and
  attach evidence.
- `BLOCKED`: a known mismatch must be resolved before release promotion.
- `NOT APPLICABLE`: the reviewed requirement does not apply, with a reason.

## Release Blockers

| Gate | Status | Code evidence | Responsible action | Evidence path |
| --- | --- | --- | --- | --- |
| Target API for an update submitted on or after 2026-08-31 | `BLOCKED` | `app/build.gradle.kts` has `targetSdk = 35`; the current policy requires API 36 from 2026-08-31. | Android owner: migrate to and verify target API 36 before submitting an update on/after the deadline. | CI links plus final merged manifest and AAB identity in `rollout-record.md` |
| Public privacy policy | `BLOCKED` | No tracked in-app privacy-policy URL or dedicated privacy screen exists. The FAQ is not a substitute for the required public policy. | Privacy owner: publish an active, public, non-PDF policy naming the app/developer; add its URL to the app and store configuration; reconcile it with `data-safety-inventory.md`. | URL, dated screenshot from app, and dated store-configuration screenshot |
| Production AdMob resource provenance | `BLOCKED` | `app/src/main/AndroidManifest.xml` references `@string/admob_key_app_id`; screens reference `admob_key_bottom_banner`. `app/src/main/res/values/ads_key_ids.xml` is ignored and absent from `HEAD`, while the protected workflow does not create it. A clean protected build therefore has no repository-proven resource. | Android/ads owner: choose tracked public IDs or generate the resource in the protected workflow; then extend signed-AAB verification to confirm the expected app ID in the merged manifest and banner ID in resources. Never record a secret as an ad unit ID. | Protected workflow link, redacted resource provenance, and signed-manifest verification output |
| Production consent withdrawal | `BLOCKED` | `SettingsScreen.kt` invokes `ConsentInformation.reset()`. Google's UMP documentation defines `reset()` for testing; production privacy choices should use the privacy-options form/status. The callback also does not synchronize the app-wide ad eligibility flag. | Android/privacy owner: replace the test reset flow, expose the required privacy-options entry point, synchronize ad eligibility, and add regression tests before the candidate build. | Code review, tests, and EEA/non-EEA device evidence |

Do not create a release candidate until every `BLOCKED` row is resolved in a
new commit and re-reviewed. Local untracked files are not acceptable evidence.

## App And Permission Reconciliation

| Requirement | Status | Repository evidence | Responsible action | Evidence path |
| --- | --- | --- | --- | --- |
| Package identity | `CODE-VERIFIED` | `app/build.gradle.kts`: `com.indiewalk.watchdog.earthquake`. | Release owner: compare the signed AAB and accepted version to this value. | `release-provenance.json` and signed-AAB verifier output |
| precise location declaration and use | `CODE-VERIFIED` | `app/src/main/AndroidManifest.xml` declares `ACCESS_FINE_LOCATION`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_intro/presentation/IntroScreen.kt` asks after an explanatory screen; `app/src/main/java/com/indiewalk/watchdog/earthquake/core/data/repository/LocationRepositoryImpl.kt` and `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqsmap/util/MapsUtils.kt` read last-known foreground location. | Privacy owner: declare precise location consistently with SDK processing and local uses in the final form. | `data-safety-inventory.md` plus dated declaration export |
| coarse location declaration and use | `CODE-VERIFIED` | `app/src/main/AndroidManifest.xml` declares `ACCESS_COARSE_LOCATION`, but `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_intro/presentation/IntroScreen.kt` launches only the fine request and evaluates both grants. Approximate-only behavior is not proven. | Android owner: test approximate-only and decide whether coarse permission is genuinely supported/minimum scope; fix or remove it before declaration sign-off. | Physical test result and final permission-flow source |
| background location | `NOT APPLICABLE` | No `ACCESS_BACKGROUND_LOCATION`, foreground service, or background location worker exists. Location is read from foreground UI flows. | Android owner: recheck the final merged manifest for every release. | Merged-manifest report |
| Permission timing and prominent disclosure | `CODE-VERIFIED` | `app/src/main/res/values/strings.xml`, `app/src/main/res/values-it-rIT/strings.xml`, and `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_intro/presentation/IntroScreen.kt` show the purpose/fallback before the launcher; permission launching remains UI-owned. | Privacy owner: compare current localized disclosure and screenshots with the final policy and store review expectations. | EN/IT screenshots and dated review record |
| Minimum permission scope | `PENDING` | Code does not need continuous/background location, but approximate-only support is unresolved. | Android/privacy owner: complete approximate/denied/never-ask-again tests and document the minimum viable permission choice. | Device matrix rows and decision |

## Declarations And Store Content

| Requirement | Status | Repository evidence | Responsible action | Evidence path |
| --- | --- | --- | --- | --- |
| Data safety | `PENDING` | `data-safety-inventory.md` inventories app and SDK flows but is deliberately not a submitted-form answer. | Privacy owner: reconcile every collected/shared data type and purpose against the exact production SDK versions and export the submitted answers. | Dated form export/screenshots linked from `rollout-record.md` |
| Contains ads | `PENDING` | Mobile Ads and banner code are present, so the expected declaration is Yes; external configuration is unverified. | Ads owner: confirm the store declaration says the app contains ads. | Dated declaration screenshot |
| Ads content and placement | `PENDING` | Banners exist on list/map/statistics/settings; no interstitial or deceptive system-style ad code was found. | Product/ads owner: inspect production creatives and placement on all device sizes; verify accidental-click risk and age suitability. | Internal/closed screenshots and tester record |
| content rating | `PENDING` | Repository content is informational earthquake data; ratings are generated from owner answers, not source code. | Product owner: complete/update the questionnaire accurately, including ads, location, external links, and user communication through email. | Rating certificate and dated questionnaire evidence |
| target audience | `PENDING` | `setTagForUnderAgeOfConsent(false)` is hardcoded and the implementation is not designed as child-directed. | Product/privacy owner: select only truthful age groups. If any child age group is selected, pause release and perform Families/ads redesign and review. | Dated target-audience declaration |
| store listing | `PENDING` | App name/package/localized resources exist; current Console text, screenshots, contact details, category, and policy URL are inaccessible from code. | Product owner: verify title/short/full descriptions, screenshots, contact email/site, localization, app function claims, and policy URL against the exact candidate. | EN/IT listing export and screenshots |
| Account deletion | `NOT APPLICABLE` | The app has no sign-up, login, user account, or server account store. Local app data is removed by clear storage/uninstall. | Privacy owner: answer the account question truthfully and re-evaluate if accounts are introduced. | Dated form answer |

## Maps, Signing, Ads, Consent, And Diagnostics

| External gate | Status | Responsible action | Required evidence |
| --- | --- | --- | --- |
| Maps Android application restriction | `PENDING` | Google Cloud owner: restrict the production Maps key to package `com.indiewalk.watchdog.earthquake` plus every active Play App Signing SHA-1 certificate, including a key-upgrade certificate when applicable. Do not rely only on the upload certificate. | Redacted restriction screenshot showing package/fingerprint suffixes and date |
| Maps API restriction | `PENDING` | Google Cloud owner: restrict the key to Maps SDK for Android and remove unrelated APIs. | Redacted API-restriction screenshot |
| Play App Signing certificate match | `PENDING` | Release owner: compare the Play app-signing certificate to the Maps restriction and compare the upload certificate SHA-256 to `RELEASE_CERT_SHA256`. | Dated certificate screenshots and signed-AAB verifier output |
| Maps package/install check | `PENDING` | QA owner: install from the internal track and verify tiles, marker selection, manual position, recentering, and offline failure behavior. | Device-matrix result and screenshots |
| production AdMob app/banner IDs in signed artifact | `PENDING` | Ads/release owner: after resolving resource provenance, verify the signed manifest/resource table uses approved production IDs, while the configured test device alone receives test ads. | Redacted verifier output and internal-track screenshots |
| EEA consent | `PENDING` | Privacy owner: use UMP debug geography on a registered test device; verify first-run form, accept/reject paths, privacy-options visibility, withdrawal, relaunch, and ad eligibility. | Dated test sheet/screenshots; no consent payload values |
| non-EEA consent | `PENDING` | Privacy owner: test non-EEA/default geography and confirm UMP status and ad behavior match configured messages. | Dated test sheet/screenshots |
| Regulated US consent | `PENDING` | Privacy owner: test configured US-state messaging and opt-out/privacy-options behavior where applicable. | Dated test sheet/screenshots |
| Consent withdrawal | `PENDING` | QA owner: after the code blocker is fixed, change consent through the production privacy-options entry point and verify app-wide banner eligibility updates without process restart. | Screen recording and diagnostic categories only |
| Consent offline/failure | `PENDING` | QA owner: cold start and relaunch offline, with stale/no consent info; verify UI remains usable and ads are not requested unless UMP reports `canRequestAds()`. | Network-condition matrix and screenshots |
| Crashlytics delivery | `PENDING` | Firebase owner: verify a controlled nonfatal/fatal from the internal build reaches the intended project, has correct version, and is deobfuscated using the matching mapping receipt. Remove the trigger afterward. | Redacted event screenshot, CI link, mapping receipt |

## Internal And Closed Testing Gates

The exact immutable artifact in `rollout-record.md` must be promoted; never
rebuild between tracks.

| Gate | Status | Responsible action | Required evidence |
| --- | --- | --- | --- |
| internal testing install | `PENDING` | Release owner: upload the verified AAB manually, install from the Play link, and match package/version/certificate to provenance. | Play acceptance screenshot, install result, provenance hashes |
| API/device matrix | `PENDING` | QA owner: cover API 26 minimum, API 35/36, a low/mid-tier physical device, a current physical device, EN/IT, light/dark, large text, and representative screen sizes. | Completed `device-test-matrix.md` with serials redacted |
| Fresh install and permission paths | `PENDING` | QA owner: test precise, approximate-only, denied, permanent denial, manual/default location, and process restart. | Completed matrix and screenshots |
| current-public upgrade | `PENDING` | QA/release owner: install the actual current public version from Play, preserve data, upgrade through internal testing, and verify Room/DataStore behavior. | Before/after version and data evidence |
| Ads/consent/Maps/network/offline | `PENDING` | QA/privacy owner: execute every scenario above on internal-track builds. | Scenario table and timestamps |
| Physical performance baselines | `PENDING` | Performance owner: record cold startup, frame/jank, and memory on representative low/mid-tier and current devices and compare to `performance-baseline.md`. | Raw benchmark output and signed decision |
| closed testing | `PENDING` | Release owner: run representative external testers/devices, triage every report, and require zero open release blockers. | Tester matrix, defect links, vitals/Crashlytics snapshots, approval |

## Sign-Off

| Role | Name | Decision | Date | Evidence |
| --- | --- | --- | --- | --- |
| Android owner | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Privacy/ads owner | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Product/store owner | `PENDING` | `PENDING` | `PENDING` | `PENDING` |
| Release owner | `PENDING` | `PENDING` | `PENDING` | `PENDING` |

## Official Sources

- [Google Play User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311) (Reviewed 2026-08-27).
- [Google Play Data safety form guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) (Reviewed 2026-08-27).
- [Google Play target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en-AU) (Reviewed 2026-08-27).
- [Google Play location permissions policy](https://support.google.com/googleplay/android-developer/answer/16558241?hl=en) (Reviewed 2026-08-27).
- [Google Play prominent disclosure guidance](https://support.google.com/googleplay/android-developer/answer/11150561?hl=en) (Reviewed 2026-08-27).
- [Google Play ads policy](https://support.google.com/googleplay/android-developer/answer/9857753?hl=en) (Reviewed 2026-08-27).
- [Google Play app content declarations](https://support.google.com/googleplay/android-developer/answer/9859455?hl=en) (Reviewed 2026-08-27).
- [Google Play content ratings](https://support.google.com/googleplay/android-developer/answer/9859655?hl=en) (Reviewed 2026-08-27).
- [Google Play target audience and content](https://support.google.com/googleplay/android-developer/answer/9867159?hl=en-GB) (Reviewed 2026-08-27).
- [Google Play store-listing setup](https://support.google.com/googleplay/android-developer/answer/9859152) (Reviewed 2026-08-27).
- [Android location permissions](https://developer.android.com/develop/sensors-and-location/location/permissions) (Reviewed 2026-08-27).
- [Android app signing](https://developer.android.com/studio/publish/app-signing) (Reviewed 2026-08-27).
- [Maps API key security](https://developers.google.com/maps/api-security-best-practices) (Reviewed 2026-08-27).
- [UMP Android implementation](https://developers.google.com/admob/android/privacy) (Reviewed 2026-08-27).
- [Firebase Android data disclosure](https://firebase.google.com/docs/android/play-data-disclosure) (Reviewed 2026-08-27).
