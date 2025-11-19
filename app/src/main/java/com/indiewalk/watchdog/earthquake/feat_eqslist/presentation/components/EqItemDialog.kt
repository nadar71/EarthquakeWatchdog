package com.indiewalk.watchdog.earthquake.feat_eqslist.presentation.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.indiewalk.watchdog.earthquake.R
import com.indiewalk.watchdog.earthquake.core.presentation.theme.text_16
import com.indiewalk.watchdog.earthquake.core.util.GenericUtil.openUrlInBrowserNotCompose
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.model.EarthquakeUI


@Composable
fun EqItemDialog(
    eq: EarthquakeUI,
    onMapClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val TAG = "EqItemDialog"
    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            onDismiss()
        })
    {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                // goto eq map position
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            onMapClick()
                            onDismiss()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_planet),
                        contentDescription = stringResource(id = R.string.home_dialog_goto_map_label),
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.home_dialog_goto_map_label),
                        style = text_16(MaterialTheme.colorScheme.primary, false)
                    )
                }

                // Eq details on USGS site
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            eq.url?.let { url ->
                                openUrlInBrowserNotCompose(context, url)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info_outline_white_50dp),
                        contentDescription = stringResource(id = R.string.home_dialog_eq_details_label),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.home_dialog_eq_details_label),
                        style = text_16(MaterialTheme.colorScheme.primary, false)
                    )
                }

                // goto Feel it report
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            eq.url?.let { url ->
                                openUrlInBrowserNotCompose(context, "$url/tellus")
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_danger),
                        contentDescription = stringResource(id = R.string.home_dialog_eq_feelit_label),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.home_dialog_eq_feelit_label),
                        style = text_16(MaterialTheme.colorScheme.primary, false)
                    )
                }

            }
        }
    }


}
