package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientSoundEngine
import com.example.data.local.EvergreenDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.data.repository.EvergreenRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionCompletionResult(
    val durationMinutes: Int,
    val coinsEarned: Int,
    val xpEarned: Int,
    val species: TreeSpecies,
    val isCompleted: Boolean
)

enum class PomodoroStage {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

class EvergreenViewModel(application: Application) : AndroidViewModel(application) {
    private val database = EvergreenDatabase.getDatabase(application)
    val repository = EvergreenRepository(database)
    val soundEngine = AmbientSoundEngine()

    // Data Flows
    val user: StateFlow<UserEntity?> = repository.userFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val sessions: StateFlow<List<FocusSessionEntity>> = repository.allSessionsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val trees: StateFlow<List<TreeEntity>> = repository.allTreesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val tasks: StateFlow<List<TaskEntity>> = repository.allTasksFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievementsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val challenges: StateFlow<List<ChallengeEntity>> = repository.allChallengesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val friends: StateFlow<List<FriendEntity>> = repository.allFriendsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val rooms: StateFlow<List<GroupRoomEntity>> = repository.allRoomsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Focus Session Config State
    var selectedMode = MutableStateFlow(FocusMode.COUNTDOWN)
        private set
    var selectedDurationMinutes = MutableStateFlow(25)
        private set
    var selectedSpecies = MutableStateFlow(ALL_TREE_SPECIES[0])
        private set
    var selectedCategory = MutableStateFlow(FocusCategory.STUDYING)
        private set
    var selectedTask = MutableStateFlow<TaskEntity?>(null)
        private set
    var isStrictMode = MutableStateFlow(true)
        private set

    // Focus Runtime State
    var sessionStatus = MutableStateFlow(SessionStatus.ACTIVE)
        private set
    var isFocusRunning = MutableStateFlow(false)
        private set
    var isFocusPaused = MutableStateFlow(false)
        private set
    var elapsedSeconds = MutableStateFlow(0)
        private set
    var totalSeconds = MutableStateFlow(25 * 60)
        private set

    // Pomodoro Runtime
    var pomodoroStage = MutableStateFlow(PomodoroStage.FOCUS)
        private set
    var pomodoroCycleCount = MutableStateFlow(1)
        private set

    // Ambient Sound
    var currentSound = MutableStateFlow(AmbientSoundType.NONE)
        private set
    var soundVolume = MutableStateFlow(0.7f)
        private set

    // UI Dialog & Sheet states
    var showSoundMixer = MutableStateFlow(false)
    var showBreathingDialog = MutableStateFlow(false)
    var showShareDialog = MutableStateFlow(false)
    var showAbandonWarning = MutableStateFlow(false)
    var completionResult = MutableStateFlow<SessionCompletionResult?>(null)

    // Deep Focus distraction alert
    var distractionAlertVisible = MutableStateFlow(false)

    private var timerJob: Job? = null
    private var sessionStartTimestamp = 0L

    fun setMode(mode: FocusMode) {
        selectedMode.value = mode
        if (mode == FocusMode.POMODORO) {
            selectedDurationMinutes.value = 25
            totalSeconds.value = 25 * 60
        }
    }

    fun setDuration(minutes: Int) {
        selectedDurationMinutes.value = minutes
        totalSeconds.value = minutes * 60
    }

    fun setSpecies(species: TreeSpecies) {
        selectedSpecies.value = species
    }

    fun setCategory(category: FocusCategory) {
        selectedCategory.value = category
    }

    fun setTask(task: TaskEntity?) {
        selectedTask.value = task
        if (task != null) {
            FocusCategory.entries.find { it.title.equals(task.category, ignoreCase = true) }?.let {
                selectedCategory.value = it
            }
        }
    }

    fun toggleStrictMode() {
        isStrictMode.value = !isStrictMode.value
    }

    fun setAmbientSound(sound: AmbientSoundType) {
        currentSound.value = sound
        soundEngine.playSound(sound)
    }

    fun setAmbientVolume(vol: Float) {
        soundVolume.value = vol
        soundEngine.volume = vol
    }

    fun startFocusSession() {
        if (isFocusRunning.value) return

        sessionStartTimestamp = System.currentTimeMillis()
        isFocusRunning.value = true
        isFocusPaused.value = false
        sessionStatus.value = SessionStatus.ACTIVE
        elapsedSeconds.value = 0

        val durSec = when (selectedMode.value) {
            FocusMode.COUNTDOWN -> selectedDurationMinutes.value * 60
            FocusMode.STOPWATCH -> 0
            FocusMode.POMODORO -> {
                when (pomodoroStage.value) {
                    PomodoroStage.FOCUS -> 25 * 60
                    PomodoroStage.SHORT_BREAK -> 5 * 60
                    PomodoroStage.LONG_BREAK -> 15 * 60
                }
            }
        }
        totalSeconds.value = durSec

        if (currentSound.value != AmbientSoundType.NONE) {
            soundEngine.playSound(currentSound.value)
        }

        launchTimerLoop()
    }

    private fun launchTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startWallTime = System.currentTimeMillis() - (elapsedSeconds.value * 1000L)
            while (isFocusRunning.value) {
                delay(1000L)
                if (isFocusPaused.value) continue

                val currentWallTime = System.currentTimeMillis()
                val realElapsed = ((currentWallTime - startWallTime) / 1000L).toInt()
                elapsedSeconds.value = realElapsed

                if (selectedMode.value != FocusMode.STOPWATCH && totalSeconds.value > 0) {
                    if (elapsedSeconds.value >= totalSeconds.value) {
                        finishCurrentSession(isCompleted = true)
                        break
                    }
                }
            }
        }
    }

    fun pauseFocus() {
        isFocusPaused.value = true
        sessionStatus.value = SessionStatus.PAUSED
    }

    fun resumeFocus() {
        isFocusPaused.value = false
        sessionStatus.value = SessionStatus.ACTIVE
        launchTimerLoop()
    }

    fun requestAbandon() {
        showAbandonWarning.value = true
    }

    fun confirmAbandon() {
        showAbandonWarning.value = false
        finishCurrentSession(isCompleted = false)
    }

    fun finishCurrentSession(isCompleted: Boolean) {
        timerJob?.cancel()
        isFocusRunning.value = false
        soundEngine.stopSound()

        val actualMinutes = (elapsedSeconds.value / 60).coerceAtLeast(if (isCompleted) 1 else 0)
        val endTimestamp = System.currentTimeMillis()

        viewModelScope.launch {
            val envId = user.value?.selectedEnvironmentId ?: "meadow"
            val (coins, xp) = repository.completeFocusSession(
                startTime = sessionStartTimestamp,
                endTime = endTimestamp,
                durationMinutes = if (selectedMode.value == FocusMode.STOPWATCH) actualMinutes else selectedDurationMinutes.value,
                actualFocusedMinutes = actualMinutes,
                isCompleted = isCompleted,
                category = selectedCategory.value.title,
                treeSpeciesId = selectedSpecies.value.id,
                environmentId = envId,
                taskTitle = selectedTask.value?.title,
                isStrictMode = isStrictMode.value,
                note = null
            )

            completionResult.value = SessionCompletionResult(
                durationMinutes = actualMinutes,
                coinsEarned = coins,
                xpEarned = xp,
                species = selectedSpecies.value,
                isCompleted = isCompleted
            )
        }
    }

    fun dismissCompletionDialog() {
        completionResult.value = null
        elapsedSeconds.value = 0
        isFocusRunning.value = false
        sessionStatus.value = SessionStatus.ACTIVE
    }

    fun triggerAppDistraction() {
        if (isFocusRunning.value && isStrictMode.value) {
            distractionAlertVisible.value = true
        }
    }

    fun dismissDistractionAlert() {
        distractionAlertVisible.value = false
    }

    // Shop Actions
    fun buySpecies(species: TreeSpecies) {
        viewModelScope.launch {
            repository.unlockSpecies(species.id, species.price)
        }
    }

    fun buyEnvironment(env: ForestEnvironmentType) {
        viewModelScope.launch {
            if (repository.unlockEnvironment(env.id, env.price)) {
                repository.setSelectedEnvironment(env.id)
            }
        }
    }

    fun selectEnvironment(env: ForestEnvironmentType) {
        viewModelScope.launch {
            repository.setSelectedEnvironment(env.id)
        }
    }

    // Task Actions
    fun addTask(title: String, desc: String, category: String, priority: String, estimatedMin: Int) {
        viewModelScope.launch {
            repository.addTask(title, desc, category, priority, estimatedMin)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Social Group Focus
    fun createGroupRoom(title: String, duration: Int, speciesId: String, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val code = repository.createGroupRoom(title, duration, speciesId)
            onDone(code)
        }
    }

    fun joinGroupRoom(code: String) {
        viewModelScope.launch {
            repository.joinRoom(code)
        }
    }

    // Profile Settings
    fun updateUserProfile(name: String, email: String, dailyGoal: Int, isPremium: Boolean) {
        viewModelScope.launch {
            repository.updateUserProfile(name, email, dailyGoal, isPremium)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.stopSound()
    }
}
