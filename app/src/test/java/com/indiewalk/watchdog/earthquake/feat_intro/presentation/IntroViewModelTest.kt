package com.indiewalk.watchdog.earthquake.feat_intro.presentation

import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeLocationRepository
import com.indiewalk.watchdog.earthquake.sampleLatLng
import com.indiewalk.watchdog.earthquake.sampleLocationInfo
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticCategory
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticEvent
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticSink
import com.indiewalk.watchdog.earthquake.core.diagnostics.DiagnosticsTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntroViewModelTest {

    private val diagnosticReports = mutableListOf<DiagnosticCategory>()

    @get:Rule
    internal val diagnosticsTestRule = DiagnosticsTestRule(
        object : DiagnosticSink {
            override fun recordNonFatal(category: DiagnosticCategory, throwable: Throwable) {
                diagnosticReports += category
            }

            override fun breadcrumb(event: DiagnosticEvent) = Unit
        }
    )

    @Test
    fun `shows denied dialog when permission is rejected`() = runViewModelTest {
        val viewModel = IntroViewModel(
            appPreferencesRepository = FakeAppPreferencesRepository(),
            locationRepository = FakeLocationRepository()
        )

        viewModel.onPermissionResult(false)

        assertTrue(viewModel.uiState.value.showDeniedDialog)
        assertTrue(diagnosticReports.isEmpty())
    }

    @Test
    fun `emits navigation effect when permission is granted`() = runViewModelTest {
        val appPrefs = FakeAppPreferencesRepository()
        val viewModel = IntroViewModel(
            appPreferencesRepository = appPrefs,
            locationRepository = FakeLocationRepository(
                lastKnownLatLng = sampleLatLng,
                locationInfo = sampleLocationInfo
            )
        )
        val effect = async { viewModel.effects.first() }

        viewModel.onPermissionResult(true)
        advanceTimeBy(1_000)
        advanceUntilIdle()

        assertEquals(IntroEffect.NavigateHome, effect.await())
        assertEquals(sampleLatLng, appPrefs.getCurrentSettings().userPosition)
        assertEquals(sampleLocationInfo, appPrefs.getCurrentSettings().userLocationInfo)
    }

    @Test
    fun `asked once immediately navigates home`() = runViewModelTest {
        val viewModel = IntroViewModel(
            appPreferencesRepository = FakeAppPreferencesRepository(initialAskedOnce = true),
            locationRepository = FakeLocationRepository()
        )
        val effect = async { viewModel.effects.first() }

        advanceTimeBy(1_000)
        advanceUntilIdle()

        assertEquals(IntroEffect.NavigateHome, effect.await())
    }

    @Test
    fun `dismissing denied dialog stores asked once`() = runViewModelTest {
        val appPrefs = FakeAppPreferencesRepository()
        val viewModel = IntroViewModel(
            appPreferencesRepository = appPrefs,
            locationRepository = FakeLocationRepository()
        )

        viewModel.onPermissionResult(false)
        viewModel.onDeniedDialogDismissed()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.showDeniedDialog)
        assertTrue(appPrefs.askedLocationOnceFlow.first())
    }

    @Test
    fun `open settings emits effect and stores asked once`() = runViewModelTest {
        val appPrefs = FakeAppPreferencesRepository()
        val viewModel = IntroViewModel(
            appPreferencesRepository = appPrefs,
            locationRepository = FakeLocationRepository()
        )
        val effect = async { viewModel.effects.first() }

        viewModel.onOpenSettingsRequested()
        advanceUntilIdle()

        assertEquals(IntroEffect.OpenAppSettings, effect.await())
        assertTrue(appPrefs.askedLocationOnceFlow.first())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun runViewModelTest(block: suspend TestScope.() -> Unit) =
    runTest(StandardTestDispatcher(TestCoroutineScheduler())) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
