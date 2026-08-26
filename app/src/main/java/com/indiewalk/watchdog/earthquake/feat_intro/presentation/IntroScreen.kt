package com.indiewalk.watchdog.earthquake.feat_intro.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.feat_eqsmap.util.MapsUtils.openAppSettings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun IntroScreen_01(
    onContinueToHome: () -> Unit,
    introViewModel: IntroViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by introViewModel.uiState.collectAsStateWithLifecycle()

    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val allGranted by remember(permissions) { derivedStateOf { permissions.allPermissionsGranted } }

    val fineLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = introViewModel::onPermissionResult
    )

    LaunchedEffect(allGranted) {
        introViewModel.onPermissionStateChanged(allGranted)
    }

    LaunchedEffect(Unit) {
        introViewModel.effects.collect { effect ->
            when (effect) {
                IntroEffect.NavigateHome -> onContinueToHome()
                IntroEffect.OpenAppSettings -> openAppSettings(context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.intro_welcome),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (!allGranted && !uiState.askedOnce) {
            IntroLocationPermissionContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                isPermissionRequestStarted = uiState.isPermissionRequestStarted,
                onRequestLocation = {
                    introViewModel.onPermissionRequestStarted()
                    fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onContinueToHome = onContinueToHome
            )
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.intro_loading_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (uiState.showDeniedDialog) {
        AlertDialog(
            onDismissRequest = introViewModel::onDeniedDialogDismissed,
            title = { Text(text = stringResource(R.string.intro_permission_denied)) },
            text = { Text(text = stringResource(R.string.intro_enable_manual_position)) },
            confirmButton = {
                TextButton(onClick = introViewModel::onOpenSettingsRequested) {
                    Text(stringResource(R.string.intro_open_app_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = introViewModel::onDeniedDialogDismissed) {
                    Text(stringResource(R.string.generic_ok))
                }
            }
        )
    }
}

@Composable
fun IntroLocationPermissionContent(
    isPermissionRequestStarted: Boolean,
    onRequestLocation: () -> Unit,
    onContinueToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val fontScale = LocalDensity.current.fontScale
        val compactLayout = maxHeight < 760.dp || fontScale >= 1.3f
        val largeTextLayout = fontScale >= 1.5f
        val scrollState = rememberScrollState()
        val compactSpacing = if (compactLayout) 8.dp else 16.dp
        val sectionSpacing = if (compactLayout) 12.dp else 28.dp
        val imageMaxHeight = when {
            largeTextLayout -> maxHeight * 0.18f
            compactLayout -> maxHeight * 0.30f
            else -> maxHeight * 0.38f
        }
        val verticalPadding = if (compactLayout) 12.dp else 20.dp

        Column(
            Modifier
                .fillMaxSize()
                .then(
                    if (largeTextLayout) Modifier.verticalScroll(scrollState) else Modifier
                )
                .testTag("intro-location-permission-content")
                .padding(horizontal = 24.dp, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.intro_location_request_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(sectionSpacing))
            Image(
                painter = painterResource(id = R.drawable.map_img_permissions_req),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = imageMaxHeight)
            )

            Spacer(Modifier.height(compactSpacing))
            Text(
                text = stringResource(R.string.intro_localization_permission_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (largeTextLayout) {
                Spacer(Modifier.height(compactSpacing))
            } else {
                Spacer(Modifier.weight(1f))
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("intro-enable-location"),
                onClick = onRequestLocation,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(stringResource(R.string.intro_enable_localization))
            }

            Spacer(Modifier.height(compactSpacing))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("intro-continue"),
                onClick = onContinueToHome,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    stringResource(
                        if (!isPermissionRequestStarted) R.string.intro_skip else R.string.intro_allow
                    )
                )
            }
        }
    }
}
