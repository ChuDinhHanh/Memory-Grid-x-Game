package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryGridDao {

    // Player Profile
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getPlayerProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // Game History
    @Query("SELECT * FROM game_history ORDER BY timestamp DESC LIMIT 30")
    fun getRecentGames(): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history ORDER BY score DESC, levelReached DESC LIMIT 50")
    fun getTopScores(): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history ORDER BY levelReached DESC, score DESC LIMIT 50")
    fun getTopLevels(): Flow<List<GameHistoryEntity>>

    @Query("SELECT COUNT(*) FROM game_history")
    suspend fun getGameHistoryCount(): Int

    @Insert
    suspend fun insertGameHistory(history: GameHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGameHistories(histories: List<GameHistoryEntity>)

    // Daily Challenge
    @Query("SELECT * FROM daily_challenges WHERE dateKey = :dateKey")
    fun getDailyChallenge(dateKey: String): Flow<DailyChallengeEntity?>

    @Query("SELECT * FROM daily_challenges WHERE dateKey = :dateKey")
    suspend fun getDailyChallengeSync(dateKey: String): DailyChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyChallenge(record: DailyChallengeEntity)

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE completed = 1")
    fun getCompletedDailyChallengesCount(): Flow<Int>

    // Reset Data
    @Query("DELETE FROM game_history")
    suspend fun clearHistory()

    @Query("DELETE FROM daily_challenges")
    suspend fun clearDailyChallenges()
}
