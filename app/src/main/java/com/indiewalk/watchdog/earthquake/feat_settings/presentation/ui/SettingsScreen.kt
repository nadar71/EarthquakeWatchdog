package com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.data.local.enums.UnitSystem
import com.indiewalk.watchdog.earthquake.core.presentation.animations.LogoAnimationForward
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.preferences.AppPrefsViewModel
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
                        modifier = Modifier
                            .padding(start = 8.dp),
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,       // background
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,  // title text
                    )
            )
        },
    ) { padding ->

        val bottomInset = padding.calculateBottomPadding()
        val topInset = padding.calculateTopPadding()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = topInset + 32.dp, end = 16.dp,  )
        ) {
            Text(
                text = stringResource(R.string.settings_unit_system_title),
                style = MaterialTheme.typography.bodyMedium,
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

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_location_permissions_settings_label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = stringResource(R.string.settings_location_permissions_settings_label),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            onClick = {
                                openAppSettings(context)
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_disclaimer_label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = stringResource(R.string.settings_disclaimer_label),
                    modifier = Modifier
                        .size(20.dp)
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
    }
}