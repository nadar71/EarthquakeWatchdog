package com.indiewalk.watchdog.earthquake.feat_settings.presentation.components

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.indiewalk.watchdog.earthquake.R


@Composable
fun DisclaimerDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val htmlText = stringResource(id = R.string.settings_faq_text)
    val spannedText = remember(htmlText) {
        HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    val onSurfaceArgb = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkArgb = MaterialTheme.colorScheme.primary.toArgb()

    AlertDialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        ),
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(id = R.string.settings_disclaimer_label),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            // Scroll container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Use a TextView so URLSpan/mailto works out-of-the-box
                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                            // Set the font family to Quicksand
                            val typeface = ResourcesCompat.getFont(context, R.font.quicksand_regular)
                            setTypeface(typeface)

                            // Render HTML
                            text = spannedText
                            // Make <a href> clickable (http/https/mailto/tel…)
                            movementMethod = LinkMovementMethod.getInstance()
                            linksClickable = true

                            // Optional cosmetics
                            setTextIsSelectable(false)
                            highlightColor = android.graphics.Color.TRANSPARENT
                            setTextColor(onSurfaceArgb)
                            setLinkTextColor(linkArgb)
                            textSize = 16f
                        }
                    },
                    update = { view ->
                        view.text = spannedText
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        modifier = modifier
    )
}
