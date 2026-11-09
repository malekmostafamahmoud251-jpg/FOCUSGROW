package com.example.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.model.ALL_TREE_SPECIES
import com.example.ui.EvergreenViewModel
import com.example.ui.components.TreeCanvas

@Composable
fun OnboardingScreen(
    viewModel: EvergreenViewModel,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) } // 0: Welcome, 1: Demo Tree Growth, 2: Goal Setup
    var selectedCategory by remember { mutableStateOf("Studying") }
    var selectedGoalMinutes by remember { mutableIntStateOf(60) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            for (i in 0..2) {
                Box(
                    modifier = Modifier
                        .size(if (step == i) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (step == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        // Center Content based on Step
        when (step) {
            0 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    TreeCanvas(
                        species = ALL_TREE_SPECIES[0],
                        growthProgress = 1.0f,
                        size = 180.dp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Welcome to Evergreen",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Transform your concentration into a flourishing virtual forest. Plant seeds, focus without distraction, and watch your botanical sanctuary grow.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            1 -> {
                var previewProgress by remember { mutableFloatStateOf(0.4f) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    TreeCanvas(
                        species = ALL_TREE_SPECIES[1],
                        growthProgress = previewProgress,
                        size = 180.dp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your Effort Grows Trees",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "As you stay focused, your tree matures through 5 growth stages. Leaving your app in Strict Mode causes your tree to wither.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Slider(
                        value = previewProgress,
                        onValueChange = { previewProgress = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Drag slider to preview tree growth",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            2 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Tailor Your Sanctuary",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "What is your primary focus intention?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Studying", "Work", "Coding", "Reading").forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Daily Target: $selectedGoalMinutes minutes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 45, 60, 90).forEach { mins ->
                            FilterChip(
                                selected = selectedGoalMinutes == mins,
                                onClick = { selectedGoalMinutes = mins },
                                label = { Text("${mins}m") }
                            )
                        }
                    }
                }
            }
        }

        // Bottom CTA Button
        Button(
            onClick = {
                if (step < 2) {
                    step++
                } else {
                    viewModel.updateUserProfile(
                        name = "Forest Walker",
                        email = "guest@evergreen.app",
                        dailyGoal = selectedGoalMinutes,
                        isPremium = false
                    )
                    onFinishOnboarding()
                }
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (step < 2) "Continue" else "Enter My Sanctuary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
