package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

enum class ForestWeatherType(
    val title: String,
    val iconName: String,
    val description: String
) {
    GOLDEN_SUNBEAMS(
        "Golden Sunbeams",
        "wb_sunny",
        "Consistent focus streak illuminates the forest with radiant sunbeams."
    ),
    GENTLE_RAIN(
        "Gentle Rain",
        "water_drop",
        "Soothing raindrops nourish the virtual soil and refresh concentration."
    ),
    BREEZY_AUTUMN_LEAVES(
        "Amber Breeze",
        "air",
        "Crisp autumn wind gracefully swarms golden foliage across the groves."
    ),
    STARRY_TWILIGHT(
        "Starry Twilight",
        "nightlight",
        "Serene nighttime canopy glistening with constellations and soft lunar glow."
    ),
    CLEAR_SUNNY(
        "Warm Sunshine",
        "light_mode",
        "Bright peaceful daylight warming your growing trees."
    );

    companion object {
        fun resolveFromActivity(streak: Int, hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): ForestWeatherType {
            return when {
                hourOfDay in 21..23 || hourOfDay in 0..5 -> STARRY_TWILIGHT
                streak >= 3 -> GOLDEN_SUNBEAMS
                hourOfDay in 6..10 -> CLEAR_SUNNY
                streak in 1..2 -> CLEAR_SUNNY
                else -> GENTLE_RAIN
            }
        }
    }
}

private data class RainDrop(
    val xRatio: Float,
    val yRatio: Float,
    val length: Float,
    val speed: Float,
    val alpha: Float
)

private data class TwinkleStar(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val phase: Float
)

@Composable
fun ForestWeatherOverlay(
    weatherType: ForestWeatherType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_anim")

    // Continuous time drivers for rays and precipitation
    val rainTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_time"
    )

    val sunbeamPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunbeam_pulse"
    )

    val sunbeamSway by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(5600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunbeam_sway"
    )

    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )

    // Pre-calculate deterministic raindrops
    val rainDrops = remember {
        List(42) { i ->
            val seed = i * 79 + 23
            RainDrop(
                xRatio = (seed % 100) / 100f,
                yRatio = ((seed * 7) % 100) / 100f,
                length = 18f + (i % 4) * 8f,
                speed = 1.2f + (i % 3) * 0.4f,
                alpha = 0.35f + (i % 4) * 0.15f
            )
        }
    }

    // Pre-calculate stars for twilight
    val stars = remember {
        List(32) { i ->
            val seed = i * 61 + 17
            TwinkleStar(
                xRatio = (seed % 100) / 100f,
                yRatio = ((seed * 5) % 45) / 100f, // Upper 45% of the sky
                radius = 1.8f + (i % 3) * 1.2f,
                phase = (i * 0.35f)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (weatherType) {
            ForestWeatherType.GOLDEN_SUNBEAMS -> {
                drawGoldenSunbeams(w, h, sunbeamPulse, sunbeamSway)
            }
            ForestWeatherType.GENTLE_RAIN -> {
                drawGentleRain(w, h, rainDrops, rainTime)
            }
            ForestWeatherType.STARRY_TWILIGHT -> {
                drawStarryTwilight(w, h, stars, starTwinkle)
            }
            ForestWeatherType.CLEAR_SUNNY -> {
                drawWarmSunshine(w, h, sunbeamPulse)
            }
            ForestWeatherType.BREEZY_AUTUMN_LEAVES -> {
                drawGoldenSunbeams(w, h, sunbeamPulse * 0.7f, sunbeamSway)
            }
        }
    }
}

private fun DrawScope.drawGoldenSunbeams(
    w: Float,
    h: Float,
    pulse: Float,
    swayDeg: Float
) {
    val originX = w * 0.88f
    val originY = -h * 0.05f

    // Center radiant sun aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFD54F).copy(alpha = 0.28f * pulse),
                Color(0xFFFFECB3).copy(alpha = 0.12f * pulse),
                Color.Transparent
            ),
            center = Offset(originX, originY + h * 0.12f),
            radius = w * 0.75f
        )
    )

    // Volumetric diagonal light rays
    val rayCount = 6
    val angles = listOf(115f, 130f, 142f, 155f, 168f, 180f)

    angles.forEachIndexed { idx, baseAngle ->
        val effectiveAngle = baseAngle + swayDeg * (0.8f + idx * 0.1f)
        val rad = Math.toRadians(effectiveAngle.toDouble())
        val spread = Math.toRadians(6.5)

        val length = h * 1.3f
        val radLeft = rad - spread
        val radRight = rad + spread

        val xLeft = originX + (cos(radLeft) * length).toFloat()
        val yLeft = originY + (sin(radLeft) * length).toFloat()

        val xRight = originX + (cos(radRight) * length).toFloat()
        val yRight = originY + (sin(radRight) * length).toFloat()

        val rayPath = Path().apply {
            moveTo(originX, originY)
            lineTo(xLeft, yLeft)
            lineTo(xRight, yRight)
            close()
        }

        val alphaFactor = if (idx % 2 == 0) 0.18f else 0.12f
        drawPath(
            path = rayPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFF9C4).copy(alpha = alphaFactor * pulse),
                    Color(0xFFFFE082).copy(alpha = (alphaFactor * 0.5f) * pulse),
                    Color.Transparent
                ),
                start = Offset(originX, originY),
                end = Offset((xLeft + xRight) / 2f, (yLeft + yRight) / 2f)
            )
        )
    }
}

private fun DrawScope.drawGentleRain(
    w: Float,
    h: Float,
    drops: List<RainDrop>,
    timeProg: Float
) {
    val slantX = -12f

    drops.forEach { drop ->
        val progressY = (drop.yRatio + (timeProg * drop.speed)) % 1.0f
        val py = progressY * (h + 60f) - 30f
        val px = (drop.xRatio * w + progressY * slantX * 3.5f) % w
        val finalX = if (px < 0f) px + w else px

        // Rain streak
        drawLine(
            color = Color(0xFF90CAF9).copy(alpha = drop.alpha),
            start = Offset(finalX, py),
            end = Offset(finalX + slantX * 0.7f, py + drop.length),
            strokeWidth = 1.6f,
            cap = StrokeCap.Round
        )

        // Occasional ground ripple splash
        if (progressY > 0.88f) {
            val rippleProg = (progressY - 0.88f) / 0.12f
            val rippleRadius = rippleProg * 8f
            val rippleAlpha = (1f - rippleProg) * drop.alpha * 0.75f
            drawCircle(
                color = Color(0xFFBBDEFB).copy(alpha = rippleAlpha),
                radius = rippleRadius,
                center = Offset(finalX, py + drop.length)
            )
        }
    }
}

private fun DrawScope.drawStarryTwilight(
    w: Float,
    h: Float,
    stars: List<TwinkleStar>,
    twinkle: Float
) {
    // Soft lunar aura in top corner
    val moonCenter = Offset(w * 0.84f, h * 0.14f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF9C4).copy(alpha = 0.22f),
                Color(0xFFE0F7FA).copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = moonCenter,
            radius = w * 0.35f
        )
    )
    drawCircle(
        color = Color(0xFFFFF9C4).copy(alpha = 0.85f),
        radius = 20f,
        center = moonCenter
    )

    // Twinkling star field
    stars.forEach { star ->
        val alphaOsc = (sin((twinkle * Math.PI * 2 + star.phase).toDouble()).toFloat() + 1.2f) * 0.45f
        val starAlpha = (alphaOsc).coerceIn(0.15f, 0.95f)
        val sx = star.xRatio * w
        val sy = star.yRatio * h

        drawCircle(
            color = Color.White.copy(alpha = starAlpha),
            radius = star.radius,
            center = Offset(sx, sy)
        )

        // Subtle 4-point star glimmer on larger stars
        if (star.radius > 2.5f) {
            val glowLen = star.radius * 2.8f
            drawLine(
                color = Color(0xFFFFF9C4).copy(alpha = starAlpha * 0.6f),
                start = Offset(sx - glowLen, sy),
                end = Offset(sx + glowLen, sy),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFFFFF9C4).copy(alpha = starAlpha * 0.6f),
                start = Offset(sx, sy - glowLen),
                end = Offset(sx, sy + glowLen),
                strokeWidth = 1f
            )
        }
    }
}

private fun DrawScope.drawWarmSunshine(
    w: Float,
    h: Float,
    pulse: Float
) {
    val sunX = w * 0.85f
    val sunY = h * 0.12f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFE082).copy(alpha = 0.25f * pulse),
                Color(0xFFFFF9C4).copy(alpha = 0.08f * pulse),
                Color.Transparent
            ),
            center = Offset(sunX, sunY),
            radius = w * 0.65f
        )
    )
}
