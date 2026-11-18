package com.gswift.relay.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClaudeStarburstLogo(
    modifier: Modifier = Modifier,
    color: Color = ClaudeTerracotta,
    animated: Boolean = true
) {
    val rotation = if (animated) {
        val transition = rememberInfiniteTransition(label = "claude-spin")
        val angle by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "angle"
        )
        angle
    } else {
        0f
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.45f
        val stroke = size.minDimension * 0.13f

        rotate(degrees = rotation, pivot = center) {
            for (i in 0 until 12) {
                val rad = Math.toRadians((i * 30.0))
                val inner = radius * 0.22f
                val outer = if (i % 2 == 0) radius else radius * 0.82f
                val start = Offset(
                    x = center.x + inner * cos(rad).toFloat(),
                    y = center.y + inner * sin(rad).toFloat()
                )
                val end = Offset(
                    x = center.x + outer * cos(rad).toFloat(),
                    y = center.y + outer * sin(rad).toFloat()
                )
                drawLine(
                    color = color,
                    start = start,
                    end = end,
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
