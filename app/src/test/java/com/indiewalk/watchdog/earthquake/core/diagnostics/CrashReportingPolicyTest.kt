package com.indiewalk.watchdog.earthquake.core.diagnostics

import com.indiewalk.watchdog.earthquake.core.domain.model.AppError
import java.io.IOException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashReportingPolicyTest {

    @After
    fun tearDown() {
        AppDiagnostics.resetSinkForTests()
    }

    @Test
    fun debugBuildsNeverEnableCrashCollection() {
        assertFalse(CrashReportingPolicy.isCollectionEnabled(isDebugBuild = true))
        assertTrue(CrashReportingPolicy.isCollectionEnabled(isDebugBuild = false))
    }

    @Test
    fun expectedNetworkLocationAndPermissionUiFailuresAreNotReported() {
        val reports = mutableListOf<Pair<DiagnosticCategory, Throwable>>()
        AppDiagnostics.setSinkForTests(recordingSink(reports))
        val expectedUiFailures = listOf(
            ExpectedUiFailure(AppError.Network("offline"), DiagnosticCategory.NETWORK, IOException("offline")),
            ExpectedUiFailure(AppError.Location("location unavailable"), DiagnosticCategory.LOCATION, IOException("location unavailable")),
            ExpectedUiFailure(AppError.Location("permission denied"), DiagnosticCategory.LOCATION, SecurityException("permission denied"))
        )

        expectedUiFailures.forEach { failure ->
            assertTrue(
                "Expected UI error must remain a network or location state",
                failure.error is AppError.Network || failure.error is AppError.Location
            )
            AppDiagnostics.recordNonFatal(failure.category, failure.throwable)
        }

        assertTrue(reports.isEmpty())
    }

    @Test
    fun unexpectedOwnershipFailureIsReportedOnceWithOnlyASanitizedCategory() {
        val reports = mutableListOf<Pair<DiagnosticCategory, Throwable>>()
        AppDiagnostics.setSinkForTests(recordingSink(reports))

        AppDiagnostics.recordNonFatal(
            DiagnosticCategory.STORAGE,
            IllegalStateException("address=Via del Corso 10 latitude=41.9028")
        )

        assertEquals(1, reports.size)
        assertEquals(DiagnosticCategory.STORAGE, reports.single().first)
        assertEquals("STORAGE/ILLEGAL_STATE", reports.single().second.message)
        assertFalse(reports.single().second.stackTraceToString().contains("latitude=41.9028"))
    }

    private fun recordingSink(reports: MutableList<Pair<DiagnosticCategory, Throwable>>) =
        object : DiagnosticSink {
            override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
                reports += category to throwable
            }

            override fun breadcrumb(event: DiagnosticEvent) = Unit
        }

    private data class ExpectedUiFailure(
        val error: AppError,
        val category: DiagnosticCategory,
        val throwable: Throwable
    )
}
