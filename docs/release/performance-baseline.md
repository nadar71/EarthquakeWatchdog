# Performance Baseline

## Scope

The `:benchmark` module targets `nonMinifiedRelease` with AndroidX Benchmark,
Baseline Profile, and ProfileInstaller `1.4.1`. The target launches the real
`MainActivity`, `AppNavigationHost`, Navigation 3 back stack, bottom navigation,
and production list, filter, statistics, settings, and selected-earthquake
Composables. A source-set-only screen factory supplies fixed in-memory state.
Only the Google Maps widget is replaced with a deterministic marker surface.

The benchmark target does not depend on USGS, Maps tiles, ads, UMP, Firebase,
geocoding, or location. Its merged manifest disables `MobileAdsInitProvider`
and `FirebaseInitProvider` and sets Crashlytics collection to false before
`Application.onCreate`. `EarthquakeApp` also skips manual SDK initialization.
Production debug/release variants keep their normal provider and collection
configuration.

Verify the actual merged benchmark manifest with:

```text
./gradlew :app:processNonMinifiedReleaseManifest
bash scripts/verify_benchmark_manifest.sh \
  app/build/intermediates/merged_manifests/nonMinifiedRelease/processNonMinifiedReleaseManifest/AndroidManifest.xml
```

## Baseline Profile

Generated on 2026-08-27 on `MathBrainer_7in`, API 35, serial
`emulator-5556`:

```text
ANDROID_SERIAL=emulator-5556 ./gradlew :app:generateBaselineProfile
bash scripts/verify_baseline_profile.sh \
  app/src/release/generated/baselineProfiles/baseline-prof.txt
```

The generated release inputs contain 22,362 baseline rules and 18,803 startup
rules. The verifier confirms rules for the production Navigation 3 host,
bottom bar, earthquake list, statistics, and settings. The release artifact
verifier additionally confirms that the minified APK/AAB contains profile
metadata but no benchmark-only factory, fixture data, or selectors.

## Reference Measurements

Reference environment: `MathBrainer_7in` AVD, API 35,
`nonMinifiedRelease`, AndroidX Benchmark `1.4.1`. Every measurement used ten
iterations. These emulator numbers are deterministic regression references,
not product SLOs and not evidence of physical-device performance.

| Journey | Metric | Baseline | 25% failure limit |
| --- | --- | ---: | ---: |
| Cold start | TTID median / max | 660.2 / 1135.2 ms | 825.3 / 1419.0 ms |
| List scroll | CPU frame P50 / P90 / P95 / P99 | 30.1 / 39.7 / 45.3 / 61.9 ms | 37.6 / 49.6 / 56.6 / 77.4 ms |
| List scroll | Frame overrun P50 / P90 / P95 / P99 | 15.3 / 33.3 / 45.6 / 69.9 ms | 19.1 / 41.6 / 56.9 / 87.3 ms |
| List scroll | Peak heap median / max | 12,005 / 18,676 KiB | 15,006 / 23,345 KiB |
| List scroll | Peak anonymous RSS median / max | 62,028 / 67,688 KiB | 77,535 / 84,610 KiB |
| Top-level navigation | CPU frame P50 / P90 / P95 / P99 | 29.1 / 44.9 / 47.0 / 59.3 ms | 36.4 / 56.1 / 58.7 / 74.1 ms |
| Top-level navigation | Frame overrun P50 / P90 / P95 / P99 | 15.3 / 35.6 / 45.6 / 56.3 ms | 19.1 / 44.5 / 56.9 / 70.4 ms |
| Top-level navigation | Peak heap median / max | 11,360 / 17,465 KiB | 14,199 / 21,831 KiB |
| Top-level navigation | Peak anonymous RSS median / max | 61,170 / 65,380 KiB | 76,463 / 81,725 KiB |

AndroidX `FrameTimingMetric` emitted frame-duration and frame-overrun
percentiles, but not a jank-rate percentage, for this run. The checked tails
therefore use P90, P95, and P99 frame/overrun values. Do not infer a physical
device jank rate from the emulator.

## Executable Gate

Run startup and core journeys separately so each generated JSON can be saved
before the next Gradle invocation replaces the output file:

```text
ANDROID_SERIAL=emulator-5556 ./gradlew \
  :benchmark:connectedNonMinifiedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.benchmark.StartupBenchmark \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR

ANDROID_SERIAL=emulator-5556 ./gradlew \
  :benchmark:connectedNonMinifiedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.benchmark.CoreJourneyBenchmark \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR

scripts/check_performance_budgets.py \
  --budgets config/performance-budgets.json \
  <saved-startup-benchmarkData.json> \
  <saved-core-benchmarkData.json>
```

The checker fails when a required benchmark/metric is absent, fewer than ten
iterations were recorded, or any checked median, maximum, or frame percentile
exceeds its limit. A second ten-iteration startup validation run measured a
610.0 ms median and 953.6 ms maximum; the complete startup/core result set
passed the executable gate.

## Physical Release Gate

Before production rollout, repeat startup, frame/jank, and memory measurements
on at least one representative low/mid-tier physical device and one current
device. Record device-specific baselines rather than comparing physical values
to this emulator. That external-device step remains required and intentionally
cannot be satisfied by this local implementation.
