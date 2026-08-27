# Data Safety And Processing Inventory

Reviewed: 2026-08-27. This code inventory supports, but does not replace, the
owner's final Data safety and privacy-policy review of the exact production
artifact and external SDK configuration.

## Status Vocabulary

- `CODE-VERIFIED`: behavior is evidenced in tracked code/configuration.
- `PENDING`: vendor-version, Console, legal, or physical evidence is required.
- `BLOCKED`: a known disclosure or implementation mismatch prevents release.
- `NOT APPLICABLE`: reviewed and not used by the current app.

Google Play distinguishes data kept only on-device from data transmitted off
device. SDK collection counts as app collection, while opening a user-selected
external browser is generally not app sharing. The candidate form below is
therefore intentionally conservative and remains `PENDING`.

## First-Party Data And Retention

| Data / source | Processing and purpose | Storage / retention / deletion | Status and evidence |
| --- | --- | --- | --- |
| Precise or approximate device location | A foreground permission flow obtains the last-known location. It calculates earthquake distances, centers the map, and drives nearest/nearby statistics. No background location exists. | Exact latitude/longitude plus reverse-geocoded city, country, and address are retained in app DataStore until overwritten, app storage is cleared, or the app is uninstalled. There is no TTL or in-app clear-all action. | `CODE-VERIFIED`: `app/src/main/AndroidManifest.xml`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_intro/presentation/IntroScreen.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/core/data/repository/LocationRepositoryImpl.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqsmap/util/MapsUtils.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/core/data/local/preferences/AppPrefs.kt` |
| Manual position | A map long-press stores exact latitude/longitude and resolved location text for the same distance/map/statistics purposes without GPS. | Same local retention/deletion behavior as device location. | `CODE-VERIFIED`: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqsmap/presentation/ui/EarthquakeMapScreen.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqsmap/presentation/ui/MapViewModel.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/core/data/local/preferences/AppPrefs.kt` |
| App/filter preferences | Theme, units, permission-asked flag, last refresh, manual-location flag, filters, sort, magnitude, interval, and count. | DataStore files persist until clear storage/uninstall. | `CODE-VERIFIED`: `app/src/main/java/com/indiewalk/watchdog/earthquake/core/data/local/preferences/AppPrefs.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/data/local/preferences/FilterPrefs.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/core/data/repository/AppPreferencesRepositoryImpl.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/core/data/repository/FilterPreferencesRepositoryImpl.kt` |
| Earthquake database | Public USGS event/feed fields, coordinates, event URLs/IDs, and locally derived distance from selected position. | Room database refresh replaces feed/event data. No user-controlled purge; clear storage/uninstall deletes it. | `CODE-VERIFIED`: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/data/local/db/EarthquakeDatabase.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/domain/model/db/EQEntity.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/domain/model/db/EQFeedSnapshotEntity.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/data/repository/EQRepositoryImpl.kt` |
| Statistics cache | Global counts, strongest/nearest/insight event fields and selected-position-derived nearby distance. | One Room cache row is replaced; a 15-minute TTL controls refresh, not deletion. Clear storage/uninstall deletes it. | `CODE-VERIFIED`: `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsCacheEntity.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/local/StatisticsCacheDao.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/repository/EarthquakeStatisticsRepositoryImpl.kt` |
| Support email | The user may open an external mail client with the developer address. Their email address, message, and attachments are voluntarily processed by the selected mail provider and developer only after the user sends. | No in-app copy is stored. External mailbox retention is an owner-policy decision. | `PENDING`: privacy owner must describe support-contact handling and retention; source evidence is `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_settings/presentation/ui/SettingsScreen.kt` and `app/src/main/java/com/indiewalk/watchdog/earthquake/core/util/MailUtil.kt`. |
| User account/profile | No registration, login, cloud account, social graph, payment, or first-party account identifier. | No account store exists. Local deletion is by clear storage/uninstall. | `NOT APPLICABLE`: source/dependency/manifest inventory |

`app/src/main/res/xml/backup_rules.xml` and
`app/src/main/res/xml/data_extraction_rules.xml` exclude all database,
DataStore, shared-preference, and cache domains from cloud backup and
device-to-device transfer. This is `CODE-VERIFIED`; final merged rules still
need release-artifact inspection. The app does not provide an in-app clear-all
control. A public privacy policy must state practical retention and deletion
behavior rather than claiming automatic expiry.

## Off-Device And SDK Processing

| Recipient / flow | Data that can leave the device | Purpose | Candidate Play treatment | Evidence / owner action |
| --- | --- | --- | --- | --- |
| USGS Earthquake API through Ktor / OkHttp | Query dates, magnitude threshold, pagination/count parameters, IP/network metadata inherent to HTTPS. User/manual coordinates are not included in app-controlled USGS requests. | App functionality: earthquake feed and statistics. | Network request metadata is `PENDING` privacy review; public earthquake data is not user data supplied by the user. | `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/data/remote/EarthquakeApi.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_statistics/data/remote/EarthquakeStatisticsApi.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/core/di/NetworkModule.kt`; verify production traffic. |
| Android Geocoder | Device/manual coordinates and locale can be processed by the device's implementation-dependent geocoding service. | Convert a selected position to city/country/address. | Precise location collection/processing candidate; service-provider sharing analysis `PENDING`. | `app/src/main/java/com/indiewalk/watchdog/earthquake/core/data/repository/LocationRepositoryImpl.kt` and `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqsmap/util/MapsUtils.kt`; validate behavior/provider on physical devices. |
| Google Maps SDK | Map request metadata, IP address, SDK identifier/version, device metadata, crash/diagnostic metrics, interaction/camera events, and coordinates needed to render/center the map. | Map display, markers, user/manual location selection. | Location, app interactions, diagnostics, device identifiers candidates; purposes include app functionality, analytics, fraud/security depending on SDK behavior. | `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqsmap/presentation/ui/EarthquakeMapScreen.kt`, Maps SDK disclosure; reconcile exact production version/configuration. |
| Firebase Crashlytics | Sanitized app diagnostic category/type plus automatic stack traces, app state, device metadata, Crashlytics installation UUID, and transitive Firebase Installations/Sessions data. | Crash diagnosis and app stability. | Crash logs/diagnostics and device/other identifiers candidates; collection required vs optional must match automatic release behavior. | `app/src/main/java/com/indiewalk/watchdog/earthquake/core/diagnostics/AppDiagnostics.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/core/diagnostics/CrashlyticsDiagnostics.kt`; `app/src/main/AndroidManifest.xml`. Delivery and exact project remain `PENDING`. |
| Google Mobile Ads SDK | IP-derived approximate location, app interactions, diagnostics, device/account advertising identifiers, and network metadata as described by the SDK. | Advertising, analytics, fraud prevention/security, personalization according to consent/configuration. | Approximate location, app activity, diagnostics, device/other IDs are collection/share candidates. Exact answers remain `PENDING`. | `app/src/main/java/com/indiewalk/watchdog/earthquake/EarthquakeApp.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_ads/presentation/AdMobBannerView.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_ads/util/RequestConfigurationUtils.kt`. Production ID provenance is `BLOCKED`. |
| Google UMP SDK | Consent-status/request metadata, device/network identifiers needed to determine/show configured privacy messages. | Consent/privacy-choice management for ads. | App activity/device identifier handling is `PENDING` exact-version review. Never preserve consent payload values in incident evidence. | `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_eqslist/presentation/ui/MainActivity.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_ads/util/ConsentManager.kt`; `app/src/main/java/com/indiewalk/watchdog/earthquake/feat_settings/presentation/ui/SettingsScreen.kt`; production withdrawal path is `BLOCKED`. |
| External browser links | User-selected USGS event/tell-us pages, Play listing, and project website open in another app. The destination receives normal browser request data, not data posted by this app. | View source/event content, report to USGS, view listing/site. | Generally outside in-app sharing when navigation is user-initiated; final policy wording remains `PENDING`. | `EarthquakeDetailDialog.kt`, `SettingsScreen.kt`, URI helpers; verify every URL is HTTPS/current. |
| Android Sharesheet / email | Selected earthquake text/link or voluntary support content is handed to a user-selected external app. | User-requested sharing/support. | User-initiated transfer; document accurately, do not claim the receiving app is controlled by this app. | Share and email intents in presentation code. |

No WebView, first-party server, authentication SDK, payment SDK, social SDK,
analytics product, push-messaging product, camera, microphone, contacts, files,
health data, or background-location path was found (`NOT APPLICABLE`).

## Data Safety Candidate Worksheet

These are review candidates, not submitted answers.

| Play data category | Collected | Shared | Required/optional | Candidate purposes | Decision |
| --- | --- | --- | --- | --- | --- |
| Precise location | Yes via Maps/Geocoder when location/manual map features are used | SDK/service-provider assessment required | Permission optional; manual coordinates optional | App functionality | `PENDING` privacy review |
| Approximate location | Yes via Ads IP processing; possibly permission/SDK paths | Ads recipient assessment required | Ads/SDK behavior dependent | Advertising, analytics, fraud prevention, app functionality | `PENDING` privacy review |
| App interactions | Maps and Mobile Ads candidates | Recipient assessment required | SDK behavior | App functionality, analytics, advertising | `PENDING` exact-SDK review |
| Crash logs / diagnostics | Yes in release via Crashlytics plus Ads/Maps SDK candidates | Service-provider assessment required | Crashlytics automatic in release | Analytics/app functionality/fraud prevention as applicable | `PENDING` exact-SDK review |
| Device or other IDs | Crashlytics UUID/Firebase installation data and Mobile Ads/Maps identifiers | Recipient assessment required | SDK behavior | Analytics, advertising, fraud prevention, app functionality | `PENDING` exact-SDK review |
| Developer communications | Voluntary support email after external mail-app send | Mail provider/developer | Optional | Developer communications | `PENDING` policy/retention decision |

The privacy owner must review whether each provider is a service provider or a
third party under the final Play definitions and contracts. Do not infer
"encrypted in transit," deletion-request behavior, or optionality solely from
this code inventory. App-controlled USGS calls use HTTPS and app cleartext is
disabled, but every SDK must be evaluated from current official documentation.

## Diagnostics Sanitization

`app/src/main/java/com/indiewalk/watchdog/earthquake/core/diagnostics/AppDiagnostics.kt`
maps failures to closed categories/safe types and
constructs a sanitized exception without the original message, cause,
suppressed exceptions, or caller-controlled stack frames. Expected network,
location, security, and cancellation failures are filtered. Release collection
is enabled and debug collection disabled through manifest placeholders.

This is `CODE-VERIFIED` for app-added diagnostics only. It does not suppress
automatic Crashlytics/SDK metadata or prove Firebase delivery. Incident notes
must not contain credentials, raw coordinates/addresses, ad identifiers,
consent payloads, or user support content.

## Production Dependency And Endpoint Inventory

Resolved production families reviewed from `app/build.gradle.kts` and
`releaseRuntimeClasspath`:

- Google Maps SDK 19.0.0 and Maps Compose 4.3.0.
- Google Play Services Location 21.3.0.
- Google Mobile Ads 24.5.0 and UMP 3.2.0.
- Firebase Crashlytics 20.1.0 via Firebase BOM 34.18.0, including Firebase
  Installations/Sessions transitively.
- Ktor client 2.3.7 with OkHttp transport.
- Room 2.7.1 and DataStore 1.1.7 for local persistence.
- Hilt/Dagger (resolved Hilt 2.54), Gson, Compose/Navigation 3, AndroidX
  lifecycle/activity/core, and Material UI libraries. These local/UI/DI
  families do not create a separately identified app endpoint in the current
  source, but their transitive graph remains subject to dependency review.

App-authored network destinations are `https://earthquake.usgs.gov/fdsnws/event/1/`
and user-initiated external links to USGS, Google Play, email, and the project
site. Firebase, Ads/UMP, Maps, Geocoder, and Play Services use vendor-managed
destinations. Verify the exact signed artifact and runtime traffic before form
submission.

## Official Sources

- [Google Play Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) (Reviewed 2026-08-27).
- [Google Play User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311) (Reviewed 2026-08-27).
- [Android Data safety declaration guidance](https://developer.android.com/privacy-and-security/declare-data-use) (Reviewed 2026-08-27).
- [Android Geocoder reference](https://developer.android.com/reference/android/location/Geocoder.html) (Reviewed 2026-08-27).
- [Maps SDK Play data disclosure](https://developers.google.com/maps/documentation/android-sdk/play-data-disclosure) (Reviewed 2026-08-27).
- [Mobile Ads Play data disclosure](https://developers.google.com/admob/android/privacy/play-data-disclosure) (Reviewed 2026-08-27).
- [Firebase Android Play data disclosure](https://firebase.google.com/docs/android/play-data-disclosure) (Reviewed 2026-08-27).
- [USGS Earthquake Catalog API](https://earthquake.usgs.gov/fdsnws/event/1/) (Reviewed 2026-08-27).
