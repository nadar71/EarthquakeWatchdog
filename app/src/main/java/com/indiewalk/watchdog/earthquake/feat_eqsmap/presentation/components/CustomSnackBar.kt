package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.indiewalk.watchdog.earthquake.core.presentation.theme.text_16
import kotlinx.coroutines.launch
import com.indiewalk.watchdog.earthquake.R


@SuppressLint("RestrictedApi")
@Composable
fun SnackbarAlert(
    message: String,
    showSb: Boolean,
    backgroundColorIn: Color,
    messageColorIn: Color,
    openSnackbar: (Boolean) -> Unit,
    snackbarMsg: (String) -> Unit,
) {
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    SnackbarHost(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.Bottom),
        hostState = snackState,
        snackbar = { data ->
            MessageCardForSnackbar(
                snackbarData = data,
                backgroundColor = backgroundColorIn,
                strokeColor = backgroundColorIn,
                icon = painterResource(id = R.drawable.ic_xmark_white),
                messageColor = messageColorIn
            )
        }
    )

    if (showSb) {
        LaunchedEffect(Unit) {
            snackScope.launch {
                snackState.showSnackbar(
                    message = message,
                    // actionLabel = "OK",
                    duration = SnackbarDuration.Short
                )
                openSnackbar(false)
            }
            snackbarMsg(" ")
        }
    }
}


// it changes only the icon from SnackbarAlert
// can pass in remember because painterResource can be call only inside a composable
@SuppressLint("RestrictedApi")
@Composable
fun SnackbarConfirm(
    message: String,
    showSb: Boolean,
    backgroundColorIn: Color,
    messageColorIn: Color,
    openSnackbar: (Boolean) -> Unit,
    snackbarMsg: (String) -> Unit,
) {
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    SnackbarHost(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.Bottom),
        hostState = snackState,
        snackbar = { data ->
            MessageCardForSnackbar(
                snackbarData = data,
                backgroundColor = backgroundColorIn,
                strokeColor = backgroundColorIn,
                icon =  painterResource(id = R.drawable.ic_checkmark),
                messageColor = messageColorIn
            )
        }
    )

    if (showSb) {
        LaunchedEffect(Unit) {
            snackScope.launch {
                snackState.showSnackbar(
                    message = message,
                    // actionLabel = "OK",
                    duration = SnackbarDuration.Short
                )
                openSnackbar(false)
            }
            snackbarMsg(" ")
        }
    }
}


// customized content
@Composable
fun MessageCardForSnackbar(
    modifier: Modifier = Modifier,
    snackbarData: SnackbarData,
    cornerRadius: Dp = 10.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
    strokeColor: Color = Color.LightGray,
    icon: Painter,
    iconPadding: Dp = 16.dp,
    messageColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(
            width = strokeWidth,
            color = strokeColor
        )
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val (iconRef, textRef) = createRefs()

            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(iconRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .padding(iconPadding)
            )

            Text(
                text = snackbarData.visuals.message,
                style = text_16(messageColor, false),
                modifier = Modifier
                    .constrainAs(textRef) {
                        start.linkTo(iconRef.end, 16.dp)
                        end.linkTo(parent.end, 16.dp)
                        top.linkTo(parent.top, 16.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                        width = Dimension.fillToConstraints
                    }
            )
        }
    }
}




// ------------------------------ PREVIEWS ------------------------------
@Preview(showBackground = true)
@Composable
fun SnackbarAlert_Preview_01() {
    // val context = LocalContext.current // Get the context

    val backgroundColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onPrimary
    val backgroundColorAlert = MaterialTheme.colorScheme.errorContainer
    val textColorAlert = MaterialTheme.colorScheme.onErrorContainer

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarBackgroundColorIn  by remember { mutableStateOf(backgroundColor) }
    var snackbarMessageColorIn  by remember { mutableStateOf(textColor) }

    SnackbarAlert(
        message = snackbarMessage,
        showSb = showSnackbar,
        backgroundColorIn = snackbarBackgroundColorIn,
        messageColorIn = snackbarMessageColorIn,
        openSnackbar = { showSnackbar = it },
        snackbarMsg = { snackbarMessage = it }
    )

    Button(onClick = {
        showSnackbar = true
        snackbarBackgroundColorIn = backgroundColorAlert
        snackbarMessageColorIn = textColorAlert
        snackbarMessage = "L'indirizzo e-mail è già utilizzato da un altro account."
    }) {
        Text("Show Snackbar")
    }
}




@Preview(showBackground = true)
@Composable
fun SnackbarConfirm_Preview() {

    val backgroundColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onPrimary
    val backgroundColorAlert = MaterialTheme.colorScheme.errorContainer
    val textColorAlert = MaterialTheme.colorScheme.onErrorContainer

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarBackgroundColorIn  by remember { mutableStateOf(backgroundColor) }
    var snackbarMessageColorIn  by remember { mutableStateOf(textColor) }

    SnackbarConfirm(
        message = snackbarMessage,
        showSb = showSnackbar,
        backgroundColorIn = snackbarBackgroundColorIn,
        messageColorIn = snackbarMessageColorIn,
        openSnackbar = { showSnackbar = it },
        snackbarMsg = { snackbarMessage = it }
    )

    Button(onClick = {
        showSnackbar = true
        snackbarBackgroundColorIn = backgroundColorAlert
        snackbarMessageColorIn = textColorAlert
        snackbarMessage = "Tutto ok."
    }) {
        Text("Show Snackbar")
    }
}







