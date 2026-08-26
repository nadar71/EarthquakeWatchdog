package com.indiewalk.watchdog.earthquake.core.diagnostics

import com.indiewalk.watchdog.earthquake.feat_statistics.data.repository.EarthquakeStatisticsLoadException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AppDiagnosticsTest {

    @After
    fun tearDown() {
        AppDiagnostics.resetSinkForTests()
    }

    @Test
    fun recordNonFatalRedactsSensitiveThrowableMessagesBeforeTheyReachTheSink() {
        val reports = mutableListOf<Pair<DiagnosticCategory, Throwable>>()
        AppDiagnostics.setSinkForTests(
            object : DiagnosticSink {
                override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
                    reports += category to throwable
                }

                override fun breadcrumb(event: DiagnosticEvent) = Unit
            }
        )
        val sensitiveMessages = listOf(
            "latitude=41.9028&longitude=12.4964",
            "Via del Corso 10, Roma",
            "https://earthquake.usgs.gov/query?minlatitude=41.9",
            "API key=AIzaSyExampleSecret",
            "advertising_id=38400000-8cf0-11bd-b23e-10b96e40000d"
        )

        val sourceFailures = sensitiveMessages.map { message ->
            IllegalStateException(message, IllegalArgumentException("cause: $message")).apply {
                addSuppressed(IllegalStateException("suppressed: $message"))
            }
        }

        sourceFailures.forEach { failure ->
            AppDiagnostics.recordNonFatal(DiagnosticCategory.NETWORK, failure)
        }

        assertEquals(sensitiveMessages.size, reports.size)
        reports.forEachIndexed { index, (category, throwable) ->
            assertEquals(DiagnosticCategory.NETWORK, category)
            assertEquals("NETWORK/ILLEGAL_STATE", throwable.message)
            assertEquals(null, throwable.cause)
            assertTrue(throwable.suppressed.isEmpty())
            assertTrue(throwable.stackTrace.isNotEmpty())
            assertEquals(AppDiagnosticsTest::class.java.name, throwable.stackTrace.first().className)
            sensitiveMessages.forEach { sensitiveMessage ->
                assertFalse(throwable.toString().contains(sensitiveMessage))
                assertFalse(throwable.stackTraceToString().contains(sensitiveMessage))
            }
        }
    }

    @Test
    fun recordNonFatalDoesNotForwardCallerControlledStackFrames() {
        val reports = mutableListOf<Throwable>()
        AppDiagnostics.setSinkForTests(
            object : DiagnosticSink {
                override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
                    reports += throwable
                }

                override fun breadcrumb(event: DiagnosticEvent) = Unit
            }
        )
        val sensitiveValues = listOf(
            "latitude=41.9028",
            "AIzaSyExampleSecret",
            "38400000-8cf0-11bd-b23e-10b96e40000d"
        )
        val hostileThrowable = IllegalStateException("safe message").apply {
            stackTrace = arrayOf(
                StackTraceElement(sensitiveValues[0], sensitiveValues[1], sensitiveValues[2], 1)
            )
        }

        AppDiagnostics.recordNonFatal(DiagnosticCategory.NETWORK, hostileThrowable)

        val report = reports.single()
        assertEquals(AppDiagnosticsTest::class.java.name, report.stackTrace.first().className)
        sensitiveValues.forEach { sensitiveValue ->
            assertFalse(report.message.orEmpty().contains(sensitiveValue))
            assertFalse(report.cause?.toString().orEmpty().contains(sensitiveValue))
            assertFalse(report.suppressed.joinToString().contains(sensitiveValue))
            assertFalse(report.stackTraceToString().contains(sensitiveValue))
        }
    }

    @Test
    fun expectedNetworkAndPermissionFailuresAreNotReportedButUnexpectedFailuresReportOnce() {
        val reports = mutableListOf<Pair<DiagnosticCategory, Throwable>>()
        AppDiagnostics.setSinkForTests(
            object : DiagnosticSink {
                override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
                    reports += category to throwable
                }

                override fun breadcrumb(event: DiagnosticEvent) = Unit
            }
        )

        AppDiagnostics.recordNonFatal(DiagnosticCategory.NETWORK, IOException("offline"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.LOCATION, SecurityException("permission denied"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.LOCATION, IOException("geocoder unavailable"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.STORAGE, IllegalStateException("database invariant"))

        assertEquals(1, reports.size)
        assertEquals(DiagnosticCategory.STORAGE, reports.single().first)
        assertEquals("STORAGE/ILLEGAL_STATE", reports.single().second.message)
    }

    @Test
    fun reportingPolicyOnlySuppressesExpectedFailuresForTheirCategory() {
        val reports = mutableListOf<Pair<DiagnosticCategory, Throwable>>()
        AppDiagnostics.setSinkForTests(recordingSink(reports))

        AppDiagnostics.recordNonFatal(DiagnosticCategory.NETWORK, IOException("offline"))
        AppDiagnostics.recordNonFatal(
            DiagnosticCategory.NETWORK,
            IllegalStateException("network wrapper", IOException("offline"))
        )
        AppDiagnostics.recordNonFatal(DiagnosticCategory.LOCATION, IOException("geocoder unavailable"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.LOCATION, SecurityException("permission denied"))
        AppDiagnostics.recordNonFatal(
            DiagnosticCategory.STATISTICS,
            EarthquakeStatisticsLoadException("offline", IOException("offline"))
        )
        AppDiagnostics.recordNonFatal(DiagnosticCategory.NETWORK, SecurityException("unexpected"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.LOCATION, IllegalStateException("unexpected"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.STATISTICS, IOException("unexpected statistics io"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.STORAGE, IOException("database io"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.MAP, SecurityException("map security"))
        AppDiagnostics.recordNonFatal(DiagnosticCategory.EXTERNAL_INTENT, IOException("intent io"))

        assertEquals(
            listOf(
                DiagnosticCategory.NETWORK,
                DiagnosticCategory.LOCATION,
                DiagnosticCategory.STATISTICS,
                DiagnosticCategory.STORAGE,
                DiagnosticCategory.MAP,
                DiagnosticCategory.EXTERNAL_INTENT
            ),
            reports.map { it.first }
        )
    }

    @Test
    fun reportingPolicyHandlesCyclicCausesWithoutTraversingForever() {
        val reports = mutableListOf<Pair<DiagnosticCategory, Throwable>>()
        AppDiagnostics.setSinkForTests(recordingSink(reports))
        val first = IllegalStateException("first")
        val second = IllegalArgumentException("second")
        first.initCause(second)
        second.initCause(first)

        AppDiagnostics.recordNonFatal(DiagnosticCategory.NETWORK, first)

        assertEquals(1, reports.size)
        assertEquals(DiagnosticCategory.NETWORK, reports.single().first)
    }

    @Test
    fun breadcrumbOnlyForwardsClosedEvents() {
        val events = mutableListOf<DiagnosticEvent>()
        AppDiagnostics.setSinkForTests(
            object : DiagnosticSink {
                override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) = Unit

                override fun breadcrumb(event: DiagnosticEvent) {
                    events += event
                }
            }
        )

        AppDiagnostics.breadcrumb(DiagnosticEvent.EARTHQUAKE_REFRESH_REQUESTED)

        assertEquals(listOf(DiagnosticEvent.EARTHQUAKE_REFRESH_REQUESTED), events)
    }

    private fun recordingSink(reports: MutableList<Pair<DiagnosticCategory, Throwable>>) =
        object : DiagnosticSink {
            override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
                reports += category to throwable
            }

            override fun breadcrumb(event: DiagnosticEvent) = Unit
        }
}
