package com.example.ui.screens.forest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.entity.TreeEntity
import com.example.data.model.ALL_TREE_SPECIES
import com.example.data.model.ForestEnvironmentType
import com.example.ui.EvergreenViewModel
import com.example.ui.components.ForestPanorama
import com.example.ui.components.ForestParticleSystem
import com.example.ui.components.ForestTreeItem
import com.example.ui.components.ForestWeatherOverlay
import com.example.ui.components.ForestWeatherType
import com.example.ui.components.TreeCanvas
import java.text.SimpleDateFormat
import java.util.*

enum class ForestFilter {
    ALL, TODAY, WEEK, HEALTHY, WITHERED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForestScreen(
    viewModel: EvergreenViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsState()
    val allTrees by viewModel.trees.collectAsState()

    val currentEnvType = remember(user?.selectedEnvironmentId) {
        ForestEnvironmentType.entries.find { it.id == user?.selectedEnvironmentId } ?: ForestEnvironmentType.VERDANT_MEADOW
    }

    var selectedFilter by remember { mutableStateOf(ForestFilter.ALL) }
    var inspectTree by remember { mutableStateOf<TreeEntity?>(null) }
    var showWeatherDialog by remember { mutableStateOf(false) }

    val streak = user?.currentStreak ?: 1
    val autoWeather = remember(streak) {
        ForestWeatherType.resolveFromActivity(streak)
    }
    var selectedWeather by remember(autoWeather) { mutableStateOf(autoWeather) }

    val now = remember { System.currentTimeMillis() }
    val dayMillis = 86400000L
    val weekMillis = dayMillis * 7

    val filteredTrees = remember(allTrees, selectedFilter) {
        when (selectedFilter) {
            ForestFilter.ALL -> allTrees
            ForestFilter.TODAY -> allTrees.filter { now - it.plantedAt <= dayMillis }
            ForestFilter.WEEK -> allTrees.filter { now - it.plantedAt <= weekMillis }
            ForestFilter.HEALTHY -> allTrees.filter { it.isAlive }
            ForestFilter.WITHERED -> allTrees.filter { !it.isAlive }
        }
    }

    val aliveCount = allTrees.count { it.isAlive }
    val witheredCount = allTrees.count { !it.isAlive }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative Canvas weather overlay (sunbeams, gentle rain, starry twilight based on streak and time of day)
        ForestWeatherOverlay(
            weatherType = selectedWeather,
            modifier = Modifier.fillMaxSize()
        )

        // Atmospheric Canvas particle system for falling leaves and floating pollen
        ForestParticleSystem(
            environment = currentEnvType,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Virtual Forest",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$aliveCount flourishing trees • $witheredCount withered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Weather Badge Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier.clickable { showWeatherDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = when (selectedWeather) {
                                    ForestWeatherType.GOLDEN_SUNBEAMS -> Icons.Default.WbSunny
                                    ForestWeatherType.GENTLE_RAIN -> Icons.Default.WaterDrop
                                    ForestWeatherType.STARRY_TWILIGHT -> Icons.Default.Nightlight
                                    ForestWeatherType.CLEAR_SUNNY -> Icons.Default.LightMode
                                    ForestWeatherType.BREEZY_AUTUMN_LEAVES -> Icons.Default.Air
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedWeather.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Share Card trigger
                    IconButton(
                        onClick = { viewModel.showShareDialog.value = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Forest",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Forest Environment Panorama Banner
            ForestPanorama(
                environment = currentEnvType,
                treeCount = filteredTrees.size
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Environments selector row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ForestEnvironmentType.entries) { env ->
                    val isSelected = env == currentEnvType
                    val userUnlocked = (user?.unlockedEnvironmentIds ?: "meadow").split(",")
                    val isUnlocked = userUnlocked.contains(env.id)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isUnlocked) {
                                viewModel.selectEnvironment(env)
                            } else {
                                viewModel.buyEnvironment(env)
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isUnlocked) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(env.title)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter chips (All, Today, Week, Healthy, Withered)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ForestFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    SuggestionChip(
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trees Grid
            if (filteredTrees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Park,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No trees found in this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredTrees) { tree ->
                        ForestTreeItem(
                            tree = tree,
                            onClick = { inspectTree = tree }
                        )
                    }
                }
            }
        }
    }

    // Weather Mood Picker Dialog
    if (showWeatherDialog) {
        AlertDialog(
            onDismissRequest = { showWeatherDialog = false },
            title = {
                Text("Forest Weather & Atmosphere", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Weather reflects your current $streak-day streak and time of day. Choose an ambiance to preview:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    ForestWeatherType.entries.forEach { weather ->
                        val isSelected = weather == selectedWeather
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedWeather = weather
                                    showWeatherDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (weather) {
                                        ForestWeatherType.GOLDEN_SUNBEAMS -> Icons.Default.WbSunny
                                        ForestWeatherType.GENTLE_RAIN -> Icons.Default.WaterDrop
                                        ForestWeatherType.STARRY_TWILIGHT -> Icons.Default.Nightlight
                                        ForestWeatherType.CLEAR_SUNNY -> Icons.Default.LightMode
                                        ForestWeatherType.BREEZY_AUTUMN_LEAVES -> Icons.Default.Air
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = weather.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = weather.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWeatherDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Inspect Tree Bottom Sheet
    inspectTree?.let { tree ->
        val species = ALL_TREE_SPECIES.find { it.id == tree.speciesId } ?: ALL_TREE_SPECIES[0]
        val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy • HH:mm", Locale.getDefault()) }

        ModalBottomSheet(
            onDismissRequest = { inspectTree = null },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TreeCanvas(
                    species = species,
                    growthProgress = if (tree.isAlive) 1.0f else 0.0f,
                    isWithered = !tree.isAlive,
                    size = 140.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (tree.isAlive) species.name else "Withered ${species.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (tree.isAlive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )

                Text(
                    text = species.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Planted At:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(dateFormat.format(Date(tree.plantedAt)), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Focus Duration:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${tree.durationMinutes} minutes", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Category:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(tree.category, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (tree.isAlive) "Flourishing" else "Interrupted",
                                color = if (tree.isAlive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { inspectTree = null },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}
