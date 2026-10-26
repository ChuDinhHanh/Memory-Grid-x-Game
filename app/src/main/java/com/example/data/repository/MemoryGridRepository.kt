package com.example.data.repository

import com.example.data.local.AchievementEntity
import com.example.data.local.DailyChallengeEntity
import com.example.data.local.GameHistoryEntity
import com.example.data.local.MemoryGridDao
import com.example.data.local.PlayerProfileEntity
import com.example.data.model.GameEndSummary
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

    suspend fun updatePlayerAvatar(newAvatar: String) {
        val profile = dao.getPlayerProfileSync() ?: PlayerProfileEntity()
        dao.insertOrUpdateProfile(profile.copy(avatar = newAvatar))
    }

    suspend fun ensureInitialized() {
        val currentProfile = dao.getPlayerProfileSync()
        if (currentProfile == null) {
            // Tự động sinh tên ngẫu nhiên dạng Player_XXXX cho lần đầu tiên
            val randomSuffix = (1000..9999).random()
            val autoPlayerName = "Player_$randomSuffix"

            dao.insertOrUpdateProfile(
                PlayerProfileEntity(
                    id = 1,
                    playerName = autoPlayerName, // Gán tên tự sinh ở đây
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

        // Initialize default achievements (Translated to English)
        val defaultAchievements = listOf(
            AchievementEntity(
                id = "first_memory",
                title = "First Memory",
                description = "Successfully complete your very first round.",
                icon = "🧠",
                currentProgress = 0,
                maxProgress = 1
            ),
            AchievementEntity(
                id = "getting_hot",
                title = "Warming Up",
                description = "Achieve a combo streak of 5 consecutive rounds.",
                icon = "🔥",
                currentProgress = 0,
                maxProgress = 5
            ),
            AchievementEntity(
                id = "memory_master",
                title = "Memory Master",
                description = "Achieve an impressive 20-round combo streak.",
                icon = "⚡",
                currentProgress = 0,
                maxProgress = 20
            ),
            AchievementEntity(
                id = "high_score",
                title = "High Scorer",
                description = "Reach 10,000 points in a single game.",
                icon = "🏆",
                currentProgress = 0,
                maxProgress = 10000
            ),
            AchievementEntity(
                id = "perfect_rounds",
                title = "Perfectionist",
                description = "Complete 10 rounds without making a single mistake.",
                icon = "🎯",
                currentProgress = 0,
                maxProgress = 10
            ),
            AchievementEntity(
                id = "dedicated",
                title = "Dedicated Learner",
                description = "Complete 7 daily challenges.",
                icon = "📅",
                currentProgress = 0,
                maxProgress = 7
            ),
            AchievementEntity(
                id = "brain_machine",
                title = "Supreme Brain",
                description = "Conquer Level 30 in the game.",
                icon = "👑",
                currentProgress = 1,
                maxProgress = 30
            ),
            AchievementEntity(
                id = "speed_demon",
                title = "Mind Lightning",
                description = "Reach Level 5 in Ladder Mode.",
                icon = "⚡",
                currentProgress = 0,
                maxProgress = 5
            )
        )
        dao.insertAchievements(defaultAchievements)

        // Seed initial hall of fame records if empty
        if (dao.getGameHistoryCount() == 0) {
            val now = System.currentTimeMillis()
            val dayInMillis = 86400000L
            val hourInMillis = 3600000L

            val seedScores = listOf(
                GameHistoryEntity(
                    playerName = "Brain Champion",
                    gameMode = "Ladder Mode",
                    score = 24500,
                    levelReached = 15,
                    accuracy = 96,
                    longestCombo = 15,
                    xpEarned = 750,
                    timestamp = now - (dayInMillis * 4)
                ),
                GameHistoryEntity(
                    playerName = "Super Memory",
                    gameMode = "Ladder Mode",
                    score = 18200,
                    levelReached = 12,
                    accuracy = 93,
                    longestCombo = 12,
                    xpEarned = 580,
                    timestamp = now - (dayInMillis * 3)
                ),
                GameHistoryEntity(
                    playerName = "Mind Master",
                    gameMode = "Ladder Mode",
                    score = 14800,
                    levelReached = 10,
                    accuracy = 90,
                    longestCombo = 10,
                    xpEarned = 460,
                    timestamp = now - (dayInMillis * 2)
                ),
                GameHistoryEntity(
                    playerName = "BrainMaster",
                    gameMode = "Ladder Mode",
                    score = 9600,
                    levelReached = 7,
                    accuracy = 88,
                    longestCombo = 7,
                    xpEarned = 320,
                    timestamp = now - dayInMillis
                ),
                GameHistoryEntity(
                    playerName = "Nova Mind",
                    gameMode = "Ladder Mode",
                    score = 6500,
                    levelReached = 5,
                    accuracy = 85,
                    longestCombo = 5,
                    xpEarned = 210,
                    timestamp = now - (hourInMillis * 5)
                )
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