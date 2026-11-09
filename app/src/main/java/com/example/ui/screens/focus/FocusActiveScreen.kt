package com.example.ui.screens.focus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbientSoundType
import com.example.data.model.FocusMode
import com.example.ui.EvergreenViewModel
import com.example.ui.components.CelebrationBurstCanvas
import com.example.ui.components.CircularFocusTimer
import com.example.ui.components.NatureParticleCanvas
import com.example.ui.components.TreeCanvas
import java.util.Locale

@Composable
fun FocusActiveScreen(
    viewModel: EvergreenViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val totalSeconds by viewModel.totalSeconds.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTask by viewModel.selectedTask.collectAsState()
    val isStrictMode by viewModel.isStrictMode.collectAsState()
    val isFocusRunning by viewModel.isFocusRunning.collectAsState()
    val isFocusPaused by viewModel.isFocusPaused.collectAsState()
    val currentSound by viewModel.currentSound.collectAsState()
    val showAbandonWarning by viewModel.showAbandonWarning.collectAsState()
    val completionResult by viewModel.completionResult.collectAsState()
    val distractionAlertVisible by viewModel.distractionAlertVisible.collectAsState()

    // Prevent accidental system back press from abandoning without warning
    BackHandler {
        if (isFocusRunning) {
            viewModel.requestAbandon()
        } else {
            onNavigateBack()
        }
    }

    // Time calculations
    val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
    val displaySeconds = if (selectedMode == FocusMode.STOPWATCH) elapsedSeconds else remainingSeconds
    val minutes = displaySeconds / 60
    val seconds = displaySeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val progress = if (selectedMode == FocusMode.STOPWATCH) {
        ((elapsedSeconds % 1800) / 1800f).coerceIn(0f, 1f)
    } else {
        if (totalSeconds > 0) (elapsedSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f) else 0f
    }

    // Gentle motivational quotes inspired by Forest app
    val quotes = remember {
        listOf(
            "Every second of focus nourishes your living forest.",
            "Deep roots grow quietly in the silence of deep work.",
            "Stay present with this breath, and let the distractions fade.",
            "Patience is the fertile soil where mastery blooms.",
            "A mighty oak began as a little acorn that held its ground.",
            "Your attention is the sunlight that helps you grow."
        )
    }
    val currentQuote = remember(elapsedSeconds / 60) {
        quotes[(elapsedSeconds / 60) % quotes.size]
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Nature particle floating background
        NatureParticleCanvas(
            modifier = Modifier.fillMaxSize(),
            particleColor = Color(selectedSpecies.foliageColor),
            petalCount = 14
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Focus Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category & Task Indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(selectedCategory.colorHex).copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(selectedCategory.colorHex))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedTask?.title ?: selectedCategory.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Strict Mode Badge & Sound Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isStrictMode) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = "Strict Mode",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Strict",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = { viewModel.showSoundMixer.value = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (currentSound != AmbientSoundType.NONE) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Ambient Sound",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Distraction Alert Banner
            if (distractionAlertVisible) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App distraction detected! Return to Evergreen to save your tree.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissDistractionAlert() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Center: Circular Timer with Growing Living Tree
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                CircularFocusTimer(
                    timeDisplay = timeFormatted,
                    progress = progress,
                    species = selectedSpecies,
                    isWithered = false,
                    isActive = isFocusRunning && !isFocusPaused,
                    timerSize = 310.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Calm inspirational quote
                AnimatedContent(targetState = currentQuote, label = "quote") { quote ->
                    Text(
                        text = quote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Bottom Controls: Pause/Resume and Abandon/Finish
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Give up button
                OutlinedButton(
                    onClick = { viewModel.requestAbandon() },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.height(52.dp).weight(1f)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Give Up", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Pause / Resume Button
                if (isFocusPaused) {
                    Button(
                        onClick = { viewModel.resumeFocus() },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(52.dp).weight(1.2f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.pauseFocus() },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.height(52.dp).weight(1.2f)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause", fontWeight = FontWeight.Bold)
                    }
                }

                // For stopwatch mode: explicit finish button
                if (selectedMode == FocusMode.STOPWATCH) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { viewModel.finishCurrentSession(isCompleted = true) },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.height(52.dp).weight(1.2f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Abandon Warning Dialog
    if (showAbandonWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.showAbandonWarning.value = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = {
                Text("Abandon Session?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "If you leave now, your ${selectedSpecies.name} will wither and die! " +
                    "Take a deep breath and keep going. Are you sure you want to surrender?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmAbandon()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Give Up")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.showAbandonWarning.value = false }) {
                    Text("Keep Focusing")
                }
            }
        )
    }

    // Session Completion Dialog with Celebration Burst
    completionResult?.let { result ->
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissCompletionDialog()
                onNavigateBack()
            },
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    text = if (result.isCompleted) "Tree Planted Successfully!" else "Tree Withered",
                    fontWeight = FontWeight.Bold,
                    color = if (result.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Celebration burst particles behind the mature tree!
                    if (result.isCompleted) {
                        CelebrationBurstCanvas(modifier = Modifier.fillMaxSize())
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        TreeCanvas(
                            species = result.species,
                            growthProgress = if (result.isCompleted) 1.0f else 0.0f,
                            isWithered = !result.isCompleted,
                            size = 135.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (result.isCompleted) {
                            Text(
                                text = "Congratulations! You focused for ${result.durationMinutes} minutes.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFB300).copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("+${result.coinsEarned} Coins", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF7E57C2).copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF7E57C2), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("+${result.xpEarned} XP", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Don't be discouraged! Every effort strengthens your future focus. Plant another seed when you are ready.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissCompletionDialog()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (result.isCompleted) "Add to Forest" else "Back to Home")
                }
            }
        )
    }
}
