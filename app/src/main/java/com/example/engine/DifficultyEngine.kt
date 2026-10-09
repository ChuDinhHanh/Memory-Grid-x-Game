
package com.example.engine

import com.example.data.model.DifficultyConfig
import com.example.data.model.GameMode

object DifficultyEngine {

    /**
     * Calculates difficulty configuration for a given level and mode.
     * Specification rules:
     * Level 1-2: 3x3 free recall, 3 to 4 cells
     * Level 3-6: 4x4 ordered recall, 5 to 8 cells
     * Level 7-12: 5x5 ordered recall, growing cell count
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

        // Endless progression. The very first rounds are a forgiving 3x3
        // free-recall game so new players start on the smallest possible
        // board; later rounds grow the grid and require the shown order.
        val gridSize = when {
            level <= 2 -> 3
            level <= 6 -> 4
            level <= 12 -> 5
            level <= 20 -> 6
            level <= 30 -> 7
            else -> 8
        }

        val cells = when {
            level == 1 -> 3
            level == 2 -> 4
            level == 3 -> 5
            level == 4 -> 6
            level == 5 -> 7
            level == 6 -> 8
            level <= 12 -> 6 + level
            level <= 20 -> 12 + (level - 12) * 2
            else -> minOf(gridSize * gridSize - 4, 20 + (level - 20) * 2)
        }

        val baseTime = when {
            level <= 2 -> 2400L
            level <= 6 -> 2000L
            level <= 12 -> 1600L
            level <= 20 -> 1300L
            else -> 1050L
        }

        return DifficultyConfig(
            level = level,
            gridSize = gridSize,
            cellsToRemember = minOf(cells, gridSize * gridSize - 3),
            memorizationTimeMs = baseTime
        )
    }
}