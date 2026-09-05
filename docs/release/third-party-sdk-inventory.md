# Third-Party SDK Inventory

Last reviewed: 2026-08-27. This inventory covers the direct production dependencies in `app/build.gradle.kts` and the material transitive SDK families. It must be reviewed whenever a dependency, ad mediation setting, Firebase product, or network policy changes.

## Networked SDKs

| SDK / artifact family | Status | Purpose | Data and network scope | Play Data safety and owner action |
| --- | --- | --- | --- | --- |
| USGS Earthquake API via Ktor 2.3.7 / transitive OkHttp (`io.ktor:*`, `com.squareup.okhttp3:okhttp`) | Direct Ktor; transitive OkHttp | Download public earthquake feeds/statistics. | HTTPS requests go to `earthquake.usgs.gov/fdsnws/event/1/query` and `/count`. App code sends dates, magnitude threshold, and pagination/count parameters, but not selected user/manual coordinates or ad identifiers; normal network metadata such as IP is visible to the service. | Owner: Android. Validate the exact release endpoint/headers with a proxy and update this row if request parameters change. |
| Google Maps SDK 19.0.0 and Maps Compose 4.3.0 (`play-services-maps`, `maps-compose`) | Direct | Render map tiles and earthquake pins. | Official SDK disclosure includes map-request/device metadata, IP address, SDK identifier, crash/diagnostic metrics, and interaction/camera events. The app supplies camera coordinates, including user/manual positions. | Owner: Android + privacy. Reconcile location/interactions/diagnostics/identifiers in Data safety and verify package plus Play App Signing/API key restrictions. |
| Play Services Location 21.3.0 (`play-services-location`) | Direct | Obtain device last-known location after permission. | Reads precise or approximate device location in foreground. No app-controlled backend request is made by this path; the retained exact location and reverse-geocoded text are local app data. | Owner: Android. Validate precise, approximate-only, denial, fallback, manual-location, and retention behavior on physical devices. |
| Google Mobile Ads SDK 24.5.0 (`play-services-ads`) | Direct | Show banner advertisements. | Official disclosure identifies IP-derived approximate location, app interactions, diagnostics, and device/account identifiers for ads, analytics, and fraud prevention. Mediation/configuration can add recipients. | `BLOCKED`: production app/banner ID resources are ignored and absent from `HEAD`/protected provisioning. Owner: ads/privacy. Fix provenance, inspect the signed artifact, declare Contains ads, and reconcile exact-version Data safety behavior. |
| Google User Messaging Platform 3.2.0 (`user-messaging-platform`) | Direct | Present and store ad-consent choices. | Contacts Google consent/ads services; applicable messages and partners depend on published configuration and geography. | Code uses the production privacy-options flow and fail-closed ad gating. `PENDING`: owner tests EEA/non-EEA/regulated-US/withdrawal/relaunch/offline behavior against published configuration. |
| Firebase Crashlytics 20.1.0 via Firebase BOM 34.18.0 | Direct | Release crash and non-fatal diagnostics. | Automatic fields include stack trace, app state, device metadata, Crashlytics installation UUID, and transitive Firebase Installations/Sessions data. App-added diagnostics use closed sanitized categories/types and exclude raw location/address/distance/consent/request/ad-ID values. | Owner: Android/privacy. Reconcile diagnostics/identifier declarations and verify protected internal-build delivery, version, deobfuscation, intended project, and matching mapping receipt. |

## Local Or Build-Time SDKs

| SDK / artifact family | Status | Purpose | Data and network scope | Play Data safety and owner action |
| --- | --- | --- | --- | --- |
| AndroidX Compose, Activity, Lifecycle, Navigation 3 1.0.0, ConstraintLayout Compose, Material Components | Direct | UI, lifecycle, navigation, and app theme. | No app-controlled network behavior found. | No independent SDK disclosure expected. Owner: Android; reassess resolved transitives after upgrades. |
| Hilt / Dagger (resolved Hilt 2.54), KSP, AndroidX Hilt lifecycle Compose | Direct | Dependency injection and generated code. | Build/runtime framework; no app-controlled network behavior found. | No independent disclosure expected. Owner: Android. |
| Room 2.7.1 / DataStore 1.1.7 | Direct | Local database and preferences. | Stores earthquake cache/statistics and app/filter/location settings locally. Backup/data-extraction rules exclude database, DataStore, shared-preference, and cache domains. | Owner: Android + privacy. Retention lasts until replace/clear storage/uninstall; no in-app clear-all or TTL deletion exists. |
| Gson (`com.google.code.gson:gson`, Ktor Gson serialization) | Direct | Parse USGS JSON and statistics cache data. | No independent network behavior. | No independent disclosure expected. Owner: Android. |
| Kotlin coroutines, serialization, desugaring, Accompanist permissions | Direct | Language/runtime support and Android permission UI. | No independent network behavior. | No independent disclosure expected. Owner: Android. |

## Removed During Task 6

| Artifact family | Removal evidence | Follow-up |
| --- | --- | --- |
| Retrofit and converter-Gson | No Kotlin/Java imports; the app uses Ktor in `NetworkModule`; dependency insight showed Retrofit only came from the direct converter declaration. | Keep Ktor as the single HTTP client stack. |
| Direct OkHttp, logging-interceptor, and okhttp-urlconnection | No direct imports; Ktor's OkHttp engine supplies the compatible runtime client transitively. This also removes the direct alpha `5.0.0-alpha.2` declarations. | Do not re-add a direct OkHttp dependency unless app code uses its API; use stable `5.5.0` only if that becomes necessary and is verified against Ktor. |
| Coil | No image-loading API imports or calls. | Re-add only with an actual image-rendering feature and an approved privacy review. |
| Unity Ads | No initialization, ad display, manifest, or resource usage. | Add only with explicit mediation/product approval and an inventory/Data safety update. |
| Multidex | `minSdk` is 26, where native multidex support is available; no Multidex application setup exists. | Do not restore without a verified compatibility requirement. |
| Direct Maps KTX / Maps Utils artifacts | No direct extension or utility API imports; Maps Compose supplies the required Maps KTX transitively. | Re-add a specific utility only when code uses it. |

## Validation Notes

- Network hosts listed as SDK-managed are not an exhaustive endpoint allowlist. Confirm them with current vendor documentation and a release-candidate traffic capture before Play submission.
- The inventory does not replace the Google Play Data safety form, privacy policy, ad declaration, SDK terms, or a legal review.
- The release owner must update this file, the Data safety form, and `docs/release/device-test-matrix.md` together when an SDK or data flow changes.
- The exact release graph was last reconciled with `releaseRuntimeClasspath` on
  2026-08-27. Gson, Kotlin/coroutines/serialization/desugaring, Accompanist
  permissions, AndroidX ProfileInstaller, UI/lifecycle/core libraries, and
  build-only Android/Google Services/Crashlytics/Baseline Profile plugins were
  reviewed; no additional app-authored endpoint was found.
- `data-safety-inventory.md` is the detailed processing/retention worksheet and
  `play-compliance-checklist.md` owns the external declaration/sign-off state.
