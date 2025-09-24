package eu.indiewalkabout.fridgemanager.core.presentation.theme

import android.os.Build
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView


@Composable
fun EQWatchdogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    // Edge-to-edge Config
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? androidx.activity.ComponentActivity
        activity?.enableEdgeToEdge(
            statusBarStyle = if (darkTheme) {
                SystemBarStyle.dark(colorScheme.surface.toArgb())
            } else {
                SystemBarStyle.light(colorScheme.surface.toArgb(), colorScheme.onSurface.toArgb())
            },
            navigationBarStyle = if (darkTheme) {
                SystemBarStyle.dark(colorScheme.surface.toArgb())
            } else {
                SystemBarStyle.light(colorScheme.surface.toArgb(), colorScheme.onSurface.toArgb())
            }
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
















