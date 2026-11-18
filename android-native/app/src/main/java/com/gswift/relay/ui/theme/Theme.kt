package com.gswift.relay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NavyPrimary = Color(0xFF003A8F)
val NavyDark = Color(0xFF001D47)
val NavyContainer = Color(0xFFD8E6FF)
val GoldAccent = Color(0xFFF2A900)
val ClaudeTerracotta = Color(0xFFD97757)
val ClaudeWarmBg = Color(0xFFFAF9F5)
val EmeraldOnline = Color(0xFF059669)
val RoseError = Color(0xFFE11D48)
val SlateSurface = Color(0xFFF8FAFC)
val SlateBorder = Color(0xFFE2E8F0)

val AppSkyGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFBEE0FF),
        Color(0xFFDCEEFF),
        Color(0xFFF6FAFF),
        Color(0xFFFFFFFF)
    )
)

private val GSwiftLightColors = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyContainer,
    onPrimaryContainer = NavyDark,
    secondary = GoldAccent,
    onSecondary = Color(0xFF1E1B18),
    tertiary = ClaudeTerracotta,
    onTertiary = Color.White,
    background = SlateSurface,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = SlateBorder,
    error = RoseError,
    onError = Color.White
)

private val GSwiftTypography = Typography(
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.3).sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp
    )
)

@Composable
fun GSwiftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GSwiftLightColors,
        typography = GSwiftTypography,
        content = content
    )
}
