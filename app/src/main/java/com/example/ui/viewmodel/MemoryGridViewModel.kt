package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.MemoryGridDatabase
import com.example.data.local.PlayerProfileEntity
import com.example.data.model.DifficultyConfig
import com.example.data.model.GameEndSummary
import com.example.data.model.GameMode
import com.example.data.model.GamePhase
import com.example.data.model.RoundEvaluationResult
import com.example.data.repository.LeaderboardRepository
import com.example.data.repository.MemoryGridRepository
import com.example.engine.AudioHapticService
import com.example.engine.DifficultyEngine
import com.example.engine.PatternGenerator
import com.example.engine.ScoringEngine
import com.example.worker.SocialMediaCheckWorker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


data class GameUiState(
    val phase: GamePhase = GamePhase.IDLE,
    val isPaused: Boolean = false,
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
    val recallTimeTotalMs: Long = 10000L,       // Tổng thời gian đếm ngược Recall 10s
    val recallTimeRemainingMs: Long = 10000L,    // Thời gian Recall còn lại
    val roundScoreEarned: Int = 0,
    val roundBonusEarned: Int = 0,
    val evaluationResult: RoundEvaluationResult? = null,
    val gameEndSummary: GameEndSummary? = null,
    val dailyChallengeDateKey: String = "",
    val dailyChallengeAlreadyCompletedToday: Boolean = false,
    val requiresStrictSequence: Boolean = false,
    val selectedSequence: List<Int> = emptyList(),
    val rewardedContinueUsed: Boolean = false,
    val successMessage: Int = com.example.R.drawable.success_txt
)

private val successMessages = listOf(
    com.example.R.drawable.success_txt,
    com.example.R.drawable.waouh_txt,
    com.example.R.drawable.amazing_txt,
    com.example.R.drawable.good_txt,
    com.example.R.drawable.awesome_txt
)

class MemoryGridViewModel(application: Application) : AndroidViewModel(application) {
    private val leaderboardRepo = LeaderboardRepository()
    private val db = MemoryGridDatabase.getDatabase(application)
    private val repository = MemoryGridRepository(db.memoryGridDao())
    val audioHaptic = AudioHapticService(application)

    val firebaseLeaderboard = leaderboardRepo.getTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val playerProfile: StateFlow<PlayerProfileEntity?> =
        repository.playerProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements = repository.achievements.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val recentGames = repository.recentGames.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val topScores = repository.topScores.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val topLevels = repository.topLevels.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private var phaseTimerJob: Job? = null
    private var recallTimerJob: Job? = null
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
                    if (profile.bestScore > 0) {
                        syncOfflineScoreToCloud()
                    }
                }
            }
        }
        viewModelScope.launch {
            val periodicWorkRequest = PeriodicWorkRequestBuilder<SocialMediaCheckWorker>(
                30, TimeUnit.MINUTES // Kiểm tra mỗi 30 phút một lần
            ).build()

            // Dùng 'application' thay vì 'applicationContext'
            WorkManager.getInstance(application).enqueueUniquePeriodicWork(
                "SocialMediaCheckJob",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }

    fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun startGame(mode: GameMode = GameMode.CLASSIC) {
        phaseTimerJob?.cancel()
        recallTimerJob?.cancel()
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
        recallTimerJob?.cancel()
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
        recallTimerJob?.cancel()
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
            recallTimeTotalMs = 10000L,
            recallTimeRemainingMs = 10000L,
            evaluationResult = null,
            roundScoreEarned = 0,
            roundBonusEarned = 0,
            requiresStrictSequence = isStrictSequence
        )

        phaseTimerJob?.cancel()
        if (!isStrictSequence) {
            _uiState.value = _uiState.value.copy(
                revealedSequenceIndices = pattern.toSet(), currentlyLitCell = null
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
                    currentlyLitCell = null, revealedSequenceIndices = pattern.toSet()
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

        val recallDuration = 10000L // 10 Giây
        _uiState.value = _uiState.value.copy(
            phase = GamePhase.RECALL,
            memorizeTimeRemainingMs = 0L,
            recallTimeTotalMs = recallDuration,
            recallTimeRemainingMs = recallDuration
        )

        // BẮT ĐẦU ĐẾM NGƯỢC 10 GIÂY DÀNH CHO PHASE RECALL
        recallTimerJob?.cancel()
        recallTimerJob = viewModelScope.launch {
            val interval = 50L
            var timeRemaining = recallDuration

            while (timeRemaining > 0) {
                delay(interval)
                timeRemaining -= interval
                _uiState.value = _uiState.value.copy(
                    recallTimeRemainingMs = maxOf(0L, timeRemaining)
                )
            }

            // Hết 10 giây mà người chơi chưa chọn xong -> Tự động thua!
            val currentState = _uiState.value
            if (currentState.phase == GamePhase.RECALL) {
                evaluateFailure(wrongCellIndex = -1, allSelected = currentState.selectedCells)
            }
        }
    }

    fun onCellTapped(cellIndex: Int) {
        val state = _uiState.value
        if (state.phase != GamePhase.RECALL) return

        if (cellIndex in state.selectedCells) return // Cannot pick same cell twice

        val isStrict = state.requiresStrictSequence

        if (!isStrict) {
            if (cellIndex !in state.targetPattern) {
                val newSelected = state.selectedCells + cellIndex
                _uiState.value = state.copy(selectedCells = newSelected)
                evaluateFailure(wrongCellIndex = cellIndex, allSelected = newSelected)
                return
            }

            val newSelected = state.selectedCells + cellIndex
            val newSeq = state.selectedSequence + cellIndex
            _uiState.value = state.copy(
                selectedCells = newSelected, selectedSequence = newSeq
            )
            audioHaptic.playTap()
            audioHaptic.vibrateTap()

            if (newSelected.size == state.targetPattern.size) {
                evaluateSuccess(newSelected)
            }
        } else {
            val expectedStep = state.selectedSequence.size
            val expectedCell = state.targetPattern[expectedStep]

            if (cellIndex != expectedCell) {
                val newSelected = state.selectedCells + cellIndex
                _uiState.value = state.copy(selectedCells = newSelected)
                evaluateFailure(wrongCellIndex = cellIndex, allSelected = newSelected)
                return
            }

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
        recallTimerJob?.cancel() // Dừng bộ đếm ngược 10s khi thành công

        val randomSuccessMessage = successMessages.random()
        _uiState.value = _uiState.value.copy(
            phase = GamePhase.EVALUATING,
            successMessage = randomSuccessMessage
        )

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

        if (state.gameMode == GameMode.DAILY) {
            viewModelScope.launch {
                val accuracy =
                    ScoringEngine.calculateAccuracy(correctCellsInGame, cellsAttemptedInGame)
                repository.saveDailyChallengeCompleted(
                    state.dailyChallengeDateKey, newTotalScore, accuracy
                )
                saveGameEndSummary(newTotalScore, state.level, isSuccess = true)
            }
            return
        }

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
        recallTimerJob?.cancel() // Dừng bộ đếm ngược 10s khi thất bại

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
            wrongSelected = if (wrongCellIndex != -1) 1 else 0,
            baseScore = 0,
            memoryBonus = 0,
            streakMultiplier = 1.0f,
            roundScore = 0,
            xpEarned = 0,
            correctIndices = correctList,
            wrongIndices = if (wrongCellIndex != -1) listOf(wrongCellIndex) else emptyList(),
            missedIndices = missedList
        )

        _uiState.value = state.copy(
            phase = GamePhase.FAILURE, lives = 0, combo = 0, evaluationResult = evalResult
        )

        audioHaptic.playWrong()
        audioHaptic.vibrateWrong()

        phaseTimerJob = viewModelScope.launch {
            delay(1300)
            saveGameEndSummary(state.score, state.level, isSuccess = false)
        }
    }

    private suspend fun saveGameEndSummary(finalScore: Int, levelReached: Int, isSuccess: Boolean) {
        val currentProfile = playerProfile.value
        val bestScore = currentProfile?.bestScore ?: 0
        val isNewHigh = finalScore > bestScore
        val accuracy = ScoringEngine.calculateAccuracy(correctCellsInGame, cellsAttemptedInGame)
        val xpEarned =
            ScoringEngine.calculateXpGained(levelReached, _uiState.value.gameMode == GameMode.DAILY)

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
            phase = GamePhase.RESULT, gameEndSummary = summary
        )

        viewModelScope.launch {
            try {
                val deviceId = android.provider.Settings.Secure.ANDROID_ID
                leaderboardRepo.submitScore(
                    userId = deviceId,
                    playerName = currentProfile?.playerName ?: "Player",
                    score = finalScore,
                    avatar = currentProfile?.avatar ?: "🐶"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun exitToHome() {
        phaseTimerJob?.cancel()
        recallTimerJob?.cancel()
        _uiState.value = GameUiState(phase = GamePhase.IDLE)
    }

    fun updatePlayerAvatar(newAvatar: String) {
        viewModelScope.launch {
            // 1. Lưu xuống Room database cục bộ
            repository.updatePlayerAvatar(newAvatar)

            // 2. Đẩy lên Firebase Firestore ngay lập tức (giữ nguyên điểm số và tên hiện tại)
            try {
                val currentProfile = playerProfile.value
                val deviceId = android.provider.Settings.Secure.ANDROID_ID
                val currentScore = currentProfile?.bestScore ?: 0
                val currentName = currentProfile?.playerName ?: "Player"

                // Gửi kèm avatar lên Firestore (collection leaderboard)
                val docRef =
                    FirebaseFirestore.getInstance().collection("leaderboard").document(deviceId)

                // Lấy dữ liệu cũ hoặc tạo map mới bao gồm avatar
                val updates = mapOf(
                    "userId" to deviceId,
                    "playerName" to currentName,
                    "score" to currentScore,
                    "avatar" to newAvatar, // Cập nhật avatar mới lên cloud
                    "timestamp" to System.currentTimeMillis()
                )
                docRef.set(updates, com.google.firebase.firestore.SetOptions.merge()).await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun continueCurrentLevelAfterReward() {
        val state = _uiState.value

        if (state.phase != GamePhase.RESULT || state.gameMode == GameMode.DAILY) {
            return
        }

        phaseTimerJob?.cancel()
        recallTimerJob?.cancel()

        _uiState.value = state.copy(
            phase = GamePhase.COUNTDOWN,
            countdownNumber = 3,
            lives = 1,
            combo = 0,
            selectedCells = emptySet(),
            selectedSequence = emptyList(),
            currentlyLitCell = null,
            revealedSequenceIndices = emptySet(),
            activeStepNumberMap = emptyMap(),
            evaluationResult = null,
            gameEndSummary = null
        )

        startCountdown()
    }


    fun setThemeSetting(theme: String) {
        val profile = playerProfile.value ?: return
        viewModelScope.launch {
            repository.updateSettings(
                sound = profile.soundEnabled, haptics = profile.hapticsEnabled, theme = theme
            )
        }
    }

    fun updatePlayerName(newName: String) {
        viewModelScope.launch {
            // 1. Cập nhật tên trong Room database cục bộ
            repository.updatePlayerName(newName)

            // 2. Đồng thời đẩy tên mới lên Firebase Leaderboard ngay lập tức với điểm hiện tại
            try {
                val currentProfile = playerProfile.value
                val deviceId = android.provider.Settings.Secure.ANDROID_ID
                val currentScore = currentProfile?.bestScore ?: 0
                val currentAvatar = currentProfile?.avatar ?: "🐶"

                leaderboardRepo.submitScore(
                    userId = deviceId,
                    playerName = newName,
                    score = currentScore,
                    avatar = currentAvatar
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            exitToHome()
        }
    }
    // Thêm 2 hàm pauseGame và resumeGame vào class MemoryGridViewModel:

    fun pauseGame() {
        val state = _uiState.value
        // Chỉ tạm dừng khi đang ở các phase có chạy đếm ngược (MEMORIZE hoặc RECALL)
        if (state.phase == GamePhase.MEMORIZE || state.phase == GamePhase.RECALL) {
            phaseTimerJob?.cancel()
            recallTimerJob?.cancel()
            _uiState.value = state.copy(isPaused = true)
        }
    }

    fun resumeGame() {
        val state = _uiState.value
        if (!state.isPaused) return

        _uiState.value = state.copy(isPaused = false)

        // Nếu đang ở phase Memorize, tiếp tục đếm ngược thời gian Memorize còn lại
        if (state.phase == GamePhase.MEMORIZE) {
            val interval = 50L
            var timeRemaining = state.memorizeTimeRemainingMs

            phaseTimerJob = viewModelScope.launch {
                while (timeRemaining > 0) {
                    delay(interval)
                    timeRemaining -= interval
                    _uiState.value = _uiState.value.copy(
                        memorizeTimeRemainingMs = maxOf(0L, timeRemaining)
                    )
                }
                _uiState.value = _uiState.value.copy(revealedSequenceIndices = emptySet())
                delay(120L)
                transitionToRecall()
            }
        }
        // Nếu đang ở phase Recall, tiếp tục đếm ngược thời gian Recall còn lại
        else if (state.phase == GamePhase.RECALL) {
            val interval = 50L
            var timeRemaining = state.recallTimeRemainingMs

            recallTimerJob = viewModelScope.launch {
                while (timeRemaining > 0) {
                    delay(interval)
                    timeRemaining -= interval
                    _uiState.value = _uiState.value.copy(
                        recallTimeRemainingMs = maxOf(0L, timeRemaining)
                    )
                }

                val currentState = _uiState.value
                if (currentState.phase == GamePhase.RECALL) {
                    evaluateFailure(wrongCellIndex = -1, allSelected = currentState.selectedCells)
                }
            }
        }
    }

    // =========================================================
    // SETTINGS TOGGLES (SOUND / MUSIC / VIBRATION)
    // =========================================================

    fun toggleSound() {
        val profile = playerProfile.value ?: return
        val newSoundState = !profile.soundEnabled

        // Cập nhật trạng thái tức thì cho AudioHapticService
        audioHaptic.soundEnabled = newSoundState

        viewModelScope.launch {
            repository.updateSettings(
                sound = newSoundState,
                haptics = profile.hapticsEnabled,
                theme = profile.themeSetting
            )
        }
    }

    fun toggleMusic() {
        val profile = playerProfile.value ?: return
        // Giả sử logic lưu Music chung hoặc lưu cấu hình riêng
        val newSoundState = !profile.soundEnabled

        viewModelScope.launch {
            repository.updateSettings(
                sound = newSoundState,
                haptics = profile.hapticsEnabled,
                theme = profile.themeSetting
            )
        }
    }

    fun toggleHaptics() {
        val profile = playerProfile.value ?: return
        val newHapticsState = !profile.hapticsEnabled

        // Cập nhật trạng thái tức thì cho AudioHapticService
        audioHaptic.hapticsEnabled = newHapticsState

        viewModelScope.launch {
            repository.updateSettings(
                sound = profile.soundEnabled,
                haptics = newHapticsState,
                theme = profile.themeSetting
            )
        }
    }

    // Bạn có thể gọi hàm này khi app khởi động hoặc khi có kết nối mạng trở lại
    fun syncOfflineScoreToCloud() {
        viewModelScope.launch {
            val currentProfile = playerProfile.value
            if (currentProfile != null && currentProfile.bestScore > 0) {
                try {
                    val deviceId = android.provider.Settings.Secure.ANDROID_ID
                    // Gọi submitScore đã có logic kiểm tra maxOf ở LeaderboardRepository
                    leaderboardRepo.submitScore(
                        userId = deviceId,
                        playerName = currentProfile.playerName,
                        score = currentProfile.bestScore,
                        avatar = currentProfile.avatar
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}