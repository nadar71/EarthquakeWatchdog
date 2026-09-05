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
        device.waitForSelector("earthquake-list-content")
    }

    @Test
    fun coreJourneys() = baselineProfileRule.collect(packageName = TARGET_PACKAGE) {
        pressHome()
        startActivityAndWait()
        device.tap("earthquake-list-filter")
        device.waitForSelector("filter-sheet-content")

        device.tap("bottom-nav-map")
        device.tap("map-earthquake-marker")
        device.waitForSelector("map-selected-earthquake-detail")

        device.tap("bottom-nav-statistics")
        device.waitForSelector("statistics-list")
        device.tap("bottom-nav-settings")
        device.waitForSelector("settings-content")
    }
}
