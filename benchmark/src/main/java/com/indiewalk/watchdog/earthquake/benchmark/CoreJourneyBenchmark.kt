package com.indiewalk.watchdog.earthquake.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreJourneyBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun listScroll() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = DEFAULT_ITERATIONS,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.waitForSelector("benchmark-home-list")
        }
    ) {
        device.findObject(androidx.test.uiautomator.By.res("benchmark-home-list")).apply {
            setGestureMargin(device.displayWidth / 6)
            fling(Direction.DOWN)
        }
    }

    @Test
    fun topLevelNavigation() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = DEFAULT_ITERATIONS,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.waitForSelector("benchmark-home-list")
        }
    ) {
        device.tap("benchmark-nav-map")
        device.waitForSelector("benchmark-map-content")
        device.tap("benchmark-nav-statistics")
        device.waitForSelector("benchmark-statistics-content")
        device.tap("benchmark-nav-settings")
        device.waitForSelector("benchmark-settings-content")
    }
}
