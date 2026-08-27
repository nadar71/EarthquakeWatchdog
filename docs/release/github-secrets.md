# Protected GitHub Release Configuration

The `Android Release Bundle` workflow binds only its credential-bearing job to
the GitHub environment named exactly `production-release`. Configure required
reviewers, prevent self-review where the repository plan permits it, and
restrict the environment to protected `v*` tags before adding credentials.
Repository and pull-request workflows do not receive these values. Manual runs
must select the exact `v<versionName>` tag in the workflow-dispatch ref picker.

## Required Environment Secrets

| GitHub environment secret | Purpose | Gradle contract |
| --- | --- | --- |
| `RELEASE_KEYSTORE_BASE64` | One-line base64 encoding of the Play upload keystore. | Decoded to a temporary mode-`600` file and written as generated `release_storeFile`. |
| `GOOGLE_SERVICES_JSON_BASE64` | One-line base64 encoding of the production Firebase `google-services.json`. | Decoded temporarily to `app/google-services.json`. The package must include `com.indiewalk.watchdog.earthquake`. |
| `release_keyAlias` | Upload-key alias. | Exact Task 2 property name. |
| `release_keyPassword` | Upload-key password. | Exact Task 2 property name. |
| `release_storePassword` | Upload-keystore password. | Exact Task 2 property name. |
| `MAPS_API_KEY_RELEASE` | Production Google Maps Android API key restricted to the release package and Play app-signing certificate. | Exact Task 2 property name. |

Do not create a `release_storeFile` secret. The workflow generates that exact
property with the private runner path. The generated `release.properties`,
keystore, and Firebase file use mode `600` inside a mode-`700` temporary
directory and are removed by an explicit trap before artifact upload.
After preparation, the workflow unsets all six source secret variables before
starting Gradle; only the temporary private files remain available to the build.

Create single-line transport values locally without printing decoded content:

```bash
base64 < upload-keystore.jks | tr -d '\r\n'
base64 < google-services.json | tr -d '\r\n'
```

Paste each result directly into its environment secret. Never commit the
encoded or decoded files; base64 is transport encoding, not encryption.

## Required Environment Variable

Set `RELEASE_CERT_SHA256` as a non-secret variable on `production-release`.
It is the approved SHA-256 fingerprint of the upload certificate, with or
without colons. Obtain it from the keystore while allowing `keytool` to prompt
for the password:

```bash
keytool -exportcert -rfc -alias YOUR_ALIAS -keystore upload-keystore.jks \
  | openssl x509 -noout -fingerprint -sha256
```

Compare this value with the upload certificate shown by Play Console before
the first protected run. Do not substitute the Play app-signing certificate:
the AAB uploaded to Play is signed by the upload key.

## Crashlytics Mapping Control

R8 mapping upload is disabled by default. It is enabled only when a reviewer
approves a manual `workflow_dispatch` run on the exact version tag whose
`upload_crashlytics_mapping` input is explicitly `true`. A `v*` tag build can
create and verify an artifact, but it cannot opt in to an external mapping
upload. Local dry runs always pass
`-PcrashlyticsMappingUploadEnabled=false`.

## Future Play API Credential

No Play service-account secret is currently consumed or decoded. If automated
internal-track upload is approved later, use a separately reviewed environment
secret such as `PLAY_SERVICE_ACCOUNT_JSON_BASE64`, grant only the minimum Play
Console release permission, and add a distinct human-approved internal-track
job. Do not extend this workflow directly to closed or production promotion.
