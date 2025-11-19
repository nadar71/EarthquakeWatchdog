package com.indiewalk.watchdog.earthquake.feat_settings.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.core.presentation.theme.QuickSand
import com.indiewalk.watchdog.earthquake.core.presentation.theme.text_16

@Composable
fun SettingsGroupTitle(
    title: String,
    style: TextStyle = text_16(MaterialTheme.colorScheme.primary)
) {
    Text(
        text = title,
        fontFamily = QuickSand,
        style = style,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}