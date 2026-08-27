package com.indiewalk.watchdog.earthquake.core.performance

import androidx.compose.runtime.Composable
import com.indiewalk.watchdog.earthquake.BuildConfig

/**
 * Loads deterministic benchmark content only from the benchmark build type.
 * The implementation class lives in the nonMinifiedRelease source set and is absent from
 * debug/release artifacts.
 */
object BenchmarkRuntime {
    private const val HARNESS_CLASS =
        "com.indiewalk.watchdog.earthquake.benchmark.BenchmarkHarness"

    val isEnabled: Boolean
        get() = BuildConfig.BUILD_TYPE == "nonMinifiedRelease"

    fun createScreenOrNull(): BenchmarkScreen? =
        if (isEnabled) {
            Class.forName(HARNESS_CLASS).getDeclaredConstructor().newInstance() as BenchmarkScreen
        } else {
            null
        }
}

interface BenchmarkScreen {
    @Composable
    fun Content()
}
