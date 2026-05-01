package com.indiewalk.watchdog.earthquake.feat_intro.presentation

import com.indiewalk.watchdog.earthquake.FakeAppPreferencesRepository
import com.indiewalk.watchdog.earthquake.FakeLocationRepository
import com.indiewalk.watchdog.earthquake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntroViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `shows denied dialog when permission is rejected`() = runTest {
        val viewModel = IntroViewModel(
            appPreferencesRepository = FakeAppPreferencesRepository(),
            locationRepository = FakeLocationRepository()
        )

        viewModel.onPermissionResult(false)

        assertTrue(viewModel.uiState.value.showDeniedDialog)
    }

    @Test
    fun `emits navigation effect when permission is granted`() = runTest {
        val viewModel = IntroViewModel(
            appPreferencesRepository = FakeAppPreferencesRepository(),
            locationRepository = FakeLocationRepository()
        )
        val effect = async { viewModel.effects.first() }

        viewModel.onPermissionResult(true)
        advanceTimeBy(1_000)
        advanceUntilIdle()

        assertEquals(IntroEffect.NavigateHome, effect.await())
    }
}
