package com.indiewalk.watchdog.earthquake.feat_ads.data

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.indiewalk.watchdog.earthquake.feat_ads.domain.ConsentClient
import com.indiewalk.watchdog.earthquake.feat_ads.domain.ConsentSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface ConsentClientFactory {
    fun create(activity: Activity): ConsentClient
}

@Singleton
class UmpConsentClientFactory @Inject constructor(
    @ApplicationContext private val context: Context
) : ConsentClientFactory {
    override fun create(activity: Activity): ConsentClient = UmpConsentClient(
        activity = activity,
        consentInformation = UserMessagingPlatform.getConsentInformation(context)
    )
}

private class UmpConsentClient(
    private val activity: Activity,
    private val consentInformation: ConsentInformation
) : ConsentClient {
    override fun requestConsent(onComplete: (ConsentSnapshot) -> Unit) {
        val parameters = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    onComplete(currentSnapshot())
                }
            },
            {
                onComplete(currentSnapshot())
            }
        )
    }

    override fun showPrivacyOptions(onComplete: (ConsentSnapshot) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            onComplete(currentSnapshot())
        }
    }

    private fun currentSnapshot(): ConsentSnapshot = ConsentSnapshot(
        canRequestAds = consentInformation.canRequestAds(),
        privacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    )
}
