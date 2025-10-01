package com.indiewalk.watchdog.earthquake.core.presentation.theme


import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.indiewalk.watchdog.earthquake.R


// fonts
// Font families

val QuickSand = FontFamily(
    Font(R.font.quicksand_regular, FontWeight.Normal),
    Font(R.font.quicksand_medium, FontWeight.Medium),
    Font(R.font.quicksand_bold, FontWeight.Bold),
    Font(R.font.quicksand_semibold, FontWeight.SemiBold),
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.Medium,  // W500 is roughly equivalent to Medium
        fontSize = 14.sp
    ),
    titleLarge = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)







// text styles
@Composable
fun text_26(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 26.sp
)

@Composable
fun text_24(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 24.sp
)

@Composable
fun text_20(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 20.sp
)

@Composable
fun text_18(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 18.sp
)

@Composable
fun text_17(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 17.sp
)

@Composable
fun text_16(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 16.sp
)

@Composable
fun text_15(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 15.sp
)

@Composable
fun text_14(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 14.sp
)

@Composable
fun text_13(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 13.sp
)

@Composable
fun text_12(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 12.sp
)

@Composable
fun text_8(color: Color, isBold: Boolean = false): TextStyle = TextStyle(
    color = color,
    fontFamily = QuickSand,
    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
    fontSize = 8.sp
)







