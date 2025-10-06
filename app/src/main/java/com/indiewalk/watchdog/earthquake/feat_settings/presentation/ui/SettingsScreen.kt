package com.indiewalk.watchdog.earthquake.feat_settings.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val TAG = "SettingsScreen"
    Log.d(TAG, "SettingsScreen on")
    ScaffoldModel(navController, title = "Settings") { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Settings Placeholder", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
        }
    }
}