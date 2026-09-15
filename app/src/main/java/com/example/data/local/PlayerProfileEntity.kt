package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val playerName: String = "Tôi",
    val playerLevel: Int = 1,
    val currentXp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val bestScore: Int = 0,
    val bestLevel: Int = 1,
    val bestCombo: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalRoundsPlayed: Int = 0,
    val totalCorrectRounds: Int = 0,
    val totalIncorrectRounds: Int = 0,
    val totalCellsAttempted: Int = 0,
    val totalCorrectCells: Int = 0,
    val totalDailyCompleted: Int = 0,
    val lastPlayedDate: String = "",           // "yyyy-MM-dd"
    val lastDailyChallengeDate: String = "",   // "yyyy-MM-dd"
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeSetting: String = "SYSTEM"        // "SYSTEM", "LIGHT", "DARK"
)
