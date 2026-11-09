package com.example.data.repository

import com.example.data.local.EvergreenDatabase
import com.example.data.local.entity.*
import com.example.data.model.ALL_TREE_SPECIES
import com.example.data.model.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class EvergreenRepository(private val database: EvergreenDatabase) {
    val userFlow: Flow<UserEntity?> = database.userDao().getUser()
    val allSessionsFlow: Flow<List<FocusSessionEntity>> = database.focusSessionDao().getAllSessions()
    val allTreesFlow: Flow<List<TreeEntity>> = database.treeDao().getAllTrees()
    val allTasksFlow: Flow<List<TaskEntity>> = database.taskDao().getAllTasks()
    val allAchievementsFlow: Flow<List<AchievementEntity>> = database.gamificationDao().getAllAchievements()
    val allChallengesFlow: Flow<List<ChallengeEntity>> = database.gamificationDao().getAllChallenges()
    val allFriendsFlow: Flow<List<FriendEntity>> = database.socialDao().getAllFriends()
    val allRoomsFlow: Flow<List<GroupRoomEntity>> = database.socialDao().getAllRooms()

    suspend fun completeFocusSession(
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        actualFocusedMinutes: Int,
        isCompleted: Boolean,
        category: String,
        treeSpeciesId: String,
        environmentId: String,
        taskTitle: String?,
        isStrictMode: Boolean,
        note: String?
    ): Pair<Int, Int> {
        val coinsEarned = if (isCompleted) {
            val baseCoins = (actualFocusedMinutes / 2).coerceAtLeast(5)
            val strictBonus = if (isStrictMode) 10 else 0
            baseCoins + strictBonus
        } else {
            0
        }

        val xpEarned = if (isCompleted) actualFocusedMinutes * 2 else actualFocusedMinutes / 2

        // Record session
        val sessionId = database.focusSessionDao().insertSession(
            FocusSessionEntity(
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                actualFocusedMinutes = actualFocusedMinutes,
                status = if (isCompleted) SessionStatus.COMPLETED.name else SessionStatus.INTERRUPTED.name,
                category = category,
                treeSpeciesId = treeSpeciesId,
                taskTitle = taskTitle,
                coinsEarned = coinsEarned,
                xpEarned = xpEarned,
                note = note,
                isStrictMode = isStrictMode
            )
        )

        // Plant Tree
        database.treeDao().insertTree(
            TreeEntity(
                sessionId = sessionId,
                speciesId = treeSpeciesId,
                plantedAt = endTime,
                environmentId = environmentId,
                isAlive = isCompleted,
                category = category,
                durationMinutes = actualFocusedMinutes
            )
        )

        // Update User
        val currentUser = database.userDao().getUserSync() ?: UserEntity()
        val newXp = currentUser.xp + xpEarned
        val newLevel = 1 + (newXp / 150)
        val newCoins = currentUser.coins + coinsEarned
        val newTotalMinutes = currentUser.totalFocusMinutes + actualFocusedMinutes
        val newTotalTrees = currentUser.totalTreesPlanted + (if (isCompleted) 1 else 0)

        database.userDao().insertOrUpdateUser(
            currentUser.copy(
                coins = newCoins,
                xp = newXp,
                level = newLevel,
                totalFocusMinutes = newTotalMinutes,
                totalTreesPlanted = newTotalTrees
            )
        )

        // Update associated task if exists
        if (taskTitle != null && isCompleted) {
            val tasks = database.taskDao().getAllTasks().firstOrNull() ?: emptyList()
            tasks.find { it.title == taskTitle }?.let { task ->
                val updatedMinutes = task.completedMinutes + actualFocusedMinutes
                val completed = updatedMinutes >= task.estimatedMinutes
                database.taskDao().updateTask(
                    task.copy(completedMinutes = updatedMinutes, isCompleted = completed)
                )
            }
        }

        // Update Challenges
        if (isCompleted) {
            database.gamificationDao().addProgressToChallenge("daily_focus_30", actualFocusedMinutes)
            database.gamificationDao().addProgressToChallenge("weekly_hours_5", actualFocusedMinutes)
            database.gamificationDao().addProgressToChallenge("daily_trees_2", 1)
        }

        return Pair(coinsEarned, xpEarned)
    }

    suspend fun unlockSpecies(speciesId: String, price: Int): Boolean {
        val user = database.userDao().getUserSync() ?: return false
        if (user.coins < price) return false

        val currentUnlocked = user.unlockedSpeciesIds.split(",").toMutableSet()
        if (currentUnlocked.contains(speciesId)) return true

        currentUnlocked.add(speciesId)
        val updatedUser = user.copy(
            coins = user.coins - price,
            unlockedSpeciesIds = currentUnlocked.joinToString(",")
        )
        database.userDao().insertOrUpdateUser(updatedUser)
        return true
    }

    suspend fun unlockEnvironment(envId: String, price: Int): Boolean {
        val user = database.userDao().getUserSync() ?: return false
        if (user.coins < price) return false

        val currentUnlocked = user.unlockedEnvironmentIds.split(",").toMutableSet()
        if (currentUnlocked.contains(envId)) return true

        currentUnlocked.add(envId)
        val updatedUser = user.copy(
            coins = user.coins - price,
            unlockedEnvironmentIds = currentUnlocked.joinToString(",")
        )
        database.userDao().insertOrUpdateUser(updatedUser)
        return true
    }

    suspend fun setSelectedEnvironment(envId: String) {
        database.userDao().setSelectedEnvironment(envId)
    }

    suspend fun addTask(title: String, desc: String, category: String, priority: String, estimatedMin: Int) {
        database.taskDao().insertTask(
            TaskEntity(
                title = title,
                description = desc,
                category = category,
                priority = priority,
                estimatedMinutes = estimatedMin
            )
        )
    }

    suspend fun toggleTaskCompleted(task: TaskEntity) {
        database.taskDao().setTaskCompleted(task.id, !task.isCompleted)
    }

    suspend fun deleteTask(task: TaskEntity) {
        database.taskDao().deleteTask(task)
    }

    suspend fun createGroupRoom(title: String, duration: Int, speciesId: String): String {
        val randomSuffix = (100..999).random()
        val prefix = speciesId.take(4).uppercase()
        val roomCode = "$prefix-$randomSuffix"
        val user = database.userDao().getUserSync() ?: UserEntity()

        database.socialDao().insertRoom(
            GroupRoomEntity(
                roomCode = roomCode,
                title = title,
                hostName = user.name,
                durationMinutes = duration,
                treeSpeciesId = speciesId,
                participantCount = 1
            )
        )
        return roomCode
    }

    suspend fun joinRoom(roomCode: String) {
        database.socialDao().joinRoom(roomCode)
    }

    suspend fun updateUserProfile(name: String, email: String, dailyGoal: Int, isPremium: Boolean) {
        val user = database.userDao().getUserSync() ?: UserEntity()
        database.userDao().insertOrUpdateUser(
            user.copy(
                name = name,
                email = email,
                dailyGoalMinutes = dailyGoal,
                isPremium = isPremium
            )
        )
    }
}
