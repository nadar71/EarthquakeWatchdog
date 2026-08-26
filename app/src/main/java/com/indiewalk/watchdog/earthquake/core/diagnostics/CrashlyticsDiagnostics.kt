package com.indiewalk.watchdog.earthquake.core.diagnostics

import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal interface CrashlyticsGateway {
    fun recordException(throwable: Throwable, metadata: CrashlyticsReportMetadata)
    fun log(event: DiagnosticEvent)
    fun setCollectionEnabled(enabled: Boolean)
}

internal class CrashlyticsDiagnosticsAdapter(
    private val gateway: CrashlyticsGateway
) : DiagnosticSink {
    override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
        gateway.recordException(throwable, CrashlyticsReportMetadata.forCategory(category))
    }

    override fun breadcrumb(event: DiagnosticEvent) {
        gateway.log(event)
    }
}

internal class CrashlyticsReportMetadata private constructor(
    internal val values: Map<String, String>
) {
    companion object {
        private const val DIAGNOSTIC_CATEGORY_KEY = "diagnostic_category"

        fun forCategory(category: DiagnosticCategory) = CrashlyticsReportMetadata(
            mapOf(DIAGNOSTIC_CATEGORY_KEY to category.name)
        )
    }
}

internal object CrashlyticsStartup {
    fun configure(
        isDebugBuild: Boolean,
        initializeGateway: () -> CrashlyticsGateway?
    ) {
        initializeGateway()?.setCollectionEnabled(
            CrashReportingPolicy.isCollectionEnabled(isDebugBuild)
        )
    }
}

internal class FirebaseCrashlyticsGateway(
    private val crashlytics: FirebaseCrashlytics
) : CrashlyticsGateway {
    override fun recordException(throwable: Throwable, metadata: CrashlyticsReportMetadata) {
        val keys = CustomKeysAndValues.Builder().apply {
            metadata.values.forEach { (key, value) -> putString(key, value) }
        }.build()
        crashlytics.recordException(throwable, keys)
    }

    override fun log(event: DiagnosticEvent) {
        crashlytics.log(event.name)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }
}
