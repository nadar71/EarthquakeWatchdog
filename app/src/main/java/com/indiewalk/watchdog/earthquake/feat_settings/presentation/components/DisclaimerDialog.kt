package com.indiewalk.watchdog.earthquake.feat_settings.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.indiewalk.watchdog.earthquake.R

@Composable
fun DisclaimerDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val htmlText = stringResource(id = R.string.settings_faq_text)
    val spannedText = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    
    AlertDialog(
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = spannedText.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
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

/*
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val htmlContent = remember(html) { 
        HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) 
    }
    
    Text(
        text = htmlContent,
        modifier = modifier
    )
}
*/
