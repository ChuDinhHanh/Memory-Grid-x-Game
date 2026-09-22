package com.example.engine

import com.example.data.model.DifficultyConfig
import com.example.data.model.GameMode

object DifficultyEngine {

    /**
     * Calculates difficulty configuration for a given level and mode.
     * Specification rules:
     * Level 1-4: 4x4 free recall, 3 to 6 cells
     * Level 5-10: 5x5 ordered recall, 10 to 18 cells
     * Later levels: boards grow to 6x6, 7x7, then 8x8 while the
     * sequence and reveal speed continue to scale without a hard level cap.
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

        // Endless progression. Early rounds are a forgiving 4x4 free-recall
        // game; later rounds grow the board and require the shown order.
        val gridSize = when {
            level <= 4 -> 4
            level <= 10 -> 5
            level <= 18 -> 6
            level <= 28 -> 7
            else -> 8
        }

        val cells = when {
            level == 1 -> 3
            level == 2 -> 4
            level == 3 -> 5
            level == 4 -> 6
            level <= 6 -> 5 + level
            level <= 10 -> 8 + level
            else -> minOf(gridSize * gridSize - 4, 18 + (level - 11) * 2)
        }

        val baseTime = when {
            level <= 4 -> 2200L
            level <= 10 -> 1800L
            level <= 18 -> 1450L
            else -> 1150L
        }

        return DifficultyConfig(
            level = level,
            gridSize = gridSize,
            cellsToRemember = minOf(cells, gridSize * gridSize - 3),
            memorizationTimeMs = baseTime
        )
    }
}
