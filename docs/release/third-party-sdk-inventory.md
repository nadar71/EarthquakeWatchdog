# Third-Party SDK Inventory

Last reviewed: 2026-08-27. This inventory covers the direct production dependencies in `app/build.gradle.kts` and the material transitive SDK families. It must be reviewed whenever a dependency, ad mediation setting, Firebase product, or network policy changes.

## Networked SDKs

| SDK / artifact family | Status | Purpose | Data and network scope | Play Data safety and owner action |
| --- | --- | --- | --- | --- |
| USGS Earthquake API via Ktor / OkHttp (`io.ktor:*`, transitive `com.squareup.okhttp3:okhttp`) | Direct Ktor; transitive OkHttp | Download public earthquake feeds. | Requests go to the configured USGS endpoint (`earthquake.usgs.gov`). The app does not send account data, location, or ad identifiers in this request; normal network metadata such as IP address is still visible to the service. | Declare no user data sent by app code for the feed unless a future request changes this. Owner: Android. Validate the exact release endpoint and request headers with a proxy before release. |
| Google Maps SDK, Maps Compose, Play Services Maps (`com.google.android.gms:play-services-maps`, `com.google.maps.android:maps-compose`) | Direct | Render map tiles and earthquake pins. | Google Maps Platform service traffic and SDK telemetry. The app supplies camera coordinates, including the user/manual map position when that flow is used. Exact hosts and automatic SDK fields require Google documentation and release-traffic validation. | Location is processed when the map is centered on user/manual position. Review Maps Platform terms, privacy policy, and Data safety location disclosure. Owner: Android + product. |
| Play Services Location (`com.google.android.gms:play-services-location`) | Direct | Obtain device last-known location after permission. | Reads precise or approximate device location only after Android permission. No app-controlled backend request is made by this SDK path. | Data safety must match the actual permission flow and retained local location preference. Owner: Android. Validate denial/manual-location behavior on device. |
| Google Mobile Ads SDK (`com.google.android.gms:play-services-ads`) | Direct | Show banner advertisements. | Contacts Google ad services and may collect or share identifiers, device information, diagnostics, and advertising/usage data according to SDK configuration and consent. Ad mediation can add further partners. | High-risk declaration area. Complete the Google Play Data safety and Ads declaration from the exact production ad configuration, not this table alone. Owner: product/privacy. Validate EEA and non-EEA consent flows. |
| Google User Messaging Platform (`com.google.android.ump:user-messaging-platform`) | Direct | Present and store ad-consent choices. | Contacts Google consent/ads services and handles consent state. Hosts and downstream partners depend on the published message and ad configuration. | Privacy policy and Data safety must reflect consent handling and any Ads SDK sharing. Owner: product/privacy. Re-validate whenever the UMP message or mediation changes. |
| Firebase Crashlytics (`com.google.firebase:firebase-crashlytics` with Google Services/Crashlytics Gradle plugins) | Direct | Release crash and non-fatal diagnostics. | Firebase endpoint families receive automatic crash/device/app-version diagnostics. App code sends only closed error and operation categories through `AppDiagnostics`; it excludes location, address, distance, consent payloads, request values, and ad identifiers. | Declare diagnostics/crash data according to the active Firebase SDK version and collection policy. Owner: Android. Verify a protected release build reaches the intended Firebase project with mapping symbols. |

## Local Or Build-Time SDKs

| SDK / artifact family | Status | Purpose | Data and network scope | Play Data safety and owner action |
| --- | --- | --- | --- | --- |
| AndroidX Compose, Activity, Lifecycle, Navigation 3, ConstraintLayout, Material Components | Direct | UI, lifecycle, navigation, and app theme. | No app-controlled network behavior. | No independent SDK disclosure expected. Owner: Android; reassess if a library adds telemetry. |
| Hilt / KSP (`com.google.dagger:hilt-android`, `androidx.hilt:*`) | Direct | Dependency injection and generated code. | Build/runtime framework; no network behavior. | No independent disclosure expected. Owner: Android. |
| Room / DataStore (`androidx.room:*`, `androidx.datastore:*`) | Direct | Local database and preferences. | Stores earthquake cache and app settings locally. Backup/data-extraction rules exclude sensitive preference and database paths. | Data retention/deletion statements must match product behavior. Owner: Android + product. |
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
