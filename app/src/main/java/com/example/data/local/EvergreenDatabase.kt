package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        FocusSessionEntity::class,
        TreeEntity::class,
        TaskEntity::class,
        AchievementEntity::class,
        ChallengeEntity::class,
        FriendEntity::class,
        GroupRoomEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EvergreenDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun treeDao(): TreeDao
    abstract fun taskDao(): TaskDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun socialDao(): SocialDao

    companion object {
        @Volatile
        private var INSTANCE: EvergreenDatabase? = null

        fun getDatabase(context: Context): EvergreenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EvergreenDatabase::class.java,
                    "evergreen_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedInitialData(database)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: EvergreenDatabase) {
            // Seed User
            db.userDao().insertOrUpdateUser(
                UserEntity(
                    id = 1,
                    name = "Forest Walker",
                    email = "guest@evergreen.app",
                    level = 2,
                    xp = 180,
                    coins = 240,
                    currentStreak = 3,
                    bestStreak = 5,
                    totalFocusMinutes = 135,
                    totalTreesPlanted = 4,
                    isPremium = false,
                    selectedEnvironmentId = "meadow",
                    preferredLanguage = "English",
                    preferredTheme = "Forest",
                    dailyGoalMinutes = 60,
                    unlockedSpeciesIds = "classic_pine,ancient_oak",
                    unlockedEnvironmentIds = "meadow"
                )
            )

            // Seed sample past trees
            val now = System.currentTimeMillis()
            val day = 86400000L
            db.treeDao().insertTree(
                TreeEntity(
                    sessionId = 1,
                    speciesId = "classic_pine",
                    plantedAt = now - (day * 2),
                    environmentId = "meadow",
                    isAlive = true,
                    category = "Studying",
                    durationMinutes = 25
                )
            )
            db.treeDao().insertTree(
                TreeEntity(
                    sessionId = 2,
                    speciesId = "ancient_oak",
                    plantedAt = now - (day * 1) + 14000000L,
                    environmentId = "meadow",
                    isAlive = true,
                    category = "Work",
                    durationMinutes = 45
                )
            )
            db.treeDao().insertTree(
                TreeEntity(
                    sessionId = 3,
                    speciesId = "classic_pine",
                    plantedAt = now - 7200000L,
                    environmentId = "meadow",
                    isAlive = true,
                    category = "Coding",
                    durationMinutes = 30
                )
            )
            db.treeDao().insertTree(
                TreeEntity(
                    sessionId = 4,
                    speciesId = "classic_pine",
                    plantedAt = now - 3600000L,
                    environmentId = "meadow",
                    isAlive = true,
                    category = "Reading",
                    durationMinutes = 35
                )
            )

            // Seed Sessions
            db.focusSessionDao().insertSession(
                FocusSessionEntity(
                    startTime = now - (day * 2),
                    endTime = now - (day * 2) + 1500000L,
                    durationMinutes = 25,
                    actualFocusedMinutes = 25,
                    status = "COMPLETED",
                    category = "Studying",
                    treeSpeciesId = "classic_pine",
                    taskTitle = "Calculus Problem Set",
                    coinsEarned = 15,
                    xpEarned = 25
                )
            )
            db.focusSessionDao().insertSession(
                FocusSessionEntity(
                    startTime = now - (day * 1),
                    endTime = now - (day * 1) + 2700000L,
                    durationMinutes = 45,
                    actualFocusedMinutes = 45,
                    status = "COMPLETED",
                    category = "Work",
                    treeSpeciesId = "ancient_oak",
                    taskTitle = "Project Proposal",
                    coinsEarned = 25,
                    xpEarned = 45
                )
            )

            // Seed Tasks
            db.taskDao().insertTask(
                TaskEntity(
                    title = "Prepare Chemistry Summary",
                    description = "Review thermodynamics & equilibrium chapters",
                    category = "Studying",
                    priority = "HIGH",
                    estimatedMinutes = 50,
                    completedMinutes = 25,
                    isCompleted = false
                )
            )
            db.taskDao().insertTask(
                TaskEntity(
                    title = "Refactor Data Layer",
                    description = "Clean up repository queries & Flow states",
                    category = "Coding",
                    priority = "MEDIUM",
                    estimatedMinutes = 45,
                    completedMinutes = 0,
                    isCompleted = false
                )
            )
            db.taskDao().insertTask(
                TaskEntity(
                    title = "Read 20 pages of Walden",
                    description = "Mindful evening reading session",
                    category = "Reading",
                    priority = "LOW",
                    estimatedMinutes = 30,
                    completedMinutes = 30,
                    isCompleted = true
                )
            )

            // Seed Achievements
            db.gamificationDao().insertAchievements(
                listOf(
                    AchievementEntity("first_tree", "First Seed", "Plant your very first tree in Evergreen", 1, 1, true, now - (day * 2), 50),
                    AchievementEntity("hour_club", "Deep Roots", "Accumulate over 2 hours of focused study", 135, 120, true, now - 3600000L, 100),
                    AchievementEntity("streak_3", "Green Thumb", "Maintain a 3-day continuous focus streak", 3, 3, true, now, 75),
                    AchievementEntity("streak_7", "Forest Keeper", "Maintain a 7-day continuous focus streak", 3, 7, false, null, 150),
                    AchievementEntity("trees_10", "Small Grove", "Successfully grow 10 healthy trees", 4, 10, false, null, 200),
                    AchievementEntity("pomodoro_master", "Pacing Master", "Complete 5 full Pomodoro intervals", 2, 5, false, null, 120)
                )
            )

            // Seed Challenges
            db.gamificationDao().insertChallenges(
                listOf(
                    ChallengeEntity("daily_focus_30", "Daily Focus Sprint", "Focus for at least 30 minutes today", 30, 25, false, 30, true),
                    ChallengeEntity("daily_trees_2", "Double Sprout", "Plant 2 trees today without interruptions", 2, 1, false, 40, true),
                    ChallengeEntity("weekly_hours_5", "Weekly Arborist", "Accumulate 5 hours of focused work this week", 300, 135, false, 150, false)
                )
            )

            // Seed Friends & Social Leaderboard
            db.socialDao().insertFriends(
                listOf(
                    FriendEntity("f1", "Elena Rostova", 5, 85, 34, 6, "Focusing..."),
                    FriendEntity("f2", "Lucas Vance", 4, 65, 28, 4, "Online"),
                    FriendEntity("f3", "Sophia Chen", 6, 120, 52, 12, "Focusing..."),
                    FriendEntity("f4", "Marcus Thorne", 3, 45, 19, 2, "Resting"),
                    FriendEntity("f5", "Yuki Tanaka", 4, 30, 22, 5, "Online")
                )
            )

            // Seed Study Group Rooms
            db.socialDao().insertRoom(
                GroupRoomEntity(
                    roomCode = "PINE-774",
                    title = "Silent Library & Deep Work",
                    hostName = "Sophia Chen",
                    durationMinutes = 45,
                    treeSpeciesId = "classic_pine",
                    participantCount = 4
                )
            )
            db.socialDao().insertRoom(
                GroupRoomEntity(
                    roomCode = "OAK-219",
                    title = "Finals Prep Study Hall",
                    hostName = "Lucas Vance",
                    durationMinutes = 25,
                    treeSpeciesId = "ancient_oak",
                    participantCount = 3
                )
            )
        }
    }
}
