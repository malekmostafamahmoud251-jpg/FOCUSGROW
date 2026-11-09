package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@Composable
fun BreathingExerciseDialog(
    onDismiss: () -> Unit
) {
    // 4-4-4-4 Box Breathing or 4-7-8 Relaxing Breath
    var cycleState by remember { mutableStateOf("Breathe In") }
    var secondsLeftInPhase by remember { mutableIntStateOf(4) }
    var phaseIndex by remember { mutableIntStateOf(0) } // 0: Inhale (4s), 1: Hold (4s), 2: Exhale (4s), 3: Rest (4s)

    LaunchedEffect(Unit) {
        while (true) {
            val phases = listOf(
                "Breathe In..." to 4,
                "Hold..." to 4,
                "Breathe Out..." to 4,
                "Rest..." to 4
            )
            for (i in phases.indices) {
                phaseIndex = i
                cycleState = phases[i].first
                val duration = phases[i].second
                for (sec in duration downTo 1) {
                    secondsLeftInPhase = sec
                    delay(1000L)
                }
            }
        }
    }

    val targetScale = when (phaseIndex) {
        0 -> 1.0f // Inhaling expands
        1 -> 1.0f // Hold steady
        2 -> 0.45f // Exhale contracts
        else -> 0.45f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = 3800,
            easing = FastOutSlowInEasing
        ),
        label = "breath_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mindful Breathing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Box Breathing (4-4-4-4) to restore calm & focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Expanding/Contracting Breathing Canvas
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val maxR = size.minDimension / 2f
                        val currentR = maxR * animatedScale

                        // Outer soft aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF81C784).copy(alpha = 0.35f),
                                    Color(0xFF4CAF50).copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = currentR * 1.15f
                            ),
                            radius = currentR * 1.15f,
                            center = Offset(cx, cy)
                        )

                        // Inner solid circle
                        drawCircle(
                            color = Color(0xFF4CAF50),
                            radius = currentR * 0.75f,
                            center = Offset(cx, cy)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = cycleState,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$secondsLeftInPhase",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Finish Break", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
