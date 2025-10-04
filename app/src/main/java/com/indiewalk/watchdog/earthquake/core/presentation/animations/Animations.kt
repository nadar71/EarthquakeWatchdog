package com.indiewalk.watchdog.earthquake.core.presentation.animations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.indiewalk.watchdog.earthquake.R

@Composable
fun LogoAnimationForward(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    frameDurationMs: Long = 120L // adjust speed here
) {
    val frames = listOf(
        /*R.drawable.logo_frame_001,
        R.drawable.logo_frame_002,
        R.drawable.logo_frame_003,
        R.drawable.logo_frame_004,
        R.drawable.logo_frame_005,
        R.drawable.logo_frame_006,
        R.drawable.logo_frame_007,
        R.drawable.logo_frame_008,
        R.drawable.logo_frame_009,
        R.drawable.logo_frame_010,*/
        R.drawable.logo_frame_011,
        R.drawable.logo_frame_012,
        R.drawable.logo_frame_013,
        R.drawable.logo_frame_014,
        R.drawable.logo_frame_015,
        R.drawable.logo_frame_016,
        R.drawable.logo_frame_017,
        R.drawable.logo_frame_018,
        R.drawable.logo_frame_019,
        R.drawable.logo_frame_020,
        R.drawable.logo_frame_021,
        R.drawable.logo_frame_022,
        R.drawable.logo_frame_023,
        R.drawable.logo_frame_024,
        R.drawable.logo_frame_025,
        R.drawable.logo_frame_026,
        R.drawable.logo_frame_027,
        R.drawable.logo_frame_028,
        R.drawable.logo_frame_029,
        R.drawable.logo_frame_030,
        R.drawable.logo_frame_031,
        R.drawable.logo_frame_032,
        R.drawable.logo_frame_033,
        R.drawable.logo_frame_034,
        R.drawable.logo_frame_035,
        R.drawable.logo_frame_036,
        R.drawable.logo_frame_037,
        R.drawable.logo_frame_038,
        R.drawable.logo_frame_039,
        R.drawable.logo_frame_040,
        R.drawable.logo_frame_041,
        R.drawable.logo_frame_042,/*
        R.drawable.logo_frame_042,
        R.drawable.logo_frame_042,
        R.drawable.logo_frame_042,
        R.drawable.logo_frame_042,*/
        R.drawable.logo_frame_043a,
        R.drawable.logo_frame_044a,
        R.drawable.logo_frame_045a,
        R.drawable.logo_frame_046a,
        R.drawable.logo_frame_047a
    )

    var currentFrame by remember { mutableStateOf(0) }
    var currentAlpha by remember { mutableStateOf(0.3f) }
    var signCoeff by remember { mutableStateOf(1) }
    val half = (frames.size/2).toInt()

    LaunchedEffect(Unit) {
        while (true) {
            delay(frameDurationMs)
            currentFrame = (currentFrame + 1) % frames.size
            signCoeff = if (currentFrame <= half)  1
                        else if (currentFrame > half)  -1
                        else signCoeff

            currentAlpha = currentAlpha + signCoeff*0.05f

        }
    }

    Image(
        painter = painterResource(id = frames[currentFrame]),
        contentDescription = "Frame animation",
        modifier = modifier
            // .fillMaxWidth()
            .size(size)
            .aspectRatio(1f) // keeps square ratio, adjust as needed
            .padding(8.dp)
            .alpha(currentAlpha)
            )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF202E37)
@Composable
fun PreviewLogoAnimationForward() {
    Box(modifier = Modifier
        .size(200.dp)
    ) {
        LogoAnimationForward(
            modifier = Modifier
                .padding(16.dp)
        )
    }
}


@Composable
fun LogoPingPongAnimation(
    modifier: Modifier = Modifier,
    frameDurationMs: Long = 80L // uniform speed
) {
    val frames = listOf(
        R.drawable.logo_frame_001,
        R.drawable.logo_frame_002,
        R.drawable.logo_frame_003,
        R.drawable.logo_frame_004,
        R.drawable.logo_frame_005,
        R.drawable.logo_frame_006,
        R.drawable.logo_frame_007,
        R.drawable.logo_frame_008,
        R.drawable.logo_frame_009,
        R.drawable.logo_frame_010,
        R.drawable.logo_frame_011,
        R.drawable.logo_frame_012,
        R.drawable.logo_frame_013,
        R.drawable.logo_frame_014,
        R.drawable.logo_frame_015,
        R.drawable.logo_frame_016,
        R.drawable.logo_frame_017,
        R.drawable.logo_frame_018,
        R.drawable.logo_frame_019,
        R.drawable.logo_frame_020,
        R.drawable.logo_frame_021,
        R.drawable.logo_frame_022,
        R.drawable.logo_frame_023,
        R.drawable.logo_frame_024,
        R.drawable.logo_frame_025,
        R.drawable.logo_frame_026,
        R.drawable.logo_frame_027,
        R.drawable.logo_frame_028,
        R.drawable.logo_frame_029,
        R.drawable.logo_frame_030,
        R.drawable.logo_frame_031,
        R.drawable.logo_frame_032,
        R.drawable.logo_frame_033,
        R.drawable.logo_frame_034,
        R.drawable.logo_frame_035,
        R.drawable.logo_frame_036,
        R.drawable.logo_frame_037,
        R.drawable.logo_frame_038,
        R.drawable.logo_frame_039,
        R.drawable.logo_frame_040,
        R.drawable.logo_frame_041,
        R.drawable.logo_frame_042,
        R.drawable.logo_frame_043,
        R.drawable.logo_frame_044,
        R.drawable.logo_frame_045,
        R.drawable.logo_frame_046,
        R.drawable.logo_frame_047
    )

    var currentFrame by remember { mutableStateOf(0) }
    var direction by remember { mutableStateOf(1) } // +1 = forward, -1 = backward

    LaunchedEffect(Unit) {
        while (true) {
            delay(frameDurationMs)
            currentFrame += direction

            if (currentFrame >= frames.lastIndex) {
                currentFrame = frames.lastIndex
                direction = -1
            } else if (currentFrame <= 0) {
                currentFrame = 0
                direction = 1
            }
        }
    }

    Image(
        painter = painterResource(id = frames[currentFrame]),
        contentDescription = "Ping-pong animation",
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewPingPongAnimation() {
    Box(modifier = Modifier.size(200.dp)) {
        LogoPingPongAnimation()
    }
}