#!/usr/bin/env bash
set -euo pipefail

if [[ "$#" -ne 2 ]]; then
    printf 'Usage: %s <release-apk> <release-aab>\n' "$0" >&2
    exit 2
fi

readonly APK="$1"
readonly AAB="$2"
readonly TEMP_DIRECTORY="$(mktemp -d "${TMPDIR:-/tmp}/verify_benchmark_isolation.XXXXXX")"
trap 'rm -rf "$TEMP_DIRECTORY"' EXIT

for artifact in "$APK" "$AAB"; do
    if [[ ! -s "$artifact" ]]; then
        printf 'FAIL: missing release artifact: %s\n' "$artifact" >&2
        exit 1
    fi
done

unzip -p "$APK" 'classes*.dex' > "$TEMP_DIRECTORY/release-apk.dex"
unzip -p "$AAB" 'base/dex/classes*.dex' > "$TEMP_DIRECTORY/release-aab.dex"

for dex_file in "$TEMP_DIRECTORY/release-apk.dex" "$TEMP_DIRECTORY/release-aab.dex"; do
    strings "$dex_file" > "$dex_file.strings"

    if grep -E -q 'BenchmarkHarness|benchmark-(home|filter|map|statistics|settings)' "$dex_file.strings"; then
        printf 'FAIL: benchmark-only implementation leaked into %s\n' "$(basename "$dex_file")" >&2
        exit 1
    fi
done

printf 'Benchmark isolation verification passed.\n'
