#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
readonly PROJECT_ROOT
readonly BUNDLETOOL="${BUNDLETOOL_FIXTURE:?Set BUNDLETOOL_FIXTURE to the checksum-verified bundletool JAR}"
readonly BUNDLETOOL_VERSION="${BUNDLETOOL_VERSION:-1.18.3}"
readonly BUNDLETOOL_SHA256="${BUNDLETOOL_SHA256:-a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29}"
TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/signed_release_dry_run.XXXXXX")"
readonly TEST_ROOT
readonly SOURCE_KEYSTORE="$TEST_ROOT/disposable-source.keystore"
readonly SOURCE_FIREBASE="$TEST_ROOT/disposable-google-services.json"
readonly SECRET_DIR="$TEST_ROOT/prepared-secrets"
readonly FIREBASE_DESTINATION="$PROJECT_ROOT/app/google-services.json"
readonly VERIFIED_DIR="$TEST_ROOT/verified-release"

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    exit 1
}

cleanup() {
    if [[ -f "$SECRET_DIR/.release-secrets" ]]; then
        "$PROJECT_ROOT/scripts/prepare_release_secrets.sh" cleanup \
            --output-dir "$SECRET_DIR" \
            --firebase-destination "$FIREBASE_DESTINATION" >/dev/null
    fi
    chmod -R u+w "$TEST_ROOT" 2>/dev/null || true
    rm -rf "$TEST_ROOT"
}
trap cleanup EXIT HUP INT TERM

[[ -s "$BUNDLETOOL" ]] || fail "bundletool fixture is missing"
[[ ! -e "$FIREBASE_DESTINATION" ]] || fail "refusing to replace an existing app/google-services.json"

export release_keyAlias="task9-disposable-alias"
export release_keyPassword="task9-disposable-key-password"
export release_storePassword="task9-disposable-store-password"
export MAPS_API_KEY_RELEASE="task9-disposable-maps-placeholder"
export FIREBASE_PROJECT_ID="earthquake-task9-disposable"
export FIREBASE_APP_ID="1:123456789:android:task9disposable"
export ADMOB_APP_ID_RELEASE="ca-app-pub-1234567890123456~1234567890"
export ADMOB_BANNER_ID_RELEASE="ca-app-pub-1234567890123456/1234567890"
export PRIVACY_POLICY_URL_RELEASE="https://example.invalid/earthquake-watchdog/privacy"

keytool -genkeypair \
    -alias "$release_keyAlias" \
    -keyalg RSA \
    -keysize 2048 \
    -storetype JKS \
    -validity 365 \
    -dname 'CN=Earthquake Watchdog Task 9 Disposable' \
    -keystore "$SOURCE_KEYSTORE" \
    -storepass:env release_storePassword \
    -keypass:env release_keyPassword \
    -noprompt >/dev/null 2>&1

cat > "$SOURCE_FIREBASE" <<'JSON'
{
  "project_info": {
    "project_number": "123456789",
    "project_id": "earthquake-task9-disposable",
    "storage_bucket": "earthquake-task9-disposable.appspot.com"
  },
  "client": [{
    "client_info": {
      "mobilesdk_app_id": "1:123456789:android:task9disposable",
      "android_client_info": {
        "package_name": "com.indiewalk.watchdog.earthquake"
      }
    },
    "api_key": [{"current_key": "task9-disposable-firebase-key"}]
  }],
  "configuration_version": "1"
}
JSON

RELEASE_KEYSTORE_BASE64="$(base64 < "$SOURCE_KEYSTORE" | tr -d '\r\n')"
export RELEASE_KEYSTORE_BASE64
GOOGLE_SERVICES_JSON_BASE64="$(base64 < "$SOURCE_FIREBASE" | tr -d '\r\n')"
export GOOGLE_SERVICES_JSON_BASE64

"$PROJECT_ROOT/scripts/prepare_release_secrets.sh" prepare \
    --output-dir "$SECRET_DIR" \
    --firebase-destination "$FIREBASE_DESTINATION"

(
    cd "$PROJECT_ROOT"
    ./gradlew --offline \
        -PreleaseSecretsFile="$SECRET_DIR/release.properties" \
        -PcrashlyticsMappingUploadEnabled=false \
        :app:bundleRelease
)

CERT_SHA256="$(
    keytool -exportcert -rfc \
        -alias "$release_keyAlias" \
        -keystore "$SOURCE_KEYSTORE" \
        -storepass:env release_storePassword \
        | openssl x509 -noout -fingerprint -sha256 \
        | sed 's/^[^=]*=//' \
        | tr -d ':[:space:]'
)"
readonly CERT_SHA256

GITHUB_SHA="$(git -C "$PROJECT_ROOT" rev-parse HEAD)" \
GITHUB_REF="local-disposable-dry-run" \
CRASHLYTICS_MAPPING_UPLOAD_REQUESTED=false \
    "$PROJECT_ROOT/scripts/verify_aab.sh" \
        --aab "$PROJECT_ROOT/app/build/outputs/bundle/release/app-release.aab" \
        --mapping "$PROJECT_ROOT/app/build/outputs/mapping/release/mapping.txt" \
        --bundletool "$BUNDLETOOL" \
        --bundletool-version "$BUNDLETOOL_VERSION" \
        --bundletool-sha256 "$BUNDLETOOL_SHA256" \
        --expected-package com.indiewalk.watchdog.earthquake \
        --expected-version-code 11 \
        --expected-version-name 3.0.0 \
        --expected-cert-sha256 "$CERT_SHA256" \
        --expected-admob-app-id "$ADMOB_APP_ID_RELEASE" \
        --expected-admob-banner-id "$ADMOB_BANNER_ID_RELEASE" \
        --expected-privacy-policy-url "$PRIVACY_POLICY_URL_RELEASE" \
        --output-dir "$VERIFIED_DIR"

BUNDLETOOL_FIXTURE="$BUNDLETOOL" \
AAB_FIXTURE="$PROJECT_ROOT/app/build/outputs/bundle/release/app-release.aab" \
MAPPING_FIXTURE="$PROJECT_ROOT/app/build/outputs/mapping/release/mapping.txt" \
    "$PROJECT_ROOT/scripts/tests/verify_aab_test.sh"

"$PROJECT_ROOT/scripts/prepare_release_secrets.sh" cleanup \
    --output-dir "$SECRET_DIR" \
    --firebase-destination "$FIREBASE_DESTINATION"
[[ ! -e "$SECRET_DIR" ]] || fail "prepared release-secret directory survived cleanup"
[[ ! -e "$FIREBASE_DESTINATION" ]] || fail "decoded Firebase configuration survived cleanup"

printf 'PASS: disposable signed release built, verified, and cleaned without external upload\n'
