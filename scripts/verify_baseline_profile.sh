#!/usr/bin/env bash
set -euo pipefail

if [[ "$#" -ne 1 ]]; then
    printf 'Usage: %s <generated-baseline-profile>\n' "$0" >&2
    exit 2
fi

readonly PROFILE="$1"

if [[ ! -s "$PROFILE" ]]; then
    printf 'FAIL: missing generated baseline profile: %s\n' "$PROFILE" >&2
    exit 1
fi

readonly REQUIRED_CLASSES=(
    'core/presentation/navigation/AppNavigationHostKt'
    'core/presentation/navigation/AppBottomBarKt'
    'feat_eqslist/presentation/ui/EarthquakeListScreenKt'
    'feat_statistics/presentation/ui/StatisticsScreenKt'
    'feat_settings/presentation/ui/SettingsScreenKt'
)

for class_name in "${REQUIRED_CLASSES[@]}"; do
    if ! grep -Fq "$class_name" "$PROFILE"; then
        printf 'FAIL: baseline profile does not exercise production class %s\n' "$class_name" >&2
        exit 1
    fi
done

printf 'Baseline profile production-journey verification passed.\n'
