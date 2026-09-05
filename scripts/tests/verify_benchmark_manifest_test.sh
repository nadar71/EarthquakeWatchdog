#!/usr/bin/env bash
set -euo pipefail

readonly ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readonly CHECKER="$ROOT/scripts/verify_benchmark_manifest.sh"
readonly TEMP_DIRECTORY="$(mktemp -d "${TMPDIR:-/tmp}/benchmark-manifest-test.XXXXXX")"
trap 'find "$TEMP_DIRECTORY" -type f -delete; rmdir "$TEMP_DIRECTORY"' EXIT

cat > "$TEMP_DIRECTORY/clean.xml" <<'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application>
  <provider android:name="com.google.android.gms.ads.MobileAdsInitProvider" android:enabled="false" />
  <provider android:name="com.google.firebase.provider.FirebaseInitProvider" android:enabled="false" />
  <meta-data android:name="firebase_crashlytics_collection_enabled" android:value="false" />
  <profileable android:shell="true" />
</application></manifest>
EOF

cat > "$TEMP_DIRECTORY/dirty.xml" <<'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application>
  <provider android:name="com.google.android.gms.ads.MobileAdsInitProvider" />
  <provider android:name="com.google.firebase.provider.FirebaseInitProvider" />
  <meta-data android:name="firebase_crashlytics_collection_enabled" android:value="true" />
</application></manifest>
EOF

cat > "$TEMP_DIRECTORY/dirty-collection.xml" <<'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application>
  <meta-data
      android:name="firebase_crashlytics_collection_enabled"
      android:value="true" />
  <profileable android:shell="true" />
</application></manifest>
EOF

"$CHECKER" "$TEMP_DIRECTORY/clean.xml"

if "$CHECKER" "$TEMP_DIRECTORY/dirty.xml"; then
    printf 'FAIL: contaminating providers were accepted\n' >&2
    exit 1
fi

if "$CHECKER" "$TEMP_DIRECTORY/dirty-collection.xml"; then
    printf 'FAIL: enabled Crashlytics collection was accepted\n' >&2
    exit 1
fi

printf 'Benchmark manifest verifier tests passed.\n'
