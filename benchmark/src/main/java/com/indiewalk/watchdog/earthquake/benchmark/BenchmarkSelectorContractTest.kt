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
        device.tap("earthquake-list-filter")
        device.waitForSelector("filter-sheet-content")

        device.tap("bottom-nav-map")
        device.waitForSelector("map-content")
        device.tap("map-earthquake-marker")
        device.waitForSelector("map-selected-earthquake-detail")

        device.tap("bottom-nav-statistics")
        device.waitForSelector("statistics-list")
        device.tap("bottom-nav-settings")
        device.waitForSelector("settings-content")
    }
}
