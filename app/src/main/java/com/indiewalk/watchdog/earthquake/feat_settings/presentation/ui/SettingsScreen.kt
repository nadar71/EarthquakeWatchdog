package com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.android.ump.UserMessagingPlatform
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.Constants.support_email
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.NavigationRoutes
import com.indiewalk.watchdog.earthquake.core.presentation.preferences.AppPrefsViewModel
import com.indiewalk.watchdog.earthquake.core.presentation.theme.text_16
import com.indiewalk.watchdog.earthquake.core.util.GenericUtil.openAppStore
import com.indiewalk.watchdog.earthquake.core.util.sendEmail
import com.indiewalk.watchdog.earthquake.feat_ads.presentation.AdMobBannerView
import com.indiewalk.watchdog.earthquake.feat_ads.util.ConsentManager
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.openAppSettings
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.components.DisclaimerDialog
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    appPrefsViewModel: AppPrefsViewModel = hiltViewModel()
) {
    val TAG = "SettingsScreen"
    Log.d(TAG, "SettingsScreen on")
    val context = LocalContext.current
    val activity = LocalActivity.current

    var showDisclaimer by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val appPrefs by appPrefsViewModel.settings.collectAsStateWithLifecycle()

    ScaffoldModel(
        navController = navController,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.settings_title),
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

        val bannerHeight = 60.dp
        val topInset = padding.calculateTopPadding()
        val bottomInset = padding.calculateBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        start = 24.dp,
                        top = 24.dp,
                        end = 24.dp,
                        bottom = bottomInset + bannerHeight + 16.dp)
            ) {


                //  --- App settings ---
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_location_permissions_settings_label),
                        style = text_16(MaterialTheme.colorScheme.primary, false)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = stringResource(R.string.settings_location_permissions_settings_label),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = {
                                    openAppSettings(context)
                                }
                            )
                    )
                }

                // --- Unit System ---
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.settings_unit_system_title),
                    style = text_16(MaterialTheme.colorScheme.primary, false),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UnitSystem.entries.forEach { unitSystem ->
                        SegmentedButton(
                            selected = appPrefs.unitSystem == unitSystem,
                            onClick = { appPrefsViewModel.toggleUnitSystem(unitSystem) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = unitSystem.ordinal,
                                count = UnitSystem.entries.size
                            )
                        ) {
                            Text(
                                text = when (unitSystem) {
                                    UnitSystem.METRIC -> stringResource(R.string.settings_unit_system_metric)
                                    UnitSystem.IMPERIAL -> stringResource(R.string.settings_unit_system_imperial)
                                }
                            )
                        }
                    }
                }

                // --- Theme Mode Selector ---
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.settings_theme_mode_title),
                    style = text_16(MaterialTheme.colorScheme.primary, false),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ThemeMode.entries.forEach { themeMode ->
                        SegmentedButton(
                            selected = appPrefs.mode == themeMode,
                            onClick = { appPrefsViewModel.setThemeMode(themeMode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = themeMode.ordinal,
                                count = ThemeMode.entries.size
                            )
                        ) {
                            Text(
                                text = when (themeMode) {
                                    ThemeMode.System -> stringResource(R.string.theme_mode_system)
                                    ThemeMode.Light -> stringResource(R.string.theme_mode_light)
                                    ThemeMode.Dark -> stringResource(R.string.theme_mode_dark)
                                }
                            )
                        }
                    }
                }

                // --- Credits ---
                Spacer(modifier = Modifier.height(32.dp))
                SettingsItem(
                    title = stringResource(id = R.string.credits_title_label),
                    subtitle = stringResource(id = R.string.credits_section_subtitle_summary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(NavigationRoutes.CreditsScreen.route)
                        }
                )

                // --- GDPR ---
                Spacer(modifier = Modifier.height(32.dp))
                SettingsItem(
                    title = stringResource(id = R.string.settings_gdpr_btn_title),
                    subtitle = stringResource(id = R.string.gdpr_btn_summary),
                    modifier = Modifier.clickable {
                        UserMessagingPlatform.getConsentInformation(context).reset()
                        Toast.makeText(
                            context,
                            context.getString(R.string.gdpr_dialog_will_show_again),
                            Toast.LENGTH_LONG
                        ).show()
                        // Trigger re-consent
                        ConsentManager.requestConsent(
                            context = context,
                            activity = activity,
                            onConsentReady = { canRequestAds ->
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.gdpr_dialog_reset_done),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                )

                // --- Review ---
                Spacer(modifier = Modifier.height(32.dp))
                SettingsItem(
                    title = stringResource(id = R.string.settings_review_btn_title),
                    subtitle = stringResource(id = R.string.settings_review_btn_summary),
                    modifier = Modifier.clickable {
                        openAppStore(context, context.packageName)
                    }
                )

                // --- Support ---
                Spacer(modifier = Modifier.height(32.dp))
                SettingsItem(
                    title = stringResource(id = R.string.settings_support_btn_title),
                    subtitle = stringResource(id = R.string.settings_support_btn_summary)
                            + " " + support_email,
                    modifier = Modifier.clickable {
                        sendEmail(context, support_email)
                    }
                )

                // --- Disclaimer ---
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_disclaimer_label),
                        style = text_16(MaterialTheme.colorScheme.primary, false)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = stringResource(R.string.settings_disclaimer_label),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = {
                                    showDisclaimer = true
                                }
                            )
                    )
                }
            }


            if (showDisclaimer) {
                DisclaimerDialog(
                    onDismissRequest = { showDisclaimer = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 0.dp)
                )
            }

            AdMobBannerView(
                adUnitId = stringResource(R.string.admob_key_bottom_banner),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }




    }
}