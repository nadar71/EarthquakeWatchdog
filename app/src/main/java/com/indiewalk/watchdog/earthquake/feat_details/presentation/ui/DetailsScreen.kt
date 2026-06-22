package com.indiewalk.watchdog.earthquake.feat_details.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.indiewalk.watchdog.earthquake.core.presentation.components.ScaffoldModel
import com.indiewalk.watchdog.earthquake.core.presentation.navigation.AppDestination


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    currentDestination: AppDestination,
    id: String,
    onBack: () -> Unit
) {
    val TAG = "DetailsScreen"
    Log.d(TAG, "DetailsScreen on")
    ScaffoldModel(
        currentDestination = currentDestination,
        onBack = onBack,
        title = "Details"
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Event ID:", style = MaterialTheme.typography.labelLarge)
            Text(id, style = MaterialTheme.typography.titleLarge)
        }
    }
}
