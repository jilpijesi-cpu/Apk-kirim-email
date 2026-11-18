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
val ClaudeTerracotta = Color(0xFFCC5A36)
val ClaudeWarmBg = Color(0xFFFAF9F5)
val EmeraldOnline = Color(0xFF059669)
val RoseError = Color(0xFFE11D48)
val SlateSurface = Color(0xFFF8FAFC)
val SlateBorder = Color(0xFFE2E8F0)

// Exact gradient matching App.tsx: bg-gradient-to-b from-mandiri-blue-400 via-mandiri-blue-50 to-mandiri-gray-bg
val AppSkyGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF88C8FA),
        Color(0xFFD5ECFF),
        Color(0xFFF5F9FE),
        Color(0xFFFFFFFF)
    )
)

// Exact AI tab gradient matching App.tsx: linear-gradient(180deg, #FAF9F5 0%, #F4F7FB 35%, #D7EBFD 70%, #9BCFFA 100%)
val AiMythosGradient = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFAF9F5),
        0.35f to Color(0xFFF4F7FB),
        0.70f to Color(0xFFD7EBFD),
        1.0f to Color(0xFF9BCFFA)
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
