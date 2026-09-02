package com.indiewalk.watchdog.earthquake.core.diagnostics

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal object PlatformDiagnostics : DiagnosticSink {
    private val adapter by lazy {
        CrashlyticsDiagnosticsAdapter(FirebaseCrashlyticsGateway(FirebaseCrashlytics.getInstance()))
    }

    override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
        adapter.recordNonFatal(category, throwable)
    }

    override fun breadcrumb(event: DiagnosticEvent) {
        adapter.breadcrumb(event)
    }
}
