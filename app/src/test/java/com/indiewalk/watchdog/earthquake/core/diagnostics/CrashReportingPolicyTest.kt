package com.indiewalk.watchdog.earthquake.core.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashReportingPolicyTest {

    @Test
    fun adapterRecordsOneNonFatalWithOnlyTheClosedDiagnosticCategoryMetadata() {
        val gateway = RecordingCrashlyticsGateway()
        val adapter = CrashlyticsDiagnosticsAdapter(gateway)
        val throwable = IllegalStateException("STORAGE/ILLEGAL_STATE")

        adapter.recordNonFatal(DiagnosticCategory.STORAGE, throwable)

        assertEquals(1, gateway.reports.size)
        assertEquals(throwable, gateway.reports.single().throwable)
        assertEquals(
            mapOf("diagnostic_category" to "STORAGE"),
            gateway.reports.single().metadata.values
        )
        assertTrue(gateway.collectionStates.isEmpty())
    }

    @Test
    fun startupExplicitlyReplacesDebugCollectionOverrideForRelease() {
        val gateway = RecordingCrashlyticsGateway()

        CrashlyticsStartup.configure(isDebugBuild = true) { gateway }
        CrashlyticsStartup.configure(isDebugBuild = false) { gateway }

        assertEquals(listOf(false, true), gateway.collectionStates)
    }

    @Test
    fun startupSafelySkipsMissingDebugFirebaseConfiguration() {
        CrashlyticsStartup.configure(isDebugBuild = true) { null }
    }

    private class RecordingCrashlyticsGateway : CrashlyticsGateway {
        data class Report(
            val throwable: Throwable,
            val metadata: CrashlyticsReportMetadata
        )

        val reports = mutableListOf<Report>()
        val collectionStates = mutableListOf<Boolean>()

        override fun recordException(throwable: Throwable, metadata: CrashlyticsReportMetadata) {
            reports += Report(throwable, metadata)
        }

        override fun log(event: DiagnosticEvent) = Unit

        override fun setCollectionEnabled(enabled: Boolean) {
            collectionStates += enabled
        }
    }
}
