package com.indiewalk.watchdog.earthquake.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.indiewalk.watchdog.earthquake.R
import kotlin.apply


fun sendEmail(context: Context, receiver: String) {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(receiver))
    }

    if (emailIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(Intent.createChooser(emailIntent,
            context.getString(R.string.settings_send_email_chooser_title)))
    } else {
        Toast.makeText(context, context.getString(R.string.settings_support_choose_email_client_fallback_label),
            Toast.LENGTH_SHORT).show()
    }
}

