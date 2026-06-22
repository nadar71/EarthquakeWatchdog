package com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.USGS_URL
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.my_website
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.util.GenericUtil.openUrlInBrowserNotCompose
import com.indiewalk.watchdog.earthquake.feat_ads.presentation.AdMobBannerView
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(navController: NavHostController ) {
    val TAG = "CreditsScreen"
    val context = LocalContext.current

    ScaffoldModel(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.credits_title_label),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    LogoAnimationForward(
                        modifier = Modifier
                            .padding(start = 5.dp),
                        size = 50.dp,
                        frameDurationMs = 90L
                    )
                },
                actions = {
                    // balancing space placeholder as trail icon to title centering
                    Spacer(
                        modifier = Modifier
                            .width(50.dp)
                            .padding(end = 5.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,       // background
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,  // title text
                )
            )
        },
    ) { padding ->

        val topInset = padding.calculateTopPadding()
        val bottomInset = padding.calculateBottomPadding()

        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, top = topInset + 32.dp, end = 24.dp, bottom = 0.dp )
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                SettingsItem(
                    title = stringResource(id = R.string.credits_dev_design_label),
                    subtitle = stringResource(id = R.string.credits_dev_design_SM),
                    rightIcon = R.drawable.ic_globe,
                    iconDescription = stringResource(id = R.string.credits_dev_design_label),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                        openUrlInBrowserNotCompose(context, my_website)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
                SettingsItem(
                    title = stringResource(id = R.string.credits_eqs_data_source_label),
                    subtitle = stringResource(id = R.string.credits_eqs_data_source_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                        openUrlInBrowserNotCompose(context, USGS_URL)
                    }
                )
            }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomInset )
        ) {
            AdMobBannerView(
                adUnitId = stringResource(R.string.admob_key_bottom_banner),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }

    }
}







