#!/usr/bin/env bash
set -euo pipefail

if [[ "$#" -ne 1 ]]; then
    printf 'Usage: %s <merged-non-minified-release-manifest>\n' "$0" >&2
    exit 2
fi

readonly MANIFEST="$1"

if [[ ! -s "$MANIFEST" ]]; then
    printf 'FAIL: missing merged benchmark manifest: %s\n' "$MANIFEST" >&2
    exit 1
fi

if awk 'BEGIN { RS = ">" } /(MobileAdsInitProvider|FirebaseInitProvider)/ && !/android:enabled="false"/ { found = 1 } END { exit !found }' "$MANIFEST"; then
    printf 'FAIL: benchmark manifest contains an enabled external SDK auto-initializer\n' >&2
    exit 1
fi

if awk 'BEGIN { RS = ">" } /firebase_crashlytics_collection_enabled/ && /android:value="true"/ { found = 1 } END { exit !found }' "$MANIFEST"; then
    printf 'FAIL: benchmark manifest enables Crashlytics collection\n' >&2
    exit 1
fi

if ! awk 'BEGIN { RS = ">" } /<profileable/ && /android:shell="true"/ { found = 1 } END { exit !found }' "$MANIFEST"; then
    printf 'FAIL: benchmark target is not shell-profileable\n' >&2
    exit 1
fi

printf 'Benchmark manifest isolation verification passed.\n'
