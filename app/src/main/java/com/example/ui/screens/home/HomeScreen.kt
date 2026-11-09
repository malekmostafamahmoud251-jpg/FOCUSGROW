package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.analysis.SmartFocusRecommender
import com.example.data.model.*
import com.example.ui.EvergreenViewModel
import com.example.ui.components.NatureParticleCanvas
import com.example.ui.components.SmartRecommendationsCard
import com.example.ui.components.TreeCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EvergreenViewModel,
    onStartFocus: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenAiPlanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val durationMinutes by viewModel.selectedDurationMinutes.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTask by viewModel.selectedTask.collectAsState()
    val isStrictMode by viewModel.isStrictMode.collectAsState()
    val currentSound by viewModel.currentSound.collectAsState()
    val challenges by viewModel.challenges.collectAsState()
    val trees by viewModel.trees.collectAsState()

    var showSpeciesDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val todayMillis = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.timeInMillis
    }
    val todayTrees = trees.filter { it.plantedAt >= todayMillis && it.isAlive }
    val todayMinutes = todayTrees.sumOf { it.durationMinutes }
    val dailyGoal = user?.dailyGoalMinutes ?: 60
    val dailyProgress = (todayMinutes.toFloat() / dailyGoal).coerceIn(0f, 1f)

    // Compute Smart Focus Recommendations from user's authentic history
    val recommendations = remember(sessions, user) {
        SmartFocusRecommender.analyze(sessions, dailyGoal)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Floating soothing nature leaves background
        NatureParticleCanvas(
            modifier = Modifier.fillMaxSize(),
            particleColor = MaterialTheme.colorScheme.primary,
            petalCount = 12
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Header: Coins, Streak, Ambient Sound & AI Assistant Quick Jump
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins and Level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFB300).copy(alpha = 0.15f),
                        modifier = Modifier.clickable { onOpenShop() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${user?.coins ?: 0}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Streak
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF7043).copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF7043),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${user?.currentStreak ?: 1}d",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Quick AI Assistant, Ambient Sound & Mindful Space Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenAiPlanner,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.showSoundMixer.value = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (currentSound != AmbientSoundType.NONE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (currentSound != AmbientSoundType.NONE) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound Mixer",
                            tint = if (currentSound != AmbientSoundType.NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.showBreathingDialog.value = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Mindful Space",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Segmented Buttons
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                FocusMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = selectedMode == mode,
                        onClick = { viewModel.setMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = FocusMode.entries.size)
                    ) {
                        Text(mode.title, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Card: Living Tree Showcase & Species Selector
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Tree Preview Canvas
                    TreeCanvas(
                        species = selectedSpecies,
                        growthProgress = 1.0f,
                        size = 175.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Species Tag Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { showSpeciesDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedSpecies.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Change tree",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Duration Selector (for Countdown)
                    if (selectedMode == FocusMode.COUNTDOWN) {
                        Text(
                            text = "$durationMinutes minutes",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Slider(
                            value = durationMinutes.toFloat(),
                            onValueChange = { viewModel.setDuration(it.toInt()) },
                            valueRange = 5f..120f,
                            steps = 22,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Quick duration presets
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf(15, 25, 45, 60).forEach { mins ->
                                FilterChip(
                                    selected = durationMinutes == mins,
                                    onClick = { viewModel.setDuration(mins) },
                                    label = { Text("${mins}m") }
                                )
                            }
                        }
                    } else if (selectedMode == FocusMode.POMODORO) {
                        Text(
                            text = "Pomodoro: 25m Focus • 5m Rest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Cycle automatically transitions to replenish energy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Stopwatch: Count Up",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Timer runs until you choose to complete session",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category & Associated Task Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AssistChip(
                            onClick = { showCategoryDialog = true },
                            leadingIcon = {
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(selectedCategory.title) }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        AssistChip(
                            onClick = { onOpenTasks() },
                            leadingIcon = {
                                Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = {
                                Text(selectedTask?.title ?: "Select Task")
                            }
                        )
                    }

                    // Strict Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Strict Mode",
                                tint = if (isStrictMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Strict Deep Focus",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tree withers if you exit the app",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = isStrictMode,
                            onCheckedChange = { viewModel.toggleStrictMode() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Start Focus Button CTA
            Button(
                onClick = onStartFocus,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Park, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Plant & Start Focus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Smart Focus Recommendations Card
            SmartRecommendationsCard(
                recommendations = recommendations,
                onApplyDuration = { viewModel.setDuration(it) },
                onApplyDailyGoal = { goal ->
                    viewModel.updateUserProfile(
                        name = user?.name ?: "Forest Walker",
                        email = user?.email ?: "guest@evergreen.app",
                        dailyGoal = goal,
                        isPremium = user?.isPremium ?: false
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AI Study Assistant Hero Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiPlanner() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Study Assistant",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "AI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Goal breakdown, paced schedules & cognitive techniques",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Daily Progress & Sprint Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Sanctuary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$todayMinutes / ${dailyGoal}m",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { dailyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${todayTrees.size} trees planted today • ${challenges.firstOrNull()?.title ?: "Daily Sprint"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Species Picker Dialog
    if (showSpeciesDialog) {
        val userUnlocked = (user?.unlockedSpeciesIds ?: "classic_pine").split(",")
        AlertDialog(
            onDismissRequest = { showSpeciesDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Tree (${ALL_TREE_SPECIES.size})")
                    TextButton(onClick = {
                        showSpeciesDialog = false
                        onOpenShop()
                    }) {
                        Text("Nursery", fontSize = 12.sp)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ALL_TREE_SPECIES.forEach { species ->
                        val isUnlocked = userUnlocked.contains(species.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isUnlocked) {
                                        viewModel.setSpecies(species)
                                        showSpeciesDialog = false
                                    } else {
                                        showSpeciesDialog = false
                                        onOpenShop()
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TreeCanvas(species = species, growthProgress = 1.0f, size = 46.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(species.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(species.rarity.badgeColor).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            species.rarity.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color(species.rarity.badgeColor),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    if (isUnlocked) "Ready to plant" else "Unlock in Nursery (${species.price} coins)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selectedSpecies.id == species.id) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            } else if (!isUnlocked) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeciesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Category Picker Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Focus Category") },
            text = {
                Column {
                    FocusCategory.entries.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCategory(cat)
                                    showCategoryDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(cat.colorHex))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(cat.title, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.weight(1f))
                            if (selectedCategory == cat) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
