package com.example.data.repository

import com.example.data.local.AchievementEntity
import com.example.data.local.DailyChallengeEntity
import com.example.data.local.GameHistoryEntity
import com.example.data.local.MemoryGridDao
import com.example.data.local.PlayerProfileEntity
import com.example.data.model.GameEndSummary
import com.example.data.model.GameMode
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MemoryGridRepository(private val dao: MemoryGridDao) {

    val playerProfile: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val achievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()
    val recentGames: Flow<List<GameHistoryEntity>> = dao.getRecentGames()
    val topScores: Flow<List<GameHistoryEntity>> = dao.getTopScores()
    val topLevels: Flow<List<GameHistoryEntity>> = dao.getTopLevels()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    suspend fun ensureInitialized() {
        val currentProfile = dao.getPlayerProfileSync()
        if (currentProfile == null) {
            dao.insertOrUpdateProfile(
                PlayerProfileEntity(
                    id = 1,
                    playerLevel = 1,
                    currentXp = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    bestScore = 0,
                    bestLevel = 1,
                    bestCombo = 0,
                    totalGamesPlayed = 0,
                    totalRoundsPlayed = 0,
                    totalCorrectRounds = 0,
                    totalIncorrectRounds = 0,
                    totalCellsAttempted = 0,
                    totalCorrectCells = 0,
                    totalDailyCompleted = 0,
                    lastPlayedDate = "",
                    lastDailyChallengeDate = "",
                    soundEnabled = true,
                    hapticsEnabled = true,
                    themeSetting = "SYSTEM"
                )
            )
        }

        // Initialize default achievements
        val defaultAchievements = listOf(
            AchievementEntity(
                id = "first_memory",
                title = "Trí nhớ ban đầu",
                description = "Hoàn thành vòng chơi đầu tiên thành công.",
                icon = "🧠",
                currentProgress = 0,
                maxProgress = 1
            ),
            AchievementEntity(
                id = "getting_hot",
                title = "Bắt đầu nóng lên",
                description = "Đạt chuỗi combo 5 vòng liên tiếp.",
                icon = "🔥",
                currentProgress = 0,
                maxProgress = 5
            ),
            AchievementEntity(
                id = "memory_master",
                title = "Bậc thầy trí nhớ",
                description = "Đạt chuỗi combo 20 vòng siêu phàm.",
                icon = "⚡",
                currentProgress = 0,
                maxProgress = 20
            ),
            AchievementEntity(
                id = "high_score",
                title = "Cao thủ ghi điểm",
                description = "Đạt được 10.000 điểm trong một lượt chơi.",
                icon = "🏆",
                currentProgress = 0,
                maxProgress = 10000
            ),
            AchievementEntity(
                id = "perfect_rounds",
                title = "Hoàn hảo",
                description = "Hoàn thành 10 vòng chơi mà không mắc bất kỳ lỗi nào.",
                icon = "🎯",
                currentProgress = 0,
                maxProgress = 10
            ),
            AchievementEntity(
                id = "dedicated",
                title = "Kiên trì rèn luyện",
                description = "Hoàn thành 7 thử thách hàng ngày.",
                icon = "📅",
                currentProgress = 0,
                maxProgress = 7
            ),
            AchievementEntity(
                id = "brain_machine",
                title = "Bộ não siêu việt",
                description = "Chinh phục Cấp độ 30 trong trò chơi.",
                icon = "👑",
                currentProgress = 1,
                maxProgress = 30
            ),
            AchievementEntity(
                id = "speed_demon",
                title = "Tia chớp trí tuệ",
                description = "Chinh phục Cấp độ 5 trong Thang độ khó.",
                icon = "⚡",
                currentProgress = 0,
                maxProgress = 5
            )
        )
        dao.insertAchievements(defaultAchievements)

        // Seed initial hall of fame records if empty
        if (dao.getGameHistoryCount() == 0) {
            val now = System.currentTimeMillis()
            val seedScores = listOf(
                GameHistoryEntity(playerName = "Trí Tuệ Việt", gameMode = "Thang độ khó", score = 24500, levelReached = 15, accuracy = 96, longestCombo = 15, xpEarned = 750, timestamp = now - 86400000L * 4),
                GameHistoryEntity(playerName = "Siêu Trí Nhớ", gameMode = "Thang độ khó", score = 18200, levelReached = 12, accuracy = 93, longestCombo = 12, xpEarned = 580, timestamp = now - 86400000L * 3),
                GameHistoryEntity(playerName = "Minh Trí", gameMode = "Thang độ khó", score = 14800, levelReached = 10, accuracy = 90, longestCombo = 10, xpEarned = 460, timestamp = now - 86400000L * 2),
                GameHistoryEntity(playerName = "BrainMaster", gameMode = "Thang độ khó", score = 9600, levelReached = 7, accuracy = 88, longestCombo = 7, xpEarned = 320, timestamp = now - 86400000L),
                GameHistoryEntity(playerName = "Kim Ngân", gameMode = "Thang độ khó", score = 6500, levelReached = 5, accuracy = 85, longestCombo = 5, xpEarned = 210, timestamp = now - 3600000L * 5)
            )
            dao.insertAllGameHistories(seedScores)
        }
    }

    suspend fun saveGameResult(
        summary: GameEndSummary,
        roundsPlayed: Int,
        correctRounds: Int,
        incorrectRounds: Int,
        cellsAttempted: Int,
        correctCells: Int
    ) {
        val currentProfile = dao.getPlayerProfileSync() ?: PlayerProfileEntity()
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        // Streak calculation based on playing today vs yesterday
        val newCurrentStreak = when {
            currentProfile.lastPlayedDate == today -> currentProfile.currentStreak
            currentProfile.lastPlayedDate == yesterday -> currentProfile.currentStreak + 1
            else -> 1
        }
        val newLongestStreak = maxOf(currentProfile.longestStreak, newCurrentStreak)

        // XP and Level calculation
        val totalNewXp = currentProfile.currentXp + summary.xpEarned
        val newPlayerLevel = calculatePlayerLevel(totalNewXp)

        val updatedProfile = currentProfile.copy(
            playerLevel = newPlayerLevel,
            currentXp = totalNewXp,
            currentStreak = newCurrentStreak,
            longestStreak = newLongestStreak,
            bestScore = maxOf(currentProfile.bestScore, summary.finalScore),
            bestLevel = maxOf(currentProfile.bestLevel, summary.levelReached),
            bestCombo = maxOf(currentProfile.bestCombo, summary.longestCombo),
            totalGamesPlayed = currentProfile.totalGamesPlayed + 1,
            totalRoundsPlayed = currentProfile.totalRoundsPlayed + roundsPlayed,
            totalCorrectRounds = currentProfile.totalCorrectRounds + correctRounds,
            totalIncorrectRounds = currentProfile.totalIncorrectRounds + incorrectRounds,
            totalCellsAttempted = currentProfile.totalCellsAttempted + cellsAttempted,
            totalCorrectCells = currentProfile.totalCorrectCells + correctCells,
            lastPlayedDate = today
        )
        dao.insertOrUpdateProfile(updatedProfile)

        // Save history record
        dao.insertGameHistory(
            GameHistoryEntity(
                playerName = currentProfile.playerName,
                gameMode = summary.gameMode.displayName,
                score = summary.finalScore,
                levelReached = summary.levelReached,
                accuracy = summary.accuracyPercentage,
                longestCombo = summary.longestCombo,
                xpEarned = summary.xpEarned
            )
        )

        // Update Achievements
        updateAchievementProgress("first_memory", if (correctRounds > 0) 1 else 0)
        updateAchievementProgress("getting_hot", summary.longestCombo)
        updateAchievementProgress("memory_master", summary.longestCombo)
        updateAchievementProgress("high_score", summary.finalScore)
        if (summary.levelReached >= 10 && incorrectRounds == 0) {
            updateAchievementProgress("perfect_rounds", 10)
        }
        updateAchievementProgress("brain_machine", summary.levelReached)
        if (summary.levelReached >= 5) {
            updateAchievementProgress("speed_demon", summary.levelReached)
        }
    }

    suspend fun saveDailyChallengeCompleted(dateKey: String, score: Int, accuracy: Int) {
        val existing = dao.getDailyChallengeSync(dateKey)
        if (existing == null || !existing.completed) {
            dao.insertDailyChallenge(
                DailyChallengeEntity(
                    dateKey = dateKey,
                    completed = true,
                    score = score,
                    accuracy = accuracy
                )
            )
            val profile = dao.getPlayerProfileSync() ?: PlayerProfileEntity()
            val updatedProfile = profile.copy(
                totalDailyCompleted = profile.totalDailyCompleted + 1,
                lastDailyChallengeDate = dateKey
            )
            dao.insertOrUpdateProfile(updatedProfile)

            // Dedicated achievement
            updateAchievementProgress("dedicated", updatedProfile.totalDailyCompleted)
        }
    }

    private suspend fun updateAchievementProgress(id: String, value: Int) {
        val achievement = dao.getAchievementById(id) ?: return
        val newProgress = maxOf(achievement.currentProgress, value)
        val isNowUnlocked = newProgress >= achievement.maxProgress
        if (newProgress != achievement.currentProgress || (isNowUnlocked && !achievement.isUnlocked)) {
            dao.updateAchievement(
                achievement.copy(
                    currentProgress = minOf(newProgress, achievement.maxProgress),
                    isUnlocked = achievement.isUnlocked || isNowUnlocked,
                    unlockedTimestamp = if (isNowUnlocked && !achievement.isUnlocked) System.currentTimeMillis() else achievement.unlockedTimestamp
                )
            )
        }
    }

    fun getDailyChallengeForDate(dateKey: String): Flow<DailyChallengeEntity?> {
        return dao.getDailyChallenge(dateKey)
    }

    suspend fun updatePlayerName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val profile = dao.getPlayerProfileSync() ?: PlayerProfileEntity()
        dao.insertOrUpdateProfile(profile.copy(playerName = trimmed))
    }

    suspend fun updateSettings(sound: Boolean, haptics: Boolean, theme: String) {
        val profile = dao.getPlayerProfileSync() ?: PlayerProfileEntity()
        dao.insertOrUpdateProfile(
            profile.copy(
                soundEnabled = sound,
                hapticsEnabled = haptics,
                themeSetting = theme
            )
        )
    }

    suspend fun resetAllData() {
        dao.clearHistory()
        dao.clearDailyChallenges()
        dao.insertOrUpdateProfile(
            PlayerProfileEntity(
                id = 1,
                playerLevel = 1,
                currentXp = 0,
                currentStreak = 0,
                longestStreak = 0,
                bestScore = 0,
                bestLevel = 1,
                bestCombo = 0,
                totalGamesPlayed = 0,
                totalRoundsPlayed = 0,
                totalCorrectRounds = 0,
                totalIncorrectRounds = 0,
                totalCellsAttempted = 0,
                totalCorrectCells = 0,
                totalDailyCompleted = 0,
                lastPlayedDate = "",
                lastDailyChallengeDate = "",
                soundEnabled = true,
                hapticsEnabled = true,
                themeSetting = "SYSTEM"
            )
        )
        ensureInitialized()
    }

    companion object {
        fun calculatePlayerLevel(xp: Int): Int {
            // Level 1: 0..99
            // Level 2: 100..249
            // Level 3: 250..449, etc. (Threshold: 100 * lvl * (lvl - 1) / 2)
            var level = 1
            var requiredXpForNext = 100
            var remaining = xp
            while (remaining >= requiredXpForNext) {
                remaining -= requiredXpForNext
                level++
                requiredXpForNext = level * 100
            }
            return level
        }

        fun getLevelProgress(xp: Int): Pair<Int, Int> {
            // Returns (currentLevelXp, requiredXpForNextLevel)
            var level = 1
            var requiredXpForNext = 100
            var remaining = xp
            while (remaining >= requiredXpForNext) {
                remaining -= requiredXpForNext
                level++
                requiredXpForNext = level * 100
            }
            return Pair(remaining, requiredXpForNext)
        }
    }
}
