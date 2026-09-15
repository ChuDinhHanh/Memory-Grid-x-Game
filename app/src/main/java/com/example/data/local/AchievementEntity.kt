package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean = false,
    val unlockedTimestamp: Long? = null
)
