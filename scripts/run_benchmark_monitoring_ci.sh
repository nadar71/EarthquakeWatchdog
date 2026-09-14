#!/usr/bin/env bash
set -euo pipefail

mkdir -p benchmark-results
./gradlew :benchmark:connectedNonMinifiedReleaseAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.benchmark.BenchmarkSelectorContractTest
./gradlew :app:generateBaselineProfile
bash scripts/verify_baseline_profile.sh \
    app/src/release/generated/baselineProfiles/baseline-prof.txt

./gradlew :benchmark:clean
./gradlew :benchmark:connectedNonMinifiedReleaseAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.benchmark.StartupBenchmark \
    -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark \
    -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
startup_result="$(find benchmark/build/outputs -type f -name '*benchmarkData.json' -print -quit)"
test -n "$startup_result"
cp "$startup_result" benchmark-results/startup-benchmarkData.json

./gradlew :benchmark:clean
./gradlew :benchmark:connectedNonMinifiedReleaseAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.benchmark.CoreJourneyBenchmark \
    -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark \
    -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
core_result="$(find benchmark/build/outputs -type f -name '*benchmarkData.json' -print -quit)"
test -n "$core_result"
cp "$core_result" benchmark-results/core-benchmarkData.json

scripts/check_performance_budgets.py \
    --budgets config/performance-budgets.json \
    benchmark-results/startup-benchmarkData.json \
    benchmark-results/core-benchmarkData.json
