package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GamePhase
import com.example.ui.components.GameHeader
import com.example.ui.components.MemoryGrid
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StopGameDialog
import com.example.ui.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    onCellClick: (Int) -> Unit,
    onExitClick: () -> Unit,
    onPlayAgain: () -> Unit,
    onRewardedContinue: () -> Unit,

    // Settings
    isSoundOn: Boolean = true,
    isMusicOn: Boolean = true,
    isVibrationOn: Boolean = true,

    onSoundToggle: () -> Unit = {},
    onMusicToggle: () -> Unit = {},
    onVibrationToggle: () -> Unit = {},

    onShareClick: () -> Unit = {},
    onRateUsClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},

    modifier: Modifier = Modifier,
    onViewLeaderboard: (() -> Unit)? = null,
    onPauseGame: () -> Unit = {},
    onResumeGame: () -> Unit = {}
) {
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // =========================================================
    // GAME OVER
    // =========================================================
    if (
        uiState.phase == GamePhase.RESULT &&
        uiState.gameEndSummary != null
    ) {
        GameOverScreen(
            summary = uiState.gameEndSummary,
            onPlayAgain = onPlayAgain,
            onRewardedContinue = onRewardedContinue,
            canUseRewardedContinue = !uiState.rewardedContinueUsed,
            onGoHome = onExitClick,
            onViewLeaderboard = onViewLeaderboard
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // =====================================================
            // MAIN GAME CONTENT
            // =====================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // GAME HEADER
                GameHeader(
                    mode = uiState.gameMode,
                    level = uiState.level,
                    score = uiState.score,
                    lives = uiState.lives,
                    maxLives = uiState.maxLives,
                    combo = uiState.combo,
                    phase = uiState.phase,
                    targetCount = uiState.targetPattern.size,
                    selectedCount = uiState.selectedCells.size,

                    // MEMORIZE TIMER
                    memorizeTimeTotal = uiState.memorizeTimeTotalMs,
                    memorizeTimeRemaining = uiState.memorizeTimeRemainingMs,

                    // RECALL TIMER
                    recallTimeTotal = uiState.recallTimeTotalMs,
                    recallTimeRemaining = uiState.recallTimeRemainingMs,

                    roundScoreEarned = uiState.roundScoreEarned,

                    // BUTTONS (TẠM DỪNG ĐẾM NGƯỢC KHI MỞ DIALOG)
                    onExitClick = {
                        onPauseGame()
                        showExitDialog = true
                    },
                    onSettingClick = {
                        onPauseGame()
                        showSettingsDialog = true
                    },

                    sequenceStep = uiState.sequenceStep,
                    requiresStrictSequence = uiState.requiresStrictSequence,
                    successMessage = uiState.successMessage
                )

                Spacer(modifier = Modifier.weight(1f))

                // MEMORY GRID
                MemoryGrid(
                    gridSize = uiState.gridSize,
                    phase = uiState.phase,
                    revealedSequenceIndices = uiState.revealedSequenceIndices,
                    currentlyLitCell = uiState.currentlyLitCell,
                    selectedCells = uiState.selectedCells,
                    targetPattern = uiState.targetPattern,
                    evaluationResult = uiState.evaluationResult,
                    activeStepNumberMap = uiState.activeStepNumberMap,
                    onCellClick = onCellClick,
                    modifier = Modifier.testTag("memory_grid_board")
                )

                Spacer(modifier = Modifier.weight(1.2f))
            }

            // =====================================================
            // COUNTDOWN OVERLAY
            // =====================================================
            AnimatedVisibility(
                visible = uiState.phase == GamePhase.COUNTDOWN,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                val scale = remember { Animatable(0.5f) }

                LaunchedEffect(uiState.countdownNumber) {
                    scale.snapTo(0.5f)
                    scale.animateTo(
                        targetValue = 1.3f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${uiState.countdownNumber}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 110.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.scale(scale.value)
                    )
                }
            }

            // =====================================================
            // SETTINGS OVERLAY
            // =====================================================
            AnimatedVisibility(
                visible = showSettingsDialog,
                enter = fadeIn(animationSpec = tween(200)) +
                        scaleIn(
                            initialScale = 0.85f,
                            animationSpec = tween(250, easing = FastOutSlowInEasing)
                        ),
                exit = fadeOut(animationSpec = tween(150)) +
                        scaleOut(
                            targetScale = 0.85f,
                            animationSpec = tween(150)
                        ),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                showSettingsDialog = false
                                onResumeGame() // Tiết tục đếm ngược khi bấm nền tối
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SettingsDialog(
                        isSoundOn = isSoundOn,
                        isMusicOn = isMusicOn,
                        isVibrationOn = isVibrationOn,

                        onSoundToggle = onSoundToggle,
                        onMusicToggle = onMusicToggle,
                        onVibrationToggle = onVibrationToggle,

                        onShareClick = {
                            showSettingsDialog = false
                            onResumeGame()
                            onShareClick()
                            shareApp(context)
                        },
                        onPrivacyPolicyClick = {
                            showSettingsDialog = false
                            onResumeGame()
                            onPrivacyPolicyClick()
                            openPrivacyPolicy(context)
                        },
                        onDismissRequest = {
                            showSettingsDialog = false
                            onResumeGame() // Tiếp tục đếm ngược khi đóng dialog
                        },
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Chặn click xuyên qua dialog
                        )
                    )
                }
            }
        }
    }

    // =========================================================
    // EXIT CONFIRMATION
    // =========================================================
    if (showExitDialog) {
        StopGameDialog(
            onConfirm = {
                showExitDialog = false
                onResumeGame() // Tiếp tục chơi nếu không thoát
            },
            onCancel = {
                showExitDialog = false
                onExitClick()
            },
            confirmButtonRes = com.example.R.drawable.continue_btn,
            cancelButtonRes = com.example.R.drawable.stope_btn
        )
    }
}

// =========================================================
// HELPER FUNCTIONS
// =========================================================

private fun openPrivacyPolicy(context: Context) {
    val url = "https://qr-scan-gilt.vercel.app/#privacy"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
    }
}

private fun shareApp(context: Context) {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(
                Intent.EXTRA_TEXT,
                "🎮 Check out this awesome Memory Game! Download now: https://play.google.com/store/apps/details?id=${context.packageName}"
            )
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share game via")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot share app", Toast.LENGTH_SHORT).show()
    }
}