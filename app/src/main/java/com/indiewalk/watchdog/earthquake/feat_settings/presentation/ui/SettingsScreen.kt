package com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.enums.ThemeMode
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.preferences.AppPrefsViewModel
import com.indiewalk.watchdog.earthquake.core.presentation.theme.text_16
import com.indiewalk.watchdog.earthquake.feat_ads.presentation.AdMobBannerView
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.openAppSettings
import com.indiewalk.watchdog.earthquake.feat_settings.presentation.components.DisclaimerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    appPrefsViewModel: AppPrefsViewModel = hiltViewModel()
) {
    val TAG = "SettingsScreen"
    Log.d(TAG, "SettingsScreen on")
    val context = LocalContext.current

    var showDisclaimer by remember { mutableStateOf(false) }

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

        val topInset = padding.calculateTopPadding()
        val bottomInset = padding.calculateBottomPadding()


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, top = topInset + 32.dp, end = 24.dp, bottom = 0.dp )
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
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = stringResource(R.string.settings_location_permissions_settings_label),
                    modifier = Modifier
                        .size(18.dp)
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
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = stringResource(R.string.settings_disclaimer_label),
                    modifier = Modifier
                        .size(18.dp)
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