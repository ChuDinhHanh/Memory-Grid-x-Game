package com.example.engine

import kotlin.random.Random

object PatternGenerator {

    /**
     * Generates a list of unique cell indices for a grid.
     * @param gridSize Size of the grid (e.g. 4 for 4x4)
     * @param count Number of cells to select
     * @param previousPattern Previous pattern to avoid exact duplicate
     * @param random Optional Random instance (used for seeded daily challenges)
     */
    fun generatePattern(
        gridSize: Int,
        count: Int,
        previousPattern: List<Int>? = null,
        random: Random = Random.Default
    ): List<Int> {
        val totalCells = gridSize * gridSize
        val safeCount = count.coerceIn(1, totalCells - 1)

        val allIndices = (0 until totalCells).toList()

        // Attempt up to 10 times to find a pattern that isn't identical to the previous one
        var attempts = 0
        var selected: List<Int>

        do {
            selected = allIndices.shuffled(random).take(safeCount).sorted()
            attempts++
        } while (attempts < 10 && previousPattern != null && selected == previousPattern.sorted())

        return selected
    }

    /**
     * Generates a deterministic daily challenge pattern based on date string (e.g. "2026-09-15").
     */
    fun generateDailyPattern(
        dateKey: String,
        gridSize: Int = 5,
        count: Int = 11
    ): List<Int> {
        val seed = dateKey.hashCode().toLong()
        val seededRandom = Random(seed)
        return generatePattern(
            gridSize = gridSize,
            count = count,
            previousPattern = null,
            random = seededRandom
        )
    }

    /**
     * Checks if player's chosen cells match the target pattern.
     */
    fun evaluate(
        targetPattern: List<Int>,
        selectedCells: List<Int>
    ): Pair<List<Int>, List<Int>> {
        val targetSet = targetPattern.toSet()
        val correct = selectedCells.filter { it in targetSet }
        val wrong = selectedCells.filter { it !in targetSet }
        return Pair(correct, wrong)
    }
}
