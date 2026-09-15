package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyChallengeEntity
import com.example.data.local.MemoryGridDatabase
import com.example.data.local.PlayerProfileEntity
import com.example.data.model.CellVisualState
import com.example.data.model.DifficultyConfig
import com.example.data.model.GameEndSummary
import com.example.data.model.GameMode
import com.example.data.model.GamePhase
import com.example.data.model.RoundEvaluationResult
import com.example.data.repository.MemoryGridRepository
import com.example.engine.AudioHapticService
import com.example.engine.DifficultyEngine
import com.example.engine.PatternGenerator
import com.example.engine.ScoringEngine
import com.example.ui.theme.ThemeSetting
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GameUiState(
    val phase: GamePhase = GamePhase.IDLE,
    val gameMode: GameMode = GameMode.CLASSIC,
    val countdownNumber: Int = 3,
    val level: Int = 1,
    val score: Int = 0,
    val lives: Int = 1,
    val maxLives: Int = 1,
    val combo: Int = 0,
    val longestCombo: Int = 0,
    val gridSize: Int = 3,
    val targetPattern: List<Int> = emptyList(),
    val selectedCells: Set<Int> = emptySet(),
    val currentlyLitCell: Int? = null,
    val revealedSequenceIndices: Set<Int> = emptySet(),
    val sequenceStep: Int = 0,
    val activeStepNumberMap: Map<Int, Int> = emptyMap(),
    val memorizeTimeTotalMs: Long = 1800L,
    val memorizeTimeRemainingMs: Long = 1800L,
    val roundScoreEarned: Int = 0,
    val roundBonusEarned: Int = 0,
    val evaluationResult: RoundEvaluationResult? = null,
    val gameEndSummary: GameEndSummary? = null,
    val dailyChallengeDateKey: String = "",
    val dailyChallengeAlreadyCompletedToday: Boolean = false,
    val requiresStrictSequence: Boolean = false,
    val selectedSequence: List<Int> = emptyList()
)

class MemoryGridViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MemoryGridDatabase.getDatabase(application)
    private val repository = MemoryGridRepository(db.memoryGridDao())
    val audioHaptic = AudioHapticService(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentGames = repository.recentGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topScores = repository.topScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topLevels = repository.topLevels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var phaseTimerJob: Job? = null
    private var recallStartTimeMs: Long = 0L

    // Tracking stats during a single match
    private var roundsPlayedInGame = 0
    private var correctRoundsInGame = 0
    private var incorrectRoundsInGame = 0
    private var cellsAttemptedInGame = 0
    private var correctCellsInGame = 0
    private var previousPattern: List<Int>? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
        viewModelScope.launch {
            playerProfile.collect { profile ->
                if (profile != null) {
                    audioHaptic.soundEnabled = profile.soundEnabled
                    audioHaptic.hapticsEnabled = profile.hapticsEnabled
                }
            }
        }
    }

    fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun startGame(mode: GameMode = GameMode.CLASSIC) {
        phaseTimerJob?.cancel()
        roundsPlayedInGame = 0
        correctRoundsInGame = 0
        incorrectRoundsInGame = 0
        cellsAttemptedInGame = 0
        correctCellsInGame = 0
        previousPattern = null

        _uiState.value = GameUiState(
            phase = GamePhase.COUNTDOWN,
            gameMode = mode,
            countdownNumber = 3,
            level = 1,
            score = 0,
            lives = 1,
            maxLives = 1,
            combo = 0,
            longestCombo = 0,
            dailyChallengeDateKey = if (mode == GameMode.DAILY) getTodayDateKey() else ""
        )

        startCountdown()
    }

    private fun startCountdown() {
        phaseTimerJob?.cancel()
        phaseTimerJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.value = _uiState.value.copy(countdownNumber = i)
                audioHaptic.playTap()
                delay(650)
            }
            startRound()
        }
    }

    private fun startRound() {
        val state = _uiState.value
        val config: DifficultyConfig = if (state.gameMode == GameMode.DAILY) {
            DifficultyEngine.calculateDifficulty(state.level, GameMode.DAILY)
        } else {
            DifficultyEngine.calculateDifficulty(state.level, state.gameMode)
        }

        val pattern = if (state.gameMode == GameMode.DAILY) {
            PatternGenerator.generateDailyPattern(
                dateKey = state.dailyChallengeDateKey,
                gridSize = config.gridSize,
                count = config.cellsToRemember
            )
        } else {
            PatternGenerator.generatePattern(
                gridSize = config.gridSize,
                count = config.cellsToRemember,
                previousPattern = previousPattern
            )
        }
        previousPattern = pattern

        val isStrictSequence = state.level >= 3
        val stepMap = if (isStrictSequence) {
            pattern.mapIndexed { index, cell -> cell to (index + 1) }.toMap()
        } else {
            emptyMap()
        }

        _uiState.value = state.copy(
            phase = GamePhase.MEMORIZE,
            gridSize = config.gridSize,
            targetPattern = pattern,
            selectedCells = emptySet(),
            selectedSequence = emptyList(),
            currentlyLitCell = null,
            revealedSequenceIndices = emptySet(),
            sequenceStep = 0,
            activeStepNumberMap = stepMap,
            memorizeTimeTotalMs = config.memorizationTimeMs,
            memorizeTimeRemainingMs = config.memorizationTimeMs,
            evaluationResult = null,
            roundScoreEarned = 0,
            roundBonusEarned = 0,
            requiresStrictSequence = isStrictSequence
        )

        phaseTimerJob?.cancel()
        if (!isStrictSequence) {
            // Level 1 & 2: Free recall - All target cells illuminate together
            _uiState.value = _uiState.value.copy(
                revealedSequenceIndices = pattern.toSet(),
                currentlyLitCell = null
            )
            audioHaptic.playTargetDisplay()
            audioHaptic.vibrateTap()

            val totalTime = config.memorizationTimeMs
            val interval = 50L
            var timeRemaining = totalTime

            phaseTimerJob = viewModelScope.launch {
                while (timeRemaining > 0) {
                    delay(interval)
                    timeRemaining -= interval
                    _uiState.value = _uiState.value.copy(
                        memorizeTimeRemainingMs = maxOf(0L, timeRemaining)
                    )
                }

                _uiState.value = _uiState.value.copy(
                    revealedSequenceIndices = emptySet()
                )
                delay(120L)
                transitionToRecall()
            }
        } else {
            // Level 3+: Strict sequential recall - Cells light up 1-by-1 in order
            phaseTimerJob = viewModelScope.launch {
                val stepDelay = when {
                    state.level <= 4 -> 500L
                    state.level <= 8 -> 420L
                    else -> 360L
                }

                for (i in pattern.indices) {
                    val cell = pattern[i]
                    val currentRevealed = _uiState.value.revealedSequenceIndices + cell
                    _uiState.value = _uiState.value.copy(
                        currentlyLitCell = cell,
                        revealedSequenceIndices = currentRevealed,
                        sequenceStep = i + 1
                    )
                    audioHaptic.playSequentialTone(i, pattern.size)
                    audioHaptic.vibrateTap()
                    delay(stepDelay)
                }

                _uiState.value = _uiState.value.copy(
                    currentlyLitCell = null,
                    revealedSequenceIndices = pattern.toSet()
                )
                delay(550L)

                _uiState.value = _uiState.value.copy(
                    revealedSequenceIndices = emptySet(),
                    currentlyLitCell = null,
                    activeStepNumberMap = emptyMap()
                )
                delay(150L)
                transitionToRecall()
            }
        }
    }

    private fun transitionToRecall() {
        recallStartTimeMs = System.currentTimeMillis()
        audioHaptic.playRecallPrompt()
        _uiState.value = _uiState.value.copy(
            phase = GamePhase.RECALL,
            memorizeTimeRemainingMs = 0L
        )
    }

    fun onCellTapped(cellIndex: Int) {
        val state = _uiState.value
        if (state.phase != GamePhase.RECALL) return

        if (cellIndex in state.selectedCells) return // Cannot pick same cell twice

        val isStrict = state.requiresStrictSequence

        if (!isStrict) {
            // Level 1-2: Free recall (any order of target cells)
            if (cellIndex !in state.targetPattern) {
                // Tapped wrong cell -> Sudden death!
                val newSelected = state.selectedCells + cellIndex
                _uiState.value = state.copy(selectedCells = newSelected)
                evaluateFailure(wrongCellIndex = cellIndex, allSelected = newSelected)
                return
            }

            // Correct cell
            val newSelected = state.selectedCells + cellIndex
            val newSeq = state.selectedSequence + cellIndex
            _uiState.value = state.copy(
                selectedCells = newSelected,
                selectedSequence = newSeq
            )
            audioHaptic.playTap()
            audioHaptic.vibrateTap()

            if (newSelected.size == state.targetPattern.size) {
                evaluateSuccess(newSelected)
            }
        } else {
            // Level >= 3: Strict sequential recall!
            val expectedStep = state.selectedSequence.size
            val expectedCell = state.targetPattern[expectedStep]

            if (cellIndex != expectedCell) {
                // Tapped wrong cell OR out-of-order -> Sudden death!
                val newSelected = state.selectedCells + cellIndex
                _uiState.value = state.copy(selectedCells = newSelected)
                evaluateFailure(wrongCellIndex = cellIndex, allSelected = newSelected)
                return
            }

            // Correct cell in exact order!
            val newSeq = state.selectedSequence + cellIndex
            val newSelected = state.selectedCells + cellIndex
            val stepMap = newSeq.mapIndexed { idx, cell -> cell to (idx + 1) }.toMap()

            _uiState.value = state.copy(
                selectedCells = newSelected,
                selectedSequence = newSeq,
                activeStepNumberMap = stepMap
            )

            audioHaptic.playSequentialTone(expectedStep, state.targetPattern.size)
            audioHaptic.vibrateTap()

            if (newSeq.size == state.targetPattern.size) {
                evaluateSuccess(newSelected)
            }
        }
    }

    private fun evaluateSuccess(selected: Set<Int>) {
        phaseTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(phase = GamePhase.EVALUATING)

        val state = _uiState.value
        val targetList = state.targetPattern
        val (correctList, _) = PatternGenerator.evaluate(targetList, selected.toList())

        roundsPlayedInGame++
        cellsAttemptedInGame += selected.size
        correctCellsInGame += correctList.size
        correctRoundsInGame++

        val newCombo = state.combo + 1
        val newLongestCombo = maxOf(state.longestCombo, newCombo)

        val (roundScore, memoryBonus) = ScoringEngine.calculateRoundScore(
            level = state.level,
            remainingMemorizationTimeMs = state.memorizeTimeTotalMs / 3,
            totalMemorizationTimeMs = state.memorizeTimeTotalMs,
            combo = newCombo,
            isSpeedMode = false
        )

        val xp = ScoringEngine.calculateXpGained(state.level, state.gameMode == GameMode.DAILY)
        val newTotalScore = state.score + roundScore

        val evalResult = RoundEvaluationResult(
            isSuccess = true,
            totalExpected = targetList.size,
            correctSelected = correctList.size,
            wrongSelected = 0,
            baseScore = ScoringEngine.calculateBaseScore(state.level),
            memoryBonus = memoryBonus,
            streakMultiplier = ScoringEngine.getStreakMultiplier(newCombo),
            roundScore = roundScore,
            xpEarned = xp,
            correctIndices = correctList,
            wrongIndices = emptyList(),
            missedIndices = emptyList()
        )

        _uiState.value = state.copy(
            phase = GamePhase.SUCCESS,
            score = newTotalScore,
            combo = newCombo,
            longestCombo = newLongestCombo,
            roundScoreEarned = roundScore,
            roundBonusEarned = memoryBonus,
            evaluationResult = evalResult
        )

        audioHaptic.playCorrect()
        audioHaptic.playCombo(newCombo)
        audioHaptic.vibrateSuccess()

        // If Daily Challenge is completed (1 successful round)
        if (state.gameMode == GameMode.DAILY) {
            viewModelScope.launch {
                val accuracy = ScoringEngine.calculateAccuracy(correctCellsInGame, cellsAttemptedInGame)
                repository.saveDailyChallengeCompleted(state.dailyChallengeDateKey, newTotalScore, accuracy)
                saveGameEndSummary(newTotalScore, state.level, isSuccess = true)
            }
            return
        }

        // Proceed to next level automatically
        phaseTimerJob = viewModelScope.launch {
            delay(1000)
            audioHaptic.playLevelUp()
            _uiState.value = _uiState.value.copy(
                level = state.level + 1
            )
            startRound()
        }
    }

    private fun evaluateFailure(wrongCellIndex: Int, allSelected: Set<Int>) {
        phaseTimerJob?.cancel()

        val state = _uiState.value
        val targetList = state.targetPattern
        val correctList = allSelected.filter { it in targetList }
        val missedList = targetList.filter { it !in allSelected }

        roundsPlayedInGame++
        cellsAttemptedInGame += allSelected.size
        correctCellsInGame += correctList.size
        incorrectRoundsInGame++

        val evalResult = RoundEvaluationResult(
            isSuccess = false,
            totalExpected = targetList.size,
            correctSelected = correctList.size,
            wrongSelected = 1,
            baseScore = 0,
            memoryBonus = 0,
            streakMultiplier = 1.0f,
            roundScore = 0,
            xpEarned = 0,
            correctIndices = correctList,
            wrongIndices = listOf(wrongCellIndex),
            missedIndices = missedList
        )

        _uiState.value = state.copy(
            phase = GamePhase.FAILURE,
            lives = 0,
            combo = 0,
            evaluationResult = evalResult
        )

        audioHaptic.playWrong()
        audioHaptic.vibrateWrong()

        phaseTimerJob = viewModelScope.launch {
            // Show red wrong tile and missed tiles for 1.3 seconds
            delay(1300)
            saveGameEndSummary(state.score, state.level, isSuccess = false)
        }
    }

    private suspend fun saveGameEndSummary(finalScore: Int, levelReached: Int, isSuccess: Boolean) {
        val currentProfile = playerProfile.value
        val bestScore = currentProfile?.bestScore ?: 0
        val isNewHigh = finalScore > bestScore
        val accuracy = ScoringEngine.calculateAccuracy(correctCellsInGame, cellsAttemptedInGame)
        val xpEarned = ScoringEngine.calculateXpGained(levelReached, _uiState.value.gameMode == GameMode.DAILY)

        val summary = GameEndSummary(
            finalScore = finalScore,
            bestScore = maxOf(bestScore, finalScore),
            levelReached = levelReached,
            bestLevel = maxOf(currentProfile?.bestLevel ?: 1, levelReached),
            accuracyPercentage = accuracy,
            longestCombo = _uiState.value.longestCombo,
            xpEarned = xpEarned,
            isNewHighScore = isNewHigh,
            gameMode = _uiState.value.gameMode
        )

        repository.saveGameResult(
            summary = summary,
            roundsPlayed = roundsPlayedInGame,
            correctRounds = correctRoundsInGame,
            incorrectRounds = incorrectRoundsInGame,
            cellsAttempted = cellsAttemptedInGame,
            correctCells = correctCellsInGame
        )

        _uiState.value = _uiState.value.copy(
            phase = GamePhase.RESULT,
            gameEndSummary = summary
        )
    }

    fun exitToHome() {
        phaseTimerJob?.cancel()
        _uiState.value = GameUiState(phase = GamePhase.IDLE)
    }

    fun toggleSound() {
        val profile = playerProfile.value ?: return
        val newSound = !profile.soundEnabled
        audioHaptic.soundEnabled = newSound
        viewModelScope.launch {
            repository.updateSettings(
                sound = newSound,
                haptics = profile.hapticsEnabled,
                theme = profile.themeSetting
            )
        }
    }

    fun toggleHaptics() {
        val profile = playerProfile.value ?: return
        val newHaptics = !profile.hapticsEnabled
        audioHaptic.hapticsEnabled = newHaptics
        viewModelScope.launch {
            repository.updateSettings(
                sound = profile.soundEnabled,
                haptics = newHaptics,
                theme = profile.themeSetting
            )
        }
    }

    fun setThemeSetting(theme: String) {
        val profile = playerProfile.value ?: return
        viewModelScope.launch {
            repository.updateSettings(
                sound = profile.soundEnabled,
                haptics = profile.hapticsEnabled,
                theme = theme
            )
        }
    }

    fun updatePlayerName(newName: String) {
        viewModelScope.launch {
            repository.updatePlayerName(newName)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            exitToHome()
        }
    }

    fun getCellVisualState(cellIndex: Int): CellVisualState {
        val state = _uiState.value
        return when (state.phase) {
            GamePhase.MEMORIZE -> {
                when {
                    cellIndex == state.currentlyLitCell -> CellVisualState.LIGHTING_UP
                    cellIndex in state.revealedSequenceIndices -> CellVisualState.HIGHLIGHTED
                    else -> CellVisualState.IDLE
                }
            }
            GamePhase.RECALL -> {
                if (cellIndex in state.selectedCells) CellVisualState.SELECTED else CellVisualState.IDLE
            }
            GamePhase.EVALUATING, GamePhase.SUCCESS -> {
                if (cellIndex in state.targetPattern) CellVisualState.CORRECT_REVEAL else CellVisualState.IDLE
            }
            GamePhase.FAILURE, GamePhase.GAME_OVER, GamePhase.RESULT -> {
                val eval = state.evaluationResult
                when {
                    eval != null && cellIndex in eval.wrongIndices -> CellVisualState.WRONG_REVEAL
                    eval != null && cellIndex in eval.correctIndices -> CellVisualState.CORRECT_REVEAL
                    eval != null && cellIndex in eval.missedIndices -> CellVisualState.MISSED_REVEAL
                    else -> CellVisualState.IDLE
                }
            }
            else -> CellVisualState.IDLE
        }
    }
}
