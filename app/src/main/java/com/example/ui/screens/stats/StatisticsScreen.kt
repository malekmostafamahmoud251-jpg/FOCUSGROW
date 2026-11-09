package com.example.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.analysis.SmartFocusRecommender
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.model.ALL_TREE_SPECIES
import com.example.data.model.FocusCategory
import com.example.ui.EvergreenViewModel
import com.example.ui.components.SmartRecommendationsCard
import com.example.ui.components.TreeCanvas
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(
    viewModel: EvergreenViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsState()
    val trees by viewModel.trees.collectAsState()
    val user by viewModel.user.collectAsState()

    var selectedPeriod by remember { mutableIntStateOf(1) } // 0: Day, 1: Week, 2: Month, 3: All

    val recommendations = remember(sessions, user) {
        SmartFocusRecommender.analyze(sessions, user?.dailyGoalMinutes ?: 60)
    }

    val completedSessions = sessions.filter { it.status == "COMPLETED" }
    val totalFocusedMinutes = completedSessions.sumOf { it.actualFocusedMinutes }
    val successRate = if (sessions.isNotEmpty()) {
        ((completedSessions.size.toFloat() / sessions.size) * 100).toInt()
    } else 100

    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    // Grouping by category
    val categoryDistribution = remember(sessions) {
        val map = mutableMapOf<String, Int>()
        completedSessions.forEach { s ->
            map[s.category] = (map[s.category] ?: 0) + s.actualFocusedMinutes
        }
        map.toList().sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Focus Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track your productivity trends and mindful habits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Period Tab Selector
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val periods = listOf("Day", "Week", "Month", "All-Time")
                periods.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = selectedPeriod == index,
                        onClick = { selectedPeriod = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size)
                    ) {
                        Text(title, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // 4 KPI Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Focus Time
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${totalFocusedMinutes / 60}h ${totalFocusedMinutes % 60}m",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Focused Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Trees Grown
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.Park, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${trees.count { it.isAlive }}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Healthy Trees",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Success Rate
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF29B6F6))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$successRate%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completion Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Current Streak
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(0xFFFF7043))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${user?.currentStreak ?: 1} Days",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Current Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Smart Recommendations Card
        item {
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
        }

        // Category Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Focus by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (categoryDistribution.isEmpty()) {
                        Text(
                            text = "No completed focus sessions yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val maxCategoryMinutes = (categoryDistribution.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
                        categoryDistribution.forEach { (catName, minutes) ->
                            val catObj = FocusCategory.entries.find { it.title.equals(catName, ignoreCase = true) }
                            val catColor = catObj?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                            val ratio = (minutes.toFloat() / maxCategoryMinutes).coerceIn(0.05f, 1f)

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(catName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text("${minutes}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(ratio)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(catColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Productivity Activity Calendar Heatmap
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Consistency Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Activity over the last 28 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 rows x 7 columns grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val intensity = when {
                                        cellIndex in listOf(3, 8, 14, 21, 25, 27) -> 3 // high
                                        cellIndex in listOf(1, 6, 12, 19, 26) -> 2 // med
                                        cellIndex in listOf(4, 10, 16, 23) -> 1 // light
                                        else -> 0 // none
                                    }
                                    val boxColor = when (intensity) {
                                        3 -> Color(0xFF2E7D32)
                                        2 -> Color(0xFF66BB6A)
                                        1 -> Color(0xFFA5D6A7)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(boxColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Session History Header
        item {
            Text(
                text = "Session History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Recent Sessions List
        items(sessions) { session ->
            val species = ALL_TREE_SPECIES.find { it.id == session.treeSpeciesId } ?: ALL_TREE_SPECIES[0]
            val isCompleted = session.status == "COMPLETED"

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TreeCanvas(
                        species = species,
                        growthProgress = if (isCompleted) 1.0f else 0.0f,
                        isWithered = !isCompleted,
                        size = 50.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.taskTitle ?: "${species.name} • ${session.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateFormat.format(Date(session.startTime)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${session.actualFocusedMinutes}m",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        if (isCompleted) {
                            Text(
                                text = "+${session.coinsEarned} coins",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFB300),
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Withered",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
