package com.indiewalk.watchdog.earthquake.core.diagnostics

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

internal class DiagnosticsTestRule(
    private val diagnosticSink: DiagnosticSink = NoOpDiagnosticSink
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            AppDiagnostics.setSinkForTests(diagnosticSink)
            try {
                base.evaluate()
            } finally {
                AppDiagnostics.resetSinkForTests()
            }
        }
    }
}

private object NoOpDiagnosticSink : DiagnosticSink {
    override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) = Unit

    override fun breadcrumb(event: DiagnosticEvent) = Unit
}
