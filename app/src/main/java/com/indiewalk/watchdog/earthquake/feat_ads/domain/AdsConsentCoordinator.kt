package com.indiewalk.watchdog.earthquake.feat_ads.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AdsConsentState(
    val canRequestAds: Boolean = false,
    val privacyOptionsRequired: Boolean = false
) {
    val shouldLoadAds: Boolean
        get() = canRequestAds
}

data class ConsentSnapshot(
    val canRequestAds: Boolean,
    val privacyOptionsRequired: Boolean
)

interface ConsentClient {
    fun requestConsent(onComplete: (ConsentSnapshot) -> Unit)
    fun showPrivacyOptions(onComplete: (ConsentSnapshot) -> Unit)
}

interface AdsSdkInitializer {
    fun initialize()
}

@Singleton
class AdsConsentCoordinator @Inject constructor(
    private val adsSdkInitializer: AdsSdkInitializer
) {
    private val mutableState = MutableStateFlow(AdsConsentState())
    private var adsSdkInitialized = false

    val state: StateFlow<AdsConsentState> = mutableState.asStateFlow()

    fun requestConsent(client: ConsentClient) {
        mutableState.value = AdsConsentState()
        client.requestConsent(::applySnapshot)
    }

    fun showPrivacyOptions(client: ConsentClient) {
        mutableState.value = mutableState.value.copy(canRequestAds = false)
        client.showPrivacyOptions(::applySnapshot)
    }

    private fun applySnapshot(snapshot: ConsentSnapshot) {
        mutableState.value = AdsConsentState(
            canRequestAds = snapshot.canRequestAds,
            privacyOptionsRequired = snapshot.privacyOptionsRequired
        )
        if (snapshot.canRequestAds && !adsSdkInitialized) {
            adsSdkInitialized = true
            adsSdkInitializer.initialize()
        }
    }
}
