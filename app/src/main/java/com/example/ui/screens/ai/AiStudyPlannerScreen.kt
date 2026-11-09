package com.example.ui.screens.ai

import androidx.compose.animation.*
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
import com.example.data.model.ALL_TREE_SPECIES
import com.example.ui.EvergreenViewModel
import com.example.ui.components.NatureParticleCanvas
import com.example.ui.components.TreeCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ActionableStudyTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val durationMinutes: Int,
    val recommendedTimeSlot: String,
    val cognitiveLoad: String, // "Deep Immersion", "High Focus", "Moderate Recall", "Synthesis"
    val cognitiveTechnique: String,
    var isCompleted: Boolean = false
)

data class DailyScheduleBlock(
    val timeSlot: String,
    val activityTitle: String,
    val durationMinutes: Int,
    val isBreak: Boolean = false,
    val advice: String,
    val breakTypeIcon: String = "self_improvement"
)

enum class PacingStrategy(val title: String, val subtitle: String, val focusMinutes: Int, val breakMinutes: Int) {
    ULTRADIAN("Deep Work", "50m focus • 10m rest", 50, 10),
    POMODORO("Classic Pomodoro", "25m focus • 5m rest", 25, 5),
    INTENSE_SPRINT("Immersion Sprint", "75m focus • 15m rest", 75, 15),
    GENTLE_HABIT("Habit Builder", "20m focus • 5m rest", 20, 5)
}

data class GoalPreset(
    val title: String,
    val icon: String,
    val domain: String,
    val defaultPacing: PacingStrategy,
    val goalPrompt: String
)

val STUDY_GOAL_PRESETS = listOf(
    GoalPreset(
        title = "Final Exam Prep",
        icon = "school",
        domain = "Math & Science",
        defaultPacing = PacingStrategy.ULTRADIAN,
        goalPrompt = "Master Exam Formulas & Solve 3 Practice Papers with Active Recall"
    ),
    GoalPreset(
        title = "Software Architecture",
        icon = "code",
        domain = "Coding",
        defaultPacing = PacingStrategy.ULTRADIAN,
        goalPrompt = "Architect & Implement Android Feature Modules with Clean Architecture"
    ),
    GoalPreset(
        title = "Language Fluency",
        icon = "translate",
        domain = "Languages",
        defaultPacing = PacingStrategy.POMODORO,
        goalPrompt = "Conversational Fluency, Spaced Vocabulary Drills & Pronunciation"
    ),
    GoalPreset(
        title = "Thesis & Research",
        icon = "edit_note",
        domain = "Writing",
        defaultPacing = PacingStrategy.ULTRADIAN,
        goalPrompt = "Draft Literature Review & Synthesize Key Academic Hypotheses"
    ),
    GoalPreset(
        title = "Book Synthesis",
        icon = "menu_book",
        domain = "Reading",
        defaultPacing = PacingStrategy.GENTLE_HABIT,
        goalPrompt = "Deep Read 3 Chapters & Create 1-Page Conceptual Feynman Summary"
    ),
    GoalPreset(
        title = "Medical & Anatomy",
        icon = "biotech",
        domain = "Math & Science",
        defaultPacing = PacingStrategy.POMODORO,
        goalPrompt = "Memorize Physiological Pathways & Clinical Diagnostic Cases"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudyPlannerScreen(
    viewModel: EvergreenViewModel,
    onApplyPlan: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsState()
    val user by viewModel.user.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val scope = rememberCoroutineScope()

    val recommendations = remember(sessions, user) {
        SmartFocusRecommender.analyze(sessions, user?.dailyGoalMinutes ?: 60)
    }

    var studyGoalInput by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf("Math & Science") }
    var selectedPacing by remember { mutableStateOf(PacingStrategy.ULTRADIAN) }
    var targetDateDays by remember { mutableIntStateOf(3) }
    var dailyAvailableHours by remember { mutableIntStateOf(2) }
    var scheduleStartHour by remember { mutableIntStateOf(9) } // 9 AM default

    var isGenerating by remember { mutableStateOf(false) }
    var generatedTasks by remember { mutableStateOf<List<ActionableStudyTask>?>(null) }
    var generatedSchedule by remember { mutableStateOf<List<DailyScheduleBlock>?>(null) }
    var allTasksImported by remember { mutableStateOf(false) }
    var showAddCustomTaskDialog by remember { mutableStateOf(false) }
    var customTaskTitleInput by remember { mutableStateOf("") }
    var customTaskDurationInput by remember { mutableIntStateOf(25) }

    var selectedAssistantTab by remember { mutableIntStateOf(0) } // 0: Planner, 1: Cognitive Frameworks

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Floating soothing nature leaves background
        NatureParticleCanvas(
            modifier = Modifier.fillMaxSize(),
            particleColor = MaterialTheme.colorScheme.primary,
            petalCount = 10
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Study Assistant",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Turn broad ambitions into paced tasks & optimized schedules",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab switch: Planner vs Study Techniques
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val tabs = listOf("Study Planner", "Cognitive Science")
                tabs.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = selectedAssistantTab == index,
                        onClick = { selectedAssistantTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size)
                    ) {
                        Text(title, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (selectedAssistantTab == 0) {
                // Interactive Goal Templates Library
                Text(
                    text = "Goal Templates & Presets",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(STUDY_GOAL_PRESETS) { preset ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (studyGoalInput == preset.goalPrompt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.clickable {
                                studyGoalInput = preset.goalPrompt
                                selectedDomain = preset.domain
                                selectedPacing = preset.defaultPacing
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = when (preset.icon) {
                                        "school" -> Icons.Default.School
                                        "code" -> Icons.Default.Code
                                        "translate" -> Icons.Default.Translate
                                        "edit_note" -> Icons.Default.EditNote
                                        "menu_book" -> Icons.Default.MenuBook
                                        else -> Icons.Default.Biotech
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = preset.domain,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Goal Specification Form Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "What is your main study or project goal?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = studyGoalInput,
                            onValueChange = { studyGoalInput = it },
                            placeholder = { Text("e.g. Master Calculus Integration & Complete Past Papers") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 3
                        )

                        // Quick Goal Domain Presets
                        Text("Subject / Domain", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Math & Science", "Coding", "Writing", "Languages").forEach { domain ->
                                FilterChip(
                                    selected = selectedDomain == domain,
                                    onClick = { selectedDomain = domain },
                                    label = { Text(domain) }
                                )
                            }
                        }

                        // Pacing Strategy Selector
                        Text("Pacing & Interval Strategy", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PacingStrategy.entries.forEach { strategy ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selectedPacing == strategy) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPacing = strategy }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedPacing == strategy,
                                            onClick = { selectedPacing = strategy }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(strategy.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text(strategy.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        // Target Horizon & Available Daily Time
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Horizon", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(1 to "1d", 3 to "3d", 7 to "7d", 14 to "14d").forEach { (days, lbl) ->
                                        FilterChip(
                                            selected = targetDateDays == days,
                                            onClick = { targetDateDays = days },
                                            label = { Text(lbl) }
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Daily Budget", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(1, 2, 3, 4).forEach { hours ->
                                        FilterChip(
                                            selected = dailyAvailableHours == hours,
                                            onClick = { dailyAvailableHours = hours },
                                            label = { Text("${hours}h") }
                                        )
                                    }
                                }
                            }
                        }

                        // Schedule Start Time
                        Text("Preferred Start Time", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(8 to "8:00 AM", 9 to "9:00 AM", 13 to "1:00 PM", 18 to "6:00 PM").forEach { (h, lbl) ->
                                FilterChip(
                                    selected = scheduleStartHour == h,
                                    onClick = { scheduleStartHour = h },
                                    label = { Text(lbl) }
                                )
                            }
                        }

                        // Historical Context Telemetry
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Calibrated with your Historical Peak Focus",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Your concentration flourishes during ${recommendations.preferredTimeWindow} with ${recommendations.optimalDurationMinutes}m sessions.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isGenerating = true
                                    delay(850L) // smooth natural calculation feel
                                    val goal = if (studyGoalInput.isNotBlank()) studyGoalInput.trim() else "Comprehensive Exam Preparation"
                                    val optDuration = selectedPacing.focusMinutes
                                    val breakDuration = selectedPacing.breakMinutes

                                    generatedTasks = listOf(
                                        ActionableStudyTask(
                                            title = "1. Core Framework & Conceptual Synthesis",
                                            description = "Map the overarching architecture for $goal. Condense key definitions onto a single master reference cheat sheet.",
                                            category = "Studying",
                                            priority = "HIGH",
                                            durationMinutes = optDuration,
                                            recommendedTimeSlot = formatTimeSlot(scheduleStartHour, 0, optDuration),
                                            cognitiveLoad = "Deep Immersion",
                                            cognitiveTechnique = "Feynman Technique (Teach to Learn)"
                                        ),
                                        ActionableStudyTask(
                                            title = "2. Deliberate Practice & Hard Problem Drills",
                                            description = "Tackle complex non-trivial exercises without consulting solutions. Log mistakes into an error ledger.",
                                            category = "Studying",
                                            priority = "HIGH",
                                            durationMinutes = (optDuration + 10).coerceAtMost(90),
                                            recommendedTimeSlot = formatTimeSlot(scheduleStartHour + 1, breakDuration, (optDuration + 10).coerceAtMost(90)),
                                            cognitiveLoad = "High Focus",
                                            cognitiveTechnique = "Active Problem Solving & Error Ledger"
                                        ),
                                        ActionableStudyTask(
                                            title = "3. Active Recall & Spaced Flashcard Drill",
                                            description = "Test rapid retrieval of core equations, code syntax, and definitions using active quizzing.",
                                            category = "Review",
                                            priority = "MEDIUM",
                                            durationMinutes = (optDuration - 10).coerceAtLeast(20),
                                            recommendedTimeSlot = formatTimeSlot(scheduleStartHour + 3, 0, (optDuration - 10).coerceAtLeast(20)),
                                            cognitiveLoad = "Moderate Recall",
                                            cognitiveTechnique = "Leitner Spaced Retrieval System"
                                        ),
                                        ActionableStudyTask(
                                            title = "4. Timed Simulation & Weak Point Consolidation",
                                            description = "Simulate real exam/interview conditions under Strict Mode. Consolidate ambiguous nuances.",
                                            category = "Studying",
                                            priority = "HIGH",
                                            durationMinutes = optDuration,
                                            recommendedTimeSlot = formatTimeSlot(scheduleStartHour + 4, breakDuration, optDuration),
                                            cognitiveLoad = "Deep Immersion",
                                            cognitiveTechnique = "Exam Simulation & Weak Point Drill"
                                        )
                                    )

                                    generatedSchedule = listOf(
                                        DailyScheduleBlock(
                                            timeSlot = formatTimeSlot(scheduleStartHour, 0, optDuration),
                                            activityTitle = "Focus Block 1: Framework Synthesis",
                                            durationMinutes = optDuration,
                                            isBreak = false,
                                            advice = "Prime high cognitive stamina. Enable Strict Mode on phone."
                                        ),
                                        DailyScheduleBlock(
                                            timeSlot = formatTimeSlot(scheduleStartHour, optDuration, breakDuration),
                                            activityTitle = "Mindful Rest & Hydration",
                                            durationMinutes = breakDuration,
                                            isBreak = true,
                                            advice = "Drink 300ml cold water and perform 4-4-4-4 Box Breathing.",
                                            breakTypeIcon = "water_drop"
                                        ),
                                        DailyScheduleBlock(
                                            timeSlot = formatTimeSlot(scheduleStartHour + 1, breakDuration, (optDuration + 10).coerceAtMost(90)),
                                            activityTitle = "Focus Block 2: Deliberate Hard Practice",
                                            durationMinutes = (optDuration + 10).coerceAtMost(90),
                                            isBreak = false,
                                            advice = "Peak mental acuity window. Dive into the most challenging topic."
                                        ),
                                        DailyScheduleBlock(
                                            timeSlot = formatTimeSlot(scheduleStartHour + 2, breakDuration + (optDuration + 10).coerceAtMost(90) - 60, breakDuration + 5),
                                            activityTitle = "Physical Movement & Eye Rest",
                                            durationMinutes = breakDuration + 5,
                                            isBreak = true,
                                            advice = "Apply 20-20-20 rule: look 20 feet away for 20 seconds. Take a quick walk.",
                                            breakTypeIcon = "directions_walk"
                                        ),
                                        DailyScheduleBlock(
                                            timeSlot = formatTimeSlot(scheduleStartHour + 3, 0, (optDuration - 10).coerceAtLeast(20)),
                                            activityTitle = "Focus Block 3: Spaced Retrieval Flashcards",
                                            durationMinutes = (optDuration - 10).coerceAtLeast(20),
                                            isBreak = false,
                                            advice = "Strengthen neural synaptic pathways through self-testing."
                                        )
                                    )
                                    allTasksImported = false
                                    isGenerating = false
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Synthesizing Personalized Plan...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Actionable Plan & Schedule", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Render Actionable Tasks Breakdown
                generatedTasks?.let { taskList ->
                    // Plan Rewards & Growth Forecast Banner
                    val totalPlanMinutes = taskList.sumOf { it.durationMinutes }
                    val estimatedTrees = (totalPlanMinutes / 25).coerceAtLeast(taskList.size)
                    val estimatedCoins = totalPlanMinutes * 2
                    val estimatedXp = totalPlanMinutes * 3

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TreeCanvas(
                                species = selectedSpecies,
                                growthProgress = 1.0f,
                                size = 64.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Plan Rewards Forecast",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Growing with your ${selectedSpecies.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "🌱 ~$estimatedTrees trees",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "💰 +$estimatedCoins coins",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300)
                                    )
                                    Text(
                                        text = "⭐ +$estimatedXp XP",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF29B6F6)
                                    )
                                }
                            }
                        }
                    }

                    // Actionable Task Breakdown List
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Actionable Tasks Checklist",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${taskList.count { it.isCompleted }}/${taskList.size} completed • ${totalPlanMinutes}m total focus",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        taskList.forEach { task ->
                                            viewModel.addTask(
                                                title = task.title,
                                                desc = "${task.description}\nTechnique: ${task.cognitiveTechnique}",
                                                category = task.category,
                                                priority = task.priority,
                                                estimatedMin = task.durationMinutes
                                            )
                                        }
                                        allTasksImported = true
                                    },
                                    enabled = !allTasksImported,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        if (allTasksImported) Icons.Default.Check else Icons.Default.PlaylistAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (allTasksImported) "Imported ✓" else "Add All")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            taskList.forEachIndexed { index, task ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = task.isCompleted,
                                                    onCheckedChange = { isChecked ->
                                                        val updatedList = taskList.toMutableList()
                                                        updatedList[index] = task.copy(isCompleted = isChecked)
                                                        generatedTasks = updatedList
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "${task.durationMinutes} min",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = task.cognitiveLoad,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            FilledTonalButton(
                                                onClick = {
                                                    viewModel.addTask(
                                                        title = task.title,
                                                        desc = "${task.description}\nTechnique: ${task.cognitiveTechnique}",
                                                        category = task.category,
                                                        priority = task.priority,
                                                        estimatedMin = task.durationMinutes
                                                    )
                                                    onApplyPlan(task.title, task.durationMinutes)
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Park, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Plant Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Cognitive Technique Tag
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Lightbulb,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFB300),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = task.cognitiveTechnique,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Add Custom Step Button
                            OutlinedButton(
                                onClick = { showAddCustomTaskDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Custom Step to Plan")
                            }
                        }
                    }

                    // Render Daily Study Schedule
                    generatedSchedule?.let { scheduleList ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Personalized Daily Study Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Paced with alternating focus blocks and purposeful rest intervals",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                scheduleList.forEach { block ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (block.isBreak) Color(0xFF00897B).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (block.isBreak) {
                                                    when (block.breakTypeIcon) {
                                                        "water_drop" -> Icons.Default.WaterDrop
                                                        "directions_walk" -> Icons.Default.DirectionsWalk
                                                        else -> Icons.Default.SelfImprovement
                                                    }
                                                } else {
                                                    Icons.Default.Timer
                                                },
                                                contentDescription = null,
                                                tint = if (block.isBreak) Color(0xFF00897B) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = block.activityTitle,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = block.timeSlot,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (block.isBreak) Color(0xFF00897B) else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Text(
                                                text = block.advice,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Cognitive Science & Study Frameworks Tab
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Proven Cognitive Study Frameworks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val techniques = listOf(
                            Triple(
                                "Feynman Technique",
                                "Explain any complex concept in plain, simple language as if teaching a child. Wherever you stumble reveals the precise gap in your knowledge.",
                                Icons.Default.RecordVoiceOver
                            ),
                            Triple(
                                "Active Retrieval & Spacing",
                                "Testing yourself without notes produces 300% stronger long-term neural retention than passive highlighting or re-reading.",
                                Icons.Default.Psychology
                            ),
                            Triple(
                                "Ultradian Focus Cycles",
                                "Human attention naturally peaks for 90 minutes before requiring a downshift. Keeping sessions between 25-50m avoids the steep fatigue cliff.",
                                Icons.Default.Schedule
                            ),
                            Triple(
                                "Strict Dopamine Fasting",
                                "Even checking notifications for 5 seconds costs up to 23 minutes of context-switching overhead to return to deep concentration.",
                                Icons.Default.DoNotDisturbOn
                            ),
                            Triple(
                                "Leitner Box Method",
                                "Sort review flashcards into tiers: cards you answer correctly move to longer review intervals; mistakes return to daily practice.",
                                Icons.Default.Inventory2
                            ),
                            Triple(
                                "Interleaving Practice",
                                "Alternate between different related subjects or problem variations instead of blocking one topic for hours to enhance cognitive flexibility.",
                                Icons.Default.Shuffle
                            )
                        )

                        techniques.forEach { (title, desc, icon) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Task Dialog
    if (showAddCustomTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomTaskDialog = false },
            title = { Text("Add Custom Step to Plan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customTaskTitleInput,
                        onValueChange = { customTaskTitleInput = it },
                        label = { Text("Step Title") },
                        placeholder = { Text("e.g. Chapter 4 Practice Questions") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Duration: $customTaskDurationInput minutes", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = customTaskDurationInput.toFloat(),
                        onValueChange = { customTaskDurationInput = it.toInt() },
                        valueRange = 10f..120f,
                        steps = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customTaskTitleInput.isNotBlank()) {
                            val newTask = ActionableStudyTask(
                                title = customTaskTitleInput.trim(),
                                description = "Custom study task configured by user.",
                                category = "Studying",
                                priority = "HIGH",
                                durationMinutes = customTaskDurationInput,
                                recommendedTimeSlot = "Self-Paced",
                                cognitiveLoad = "High Focus",
                                cognitiveTechnique = "Focused Deep Work"
                            )
                            generatedTasks = (generatedTasks ?: emptyList()) + newTask
                            showAddCustomTaskDialog = false
                            customTaskTitleInput = ""
                        }
                    }
                ) {
                    Text("Add Step")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTimeSlot(startHour: Int, startMinute: Int, durationMin: Int): String {
    val totalStartM = (startHour * 60) + startMinute
    val totalEndM = totalStartM + durationMin

    fun minToTimeStr(m: Int): String {
        val h24 = (m / 60) % 24
        val min = m % 60
        val isPm = h24 >= 12
        val h12 = if (h24 == 0) 12 else if (h24 > 12) h24 - 12 else h24
        val amPm = if (isPm) "PM" else "AM"
        return "%02d:%02d %s".format(h12, min, amPm)
    }

    return "${minToTimeStr(totalStartM)} – ${minToTimeStr(totalEndM)}"
}
