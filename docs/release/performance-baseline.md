# Performance Baseline

## Scope

The `:benchmark` module generates the app Baseline Profile and measures three
deterministic journeys against the `nonMinifiedRelease` profile target. The
target renders a fixed in-memory earthquake fixture and does not initialize or
contact USGS, Google Maps, ads, UMP, Firebase, geocoding, or device location.
It exists only in the `nonMinifiedRelease` source set; the minified production
release keeps its normal behavior.

The selected targets cover cold startup, the home list and filter, map marker
detail, statistics, settings, list scrolling, and top-level navigation. The
fixture's semantics are exported as resource ids only in this benchmark-only
source set, so no benchmark selector strings are part of the production UI.

## Baseline Profile

Generated on 2026-08-27 with AndroidX Baseline Profile and Benchmark 1.4.1:

```text
ANDROID_SERIAL=emulator-5556 ./gradlew :app:generateBaselineProfile --console=plain
```

The final single-device command passed six instrumented profile journeys on
`MathBrainer_7in` API 35 in 3 minutes 42 seconds. It produced these checked-in
plugin inputs:

- `app/src/release/generated/baselineProfiles/startup-prof.txt`: 13,218 rules.
- `app/src/release/generated/baselineProfiles/baseline-prof.txt`: 14,516 rules.

The profile target is `profileable` to the shell only. It declares no benchmark
receiver, activity, service, or other exported component.

## Reference Measurements

Reference environment: `MathBrainer_7in` AVD, API 35, serial `emulator-5556`,
`nonMinifiedRelease`, AndroidX Benchmark 1.4.1. Macrobenchmark reports warn
that an emulator is not representative of physical-user performance; these
values are therefore an automated regression reference, not a product SLO.

All macrobenchmark commands used ten iterations and explicitly suppressed only
the emulator guard:

```text
ANDROID_SERIAL=emulator-5556 ./gradlew :benchmark:connectedNonMinifiedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.benchmark.StartupBenchmark \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR --console=plain

ANDROID_SERIAL=emulator-5556 ./gradlew :benchmark:connectedNonMinifiedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.indiewalk.watchdog.earthquake.benchmark.CoreJourneyBenchmark \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR --console=plain
```

| Journey | Iterations | Recorded result | 25% regression budget |
| --- | ---: | ---: | ---: |
| Cold start | 10 | TTID median 845.4 ms | 1,056.8 ms |
| List scroll | 10 | CPU frame-duration P50 31.6 ms; frame-overrun P50 26.6 ms | 39.5 ms; 33.3 ms |
| Top-level navigation | 10 | CPU frame-duration P50 27.1 ms; frame-overrun P50 11.4 ms | 33.9 ms; 14.3 ms |

The 25% threshold is a material-regression trigger relative to the recorded
median/P50 baseline. Re-measure on the same reference configuration before
changing these numbers; do not compare emulator measurements directly to a
physical-device run.

Macrobenchmark did not emit a reliable jank-rate percentage for this emulator,
so the frame-overrun distribution is recorded instead. Memory was not measured
reliably in this run. Physical-device startup/frame/jank and memory baselines
remain release-gate follow-ups and are deliberately non-blocking here.

## Release Isolation

`scripts/verify_release_configuration.sh` performs a fresh synthetic signed,
minified release AAB and APK build, confirms the AAB packages baseline-profile
metadata, and invokes `scripts/verify_benchmark_isolation.sh`. The latter scans
every release DEX entry and fails if `BenchmarkHarness` or any benchmark
selector string remains. This proves the reflection bridge is inert in the
production artifacts as well as keeping the benchmark source set absent from
debug and release production code.
