#!/usr/bin/env bash
set -euo pipefail

readonly PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
readonly SCRIPT="$PROJECT_ROOT/scripts/prepare_release_secrets.sh"
readonly TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/prepare_release_secrets_test.XXXXXX")"
readonly KEYSTORE="$TEST_ROOT/source.keystore"
readonly FIREBASE_SOURCE="$TEST_ROOT/google-services.json"
readonly SECRET_MARKER="task9-secret-must-not-leak"

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
    stat -f '%Lp' "$1" 2>/dev/null || stat -c '%a' "$1"
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

readonly KEYSTORE_BASE64="$(base64 < "$KEYSTORE" | tr -d '\r\n')"
readonly FIREBASE_BASE64="$(base64 < "$FIREBASE_SOURCE" | tr -d '\r\n')"
readonly REQUIRED_ENV=(
    RELEASE_KEYSTORE_BASE64
    GOOGLE_SERVICES_JSON_BASE64
    release_keyAlias
    release_keyPassword
    release_storePassword
    MAPS_API_KEY_RELEASE
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
assert_no_secret_output "$TEST_ROOT/valid.log"

"$SCRIPT" cleanup \
    --output-dir "$VALID_OUTPUT" \
    --firebase-destination "$VALID_FIREBASE"
[[ ! -e "$VALID_OUTPUT" ]] || fail "explicit cleanup left the secret directory"
[[ ! -e "$VALID_FIREBASE" ]] || fail "explicit cleanup left Firebase config"

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
