package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Forest Walker",
    val email: String = "guest@evergreen.app",
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 150,
    val currentStreak: Int = 1,
    val bestStreak: Int = 1,
    val totalFocusMinutes: Int = 0,
    val totalTreesPlanted: Int = 0,
    val isPremium: Boolean = false,
    val selectedEnvironmentId: String = "meadow",
    val preferredLanguage: String = "English",
    val preferredTheme: String = "Forest",
    val dailyGoalMinutes: Int = 60,
    val unlockedSpeciesIds: String = "classic_pine,ancient_oak",
    val unlockedEnvironmentIds: String = "meadow",
    val strictModeDefault: Boolean = false,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val actualFocusedMinutes: Int,
    val status: String, // COMPLETED, INTERRUPTED
    val category: String,
    val treeSpeciesId: String,
    val taskTitle: String? = null,
    val coinsEarned: Int,
    val xpEarned: Int,
    val note: String? = null,
    val isStrictMode: Boolean = false
)

@Entity(tableName = "trees")
data class TreeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val speciesId: String,
    val plantedAt: Long,
    val environmentId: String,
    val isAlive: Boolean, // false if withered
    val category: String,
    val durationMinutes: Int
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Studying",
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val estimatedMinutes: Int = 25,
    val completedMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val deadlineMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val rewardCoins: Int
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetMinutes: Int,
    val currentMinutes: Int,
    val isCompleted: Boolean = false,
    val rewardCoins: Int,
    val isDaily: Boolean = true
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: String,
    val name: String,
    val level: Int,
    val todayMinutes: Int,
    val totalTrees: Int,
    val streak: Int,
    val status: String // "Online", "Focusing...", "Resting"
)

@Entity(tableName = "group_rooms")
data class GroupRoomEntity(
    @PrimaryKey val roomCode: String,
    val title: String,
    val hostName: String,
    val durationMinutes: Int,
    val treeSpeciesId: String,
    val participantCount: Int = 1,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
