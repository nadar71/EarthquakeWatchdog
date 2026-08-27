package com.indiewalk.watchdog.earthquake.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun startup() = baselineProfileRule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForSelector("benchmark-home-list")
    }

    @Test
    fun coreJourneys() = baselineProfileRule.collect(packageName = TARGET_PACKAGE) {
        pressHome()
        startActivityAndWait()
        device.tap("benchmark-filter-open")
        device.waitForSelector("benchmark-filter-sheet")

        device.tap("benchmark-nav-map")
        device.tap("benchmark-map-marker")
        device.waitForSelector("benchmark-map-marker-detail")

        device.tap("benchmark-nav-statistics")
        device.waitForSelector("benchmark-statistics-content")
        device.tap("benchmark-nav-settings")
        device.waitForSelector("benchmark-settings-content")
    }
}
