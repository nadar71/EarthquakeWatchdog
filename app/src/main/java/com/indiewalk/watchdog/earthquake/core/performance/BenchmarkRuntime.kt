package com.indiewalk.watchdog.earthquake.core.performance

import com.indiewalk.watchdog.earthquake.BuildConfig
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppNavigationScreenFactory

/**
 * Loads deterministic benchmark content only from the benchmark build type.
 * The implementation class lives in the nonMinifiedRelease source set and is absent from
 * debug/release artifacts.
 */
object BenchmarkRuntime {
    private const val SCREEN_FACTORY_CLASS =
        "com.indiewalk.watchdog.earthquake.benchmark.BenchmarkAppNavigationScreenFactory"

    val isEnabled: Boolean
        get() = BuildConfig.BUILD_TYPE == "nonMinifiedRelease"

    fun screenFactoryOrNull(): AppNavigationScreenFactory? =
        if (isEnabled) {
            Class.forName(SCREEN_FACTORY_CLASS)
                .getDeclaredConstructor()
                .newInstance() as AppNavigationScreenFactory
        } else {
            null
        }
}
