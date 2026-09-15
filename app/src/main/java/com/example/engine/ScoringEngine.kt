package com.example.engine

object ScoringEngine {

    fun calculateBaseScore(level: Int): Int {
        return 100 * level
    }

    fun calculateMemoryBonus(
        remainingMemorizationTimeMs: Long,
        totalMemorizationTimeMs: Long,
        level: Int
    ): Int {
        if (totalMemorizationTimeMs <= 0) return 0
        val ratio = (remainingMemorizationTimeMs.toFloat() / totalMemorizationTimeMs).coerceIn(0f, 1f)
        return (ratio * 50 * level).toInt()
    }

    fun getStreakMultiplier(combo: Int): Float {
        return when {
            combo <= 2 -> 1.0f
            combo in 3..4 -> 1.2f
            combo in 5..9 -> 1.5f
            else -> 2.0f
        }
    }

    fun calculateRoundScore(
        level: Int,
        remainingMemorizationTimeMs: Long,
        totalMemorizationTimeMs: Long,
        combo: Int,
        isSpeedMode: Boolean = false
    ): Pair<Int, Int> {
        val baseScore = calculateBaseScore(level)
        val memoryBonus = calculateMemoryBonus(
            remainingMemorizationTimeMs,
            totalMemorizationTimeMs,
            level
        )
        val streakMultiplier = getStreakMultiplier(combo)
        val modeMultiplier = if (isSpeedMode) 1.5f else 1.0f

        val totalScore = ((baseScore + memoryBonus) * streakMultiplier * modeMultiplier).toInt()
        return Pair(totalScore, memoryBonus)
    }

    fun calculateXpGained(level: Int, isDaily: Boolean = false): Int {
        val base = 10 + level * 2
        return if (isDaily) base + 50 else base
    }

    fun calculateAccuracy(correctCells: Int, totalAttempted: Int): Int {
        if (totalAttempted <= 0) return 100
        return ((correctCells.toFloat() / totalAttempted) * 100).toInt().coerceIn(0, 100)
    }
}
