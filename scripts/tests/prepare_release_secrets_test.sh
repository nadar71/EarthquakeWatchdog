#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
readonly PROJECT_ROOT
readonly SCRIPT="$PROJECT_ROOT/scripts/prepare_release_secrets.sh"
TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/prepare_release_secrets_test.XXXXXX")"
readonly TEST_ROOT
readonly KEYSTORE="$TEST_ROOT/source.keystore"
readonly FIREBASE_SOURCE="$TEST_ROOT/google-services.json"
readonly SECRET_MARKER="task9-secret-must-not-leak"
readonly FIREBASE_PROJECT_ID="earthquake-task9-test"
readonly FIREBASE_APP_ID="1:123456789:android:abcdef"
readonly ADMOB_APP_ID_RELEASE="ca-app-pub-1234567890123456~1234567890"
readonly ADMOB_BANNER_ID_RELEASE="ca-app-pub-1234567890123456/1234567890"
readonly PRIVACY_POLICY_URL_RELEASE="https://example.invalid/earthquake-watchdog/privacy"

cleanup_test() {
    chmod -R u+w "$TEST_ROOT" 2>/dev/null || true
    rm -rf "$TEST_ROOT"
}
trap cleanup_test EXIT

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    exit 1
}

assert_no_secret_output() {
    local log_file="$1"
    for secret_value in \
        "$SECRET_MARKER" \
        release-test-alias \
        "maps-$SECRET_MARKER" \
        "$KEYSTORE_BASE64" \
        "$FIREBASE_BASE64"; do
        if grep -Fq "$secret_value" "$log_file"; then
            fail "a secret value was printed to $(basename "$log_file")"
        fi
    done
}

mode_of() {
    stat -c '%a' "$1" 2>/dev/null || stat -f '%Lp' "$1"
}

[[ -x "$SCRIPT" ]] || fail "secret preparation script is missing or not executable"

keytool -genkeypair \
    -alias release-test-alias \
    -keyalg RSA \
    -keysize 2048 \
    -validity 2 \
    -dname 'CN=Task 9 Test' \
    -keystore "$KEYSTORE" \
    -storepass "$SECRET_MARKER" \
    -keypass "$SECRET_MARKER" \
    -noprompt >/dev/null 2>&1

cat > "$FIREBASE_SOURCE" <<'JSON'
{
  "project_info": {
    "project_number": "123456789",
    "project_id": "earthquake-task9-test",
    "storage_bucket": "earthquake-task9-test.appspot.com"
  },
  "client": [{
    "client_info": {
      "mobilesdk_app_id": "1:123456789:android:abcdef",
      "android_client_info": {
        "package_name": "com.indiewalk.watchdog.earthquake"
      }
    },
    "api_key": [{"current_key": "firebase-$SECRET_MARKER"}]
  }],
  "configuration_version": "1"
}
JSON

KEYSTORE_BASE64="$(base64 < "$KEYSTORE" | tr -d '\r\n')"
readonly KEYSTORE_BASE64
FIREBASE_BASE64="$(base64 < "$FIREBASE_SOURCE" | tr -d '\r\n')"
readonly FIREBASE_BASE64
readonly REQUIRED_ENV=(
    RELEASE_KEYSTORE_BASE64
    GOOGLE_SERVICES_JSON_BASE64
    release_keyAlias
    release_keyPassword
    release_storePassword
    MAPS_API_KEY_RELEASE
    FIREBASE_PROJECT_ID
    FIREBASE_APP_ID
    ADMOB_APP_ID_RELEASE
    ADMOB_BANNER_ID_RELEASE
    PRIVACY_POLICY_URL_RELEASE
)

run_prepare() {
    local output_dir="$1"
    local firebase_destination="$2"
    local -a environment=(
        env -i
        "PATH=$PATH"
        "HOME=${HOME:-$TEST_ROOT/home}"
    )
    [[ "${MISSING_ENV:-}" == "RELEASE_KEYSTORE_BASE64" ]] || environment+=("RELEASE_KEYSTORE_BASE64=$KEYSTORE_BASE64")
    [[ "${MISSING_ENV:-}" == "GOOGLE_SERVICES_JSON_BASE64" ]] || environment+=("GOOGLE_SERVICES_JSON_BASE64=$FIREBASE_BASE64")
    [[ "${MISSING_ENV:-}" == "release_keyAlias" ]] || environment+=("release_keyAlias=release-test-alias")
    [[ "${MISSING_ENV:-}" == "release_keyPassword" ]] || environment+=("release_keyPassword=$SECRET_MARKER")
    [[ "${MISSING_ENV:-}" == "release_storePassword" ]] || environment+=("release_storePassword=$SECRET_MARKER")
    [[ "${MISSING_ENV:-}" == "MAPS_API_KEY_RELEASE" ]] || environment+=("MAPS_API_KEY_RELEASE=maps-$SECRET_MARKER")
    [[ "${MISSING_ENV:-}" == "FIREBASE_PROJECT_ID" ]] || environment+=("FIREBASE_PROJECT_ID=${OVERRIDE_FIREBASE_PROJECT_ID:-$FIREBASE_PROJECT_ID}")
    [[ "${MISSING_ENV:-}" == "FIREBASE_APP_ID" ]] || environment+=("FIREBASE_APP_ID=${OVERRIDE_FIREBASE_APP_ID:-$FIREBASE_APP_ID}")
    [[ "${MISSING_ENV:-}" == "ADMOB_APP_ID_RELEASE" ]] || environment+=("ADMOB_APP_ID_RELEASE=${OVERRIDE_ADMOB_APP_ID_RELEASE:-$ADMOB_APP_ID_RELEASE}")
    [[ "${MISSING_ENV:-}" == "ADMOB_BANNER_ID_RELEASE" ]] || environment+=("ADMOB_BANNER_ID_RELEASE=${OVERRIDE_ADMOB_BANNER_ID_RELEASE:-$ADMOB_BANNER_ID_RELEASE}")
    [[ "${MISSING_ENV:-}" == "PRIVACY_POLICY_URL_RELEASE" ]] || environment+=("PRIVACY_POLICY_URL_RELEASE=${OVERRIDE_PRIVACY_POLICY_URL_RELEASE:-$PRIVACY_POLICY_URL_RELEASE}")

    "${environment[@]}" \
        "$SCRIPT" prepare \
        --output-dir "$output_dir" \
        --firebase-destination "$firebase_destination"
}

for missing_name in "${REQUIRED_ENV[@]}"; do
    output_dir="$TEST_ROOT/missing-$missing_name/secrets"
    firebase_destination="$TEST_ROOT/missing-$missing_name/app/google-services.json"
    log_file="$TEST_ROOT/missing-$missing_name.log"
    mkdir -p "$(dirname "$firebase_destination")"

    if MISSING_ENV="$missing_name" run_prepare "$output_dir" "$firebase_destination" >"$log_file" 2>&1; then
        fail "missing $missing_name was accepted"
    fi
    grep -Fq "$missing_name" "$log_file" || fail "missing $missing_name was not identified"
    assert_no_secret_output "$log_file"
    [[ ! -e "$output_dir" ]] || fail "partial secret directory remained after missing $missing_name"
    [[ ! -e "$firebase_destination" ]] || fail "Firebase config remained after missing $missing_name"
done

for invalid_config in admob-app admob-banner privacy-url test-admob-app test-admob-banner; do
    output_dir="$TEST_ROOT/invalid-$invalid_config/secrets"
    firebase_destination="$TEST_ROOT/invalid-$invalid_config/app/google-services.json"
    log_file="$TEST_ROOT/invalid-$invalid_config.log"
    mkdir -p "$(dirname "$firebase_destination")"

    case "$invalid_config" in
        admob-app)
            override=("OVERRIDE_ADMOB_APP_ID_RELEASE=invalid")
            ;;
        admob-banner)
            override=("OVERRIDE_ADMOB_BANNER_ID_RELEASE=invalid")
            ;;
        privacy-url)
            override=("OVERRIDE_PRIVACY_POLICY_URL_RELEASE=http://example.com/privacy")
            ;;
        test-admob-app)
            override=("OVERRIDE_ADMOB_APP_ID_RELEASE=ca-app-pub-3940256099942544~3347511713")
            ;;
        test-admob-banner)
            override=("OVERRIDE_ADMOB_BANNER_ID_RELEASE=ca-app-pub-3940256099942544/6300978111")
            ;;
    esac

    if (
        export "${override[@]}"
        run_prepare "$output_dir" "$firebase_destination"
    ) >"$log_file" 2>&1; then
        fail "invalid $invalid_config release configuration was accepted"
    fi
    [[ ! -e "$output_dir" ]] || fail "partial output remained after invalid $invalid_config"
    [[ ! -e "$firebase_destination" ]] || fail "Firebase config remained after invalid $invalid_config"
done

for identity_name in project app; do
    output_dir="$TEST_ROOT/wrong-$identity_name/secrets"
    firebase_destination="$TEST_ROOT/wrong-$identity_name/app/google-services.json"
    log_file="$TEST_ROOT/wrong-$identity_name.log"
    mkdir -p "$(dirname "$firebase_destination")"

    if [[ "$identity_name" == "project" ]]; then
        if OVERRIDE_FIREBASE_PROJECT_ID="same-package-wrong-project" \
            run_prepare "$output_dir" "$firebase_destination" >"$log_file" 2>&1; then
            fail "same-package Firebase configuration from a wrong project was accepted"
        fi
    elif OVERRIDE_FIREBASE_APP_ID="1:123456789:android:wrongapp" \
        run_prepare "$output_dir" "$firebase_destination" >"$log_file" 2>&1; then
        fail "same-package Firebase configuration with a wrong app id was accepted"
    fi

    grep -Fqi "$identity_name" "$log_file" || fail "wrong Firebase $identity_name was not identified"
    assert_no_secret_output "$log_file"
    [[ ! -e "$output_dir" ]] || fail "partial secret directory remained after Firebase $identity_name drift"
    [[ ! -e "$firebase_destination" ]] || fail "Firebase config remained after Firebase $identity_name drift"
done

readonly VALID_OUTPUT="$TEST_ROOT/valid/secrets"
readonly VALID_FIREBASE="$TEST_ROOT/valid/app/google-services.json"
mkdir -p "$(dirname "$VALID_FIREBASE")"
run_prepare "$VALID_OUTPUT" "$VALID_FIREBASE" >"$TEST_ROOT/valid.log" 2>&1

[[ "$(mode_of "$VALID_OUTPUT")" == "700" ]] || fail "secret directory is not mode 700"
for file in release.keystore release.properties .release-secrets app-google-services.sha256; do
    [[ -f "$VALID_OUTPUT/$file" ]] || fail "missing generated $file"
    [[ "$(mode_of "$VALID_OUTPUT/$file")" == "600" ]] || fail "$file is not mode 600"
done
[[ "$(mode_of "$VALID_FIREBASE")" == "600" ]] || fail "Firebase config is not mode 600"
grep -Fq 'release_keyAlias=release-test-alias' "$VALID_OUTPUT/release.properties" || fail "release alias property is missing"
grep -Fq 'release_storeFile=' "$VALID_OUTPUT/release.properties" || fail "release store path property is missing"
grep -Fq 'MAPS_API_KEY_RELEASE=maps-' "$VALID_OUTPUT/release.properties" || fail "release Maps property is missing"
grep -Fq "ADMOB_APP_ID_RELEASE=$ADMOB_APP_ID_RELEASE" "$VALID_OUTPUT/release.properties" || fail "release AdMob app id is missing"
grep -Fq "ADMOB_BANNER_ID_RELEASE=$ADMOB_BANNER_ID_RELEASE" "$VALID_OUTPUT/release.properties" || fail "release AdMob banner id is missing"
grep -Fq 'PRIVACY_POLICY_URL_RELEASE=https\://example.invalid/earthquake-watchdog/privacy' "$VALID_OUTPUT/release.properties" || fail "release privacy-policy URL is missing"
assert_no_secret_output "$TEST_ROOT/valid.log"

"$SCRIPT" cleanup \
    --output-dir "$VALID_OUTPUT" \
    --firebase-destination "$VALID_FIREBASE"
[[ ! -e "$VALID_OUTPUT" ]] || fail "explicit cleanup left the secret directory"
[[ ! -e "$VALID_FIREBASE" ]] || fail "explicit cleanup left Firebase config"

readonly MISSING_HASH_OUTPUT="$TEST_ROOT/missing-hash/secrets"
readonly MISSING_HASH_FIREBASE="$TEST_ROOT/missing-hash/app/google-services.json"
mkdir -p "$(dirname "$MISSING_HASH_FIREBASE")"
run_prepare "$MISSING_HASH_OUTPUT" "$MISSING_HASH_FIREBASE" >/dev/null
rm "$MISSING_HASH_OUTPUT/app-google-services.sha256"
if "$SCRIPT" cleanup \
    --output-dir "$MISSING_HASH_OUTPUT" \
    --firebase-destination "$MISSING_HASH_FIREBASE" >"$TEST_ROOT/missing-hash.log" 2>&1; then
    fail "cleanup reported success with a missing Firebase hash sidecar"
fi
[[ -f "$MISSING_HASH_FIREBASE" ]] || fail "failed cleanup removed an unverified Firebase file"
[[ -d "$MISSING_HASH_OUTPUT" ]] || fail "failed cleanup removed the evidence directory"

readonly MODIFIED_OUTPUT="$TEST_ROOT/modified/secrets"
readonly MODIFIED_FIREBASE="$TEST_ROOT/modified/app/google-services.json"
mkdir -p "$(dirname "$MODIFIED_FIREBASE")"
run_prepare "$MODIFIED_OUTPUT" "$MODIFIED_FIREBASE" >/dev/null
printf '\n' >> "$MODIFIED_FIREBASE"
if "$SCRIPT" cleanup \
    --output-dir "$MODIFIED_OUTPUT" \
    --firebase-destination "$MODIFIED_FIREBASE" >"$TEST_ROOT/modified.log" 2>&1; then
    fail "cleanup reported success after the Firebase file changed"
fi
[[ -f "$MODIFIED_FIREBASE" ]] || fail "failed cleanup removed a modified Firebase file"
[[ -d "$MODIFIED_OUTPUT" ]] || fail "failed cleanup removed evidence for a modified Firebase file"

readonly TRAP_OUTPUT="$TEST_ROOT/trap/secrets"
readonly TRAP_FIREBASE="$TEST_ROOT/trap/app/google-services.json"
mkdir -p "$(dirname "$TRAP_FIREBASE")"
(
    trap '"$SCRIPT" cleanup --output-dir "$TRAP_OUTPUT" --firebase-destination "$TRAP_FIREBASE"' EXIT HUP INT TERM
    run_prepare "$TRAP_OUTPUT" "$TRAP_FIREBASE" >/dev/null
    false
) >/dev/null 2>&1 || true
[[ ! -e "$TRAP_OUTPUT" ]] || fail "trap cleanup left the secret directory"
[[ ! -e "$TRAP_FIREBASE" ]] || fail "trap cleanup left Firebase config"

printf 'PASS: release secrets fail closed, remain private, and clean up safely\n'
