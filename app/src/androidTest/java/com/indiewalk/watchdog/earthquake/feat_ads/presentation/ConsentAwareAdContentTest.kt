package com.indiewalk.watchdog.earthquake.feat_ads.presentation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import com.indiewalk.watchdog.earthquake.feat_ads.domain.AdsConsentState
import org.junit.Rule
import org.junit.Test

class ConsentAwareAdContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun ad_content_is_not_composed_before_consent() {
        composeRule.setContent {
            ConsentAwareAdContent(consentState = AdsConsentState()) {
                Text("ad", modifier = androidx.compose.ui.Modifier.testTag("ad-content"))
            }
        }

        composeRule.onNodeWithTag("ad-content").assertDoesNotExist()
    }

    @Test
    fun ad_content_is_composed_after_consent() {
        composeRule.setContent {
            ConsentAwareAdContent(
                consentState = AdsConsentState(canRequestAds = true)
            ) {
                Text("ad", modifier = androidx.compose.ui.Modifier.testTag("ad-content"))
            }
        }

        composeRule.onNodeWithTag("ad-content").assertExists()
    }
}
