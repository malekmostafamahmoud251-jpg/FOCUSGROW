package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.data.model.ForestEnvironmentType
import kotlin.math.cos
import kotlin.math.sin

private data class LeafParticle(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val size: Float,
    val fallSpeed: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float,
    val rotationSpeed: Float,
    val colorIndex: Int
)

private data class PollenParticle(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val radius: Float,
    val floatSpeed: Float,
    val horizontalDrift: Float,
    val twinklePhase: Float,
    val colorIndex: Int
)

@Composable
fun ForestParticleSystem(
    environment: ForestEnvironmentType,
    modifier: Modifier = Modifier,
    leafCount: Int = 18,
    pollenCount: Int = 26
) {
    val infiniteTransition = rememberInfiniteTransition(label = "forest_particles")

    // Continuous time drivers for vertical flow and wind gusts
    val timeProg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val windGust by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wind_gust"
    )

    val pollenTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pollen_twinkle"
    )

    // Pre-calculate deterministic particle seeds
    val leaves = remember(leafCount) {
        List(leafCount) { i ->
            val seed = i * 47 + 13
            LeafParticle(
                initialXRatio = (seed % 100) / 100f,
                initialYRatio = ((seed * 7) % 100) / 100f,
                size = 10f + (i % 5) * 3.5f,
                fallSpeed = 0.7f + (i % 4) * 0.25f,
                swayAmplitude = 22f + (i % 3) * 14f,
                swayFrequency = 1.2f + (i % 3) * 0.8f,
                rotationSpeed = 180f + (i % 4) * 90f,
                colorIndex = i % 3
            )
        }
    }

    val pollens = remember(pollenCount) {
        List(pollenCount) { i ->
            val seed = i * 83 + 31
            PollenParticle(
                initialXRatio = (seed % 100) / 100f,
                initialYRatio = ((seed * 11) % 100) / 100f,
                radius = 2.2f + (i % 4) * 1.2f,
                floatSpeed = 0.35f + (i % 3) * 0.2f,
                horizontalDrift = 15f + (i % 5) * 8f,
                twinklePhase = (i * 0.25f),
                colorIndex = i % 3
            )
        }
    }

    // Color palette tailored to the current forest environment
    val (leafColors, pollenColors) = remember(environment) {
        when (environment) {
            ForestEnvironmentType.CHERRY_VALLEY -> {
                listOf(Color(0xFFF48FB1), Color(0xFFF8BBD0), Color(0xFFF06292)) to
                listOf(Color(0xFFFFF0F5), Color(0xFFFFD1DC), Color(0xFFFFF8E7))
            }
            ForestEnvironmentType.AMBER_RIDGE -> {
                listOf(Color(0xFFFF7043), Color(0xFFFFA726), Color(0xFFD84315)) to
                listOf(Color(0xFFFFE082), Color(0xFFFFD54F), Color(0xFFFFCC80))
            }
            ForestEnvironmentType.FROST_PEAK -> {
                listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFFFFFFFF)) to
                listOf(Color(0xFFFFFFFF), Color(0xFFE1F5FE), Color(0xFFB3E5FC))
            }
            ForestEnvironmentType.KYOTO_GARDEN -> {
                listOf(Color(0xFF81C784), Color(0xFFF8BBD0), Color(0xFF66BB6A)) to
                listOf(Color(0xFFFFF9C4), Color(0xFFE8F5E9), Color(0xFFFFECB3))
            }
            ForestEnvironmentType.FIREFLY_SANCTUARY -> {
                listOf(Color(0xFF334E40), Color(0xFF263E32), Color(0xFF1E3328)) to
                listOf(Color(0xFFFFF59D), Color(0xFFEEFF41), Color(0xFFC6FF00))
            }
            ForestEnvironmentType.VERDANT_MEADOW -> {
                listOf(Color(0xFF66BB6A), Color(0xFF81C784), Color(0xFF4CAF50)) to
                listOf(Color(0xFFFFF9C4), Color(0xFFFFE082), Color(0xFFDCEDC8))
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // 1. Draw Floating Pollen & Spores (Gentle buoyant drift upwards and sideways)
        pollens.forEach { p ->
            // Pollen floats gently upward / drifting
            val progressY = (p.initialYRatio - (timeProg * p.floatSpeed))
            val normY = (progressY % 1.0f + 1.0f) % 1.0f
            val py = normY * (h + 40f) - 20f

            val waveX = sin((timeProg * Math.PI * 2 * 1.5 + p.initialXRatio * 10).toDouble()).toFloat()
            val px = (p.initialXRatio * w + waveX * p.horizontalDrift + windGust * 12f) % w
            val finalX = if (px < 0f) px + w else px

            val alphaOsc = (sin((pollenTwinkle + p.twinklePhase).toDouble()).toFloat() + 1.2f) * 0.4f
            val isNight = environment == ForestEnvironmentType.FIREFLY_SANCTUARY
            val baseAlpha = if (isNight) 0.85f else 0.55f
            val finalAlpha = (baseAlpha * alphaOsc).coerceIn(0.15f, 0.95f)
            val pColor = pollenColors[p.colorIndex].copy(alpha = finalAlpha)

            // Outer soft halo
            drawCircle(
                color = pColor.copy(alpha = finalAlpha * 0.35f),
                radius = p.radius * 2.2f,
                center = Offset(finalX, py)
            )
            // Inner bright core
            drawCircle(
                color = pColor,
                radius = p.radius,
                center = Offset(finalX, py)
            )
        }

        // 2. Draw Falling Leaves & Petals (Swaying and descending)
        leaves.forEach { leaf ->
            val progressY = (leaf.initialYRatio + (timeProg * leaf.fallSpeed))
            val normY = (progressY % 1.0f + 1.0f) % 1.0f
            val py = normY * (h + 60f) - 30f

            val sway = sin((timeProg * Math.PI * 2 * leaf.swayFrequency + leaf.initialXRatio * 8).toDouble()).toFloat()
            val gustOffset = windGust * 35f
            val px = (leaf.initialXRatio * w + sway * leaf.swayAmplitude + gustOffset) % w
            val finalX = if (px < 0f) px + w else px

            val rotationAngle = (timeProg * leaf.rotationSpeed + leaf.initialXRatio * 360f) % 360f
            val leafColor = leafColors[leaf.colorIndex].copy(alpha = 0.65f)

            drawRealisticLeaf(
                center = Offset(finalX, py),
                size = leaf.size,
                angleDegrees = rotationAngle,
                color = leafColor
            )
        }
    }
}

private fun DrawScope.drawRealisticLeaf(
    center: Offset,
    size: Float,
    angleDegrees: Float,
    color: Color
) {
    val rad = Math.toRadians(angleDegrees.toDouble())
    val cosA = cos(rad).toFloat()
    val sinA = sin(rad).toFloat()

    // Flutter depth illusion: squeeze leaf width based on sinusoidal tumbling
    val flutterDepth = kotlin.math.abs(cos(rad * 1.5)).toFloat().coerceIn(0.35f, 1.0f)

    val path = Path().apply {
        val tipX = center.x + size * cosA
        val tipY = center.y + size * sinA

        val baseX = center.x - size * cosA
        val baseY = center.y - size * sinA

        val perpX = -sinA * (size * 0.55f * flutterDepth)
        val perpY = cosA * (size * 0.55f * flutterDepth)

        val cp1x = center.x + (size * 0.15f * cosA) + perpX
        val cp1y = center.y + (size * 0.15f * sinA) + perpY

        val cp2x = center.x + (size * 0.15f * cosA) - perpX
        val cp2y = center.y + (size * 0.15f * sinA) - perpY

        moveTo(baseX, baseY)
        quadraticTo(cp1x, cp1y, tipX, tipY)
        quadraticTo(cp2x, cp2y, baseX, baseY)
        close()
    }

    drawPath(path, color)

    // Central leaf spine / vein
    drawLine(
        color = Color.White.copy(alpha = color.alpha * 0.4f),
        start = Offset(center.x - size * 0.7f * cosA, center.y - size * 0.7f * sinA),
        end = Offset(center.x + size * 0.7f * cosA, center.y + size * 0.7f * sinA),
        strokeWidth = 1.2f
    )
}
