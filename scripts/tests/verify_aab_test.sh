#!/usr/bin/env bash
set -euo pipefail

readonly PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
readonly VERIFIER="$PROJECT_ROOT/scripts/verify_aab.sh"
readonly AAB="${AAB_FIXTURE:-$PROJECT_ROOT/app/build/outputs/bundle/release/app-release.aab}"
readonly MAPPING="${MAPPING_FIXTURE:-$PROJECT_ROOT/app/build/outputs/mapping/release/mapping.txt}"
readonly BUNDLETOOL="${BUNDLETOOL_FIXTURE:?Set BUNDLETOOL_FIXTURE to the checksum-verified bundletool JAR}"
readonly BUNDLETOOL_VERSION="${BUNDLETOOL_VERSION:-1.18.3}"
readonly BUNDLETOOL_SHA256="${BUNDLETOOL_SHA256:-a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29}"
readonly TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/verify_aab_test.XXXXXX")"

cleanup() {
    chmod -R u+w "$TEST_ROOT" 2>/dev/null || true
    rm -rf "$TEST_ROOT"
}
trap cleanup EXIT

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    exit 1
}

[[ -x "$VERIFIER" ]] || fail "AAB verifier is missing or not executable"
[[ -s "$AAB" ]] || fail "AAB fixture is missing"
[[ -s "$MAPPING" ]] || fail "mapping fixture is missing"
[[ -s "$BUNDLETOOL" ]] || fail "bundletool fixture is missing"

readonly CERT_SHA256="$(
    keytool -printcert -rfc -jarfile "$AAB" \
        | openssl x509 -noout -fingerprint -sha256 \
        | sed 's/^[^=]*=//' \
        | tr -d ':[:space:]'
)"

verify() {
    local aab="$1"
    local mapping="$2"
    local checksum="$3"
    local version_code="$4"
    local version_name="$5"
    local fingerprint="$6"
    local output="$7"

    "$VERIFIER" \
        --aab "$aab" \
        --mapping "$mapping" \
        --bundletool "$BUNDLETOOL" \
        --bundletool-version "$BUNDLETOOL_VERSION" \
        --bundletool-sha256 "$checksum" \
        --expected-package com.indiewalk.watchdog.earthquake \
        --expected-version-code "$version_code" \
        --expected-version-name "$version_name" \
        --expected-cert-sha256 "$fingerprint" \
        --output-dir "$output"
}

expect_failure() {
    local description="$1"
    local output="$2"
    shift 2

    if "$@" >"$TEST_ROOT/failure.log" 2>&1; then
        fail "$description was accepted"
    fi
    [[ ! -e "$output" ]] || fail "$description left publishable output"
}

verify "$AAB" "$MAPPING" "$BUNDLETOOL_SHA256" 11 3.0.0 "$CERT_SHA256" "$TEST_ROOT/valid"
for expected in earthquake-watchdog-release.aab mapping.txt release-provenance.json SHA256SUMS; do
    [[ -s "$TEST_ROOT/valid/$expected" ]] || fail "verified output is missing $expected"
done
(
    cd "$TEST_ROOT/valid"
    shasum -a 256 -c SHA256SUMS >/dev/null
)

expect_failure "wrong certificate fingerprint" "$TEST_ROOT/bad-fingerprint" \
    verify "$AAB" "$MAPPING" "$BUNDLETOOL_SHA256" 11 3.0.0 \
    0000000000000000000000000000000000000000000000000000000000000000 \
    "$TEST_ROOT/bad-fingerprint"

expect_failure "wrong version code" "$TEST_ROOT/bad-version-code" \
    verify "$AAB" "$MAPPING" "$BUNDLETOOL_SHA256" 999 3.0.0 "$CERT_SHA256" \
    "$TEST_ROOT/bad-version-code"

expect_failure "wrong version name" "$TEST_ROOT/bad-version-name" \
    verify "$AAB" "$MAPPING" "$BUNDLETOOL_SHA256" 11 9.9.9 "$CERT_SHA256" \
    "$TEST_ROOT/bad-version-name"

expect_failure "wrong bundletool checksum" "$TEST_ROOT/bad-bundletool" \
    verify "$AAB" "$MAPPING" \
    0000000000000000000000000000000000000000000000000000000000000000 \
    11 3.0.0 "$CERT_SHA256" "$TEST_ROOT/bad-bundletool"

cp "$MAPPING" "$TEST_ROOT/tampered-mapping.txt"
printf '\n# tampered\n' >> "$TEST_ROOT/tampered-mapping.txt"
expect_failure "tampered mapping" "$TEST_ROOT/bad-mapping" \
    verify "$AAB" "$TEST_ROOT/tampered-mapping.txt" "$BUNDLETOOL_SHA256" \
    11 3.0.0 "$CERT_SHA256" "$TEST_ROOT/bad-mapping"

cp "$AAB" "$TEST_ROOT/tampered.aab"
python3 - "$TEST_ROOT/tampered.aab" <<'PY'
import struct
import sys
import zipfile

path = sys.argv[1]
with zipfile.ZipFile(path) as archive:
    entry = archive.getinfo("base/dex/classes.dex")
with open(path, "r+b") as bundle:
    bundle.seek(entry.header_offset)
    header = bundle.read(30)
    name_length, extra_length = struct.unpack_from("<HH", header, 26)
    data_offset = entry.header_offset + 30 + name_length + extra_length
    position = data_offset + max(1, entry.compress_size // 2)
    bundle.seek(position)
    original = bundle.read(1)
    bundle.seek(position)
    bundle.write(bytes([original[0] ^ 0x01]))
PY
expect_failure "tampered AAB" "$TEST_ROOT/bad-aab" \
    verify "$TEST_ROOT/tampered.aab" "$MAPPING" "$BUNDLETOOL_SHA256" \
    11 3.0.0 "$CERT_SHA256" "$TEST_ROOT/bad-aab"

printf 'PASS: AAB verifier accepts valid output and rejects tampering or identity drift\n'
