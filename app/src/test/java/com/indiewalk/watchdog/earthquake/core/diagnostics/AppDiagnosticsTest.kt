package com.indiewalk.watchdog.earthquake.core.diagnostics

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
            assertEquals(sourceFailures[index].stackTrace.toList(), throwable.stackTrace.toList())
            sensitiveMessages.forEach { sensitiveMessage ->
                assertFalse(throwable.toString().contains(sensitiveMessage))
                assertFalse(throwable.stackTraceToString().contains(sensitiveMessage))
            }
        }
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
}
