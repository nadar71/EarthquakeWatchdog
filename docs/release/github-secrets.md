# Protected GitHub Release Configuration

The `Android Release Bundle` workflow binds only its credential-bearing job to
the GitHub environment named exactly `production-release`. Configure required
reviewers, prevent self-review where the repository plan permits it, and
restrict the environment to protected `v*` tags before adding credentials.
Repository and pull-request workflows do not receive these values. Manual runs
must be dispatched from the exact `v<versionName>` tag with GitHub CLI or the
workflow-dispatch REST API; do not rely on the web branch picker.

## Required Environment Secrets

| GitHub environment secret | Purpose | Gradle contract |
| --- | --- | --- |
| `RELEASE_KEYSTORE_BASE64` | One-line base64 encoding of the Play upload keystore. | Decoded to a temporary mode-`600` file and written as generated `release_storeFile`. |
| `GOOGLE_SERVICES_JSON_BASE64` | One-line base64 encoding of the production Firebase `google-services.json`. | Decoded temporarily to `app/google-services.json`. Package, project id, and Android app id must match the approved environment values. |
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

## Required Environment Variables

Set these non-secret variables on `production-release`:

| GitHub environment variable | Required value |
| --- | --- |
| `RELEASE_CERT_SHA256` | Approved SHA-256 fingerprint of the Play upload certificate. |
| `FIREBASE_PROJECT_ID` | Exact `project_info.project_id` from the production Firebase configuration. |
| `FIREBASE_APP_ID` | Exact `client_info.mobilesdk_app_id` for the production Android client with package `com.indiewalk.watchdog.earthquake`. |

Do not invent either Firebase identifier and do not commit them solely to make
CI pass. Read both from the approved production Firebase project and compare
them with the decoded configuration before the first protected run.

`RELEASE_CERT_SHA256` may include colons. Obtain it from the keystore while
allowing `keytool` to prompt for the password:

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
upload. The AAB is always built with upload disabled and fully verified first.
Only then does the workflow invoke `:app:uploadCrashlyticsMappingFileRelease`.
A successful upload adds a checksum-bound
`crashlytics-mapping-upload-receipt.json`; absence of that receipt means upload
completion was not recorded. Local dry runs always pass
`-PcrashlyticsMappingUploadEnabled=false` and never create a receipt.

Secret cleanup is fail-closed. If the marker or Firebase hash sidecar is
missing, or the decoded Firebase file changed, the workflow fails and does not
claim cleanup succeeded. Do not replace the sidecar or delete an unexpected
file just to make cleanup pass. Preserve the failure log without exposing file
contents, let GitHub destroy the ephemeral runner, rotate credentials if
tampering or exposure is plausible, and investigate before retrying. For a
local dry run, stop using that checkout and inspect the unexpected file before
performing any deliberate manual removal.

Run the protected manual workflow from an exact release tag:

```bash
gh workflow run android-release.yml \
  --ref v3.0.0 \
  -f upload_crashlytics_mapping=false
```

Use `true` only for an approved mapping upload. Replace `v3.0.0` with the tag
that exactly matches `versionName`. GitHub requires the workflow file itself to
exist on the repository default branch before `workflow_dispatch` can be
started; `--ref` selects the tagged workflow revision. The equivalent REST
dispatch uses `ref: "v3.0.0"` and an `inputs.upload_crashlytics_mapping` value.

## Future Play API Credential

No Play service-account secret is currently consumed or decoded. If automated
internal-track upload is approved later, use a separately reviewed environment
secret such as `PLAY_SERVICE_ACCOUNT_JSON_BASE64`, grant only the minimum Play
Console release permission, and add a distinct human-approved internal-track
job. Do not extend this workflow directly to closed or production promotion.
