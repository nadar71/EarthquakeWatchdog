package com.indiewalk.watchdog.earthquake.core.diagnostics

enum class DiagnosticCategory {
    NETWORK,
    STORAGE,
    LOCATION,
    MAP,
    STATISTICS,
    EXTERNAL_INTENT
}

enum class DiagnosticEvent {
    EARTHQUAKE_REFRESH_REQUESTED,
    STATISTICS_LOAD_REQUESTED,
    MAP_MANUAL_LOCATION_CONFIRMED
}

internal interface DiagnosticSink {
    fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable)
    fun breadcrumb(event: DiagnosticEvent)
}

object AppDiagnostics {
    @Volatile
    private var sink: DiagnosticSink = PlatformDiagnostics

    fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
        if (!DiagnosticReportingPolicy.shouldReport(throwable)) return
        sink.recordNonFatal(category, SanitizedDiagnosticException(category, throwable))
    }

    fun breadcrumb(event: DiagnosticEvent) {
        sink.breadcrumb(event)
    }

    internal fun setSinkForTests(diagnosticSink: DiagnosticSink) {
        sink = diagnosticSink
    }

    internal fun resetSinkForTests() {
        sink = PlatformDiagnostics
    }
}

private class SanitizedDiagnosticException(
    category: DiagnosticCategory,
    throwable: Throwable
) : RuntimeException(
    "${category.name}/${throwable.toSafeFailureType()}",
    null,
    false,
    true
) {
    init {
        stackTrace = captureBoundaryStack()
    }
}

internal object DiagnosticReportingPolicy {
    fun shouldReport(throwable: Throwable): Boolean = when (throwable) {
        is java.io.IOException,
        is SecurityException -> false
        else -> true
    }
}

private fun captureBoundaryStack(): Array<StackTraceElement> = Throwable()
    .stackTrace
    .dropWhile { frame -> frame.className in diagnosticInternalClassNames }
    .toTypedArray()

private val diagnosticInternalClassNames = setOf(
    AppDiagnostics::class.java.name,
    SanitizedDiagnosticException::class.java.name,
    "${AppDiagnostics::class.java.name}Kt"
)

private fun Throwable.toSafeFailureType(): SafeFailureType = when (this) {
    is java.io.IOException -> SafeFailureType.IO
    is SecurityException -> SafeFailureType.SECURITY
    is IllegalArgumentException -> SafeFailureType.ILLEGAL_ARGUMENT
    is IllegalStateException -> SafeFailureType.ILLEGAL_STATE
    else -> SafeFailureType.UNEXPECTED
}

private enum class SafeFailureType {
    IO,
    SECURITY,
    ILLEGAL_ARGUMENT,
    ILLEGAL_STATE,
    UNEXPECTED
}
