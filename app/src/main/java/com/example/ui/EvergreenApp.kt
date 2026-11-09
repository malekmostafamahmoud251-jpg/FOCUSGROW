package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ALL_TREE_SPECIES
import com.example.ui.components.BreathingExerciseDialog
import com.example.ui.components.ShareCardDialog
import com.example.ui.components.SoundMixerSheet
import com.example.ui.navigation.MainDestination
import com.example.ui.navigation.SubRoutes
import com.example.ui.screens.ai.AiStudyPlannerScreen
import com.example.ui.screens.focus.FocusActiveScreen
import com.example.ui.screens.forest.ForestScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.mindful.MindfulScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.shop.ShopScreen
import com.example.data.local.entity.TaskEntity
import com.example.ui.screens.social.SocialScreen
import com.example.ui.screens.stats.StatisticsScreen
import com.example.ui.screens.tasks.TasksScreen

@Composable
fun EvergreenApp(
    viewModel: EvergreenViewModel = viewModel()
) {
    var currentRoute by remember { mutableStateOf(MainDestination.HOME.route) }
    val navigationStack = remember { mutableStateListOf(MainDestination.HOME.route) }

    val user by viewModel.user.collectAsState()
    val trees by viewModel.trees.collectAsState()
    val showSoundMixer by viewModel.showSoundMixer.collectAsState()
    val showBreathingDialog by viewModel.showBreathingDialog.collectAsState()
    val showShareDialog by viewModel.showShareDialog.collectAsState()
    val currentSound by viewModel.currentSound.collectAsState()
    val soundVolume by viewModel.soundVolume.collectAsState()
    val isFocusRunning by viewModel.isFocusRunning.collectAsState()

    fun navigateTo(route: String) {
        if (currentRoute != route) {
            navigationStack.add(route)
            currentRoute = route
        }
    }

    fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeLastOrNull()
            currentRoute = navigationStack.lastOrNull() ?: MainDestination.HOME.route
        } else {
            currentRoute = MainDestination.HOME.route
        }
    }

    val isMainDestination = MainDestination.entries.any { it.route == currentRoute }
    val showBottomBar = isMainDestination && !isFocusRunning

    // Handle back button on secondary screens
    if (!isMainDestination && currentRoute != SubRoutes.FOCUS_ACTIVE) {
        BackHandler {
            navigateBack()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    MainDestination.entries.forEach { destination ->
                        val isSelected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navigationStack.clear()
                                    navigationStack.add(destination.route)
                                    currentRoute = destination.route
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) },
                            modifier = Modifier.testTag("nav_${destination.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { route ->
                when (route) {
                    MainDestination.HOME.route -> HomeScreen(
                        viewModel = viewModel,
                        onStartFocus = {
                            viewModel.startFocusSession()
                            navigateTo(SubRoutes.FOCUS_ACTIVE)
                        },
                        onOpenShop = { navigateTo(SubRoutes.SHOP) },
                        onOpenTasks = { navigateTo(SubRoutes.TASKS) },
                        onOpenAiPlanner = { navigateTo(SubRoutes.AI_PLANNER) }
                    )
                    MainDestination.FOREST.route -> ForestScreen(
                        viewModel = viewModel
                    )
                    MainDestination.STATISTICS.route -> StatisticsScreen(
                        viewModel = viewModel
                    )
                    MainDestination.SOCIAL.route -> SocialScreen(
                        viewModel = viewModel,
                        onStartGroupFocus = { duration, speciesId ->
                            viewModel.setDuration(duration)
                            ALL_TREE_SPECIES.find { it.id == speciesId }?.let {
                                viewModel.setSpecies(it)
                            }
                            viewModel.startFocusSession()
                            navigateTo(SubRoutes.FOCUS_ACTIVE)
                        }
                    )
                    MainDestination.PROFILE.route -> ProfileScreen(
                        viewModel = viewModel,
                        onOpenSettings = { navigateTo(SubRoutes.SETTINGS) },
                        onOpenShop = { navigateTo(SubRoutes.SHOP) },
                        onOpenAiPlanner = { navigateTo(SubRoutes.AI_PLANNER) },
                        onOpenMindful = { navigateTo(SubRoutes.MINDFUL) }
                    )
                    SubRoutes.FOCUS_ACTIVE -> FocusActiveScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navigateBack()
                        }
                    )
                    SubRoutes.SHOP -> ShopScreen(
                        viewModel = viewModel
                    )
                    SubRoutes.TASKS -> TasksScreen(
                        viewModel = viewModel,
                        onStartFocusOnTask = { task ->
                            viewModel.setTask(task)
                            viewModel.setDuration(task.estimatedMinutes)
                            viewModel.startFocusSession()
                            navigateTo(SubRoutes.FOCUS_ACTIVE)
                        }
                    )
                    SubRoutes.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                    SubRoutes.AI_PLANNER -> AiStudyPlannerScreen(
                        viewModel = viewModel,
                        onApplyPlan = { title, duration ->
                            viewModel.setDuration(duration)
                            viewModel.startFocusSession()
                            navigateTo(SubRoutes.FOCUS_ACTIVE)
                        }
                    )
                    SubRoutes.MINDFUL -> MindfulScreen(
                        viewModel = viewModel
                    )
                    SubRoutes.ONBOARDING -> OnboardingScreen(
                        viewModel = viewModel,
                        onFinishOnboarding = {
                            currentRoute = MainDestination.HOME.route
                        }
                    )
                }
            }
        }
    }

    // Global Modal Sheets & Dialogs
    if (showSoundMixer) {
        SoundMixerSheet(
            selectedSound = currentSound,
            volume = soundVolume,
            onSoundSelected = { viewModel.setAmbientSound(it) },
            onVolumeChanged = { viewModel.setAmbientVolume(it) },
            onDismiss = { viewModel.showSoundMixer.value = false }
        )
    }

    if (showBreathingDialog) {
        BreathingExerciseDialog(
            onDismiss = { viewModel.showBreathingDialog.value = false }
        )
    }

    if (showShareDialog && user != null) {
        ShareCardDialog(
            user = user!!,
            totalMinutes = user!!.totalFocusMinutes,
            totalTrees = trees.count { it.isAlive },
            onDismiss = { viewModel.showShareDialog.value = false },
            onShare = {
                viewModel.showShareDialog.value = false
            }
        )
    }
}
