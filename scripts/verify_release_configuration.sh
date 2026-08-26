#!/usr/bin/env bash
set -euo pipefail

readonly PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
readonly BUILD_FILE="$PROJECT_ROOT/app/build.gradle.kts"
readonly PROGUARD_FILE="$PROJECT_ROOT/app/proguard-rules.pro"
readonly MANIFEST_FILE="$PROJECT_ROOT/app/src/main/AndroidManifest.xml"
readonly BACKUP_RULES_FILE="$PROJECT_ROOT/app/src/main/res/xml/backup_rules.xml"
readonly EXTRACTION_RULES_FILE="$PROJECT_ROOT/app/src/main/res/xml/data_extraction_rules.xml"
readonly NETWORK_CONFIG_FILE="$PROJECT_ROOT/app/src/main/res/xml/network_security_config.xml"

failures=0

require_file() {
    local file="$1"
    local description="$2"

    if [[ ! -f "$file" ]]; then
        printf 'FAIL: missing %s: %s\n' "$description" "${file#$PROJECT_ROOT/}" >&2
        failures=$((failures + 1))
    fi
}

require_text() {
    local file="$1"
    local text="$2"
    local description="$3"

    if ! grep -Fq -- "$text" "$file"; then
        printf 'FAIL: missing %s\n' "$description" >&2
        failures=$((failures + 1))
    fi
}

require_absent_text() {
    local file="$1"
    local text="$2"
    local description="$3"

    if grep -Fq -- "$text" "$file"; then
        printf 'FAIL: unexpected %s\n' "$description" >&2
        failures=$((failures + 1))
    fi
}

require_file "$BUILD_FILE" "app Gradle configuration"
require_file "$PROGUARD_FILE" "ProGuard configuration"
require_file "$MANIFEST_FILE" "Android manifest"

require_text "$BUILD_FILE" "isMinifyEnabled = true" "release minification"
require_text "$BUILD_FILE" "isShrinkResources = true" "release resource shrinking"
require_absent_text "$BUILD_FILE" "implementation(libs.androidx.ui.tooling)" "release Compose preview tooling"
require_text "$BUILD_FILE" "tasks.register(\"validateReleaseSecrets\")" "release secret validation task"
require_text "$BUILD_FILE" "dependsOn(\"validateReleaseSecrets\")" "pre-release secret validation dependency"
require_text "$BUILD_FILE" "validateRequestedReleaseSecrets()" "configuration-time release secret validation"

for secret_name in release_keyAlias release_keyPassword release_storeFile release_storePassword MAPS_API_KEY_RELEASE; do
    require_text "$BUILD_FILE" "\"$secret_name\"" "required release secret name $secret_name"
done

require_text "$BUILD_FILE" "MAPS_API_KEY_DEBUG" "separate debug Maps key"
require_text "$BUILD_FILE" "manifestPlaceholders[\"MAPS_API_KEY\"] = releaseSecret" "release Maps key placeholder"

for gson_class in \
    com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeaturesCollectionDTO \
    com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQFeatureDTO \
    com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQGeometryDTO \
    com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQMetadataDTO \
    com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.dto.EQPropertiesDTO \
    com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsWindowCache \
    com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsWindowsCache \
    com.indiewalk.watchdog.earthquake.feat_statistics.data.local.TrendPointCache \
    com.indiewalk.watchdog.earthquake.feat_statistics.data.local.StatisticsInsightsCache \
    com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.StatisticsEvent \
    com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.DistributionBucket \
    com.indiewalk.watchdog.earthquake.feat_statistics.domain.model.ActiveRegion; do
    require_text "$PROGUARD_FILE" "-keep,allowoptimization class $gson_class" "Gson serialization rule for $gson_class"
done

require_file "$BACKUP_RULES_FILE" "backup rules"
require_file "$EXTRACTION_RULES_FILE" "data extraction rules"
require_file "$NETWORK_CONFIG_FILE" "network security configuration"

if [[ -f "$MANIFEST_FILE" ]]; then
    require_text "$MANIFEST_FILE" "android:fullBackupContent=\"@xml/backup_rules\"" "backup rules manifest reference"
    require_text "$MANIFEST_FILE" "android:dataExtractionRules=\"@xml/data_extraction_rules\"" "data extraction manifest reference"
    require_text "$MANIFEST_FILE" "android:networkSecurityConfig=\"@xml/network_security_config\"" "network security manifest reference"
    require_text "$MANIFEST_FILE" "android:usesCleartextTraffic=\"false\"" "cleartext traffic restriction"
    require_absent_text "$MANIFEST_FILE" "android.permission.WRITE_EXTERNAL_STORAGE" "app-owned legacy storage permission"
    require_absent_text "$MANIFEST_FILE" "android.permission.WAKE_LOCK" "app-owned wake-lock permission"
    require_absent_text "$MANIFEST_FILE" "org.apache.http.legacy" "app-owned legacy HTTP library"
    require_absent_text "$MANIFEST_FILE" "com.google.android.gms.version" "manual Google Play Services metadata"
    require_text "$MANIFEST_FILE" "android:name=\".feat_eqslist.presentation.ui.MainActivity\"" "launcher activity"

    if [[ "$(grep -Fc 'android:exported="true"' "$MANIFEST_FILE")" -ne 1 ]]; then
        printf 'FAIL: app manifest must export only the launcher activity\n' >&2
        failures=$((failures + 1))
    fi
fi

if [[ -f "$BACKUP_RULES_FILE" ]]; then
    require_text "$BACKUP_RULES_FILE" "domain=\"database\"" "database backup exclusion"
    require_text "$BACKUP_RULES_FILE" "domain=\"file\"" "DataStore backup exclusion"
    require_text "$BACKUP_RULES_FILE" "domain=\"sharedpref\"" "identifier backup exclusion"
fi

if [[ -f "$EXTRACTION_RULES_FILE" ]]; then
    require_text "$EXTRACTION_RULES_FILE" "<cloud-backup" "cloud backup rules"
    require_text "$EXTRACTION_RULES_FILE" "<device-transfer>" "device transfer rules"
    require_text "$EXTRACTION_RULES_FILE" "domain=\"database\"" "database extraction exclusion"
    require_text "$EXTRACTION_RULES_FILE" "domain=\"file\"" "DataStore extraction exclusion"
fi

if [[ -f "$NETWORK_CONFIG_FILE" ]]; then
    require_text "$NETWORK_CONFIG_FILE" "cleartextTrafficPermitted=\"false\"" "HTTPS-only network policy"
fi

if [[ "$failures" -gt 0 ]]; then
    printf 'Release configuration verification failed with %s issue(s).\n' "$failures" >&2
    exit 1
fi

readonly TEMP_DIRECTORY="$(mktemp -d "${TMPDIR:-/tmp}/verify_release_configuration.XXXXXX")"
readonly EMPTY_SECRETS_FILE="$TEMP_DIRECTORY/empty-release-secrets.properties"
readonly TEST_KEYSTORE_FILE="$TEMP_DIRECTORY/release-test.keystore"
readonly TEST_KEY_ALIAS="release-test-alias"
readonly TEST_KEY_PASSWORD="release-test-password"
readonly TEST_MAPS_KEY="release-test-maps-key"

cleanup() {
    rm -rf "$TEMP_DIRECTORY"
}

trap cleanup EXIT

: > "$EMPTY_SECRETS_FILE"

if "$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" \
    -PreleaseSecretsFile="$EMPTY_SECRETS_FILE" \
    :app:bundleRelease --dry-run --offline > "$TEMP_DIRECTORY/missing-secrets.log" 2>&1; then
    printf 'FAIL: release packaging configured without required secrets\n' >&2
    exit 1
fi

if ! grep -Fq 'Missing required release secrets: release_keyAlias, release_keyPassword, release_storeFile, release_storePassword, MAPS_API_KEY_RELEASE' "$TEMP_DIRECTORY/missing-secrets.log"; then
    printf 'FAIL: release configuration did not name every missing secret\n' >&2
    exit 1
fi

if ! command -v keytool >/dev/null 2>&1; then
    printf 'FAIL: keytool is required for release configuration verification\n' >&2
    exit 1
fi

if ! keytool -genkeypair \
    -alias "$TEST_KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -keystore "$TEST_KEYSTORE_FILE" \
    -storetype PKCS12 \
    -storepass "$TEST_KEY_PASSWORD" \
    -keypass "$TEST_KEY_PASSWORD" \
    -validity 1 \
    -dname 'CN=Release Configuration Test' > /dev/null 2>&1; then
    printf 'FAIL: temporary release keystore generation failed\n' >&2
    exit 1
fi

if ! "$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" \
    -PreleaseSecretsFile="$EMPTY_SECRETS_FILE" \
    -Prelease_keyAlias="$TEST_KEY_ALIAS" \
    -Prelease_keyPassword="$TEST_KEY_PASSWORD" \
    -Prelease_storeFile="$TEST_KEYSTORE_FILE" \
    -Prelease_storePassword="$TEST_KEY_PASSWORD" \
    -PMAPS_API_KEY_RELEASE="$TEST_MAPS_KEY" \
    :app:bundleRelease --rerun-tasks --offline > "$TEMP_DIRECTORY/release-build.log" 2>&1; then
    printf 'FAIL: synthetic minified release bundle verification failed\n' >&2
    exit 1
fi

if [[ ! -s "$PROJECT_ROOT/app/build/outputs/mapping/release/mapping.txt" ]]; then
    printf 'FAIL: R8 mapping output was not generated\n' >&2
    exit 1
fi

if [[ ! -s "$PROJECT_ROOT/app/build/outputs/bundle/release/app-release.aab" ]]; then
    printf 'FAIL: release AAB output was not generated\n' >&2
    exit 1
fi

if ! "$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" \
    -PreleaseSecretsFile="$EMPTY_SECRETS_FILE" \
    -Prelease_keyAlias="$TEST_KEY_ALIAS" \
    -Prelease_keyPassword="$TEST_KEY_PASSWORD" \
    -Prelease_storeFile="$TEST_KEYSTORE_FILE" \
    -Prelease_storePassword="$TEST_KEY_PASSWORD" \
    -PMAPS_API_KEY_RELEASE="$TEST_MAPS_KEY" \
    :app:validateReleaseSecrets --quiet; then
    printf 'FAIL: release secret validation rejected non-empty test inputs\n' >&2
    exit 1
fi

printf 'Release configuration verification passed.\n'
