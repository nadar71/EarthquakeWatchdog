#!/usr/bin/env bash
set -euo pipefail

fail() {
    printf 'ERROR: %s\n' "$1" >&2
    exit 1
}

usage() {
    cat >&2 <<'EOF'
Usage: verify_aab.sh \
  --aab FILE --mapping FILE --bundletool FILE --bundletool-version VERSION \
  --bundletool-sha256 HEX --expected-package ID --expected-version-code NUMBER \
  --expected-version-name NAME --expected-cert-sha256 HEX --output-dir DIRECTORY
EOF
    exit 2
}

sha256_file() {
    python3 - "$1" <<'PY'
import hashlib
import sys

digest = hashlib.sha256()
with open(sys.argv[1], "rb") as source:
    for block in iter(lambda: source.read(1024 * 1024), b""):
        digest.update(block)
print(digest.hexdigest())
PY
}

aab=""
mapping=""
bundletool=""
bundletool_version=""
bundletool_sha256=""
expected_package=""
expected_version_code=""
expected_version_name=""
expected_cert_sha256=""
output_dir=""
native_symbols=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --aab) aab="${2:-}"; shift 2 ;;
        --mapping) mapping="${2:-}"; shift 2 ;;
        --bundletool) bundletool="${2:-}"; shift 2 ;;
        --bundletool-version) bundletool_version="${2:-}"; shift 2 ;;
        --bundletool-sha256) bundletool_sha256="${2:-}"; shift 2 ;;
        --expected-package) expected_package="${2:-}"; shift 2 ;;
        --expected-version-code) expected_version_code="${2:-}"; shift 2 ;;
        --expected-version-name) expected_version_name="${2:-}"; shift 2 ;;
        --expected-cert-sha256) expected_cert_sha256="${2:-}"; shift 2 ;;
        --output-dir) output_dir="${2:-}"; shift 2 ;;
        --native-symbols) native_symbols="${2:-}"; shift 2 ;;
        *) usage ;;
    esac
done

for value in "$aab" "$mapping" "$bundletool" "$bundletool_version" "$bundletool_sha256" \
    "$expected_package" "$expected_version_code" "$expected_version_name" \
    "$expected_cert_sha256" "$output_dir"; do
    [[ -n "$value" ]] || usage
done

[[ -f "$aab" && ! -L "$aab" && -s "$aab" ]] || fail "signed AAB is missing or empty"
[[ -f "$mapping" && ! -L "$mapping" && -s "$mapping" ]] || fail "R8 mapping is missing or empty"
[[ -f "$bundletool" && ! -L "$bundletool" && -s "$bundletool" ]] || fail "bundletool is missing or empty"
if [[ -n "$native_symbols" ]]; then
    [[ -f "$native_symbols" && ! -L "$native_symbols" && -s "$native_symbols" ]] \
        || fail "native debug symbols were requested but are missing or empty"
fi
[[ "$expected_package" =~ ^[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z][A-Za-z0-9_]*)+$ ]] || fail "expected package is invalid"
[[ "$expected_version_code" =~ ^[1-9][0-9]*$ ]] || fail "expected version code is invalid"
[[ "$expected_version_name" =~ ^[0-9A-Za-z][0-9A-Za-z._+-]{0,63}$ ]] || fail "expected version name is invalid"
[[ "$bundletool_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || fail "bundletool version is invalid"

bundletool_sha256="$(printf '%s' "$bundletool_sha256" | tr '[:upper:]' '[:lower:]')"
expected_cert_sha256="$(printf '%s' "$expected_cert_sha256" | tr -d ':[:space:]' | tr '[:upper:]' '[:lower:]')"
[[ "$bundletool_sha256" =~ ^[0-9a-f]{64}$ ]] || fail "bundletool checksum is invalid"
[[ "$expected_cert_sha256" =~ ^[0-9a-f]{64}$ ]] || fail "expected certificate fingerprint is invalid"

[[ ! -e "$output_dir" ]] || fail "verification output directory already exists"
umask 077
readonly temp_dir="$(mktemp -d "${TMPDIR:-/tmp}/verify_aab.XXXXXX")"
output_created=false
verification_complete=false
cleanup() {
    rm -rf -- "$temp_dir"
    if [[ "$output_created" == "true" && "$verification_complete" != "true" ]]; then
        rm -rf -- "$output_dir"
    fi
}
trap cleanup EXIT HUP INT TERM

actual_bundletool_sha256="$(sha256_file "$bundletool")"
[[ "$actual_bundletool_sha256" == "$bundletool_sha256" ]] || fail "bundletool checksum does not match the pinned value"
actual_bundletool_version="$(java -jar "$bundletool" version | tr -d '\r\n')"
[[ "$actual_bundletool_version" == "$bundletool_version" ]] || fail "bundletool version does not match the pinned value"

if ! jarsigner -verify -verbose -certs "$aab" >"$temp_dir/jarsigner.log" 2>&1; then
    fail "AAB JAR signature verification failed"
fi
grep -Fq 'jar verified.' "$temp_dir/jarsigner.log" || fail "AAB JAR signature was not verified"
if grep -Eiq 'jar is unsigned|unsigned entries' "$temp_dir/jarsigner.log"; then
    fail "AAB contains unsigned content"
fi

if ! keytool -printcert -rfc -jarfile "$aab" >"$temp_dir/certificate.pem" 2>"$temp_dir/keytool.log"; then
    fail "AAB signing certificate could not be read"
fi
actual_cert_sha256="$(openssl x509 -in "$temp_dir/certificate.pem" -noout -fingerprint -sha256 \
    | sed 's/^[^=]*=//' | tr -d ':[:space:]' | tr '[:upper:]' '[:lower:]')"
[[ "$actual_cert_sha256" == "$expected_cert_sha256" ]] || fail "AAB certificate SHA-256 fingerprint does not match"

java -jar "$bundletool" validate --bundle="$aab" >"$temp_dir/bundletool-validate.log"
java -jar "$bundletool" dump manifest --bundle="$aab" --module=base >"$temp_dir/manifest.xml"

python3 - "$temp_dir/manifest.xml" "$expected_package" "$expected_version_code" "$expected_version_name" <<'PY'
import sys
import xml.etree.ElementTree as ET

manifest_path, expected_package, expected_code, expected_name = sys.argv[1:]
root = ET.parse(manifest_path).getroot()
android = "{http://schemas.android.com/apk/res/android}"
actual = {
    "package": root.attrib.get("package"),
    "versionCode": root.attrib.get(android + "versionCode"),
    "versionName": root.attrib.get(android + "versionName"),
}
expected = {
    "package": expected_package,
    "versionCode": expected_code,
    "versionName": expected_name,
}
for name, expected_value in expected.items():
    if actual[name] != expected_value:
        raise SystemExit(
            f"ERROR: manifest {name} mismatch: expected {expected_value!r}, got {actual[name]!r}"
        )
PY

unzip -Z1 "$aab" >"$temp_dir/entries.txt"
grep -Fxq 'BUNDLE-METADATA/com.android.tools.build.profiles/baseline.prof' "$temp_dir/entries.txt" \
    || fail "AAB does not contain baseline.prof"
grep -Fxq 'BUNDLE-METADATA/com.android.tools.build.profiles/baseline.profm' "$temp_dir/entries.txt" \
    || fail "AAB does not contain baseline.profm"
grep -Fxq 'BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map' "$temp_dir/entries.txt" \
    || fail "AAB does not contain the R8 proguard.map"

unzip -p "$aab" BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map \
    >"$temp_dir/embedded-mapping.txt"
cmp -s "$mapping" "$temp_dir/embedded-mapping.txt" || fail "external R8 mapping does not match the AAB metadata"

mkdir -p "$(dirname "$output_dir")"
mkdir "$output_dir"
output_created=true
readonly staged_aab="$output_dir/earthquake-watchdog-release.aab"
readonly staged_mapping="$output_dir/mapping.txt"
cp "$aab" "$staged_aab"
cp "$mapping" "$staged_mapping"
chmod 600 "$staged_aab" "$staged_mapping"
if [[ -n "$native_symbols" ]]; then
    cp "$native_symbols" "$output_dir/native-debug-symbols.zip"
    chmod 600 "$output_dir/native-debug-symbols.zip"
fi

export PROVENANCE_AAB_SHA256="$(sha256_file "$staged_aab")"
export PROVENANCE_MAPPING_SHA256="$(sha256_file "$staged_mapping")"
export PROVENANCE_NATIVE_SYMBOLS_SHA256=""
if [[ -n "$native_symbols" ]]; then
    PROVENANCE_NATIVE_SYMBOLS_SHA256="$(sha256_file "$output_dir/native-debug-symbols.zip")"
fi
export PROVENANCE_PACKAGE="$expected_package"
export PROVENANCE_VERSION_CODE="$expected_version_code"
export PROVENANCE_VERSION_NAME="$expected_version_name"
export PROVENANCE_CERT_SHA256="$actual_cert_sha256"
export PROVENANCE_BUNDLETOOL_VERSION="$bundletool_version"
export PROVENANCE_BUNDLETOOL_SHA256="$actual_bundletool_sha256"
export PROVENANCE_COMMIT_SHA="${GITHUB_SHA:-$(git rev-parse HEAD)}"
export PROVENANCE_SOURCE_REF="${GITHUB_REF:-local-dry-run}"
export PROVENANCE_MAPPING_UPLOAD="${CRASHLYTICS_MAPPING_UPLOAD_ENABLED:-false}"

[[ "$PROVENANCE_COMMIT_SHA" =~ ^[0-9a-fA-F]{40}$ ]] || fail "provenance commit SHA is invalid"
[[ "$PROVENANCE_MAPPING_UPLOAD" == "true" || "$PROVENANCE_MAPPING_UPLOAD" == "false" ]] \
    || fail "provenance mapping-upload value is invalid"

python3 - "$output_dir/release-provenance.json" <<'PY'
import json
import os
import sys

document = {
    "schemaVersion": 1,
    "source": {
        "commitSha": os.environ["PROVENANCE_COMMIT_SHA"].lower(),
        "ref": os.environ["PROVENANCE_SOURCE_REF"],
    },
    "application": {
        "packageName": os.environ["PROVENANCE_PACKAGE"],
        "versionCode": int(os.environ["PROVENANCE_VERSION_CODE"]),
        "versionName": os.environ["PROVENANCE_VERSION_NAME"],
    },
    "signing": {"certificateSha256": os.environ["PROVENANCE_CERT_SHA256"]},
    "verification": {
        "bundletoolVersion": os.environ["PROVENANCE_BUNDLETOOL_VERSION"],
        "bundletoolSha256": os.environ["PROVENANCE_BUNDLETOOL_SHA256"],
        "crashlyticsMappingUploadEnabled": os.environ["PROVENANCE_MAPPING_UPLOAD"] == "true",
        "baselineProfileMetadataPresent": True,
        "embeddedMappingMatches": True,
    },
    "artifacts": {
        "earthquake-watchdog-release.aab": os.environ["PROVENANCE_AAB_SHA256"],
        "mapping.txt": os.environ["PROVENANCE_MAPPING_SHA256"],
    },
}
if os.environ["PROVENANCE_NATIVE_SYMBOLS_SHA256"]:
    document["artifacts"]["native-debug-symbols.zip"] = os.environ[
        "PROVENANCE_NATIVE_SYMBOLS_SHA256"
    ]
with open(sys.argv[1], "x", encoding="utf-8") as destination:
    json.dump(document, destination, indent=2, sort_keys=True)
    destination.write("\n")
PY
chmod 600 "$output_dir/release-provenance.json"

(
    cd "$output_dir"
    python3 - <<'PY' > SHA256SUMS
import hashlib
from pathlib import Path

for path in sorted(Path(".").iterdir()):
    if path.name == "SHA256SUMS" or not path.is_file():
        continue
    print(f"{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.name}")
PY
)
chmod 600 "$output_dir/SHA256SUMS"

verification_complete=true
printf 'AAB signature, identity, profiles, mapping, and provenance verified.\n'
