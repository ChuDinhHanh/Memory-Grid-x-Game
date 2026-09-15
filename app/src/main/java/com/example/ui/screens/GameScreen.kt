package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CellVisualState
import com.example.data.model.GamePhase
import com.example.ui.components.GameHeader
import com.example.ui.components.MemoryGrid
import com.example.ui.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    getCellState: (Int) -> CellVisualState,
    onCellClick: (Int) -> Unit,
    onExitClick: () -> Unit,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
    onViewLeaderboard: (() -> Unit)? = null
) {
    var showExitDialog by remember { mutableStateOf(false) }

    // If game ended, show GameOverScreen
    if (uiState.phase == GamePhase.RESULT && uiState.gameEndSummary != null) {
        GameOverScreen(
            summary = uiState.gameEndSummary,
            onPlayAgain = onPlayAgain,
            onGoHome = onExitClick,
            onViewLeaderboard = onViewLeaderboard
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    memorizeTimeTotal = uiState.memorizeTimeTotalMs,
                    memorizeTimeRemaining = uiState.memorizeTimeRemainingMs,
                    roundScoreEarned = uiState.roundScoreEarned,
                    onExitClick = { showExitDialog = true },
                    sequenceStep = uiState.sequenceStep,
                    requiresStrictSequence = uiState.requiresStrictSequence
                )

                Spacer(modifier = Modifier.weight(1f))

                // The Central Memory Grid
                MemoryGrid(
                    gridSize = uiState.gridSize,
                    phase = uiState.phase,
                    getCellState = getCellState,
                    onCellClick = onCellClick,
                    activeStepNumberMap = uiState.activeStepNumberMap,
                    modifier = Modifier.testTag("memory_grid_board")
                )

                Spacer(modifier = Modifier.weight(1.2f))
            }

            // Countdown Animated Overlay
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
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
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
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Dừng ván chơi?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(text = "Bạn có chắc chắn muốn rời khỏi ván chơi này? Tiến trình của ván đấu hiện tại sẽ không được lưu.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onExitClick()
                    }
                ) {
                    Text("RỜI ĐI", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("TIẾP TỤC CHƠI")
                }
            }
        )
    }
}
