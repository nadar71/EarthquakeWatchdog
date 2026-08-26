package com.indiewalk.watchdog.earthquake.core.diagnostics

import java.io.IOException
import java.util.Collections
import java.util.IdentityHashMap
import kotlinx.coroutines.CancellationException

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
        if (!CrashReportingPolicy.shouldReport(category, throwable)) return
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

internal object CrashReportingPolicy {
    fun isCollectionEnabled(isDebugBuild: Boolean): Boolean = !isDebugBuild

    fun shouldReport(category: DiagnosticCategory, throwable: Throwable): Boolean =
        DiagnosticReportingPolicy.shouldReport(category, throwable)
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
    fun shouldReport(category: DiagnosticCategory, throwable: Throwable): Boolean {
        // Cancellation is control flow, never an application failure to diagnose.
        if (throwable.hasCauseMatching { it is CancellationException }) return false

        return when (category) {
            DiagnosticCategory.NETWORK -> !throwable.hasCauseMatching { it is IOException }
            DiagnosticCategory.LOCATION -> !throwable.hasCauseMatching {
                it is IOException || it is SecurityException
            }
            DiagnosticCategory.STATISTICS -> !(
                throwable is StatisticsLoadFailure &&
                    throwable.hasCauseMatching { it is IOException }
                )
            DiagnosticCategory.STORAGE,
            DiagnosticCategory.MAP,
            DiagnosticCategory.EXTERNAL_INTENT -> true
        }
    }
}

/** Marks the single statistics load wrapper recognized by the reporting policy. */
internal interface StatisticsLoadFailure

private fun captureBoundaryStack(): Array<StackTraceElement> = Throwable()
    .stackTrace
    .dropWhile { frame -> frame.className in diagnosticInternalClassNames }
    .toTypedArray()

private val diagnosticInternalClassNames = setOf(
    AppDiagnostics::class.java.name,
    SanitizedDiagnosticException::class.java.name,
    "${AppDiagnostics::class.java.name}Kt"
)

private fun Throwable.hasCauseMatching(predicate: (Throwable) -> Boolean): Boolean {
    val visited = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
    var current: Throwable? = this
    while (current != null && visited.add(current)) {
        if (predicate(current)) return true
        current = current.cause
    }
    return false
}

private fun Throwable.toSafeFailureType(): SafeFailureType = when (this) {
    is IOException -> SafeFailureType.IO
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
