package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerName: String = "Tôi",
    val gameMode: String,
    val score: Int,
    val levelReached: Int,
    val accuracy: Int,
    val longestCombo: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val dateKey: String, // "yyyy-MM-dd"
    val completed: Boolean,
    val score: Int,
    val accuracy: Int,
    val completedTimestamp: Long = System.currentTimeMillis()
)
