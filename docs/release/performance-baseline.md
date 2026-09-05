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

## Calibration Measurements

Reference environment: `MathBrainer_7in` AVD, API 35, serial `emulator-5556`,
`nonMinifiedRelease`, AndroidX Benchmark `1.4.1`. Three independent startup and
core-journey instrumentation pairs were run against unchanged commit
`dd20472`; the target app was force-stopped before each invocation and every
benchmark used ten iterations. These emulator values are local regression
references, not product SLOs or evidence of physical-device performance.

| Journey | Metric | Session 1 | Session 2 | Session 3 |
| --- | --- | ---: | ---: | ---: |
| Cold start | TTID median / max, ms | 581.2 / 792.0 | 548.8 / 780.4 | 669.4 / 857.1 |
| List scroll | CPU frame P50 / P90 / P95 / P99, ms | 28.5 / 42.3 / 49.2 / 69.7 | 28.4 / 38.7 / 46.3 / 60.4 | 28.4 / 40.7 / 45.5 / 57.1 |
| List scroll | Frame overrun P50 / P90 / P95 / P99, ms | 12.6 / 37.3 / 47.9 / 86.2 | 12.7 / 32.2 / 44.6 / 65.8 | 12.4 / 33.0 / 44.6 / 61.4 |
| List scroll | Peak heap median / max, KiB | 15,022 / 20,208 | 15,024 / 21,700 | 14,911 / 16,769 |
| List scroll | Peak anonymous RSS median / max, KiB | 65,730 / 70,728 | 66,036 / 73,296 | 65,198 / 66,460 |
| Top-level navigation | CPU frame P50 / P90 / P95 / P99, ms | 32.7 / 46.1 / 51.0 / 64.2 | 32.5 / 46.6 / 50.7 / 65.4 | 32.6 / 47.6 / 55.0 / 82.9 |
| Top-level navigation | Frame overrun P50 / P90 / P95 / P99, ms | 28.4 / 44.6 / 49.5 / 72.2 | 28.1 / 45.4 / 50.5 / 65.0 | 28.1 / 46.8 / 59.9 / 95.7 |
| Top-level navigation | Peak heap median / max, KiB | 11,478 / 22,038 | 11,498 / 21,830 | 11,512 / 21,910 |
| Top-level navigation | Peak anonymous RSS median / max, KiB | 61,812 / 68,144 | 61,892 / 67,940 | 61,756 / 68,044 |

An additional reviewer startup artifact measured a `554.1 ms` median but a
`1451.6 ms` maximum. It was inspected but excluded from the calibration table
because its matching core-journey JSON had already been overwritten. It
reinforces why a single-run maximum is unsuitable as a hard emulator gate.

AndroidX `FrameTimingMetric` emitted frame-duration and frame-overrun
percentiles, but not a jank-rate percentage. P95/P99, per-session maxima, frame
counts, and memory remain required observations because they are useful during
investigation, but their session variance makes them unsuitable hard gates.
Do not infer a physical-device jank rate from the emulator.

## Enforced Budgets

Hard limits use repeatable central statistics only. Each limit is the worst
value from the three complete sessions plus a 25% material-regression margin,
rounded upward. This catches sustained regressions without allowing an
unchanged build to fail because of one tail outlier.

| Journey | Enforced metric | Limit |
| --- | --- | ---: |
| Cold start | TTID median | 840 ms |
| List scroll | CPU frame P50 / P90 | 36 / 53 ms |
| List scroll | Frame overrun P50 / P90 | 16 / 47 ms |
| Top-level navigation | CPU frame P50 / P90 | 41 / 60 ms |
| Top-level navigation | Frame overrun P50 / P90 | 36 / 59 ms |

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
iterations were recorded, or an `enforcedBudgets` value exceeds its limit.
Metrics listed under `observations` are also required and printed with an
`OBSERVE:` prefix, but their values do not fail the gate. This keeps startup
maxima, frame tails, frame counts, and memory visible without treating noisy
emulator tails as reproducible thresholds.

## Physical Release Gate

Before production rollout, repeat startup, frame/jank, and memory measurements
on at least one representative low/mid-tier physical device and one current
device. Record device-specific baselines rather than comparing physical values
to this emulator. That external-device step remains required and intentionally
cannot be satisfied by this local implementation.
