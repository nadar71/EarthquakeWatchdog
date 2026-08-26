package com.indiewalk.watchdog.earthquake.core.diagnostics

/**
 * Task 4 replaces this no-op adapter with the consent-aware Crashlytics implementation.
 */
internal object PlatformDiagnostics : DiagnosticSink {
    override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) = Unit

    override fun breadcrumb(event: DiagnosticEvent) = Unit
}
