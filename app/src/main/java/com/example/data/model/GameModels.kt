package com.example.data.model

enum class GameMode(
    val id: String,
    val displayName: String,
    val description: String,
    val initialLives: Int = 1,
    val timeFactor: Float = 1.0f
) {
    CLASSIC(
        id = "classic",
        displayName = "Thang độ khó",
        description = "Bắt đầu từ Level 1 và khó dần. Chọn sai 1 ô là chơi lại từ đầu!",
        initialLives = 1,
        timeFactor = 1.0f
    ),
    DAILY(
        id = "daily",
        displayName = "Thử thách ngày",
        description = "Mẫu thử thách cố định mỗi ngày cho mọi người chơi.",
        initialLives = 1,
        timeFactor = 1.0f
    )
}

enum class GamePhase {
    IDLE,
    COUNTDOWN,
    MEMORIZE,
    RECALL,
    EVALUATING,
    SUCCESS,
    FAILURE,
    GAME_OVER,
    RESULT
}

enum class CellVisualState {
    IDLE,
    LIGHTING_UP,      // Currently flashing in sequential reveal
    HIGHLIGHTED,      // In Phase A: part of revealed sequence
    SELECTED,         // In Phase B: selected by player during recall
    CORRECT_REVEAL,   // In Phase evaluation: correctly identified
    WRONG_REVEAL,     // In Phase evaluation: wrongly picked by player
    MISSED_REVEAL     // In Phase evaluation: correct cell that player missed
}

data class DifficultyConfig(
    val level: Int,
    val gridSize: Int,          // 3 for 3x3, 4 for 4x4, etc.
    val cellsToRemember: Int,
    val memorizationTimeMs: Long
)

data class RoundEvaluationResult(
    val isSuccess: Boolean,
    val totalExpected: Int,
    val correctSelected: Int,
    val wrongSelected: Int,
    val baseScore: Int,
    val memoryBonus: Int,
    val streakMultiplier: Float,
    val roundScore: Int,
    val xpEarned: Int,
    val correctIndices: List<Int>,
    val wrongIndices: List<Int>,
    val missedIndices: List<Int>
)

data class GameEndSummary(
    val finalScore: Int,
    val bestScore: Int,
    val levelReached: Int,
    val bestLevel: Int,
    val accuracyPercentage: Int,
    val longestCombo: Int,
    val xpEarned: Int,
    val isNewHighScore: Boolean,
    val gameMode: GameMode
)
