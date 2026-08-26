package com.indiewalk.watchdog.earthquake.core.diagnostics

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal object PlatformDiagnostics : DiagnosticSink {
    override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
        FirebaseCrashlytics.getInstance().setCustomKey("diagnostic_category", category.name)
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    override fun breadcrumb(event: DiagnosticEvent) {
        FirebaseCrashlytics.getInstance().log(event.name)
    }
}
