package com.indiewalk.watchdog.earthquake.feat_ads.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdsConsentCoordinatorTest {

    @Test
    fun ads_are_disabled_until_consent_result_is_ready() {
        val initializer = RecordingAdsSdkInitializer()
        val client = ControllableConsentClient()
        val coordinator = AdsConsentCoordinator(initializer)

        coordinator.requestConsent(client)

        assertEquals(AdsConsentState(), coordinator.state.value)
        assertEquals(0, initializer.initializeCalls)
        assertFalse(coordinator.state.value.shouldLoadAds)
    }

    @Test
    fun denied_consent_keeps_initialization_and_ad_loading_disabled() {
        val initializer = RecordingAdsSdkInitializer()
        val client = ControllableConsentClient()
        val coordinator = AdsConsentCoordinator(initializer)

        coordinator.requestConsent(client)
        client.completeRequest(canRequestAds = false, privacyOptionsRequired = true)

        assertFalse(coordinator.state.value.canRequestAds)
        assertTrue(coordinator.state.value.privacyOptionsRequired)
        assertFalse(coordinator.state.value.shouldLoadAds)
        assertEquals(0, initializer.initializeCalls)
    }

    @Test
    fun request_error_uses_the_platform_snapshot_and_does_not_assume_consent() {
        val initializer = RecordingAdsSdkInitializer()
        val client = ControllableConsentClient()
        val coordinator = AdsConsentCoordinator(initializer)

        coordinator.requestConsent(client)
        client.completeRequest(canRequestAds = false, privacyOptionsRequired = false)

        assertEquals(AdsConsentState(), coordinator.state.value)
        assertEquals(0, initializer.initializeCalls)
    }

    @Test
    fun granted_consent_initializes_ads_once_and_enables_loading() {
        val initializer = RecordingAdsSdkInitializer()
        val client = ControllableConsentClient()
        val coordinator = AdsConsentCoordinator(initializer)

        coordinator.requestConsent(client)
        client.completeRequest(canRequestAds = true, privacyOptionsRequired = true)
        coordinator.requestConsent(client)
        client.completeRequest(canRequestAds = true, privacyOptionsRequired = true)

        assertTrue(coordinator.state.value.canRequestAds)
        assertTrue(coordinator.state.value.shouldLoadAds)
        assertEquals(1, initializer.initializeCalls)
    }

    @Test
    fun privacy_options_withdrawal_disables_future_ad_loading() {
        val initializer = RecordingAdsSdkInitializer()
        val client = ControllableConsentClient()
        val coordinator = AdsConsentCoordinator(initializer)

        coordinator.requestConsent(client)
        client.completeRequest(canRequestAds = true, privacyOptionsRequired = true)
        coordinator.showPrivacyOptions(client)
        client.completePrivacyOptions(canRequestAds = false, privacyOptionsRequired = true)

        assertFalse(coordinator.state.value.canRequestAds)
        assertFalse(coordinator.state.value.shouldLoadAds)
        assertEquals(1, initializer.initializeCalls)
    }

    @Test
    fun opening_privacy_options_disables_ads_until_the_updated_choice_is_known() {
        val initializer = RecordingAdsSdkInitializer()
        val client = ControllableConsentClient()
        val coordinator = AdsConsentCoordinator(initializer)

        coordinator.requestConsent(client)
        client.completeRequest(canRequestAds = true, privacyOptionsRequired = true)
        coordinator.showPrivacyOptions(client)

        assertFalse(coordinator.state.value.canRequestAds)
        assertFalse(coordinator.state.value.shouldLoadAds)
    }
}

private class RecordingAdsSdkInitializer : AdsSdkInitializer {
    var initializeCalls = 0

    override fun initialize() {
        initializeCalls += 1
    }
}

private class ControllableConsentClient : ConsentClient {
    private var requestCallback: ((ConsentSnapshot) -> Unit)? = null
    private var privacyOptionsCallback: ((ConsentSnapshot) -> Unit)? = null

    override fun requestConsent(onComplete: (ConsentSnapshot) -> Unit) {
        requestCallback = onComplete
    }

    override fun showPrivacyOptions(onComplete: (ConsentSnapshot) -> Unit) {
        privacyOptionsCallback = onComplete
    }

    fun completeRequest(canRequestAds: Boolean, privacyOptionsRequired: Boolean) {
        requestCallback?.invoke(ConsentSnapshot(canRequestAds, privacyOptionsRequired))
    }

    fun completePrivacyOptions(canRequestAds: Boolean, privacyOptionsRequired: Boolean) {
        privacyOptionsCallback?.invoke(ConsentSnapshot(canRequestAds, privacyOptionsRequired))
    }
}
