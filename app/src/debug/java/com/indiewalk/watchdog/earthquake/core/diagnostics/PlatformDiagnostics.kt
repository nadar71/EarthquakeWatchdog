package com.indiewalk.watchdog.earthquake.core.diagnostics

import android.util.Log

internal object PlatformDiagnostics : DiagnosticSink {
    private const val TAG = "EarthquakeDiagnostics"

    override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
        Log.e(TAG, category.name, throwable)
    }

    override fun breadcrumb(event: DiagnosticEvent) {
        Log.d(TAG, event.name)
    }
}
