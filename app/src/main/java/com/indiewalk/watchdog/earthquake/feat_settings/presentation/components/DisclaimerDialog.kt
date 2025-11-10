package com.indiewalk.watchdog.earthquake.feat_settings.presentation.components

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.indiewalk.watchdog.earthquake.R
import org.xml.sax.XMLReader


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


/*@Composable
fun DisclaimerDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val htmlText = stringResource(id = R.string.settings_faq_text)
    val spannedText = remember(htmlText) {
        HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY, null, object : Html.TagHandler {
            // This is needed to handle custom tags if needed
            override fun handleTag(
                p0: Boolean,
                p1: String?,
                p2: Editable?,
                p3: XMLReader?
            ) {
                TODO("Not yet implemented")
            }
        }, object : Html.ImageGetter {
            override fun getDrawable(source: String?): Drawable? = null
        })
    }

    val linkMovementMethod = remember { LinkMovementMethod.getInstance() }
    val textView = remember { TextView(context) }

    // Handle link clicks
    val clickableText = remember(spannedText) {
        val spannable = SpannableStringBuilder(spannedText)
        val urls = spannable.getSpans(0, spannable.length, URLSpan::class.java)

        urls.forEach { urlSpan ->
            val start = spannable.getSpanStart(urlSpan)
            val end = spannable.getSpanEnd(urlSpan)
            val flags = spannable.getSpanFlags(urlSpan)
            val url = urlSpan.url

            spannable.removeSpan(urlSpan)
            spannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        try {
                            val intent = when {
                                url.startsWith("http") -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                url.startsWith("mailto:") -> Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse(url)
                                }
                                else -> null
                            }
                            intent?.let {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                start,
                end,
                flags
            )
        }
        spannable
    }

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
            AndroidView(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                factory = { ctx ->
                    TextView(ctx).apply {
                        movementMethod = linkMovementMethod
                        text = clickableText
                        setTextIsSelectable(true)
                        setLinkTextColor(ContextCompat.getColor(context, R.color.primary))
                        setTextColor(android.graphics.Color.BLACK)
                    }
                }
            )
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
            .fillMaxWidth(0.9f)
    )
}*/

/*
@Composable
fun DisclaimerDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val htmlText = stringResource(id = R.string.settings_faq_text)
    val spannedText = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)

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

*/
