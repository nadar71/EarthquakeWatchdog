package com.indiewalk.watchdog.earthquake.feat_eqsmap.util

import com.indiewalk.watchdog.earthquake.core.diagnostics.AppDiagnostics
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticCategory
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticEvent
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticSink
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import java.io.IOException
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MapsUtilsTest {

    @After
    fun tearDown() {
        AppDiagnostics.resetSinkForTests()
    }

    @Test
    fun locationResultRethrowsCancellationWithoutReportingIt() = runTest {
        val reports = mutableListOf<Pair<DiagnosticCategory, Throwable>>()
        AppDiagnostics.setSinkForTests(recordingSink(reports))
        val cancellation = CancellationException("cancelled")

        val thrown = runCatching {
            resolveLocationResultOrNull<String> { throw cancellation }
        }.exceptionOrNull()

        assertSame(cancellation, thrown)
        assertTrue(reports.isEmpty())
    }

    @Test
    fun routineLocationFailureReturnsNullWithoutReportingIt() = runTest {
        val reports = mutableListOf<Pair<DiagnosticCategory, Throwable>>()
        AppDiagnostics.setSinkForTests(recordingSink(reports))

        val result = resolveLocationResultOrNull<String> { throw IOException("provider unavailable") }

        assertNull(result)
        assertTrue(reports.isEmpty())
    }

    private fun recordingSink(reports: MutableList<Pair<DiagnosticCategory, Throwable>>) =
        object : DiagnosticSink {
            override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
                reports += category to throwable
            }

            override fun breadcrumb(event: DiagnosticEvent) = Unit
        }
}
