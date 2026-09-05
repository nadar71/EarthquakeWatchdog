#!/usr/bin/env bash
set -euo pipefail

readonly ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readonly CHECKER="$ROOT/scripts/verify_baseline_profile.sh"
readonly TEMP_DIRECTORY="$(mktemp -d "${TMPDIR:-/tmp}/baseline-profile-test.XXXXXX")"
trap 'find "$TEMP_DIRECTORY" -type f -delete; rmdir "$TEMP_DIRECTORY"' EXIT

cat > "$TEMP_DIRECTORY/complete.txt" <<'EOF'
Lcom/indiewalk/watchdog/earthquake/core/presentation/navigation/AppNavigationHostKt;
Lcom/indiewalk/watchdog/earthquake/core/presentation/navigation/AppBottomBarKt;
Lcom/indiewalk/watchdog/earthquake/feat_eqslist/presentation/ui/EarthquakeListScreenKt;
Lcom/indiewalk/watchdog/earthquake/feat_statistics/presentation/ui/StatisticsScreenKt;
Lcom/indiewalk/watchdog/earthquake/feat_settings/presentation/ui/SettingsScreenKt;
EOF

cat > "$TEMP_DIRECTORY/incomplete.txt" <<'EOF'
Lcom/indiewalk/watchdog/earthquake/benchmark/BenchmarkAppNavigationScreenFactory;
EOF

"$CHECKER" "$TEMP_DIRECTORY/complete.txt"

if "$CHECKER" "$TEMP_DIRECTORY/incomplete.txt"; then
    printf 'FAIL: profile without production journeys was accepted\n' >&2
    exit 1
fi

printf 'Baseline profile verifier tests passed.\n'
