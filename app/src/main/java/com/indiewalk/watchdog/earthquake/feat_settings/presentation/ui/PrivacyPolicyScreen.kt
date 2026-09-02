package com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    currentDestination: AppDestination,
    onBack: () -> Unit
) {
    val publicPolicyUrl = stringResource(R.string.privacy_policy_public_url)
    val uriHandler = LocalUriHandler.current

    ScaffoldModel(
        currentDestination = currentDestination,
        onBack = onBack,
        topBar = null,
        title = stringResource(R.string.privacy_policy_title),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .testTag("privacy-policy-content")
        ) {
            Text(
                text = stringResource(R.string.privacy_policy_body),
                style = MaterialTheme.typography.bodyLarge
            )
            if (publicPolicyUrl.startsWith("https://")) {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsItem(
                    title = stringResource(R.string.privacy_policy_open_public_title),
                    subtitle = publicPolicyUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(publicPolicyUrl) }
                        .testTag("privacy-policy-public-link")
                )
            }
        }
    }
}
