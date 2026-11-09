package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NatureParticleCanvas(
    modifier: Modifier = Modifier,
    particleColor: Color = Color(0xFF81C784),
    petalCount: Int = 12
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nature_atmosphere")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_loop"
    )

    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Subtle ambient sunlight shimmer from top right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF9C4).copy(alpha = 0.12f * sunPulse),
                    Color(0xFFFFE082).copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(w * 0.9f, h * 0.12f),
                radius = w * 0.65f * sunPulse
            )
        )

        // Floating leaves / petals
        for (i in 0 until petalCount) {
            val seedX = ((i * 137 + 29) % 100) / 100f
            val seedY = ((i * 89 + 17) % 100) / 100f
            val speedFactor = 0.8f + (i % 5) * 0.15f

            val yNorm = (seedY + progress * speedFactor) % 1.0f
            val py = yNorm * (h + 80f) - 40f
            val px = (seedX * w + sin((py * 0.02f + i).toDouble()).toFloat() * 32f) % w

            val leafSize = 9f + (i % 4) * 3f
            val angle = (progress * 360f + i * 45f) % 360f

            drawFallingLeaf(
                Offset(px, py),
                leafSize,
                angle,
                particleColor.copy(alpha = 0.28f + (i % 3) * 0.12f)
            )
        }
    }
}

private fun DrawScope.drawFallingLeaf(center: Offset, size: Float, angleDegrees: Float, color: Color) {
    val rad = Math.toRadians(angleDegrees.toDouble())
    val cosA = cos(rad).toFloat()
    val sinA = sin(rad).toFloat()

    val path = Path().apply {
        val p1x = center.x - size * cosA
        val p1y = center.y - size * sinA
        val p2x = center.x + size * cosA
        val p2y = center.y + size * sinA

        val cp1x = center.x + (size * 0.7f) * (-sinA)
        val cp1y = center.y + (size * 0.7f) * (cosA)
        val cp2x = center.x - (size * 0.7f) * (-sinA)
        val cp2y = center.y - (size * 0.7f) * (cosA)

        moveTo(p1x, p1y)
        quadraticTo(cp1x, cp1y, p2x, p2y)
        quadraticTo(cp2x, cp2y, p1x, p1y)
        close()
    }
    drawPath(path, color)
}
