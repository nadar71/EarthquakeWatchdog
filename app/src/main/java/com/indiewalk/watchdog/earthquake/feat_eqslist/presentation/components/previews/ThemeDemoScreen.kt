package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components.previews

import android.R.attr.onClick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.core.data.enums.ThemeMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDemoScreen(
    mode: ThemeMode,
    dynamic: Boolean,
    onModeChange: (ThemeMode) -> Unit,
    onDynamicChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Compose Theming (M3)")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Theme Mode",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            ModeSegmentedButtons(mode, onModeChange)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = dynamic, onCheckedChange = onDynamicChange)
                Spacer(Modifier.width(8.dp))
                Text("Dynamic color (Android 12+)")
            }

            Divider()

            ColorCard("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            ColorCard("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
            ColorCard("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
            ColorCard("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)

            Spacer(Modifier.height(8.dp))
            Button(onClick = {}) { Text("Primary Button") }
            OutlinedButton(onClick = {}) { Text("Outlined Button") }
            ElevatedButton(onClick = {}) { Text("Elevated Button") }
        }
    }
}

@Composable
private fun ModeSegmentedButtons(
    mode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow {
        SegmentedButton(
            selected = mode == ThemeMode.System,
            onClick = { onModeChange(ThemeMode.System) },
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
        ) { Text("System") }

        SegmentedButton(
            selected = mode == ThemeMode.Light,
            shape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
            onClick = { onModeChange(ThemeMode.Light) }
        ) {
            Text("Light")
        }

        SegmentedButton(
            selected = mode == ThemeMode.Dark,
            onClick = { onModeChange(ThemeMode.Dark) },
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        ) { Text("Dark") }
    }
}

@Composable
private fun ColorCard(label: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeDemoScreenPreview() {
    ThemeDemoScreen(
        mode = ThemeMode.System,
        dynamic = true,
        onModeChange = {},
        onDynamicChange = {}
    )
}