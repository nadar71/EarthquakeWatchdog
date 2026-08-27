package com.indiewalk.watchdog.earthquake.benchmark

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies benchmark selectors before performance runs use them. */
@RunWith(AndroidJUnit4::class)
class BenchmarkSelectorContractTest {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun coreJourneySelectorsAreReachable() {
        device.startBenchmarkApp()
        device.tap("benchmark-filter-open")
        device.waitForSelector("benchmark-filter-sheet")

        device.tap("benchmark-nav-map")
        device.waitForSelector("benchmark-map-content")
        device.tap("benchmark-map-marker")
        device.waitForSelector("benchmark-map-marker-detail")

        device.tap("benchmark-nav-statistics")
        device.waitForSelector("benchmark-statistics-content")
        device.tap("benchmark-nav-settings")
        device.waitForSelector("benchmark-settings-content")
    }
}
