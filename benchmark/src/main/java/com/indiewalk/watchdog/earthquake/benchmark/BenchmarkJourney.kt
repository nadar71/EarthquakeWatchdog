package com.indiewalk.watchdog.earthquake.benchmark

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue

internal const val TARGET_PACKAGE = "com.indiewalk.watchdog.earthquake"
internal const val DEFAULT_ITERATIONS = 10
private const val WAIT_TIMEOUT_MS = 5_000L

internal fun UiDevice.startBenchmarkApp() {
    pressHome()
    context.startActivity(
        context.packageManager.getLaunchIntentForPackage(TARGET_PACKAGE)?.apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    )
    waitForSelector("benchmark-home-list")
}

internal fun UiDevice.waitForSelector(tag: String) {
    assertTrue("Missing benchmark selector: $tag", wait(Until.hasObject(By.res(tag)), WAIT_TIMEOUT_MS))
}

internal fun UiDevice.tap(tag: String) {
    waitForSelector(tag)
    findObject(By.res(tag)).click()
}

internal val context
    get() = InstrumentationRegistry.getInstrumentation().context
