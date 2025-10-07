package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.db.EQEntity

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val TAG = "MapScreen"
    Log.d(TAG, "MapScreen on")
    ScaffoldModel(navController, title = "Map") { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Map placeholder", style = MaterialTheme.typography.titleLarge)
        }
    }
}*/

@Composable
fun MapScreen(navController: NavHostController) {
    EarthquakeMapScreen(
        navController = navController,
        onLocationGranted = { /* TODO: enable follow-my-location, etc. */ },
        onLocationDenied = { /* TODO: show a snackbar/toast explaining features are limited */ }
    )
}
