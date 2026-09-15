package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        AchievementEntity::class,
        GameHistoryEntity::class,
        DailyChallengeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MemoryGridDatabase : RoomDatabase() {
    abstract fun memoryGridDao(): MemoryGridDao

    companion object {
        @Volatile
        private var INSTANCE: MemoryGridDatabase? = null

        fun getDatabase(context: Context): MemoryGridDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoryGridDatabase::class.java,
                    "memory_grid_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
