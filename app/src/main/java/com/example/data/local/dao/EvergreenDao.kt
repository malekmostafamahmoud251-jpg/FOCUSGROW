package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 1")
    suspend fun getUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE users SET coins = coins + :amount WHERE id = 1")
    suspend fun addCoins(amount: Int)

    @Query("UPDATE users SET xp = xp + :xpDelta WHERE id = 1")
    suspend fun addXp(xpDelta: Int)

    @Query("UPDATE users SET selectedEnvironmentId = :envId WHERE id = 1")
    suspend fun setSelectedEnvironment(envId: String)
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startOfDay ORDER BY startTime DESC")
    fun getTodaySessions(startOfDay: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT SUM(actualFocusedMinutes) FROM focus_sessions WHERE status = 'COMPLETED'")
    fun getTotalFocusedMinutes(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE status = 'COMPLETED'")
    fun getCompletedSessionCount(): Flow<Int>
}

@Dao
interface TreeDao {
    @Query("SELECT * FROM trees ORDER BY plantedAt DESC")
    fun getAllTrees(): Flow<List<TreeEntity>>

    @Query("SELECT * FROM trees WHERE plantedAt >= :sinceTimestamp ORDER BY plantedAt DESC")
    fun getTreesSince(sinceTimestamp: Long): Flow<List<TreeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTree(tree: TreeEntity): Long

    @Query("SELECT COUNT(*) FROM trees WHERE isAlive = 1")
    fun getAliveTreeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trees WHERE isAlive = 0")
    fun getWitheredTreeCount(): Flow<Int>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)

    @Query("UPDATE tasks SET completedMinutes = completedMinutes + :minutes WHERE id = :taskId")
    suspend fun addMinutesToTask(taskId: Long, minutes: Int)
}

@Dao
interface GamificationDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(list: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(list: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("UPDATE challenges SET currentMinutes = currentMinutes + :minutes WHERE id = :challengeId")
    suspend fun addProgressToChallenge(challengeId: String, minutes: Int)
}

@Dao
interface SocialDao {
    @Query("SELECT * FROM friends ORDER BY todayMinutes DESC")
    fun getAllFriends(): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Query("SELECT * FROM group_rooms ORDER BY createdAt DESC")
    fun getAllRooms(): Flow<List<GroupRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: GroupRoomEntity)

    @Query("UPDATE group_rooms SET participantCount = participantCount + 1 WHERE roomCode = :code")
    suspend fun joinRoom(code: String)
}
