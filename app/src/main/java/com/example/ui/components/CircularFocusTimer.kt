package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TreeSpecies
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularFocusTimer(
    timeDisplay: String,
    progress: Float, // 0.0f to 1.0f
    species: TreeSpecies,
    isWithered: Boolean = false,
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
    timerSize: Dp = 290.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(timerSize),
        contentAlignment = Alignment.Center
    ) {
        // Progress Ring Canvas
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 10.dp.toPx()
            val canvasDiameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(canvasDiameter, canvasDiameter)

            // Background Track
            drawArc(
                color = Color(species.foliageColor).copy(alpha = 0.12f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Glowing Arc
            val activeColor = if (isWithered) Color(0xFFC62828) else Color(species.foliageColor)
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = (progress * 360f).coerceIn(0f, 360f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Animated breathing indicator dot at the tip of the arc
            if (isActive && progress > 0f) {
                val angleRad = Math.toRadians((progress * 360f - 90f).toDouble())
                val radius = canvasDiameter / 2
                val dotCenter = Offset(
                    (size.width / 2) + (radius * cos(angleRad)).toFloat(),
                    (size.height / 2) + (radius * sin(angleRad)).toFloat()
                )
                drawCircle(
                    color = activeColor.copy(alpha = pulseAlpha),
                    radius = strokeWidth * 1.2f,
                    center = dotCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.45f,
                    center = dotCenter
                )
            }
        }

        // Center Content: Tree Canvas and Digital Time
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            TreeCanvas(
                species = species,
                growthProgress = progress,
                isWithered = isWithered,
                size = 135.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeDisplay,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (isWithered) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )
        }
    }
}
