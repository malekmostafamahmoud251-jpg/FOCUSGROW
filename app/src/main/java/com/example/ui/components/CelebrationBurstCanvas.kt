package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CelebrationBurstCanvas(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "celebration_burst")
    val burstProg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "burst"
    )

    val particles = 28
    val colors = listOf(
        Color(0xFFFFD54F),
        Color(0xFF81C784),
        Color(0xFF4CAF50),
        Color(0xFFFFB300),
        Color(0xFF80DEEA)
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = size.minDimension * 0.45f

        for (i in 0 until particles) {
            val angle = (i * (360f / particles)) + (i * 13f)
            val rad = Math.toRadians(angle.toDouble())
            val distance = burstProg * maxRadius * (0.6f + (i % 5) * 0.1f)
            val px = cx + (cos(rad) * distance).toFloat()
            val py = cy + (sin(rad) * distance).toFloat()

            val alpha = (1f - burstProg).coerceIn(0f, 1f)
            val color = colors[i % colors.size].copy(alpha = alpha)
            val pSize = (1f - burstProg * 0.5f) * (6f + (i % 3) * 3f)

            drawCircle(
                color = color,
                radius = pSize,
                center = Offset(px, py)
            )
        }
    }
}
