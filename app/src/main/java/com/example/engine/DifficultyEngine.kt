package com.example.engine

import com.example.data.model.DifficultyConfig
import com.example.data.model.GameMode

object DifficultyEngine {

    /**
     * Calculates difficulty configuration for a given level and mode.
     * Specification rules:
     * Level 1: 3x3, 3 cells, 1800 ms
     * Level 2: 3x3, 4 cells, 1800 ms
     * Level 3: 3x3, 5 cells, 1700 ms
     * Level 4: 4x4, 5 cells, 1700 ms
     * Level 5: 4x4, 6 cells, 1600 ms
     * Level 6: 4x4, 7 cells, 1500 ms
     * Level 10: 4x4, 9 cells, 1200 ms
     * Level 15: 5x5, 10 cells, 1100 ms
     * Level 20: 5x5, 13 cells, 1000 ms
     * Level 30+: 6x6, increasing cells progressively
     */
    fun calculateDifficulty(level: Int, mode: GameMode = GameMode.CLASSIC): DifficultyConfig {
        if (mode == GameMode.DAILY) {
            return DifficultyConfig(
                level = 1,
                gridSize = 4,
                cellsToRemember = 6,
                memorizationTimeMs = 2000L
            )
        }

        // Progressive Difficulty Ladder:
        // Starts simple at Level 1, gradually gets harder
        val gridSize = when {
            level <= 3 -> 3
            level <= 8 -> 4
            level <= 14 -> 5
            else -> 6
        }

        val cells = when {
            level == 1 -> 3
            level == 2 -> 4
            level == 3 -> 5
            level == 4 -> 4
            level == 5 -> 5
            level == 6 -> 6
            level == 7 -> 7
            level == 8 -> 8
            level == 9 -> 7
            level == 10 -> 8
            level == 11 -> 9
            level == 12 -> 10
            level == 13 -> 11
            level == 14 -> 12
            else -> minOf(gridSize * gridSize - 6, 12 + (level - 15) / 2)
        }

        val baseTime = when {
            level <= 3 -> 2000L
            level <= 8 -> 1700L
            level <= 14 -> 1400L
            else -> 1100L
        }

        return DifficultyConfig(
            level = level,
            gridSize = gridSize,
            cellsToRemember = minOf(cells, gridSize * gridSize - 3),
            memorizationTimeMs = baseTime
        )
    }
}
