package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TreeEntity
import com.example.data.model.ALL_TREE_SPECIES
import com.example.data.model.ForestEnvironmentType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@Composable
fun ForestPanorama(
    environment: ForestEnvironmentType,
    treeCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "env_particles")
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sky Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(environment.skyTopColor), Color(environment.skyBottomColor))
                )
            )

            // Sun or Moon
            val isNight = environment == ForestEnvironmentType.FIREFLY_SANCTUARY
            val orbColor = if (isNight) Color(0xFFFFF9C4) else Color(0xFFFFE082)
            drawCircle(
                color = orbColor.copy(alpha = 0.35f),
                radius = 38f,
                center = Offset(w * 0.82f, h * 0.28f)
            )
            drawCircle(
                color = orbColor,
                radius = 22f,
                center = Offset(w * 0.82f, h * 0.28f)
            )

            // Distant Mountain / Hills
            val hill1 = Path().apply {
                moveTo(0f, h * 0.65f)
                cubicTo(w * 0.25f, h * 0.45f, w * 0.6f, h * 0.7f, w, h * 0.52f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(hill1, Color(environment.groundColor).copy(alpha = 0.55f))

            // Near Hill
            val hill2 = Path().apply {
                moveTo(0f, h * 0.78f)
                cubicTo(w * 0.4f, h * 0.62f, w * 0.75f, h * 0.85f, w, h * 0.72f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(hill2, Color(environment.groundColor))

            // Floating pollen and falling leaves inspired by Forest atmosphere
            val pCount = 18
            for (i in 0 until pCount) {
                val seedX = ((i * 73 + 17) % 100) / 100f
                val seedY = ((i * 47 + 29) % 100) / 100f
                val speed = 0.7f + (i % 3) * 0.3f
                val py = (seedY + particleOffset * speed) % 1.0f * h
                val sway = sin((py * 0.04f + i * 1.5).toDouble()).toFloat() * 22f
                val px = (seedX * w + sway) % w

                if (i % 2 == 0) {
                    // Floating golden/ambient pollen particle with glow
                    val pollenRadius = if (isNight) 3.5f else 2.5f
                    val pColor = Color(environment.particleColor)
                    drawCircle(
                        color = pColor.copy(alpha = 0.3f),
                        radius = pollenRadius * 2.2f,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = pColor.copy(alpha = 0.85f),
                        radius = pollenRadius,
                        center = Offset(px, py)
                    )
                } else {
                    // Descending leaf / petal
                    val leafSize = 7.5f + (i % 3) * 2.5f
                    val angle = (particleOffset * 360f + i * 60f) % 360f
                    val rad = Math.toRadians(angle.toDouble())
                    val cosA = kotlin.math.cos(rad).toFloat()
                    val sinA = kotlin.math.sin(rad).toFloat()

                    val leafPath = Path().apply {
                        val tipX = px + leafSize * cosA
                        val tipY = py + leafSize * sinA
                        val baseX = px - leafSize * cosA
                        val baseY = py - leafSize * sinA
                        val perpX = -sinA * (leafSize * 0.5f)
                        val perpY = cosA * (leafSize * 0.5f)
                        moveTo(baseX, baseY)
                        quadraticTo(px + perpX, py + perpY, tipX, tipY)
                        quadraticTo(px - perpX, py - perpY, baseX, baseY)
                        close()
                    }
                    drawPath(leafPath, Color(environment.particleColor).copy(alpha = 0.7f))
                }
            }
        }

        // Overlay Summary Label
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = environment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "$treeCount trees in this sanctuary",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun ForestTreeItem(
    tree: TreeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val species = ALL_TREE_SPECIES.find { it.id == tree.speciesId } ?: ALL_TREE_SPECIES[0]
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TreeCanvas(
                species = species,
                growthProgress = if (tree.isAlive) 1.0f else 0.0f,
                isWithered = !tree.isAlive,
                size = 80.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (tree.isAlive) species.name else "Withered",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (tree.isAlive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                maxLines = 1
            )

            Text(
                text = "${tree.durationMinutes} min • ${tree.category}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}
