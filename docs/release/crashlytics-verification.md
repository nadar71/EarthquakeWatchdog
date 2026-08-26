# Crashlytics Release Verification

## Build Policy

- `debug` keeps Crashlytics collection disabled and uses the Logcat diagnostics adapter.
- A release build requires `app/google-services.json`; the file is ignored by Git and must be provisioned from the protected release environment.
- Crashlytics mapping upload defaults to disabled. A store artifact must run `:app:validateStoreReleaseConfiguration` and `:app:bundleRelease` with `-PcrashlyticsMappingUploadEnabled=true` in protected CI.
- `scripts/verify_release_configuration.sh` uses a disposable synthetic Firebase configuration, forces `-PcrashlyticsMappingUploadEnabled=false`, restores any existing local configuration, and never contacts Firebase.

## Pending Firebase Console And Device Checks

1. In the protected CI environment, provision the correct `app/google-services.json` only for the intended Firebase project.
2. Run `./gradlew :app:validateStoreReleaseConfiguration :app:bundleRelease -PcrashlyticsMappingUploadEnabled=true` using the protected signing and Maps inputs.
3. Install the signed internal-release artifact on a physical device and confirm normal app startup, list refresh, map, statistics, settings, and permission-denied flows.
4. Use a temporary, developer-only verification action outside the committed source tree to send one sanitized non-fatal and one test fatal to the intended Firebase project. Do not pass location, address, distance, consent, advertising identifiers, URLs, keys, or arbitrary values.
5. Confirm the two reports arrive in the correct project, show deobfuscated stack frames with the uploaded mapping, and contain only the closed category/event and sanitized failure type.
6. Remove the temporary action before committing or distributing any artifact, then repeat the normal signed build without it.

No Firebase fatal/non-fatal trigger is present in this repository. This task does not upload mappings or send diagnostic events.
