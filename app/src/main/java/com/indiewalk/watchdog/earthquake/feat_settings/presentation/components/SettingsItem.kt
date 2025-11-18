package com.indiewalk.watchdog.earthquake.feat_settings.presentation.components

import android.R.attr.subtitle
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.theme.QuickSand
import com.indiewalk.watchdog.earthquake.core.presentation.theme.text_14
import com.indiewalk.watchdog.earthquake.core.presentation.theme.text_16

@Composable
fun SettingsItem(
    title: String,
    titleStyle: TextStyle = text_16(MaterialTheme.colorScheme.primary),
    subtitle: String,
    subTitleStyle: TextStyle = text_14(MaterialTheme.colorScheme.primary),
    isIconVisible: Boolean = true,
    @DrawableRes rightIcon: Int = R.drawable.ic_arrow_right,
    iconDescription: String = "",
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier // Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 0.dp, vertical = 6.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    fontFamily = QuickSand,
                    style = titleStyle,
                    maxLines = 3,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontFamily = QuickSand,
                    style = subTitleStyle,
                )

            }

            if (isIconVisible) {
                Icon(
                    painter = painterResource(id = rightIcon),
                    contentDescription = iconDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                )
            }

        }
    }
}


@Preview
@Composable
fun PreviewSettingsItem() {
    SettingsItem(
        title = "Title",
        subtitle = "Subtitle"
    )
}

@Preview
@Composable
fun PreviewSettingsItem_01() {
    SettingsItem(
        title = "Title adasdf asdfasdffff asdfa asfd asdf  asdf asdfasd fasdf a sdf",
        subtitle = "Subtitle"
    )
}