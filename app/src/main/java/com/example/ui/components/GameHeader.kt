package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GameMode
import com.example.data.model.GamePhase
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintSuccess

@Composable
fun GameHeader(
    mode: GameMode,
    level: Int,
    score: Int,
    lives: Int,
    maxLives: Int,
    combo: Int,
    phase: GamePhase,
    targetCount: Int,
    selectedCount: Int,

    memorizeTimeTotal: Long,
    memorizeTimeRemaining: Long,

    recallTimeTotal: Long,
    recallTimeRemaining: Long,

    roundScoreEarned: Int,

    onExitClick: () -> Unit,
    onSettingClick: () -> Unit,

    modifier: Modifier = Modifier,

    sequenceStep: Int = 0,
    requiresStrictSequence: Boolean = false,
    successMessage: Int? = null,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {

        // =========================================================
        // TOP ROW
        // =========================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onExitClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .testTag("exit_game_button")
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.close_btn
                    ),
                    contentDescription = stringResource(
                        R.string.cd_exit_game
                    ),
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }

            LevelTag(level)

            IconButton(
                onClick = onSettingClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .testTag("settings_button")
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.settings_btn
                    ),
                    contentDescription = stringResource(
                        R.string.cd_settings
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // =========================================================
        // SCORE + COMBO
        // =========================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "%,d".format(score),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Image(
                    painter = painterResource(
                        R.drawable.coin_icon
                    ),
                    contentDescription = stringResource(
                        R.string.cd_coin
                    )
                )

                AnimatedVisibility(
                    visible =
                        roundScoreEarned > 0 &&
                                phase == GamePhase.SUCCESS,

                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {

                    Text(
                        text = " " + stringResource(
                            R.string.game_round_score_earned,
                            roundScoreEarned
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MintSuccess
                        ),
                        modifier = Modifier.padding(
                            start = 6.dp
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = combo >= 2,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberWarning.copy(
                        alpha = 0.2f
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = stringResource(
                                R.string.game_combo,
                                combo
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // =========================================================
        // INSTRUCTION + TIMER
        // =========================================================

        val instructionText = when (phase) {

            GamePhase.COUNTDOWN ->
                stringResource(
                    R.string.game_phase_get_ready
                )

            GamePhase.MEMORIZE ->
                if (requiresStrictSequence) {
                    stringResource(
                        R.string.game_phase_memorize_sequence
                    )
                } else {
                    stringResource(
                        R.string.game_phase_memorize
                    )
                }

            GamePhase.RECALL ->
                if (requiresStrictSequence) {
                    stringResource(
                        R.string.game_phase_recall_sequence
                    )
                } else {
                    stringResource(
                        R.string.game_phase_recall_select
                    )
                }

            GamePhase.EVALUATING ->
                stringResource(
                    R.string.game_phase_evaluating
                )

            GamePhase.SUCCESS ->
                ""

            GamePhase.FAILURE ->
                stringResource(
                    R.string.game_phase_try_again
                )

            else -> ""
        }

        val instructionColor by animateColorAsState(
            targetValue = when (phase) {

                GamePhase.MEMORIZE ->
                    ElectricCyan

                GamePhase.RECALL ->
                    MaterialTheme.colorScheme.primary

                GamePhase.SUCCESS ->
                    MintSuccess

                GamePhase.FAILURE ->
                    CoralError

                else ->
                    MaterialTheme.colorScheme.onBackground
            },

            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing
            ),

            label = "instructionColor"
        )

        // =========================================================
        // COUNTER
        // =========================================================

        val showCounter =
            phase == GamePhase.RECALL ||
                    (
                            phase == GamePhase.MEMORIZE &&
                                    requiresStrictSequence
                            )

        /*
         * QUAN TRỌNG:
         *
         * Không dùng:
         *
         * (recallTimeRemaining / 1000L) + 1
         *
         * vì 10000ms sẽ thành 11s.
         *
         * Dùng ceil:
         *
         * 10000 -> 10
         * 9500  -> 10
         * 5000  -> 5
         * 1000  -> 1
         * 500   -> 1
         * 0     -> 0
         */

        val secondsLeft =
            ((recallTimeRemaining + 999L) / 1000L)
                .coerceAtLeast(0L)

        // =========================================================
        // INSTRUCTION AREA
        // =========================================================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            contentAlignment = Alignment.Center
        ) {

            if (showCounter) {

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    // -------------------------------------------------
                    // LEFT: INSTRUCTION
                    // -------------------------------------------------

                    Text(
                        text = instructionText,

                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = instructionColor
                        ),

                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,

                        modifier = Modifier.weight(
                            1f,
                            fill = false
                        )
                    )

                    // -------------------------------------------------
                    // RIGHT: COUNTER
                    // -------------------------------------------------

                    Surface(
                        shape = RoundedCornerShape(10.dp),

                        color =
                            if (
                                phase == GamePhase.RECALL &&
                                secondsLeft <= 3
                            ) {
                                CoralError.copy(
                                    alpha = 0.15f
                                )
                            } else {
                                instructionColor.copy(
                                    alpha = 0.12f
                                )
                            }
                    ) {

                        Text(
                            text =
                                if (phase == GamePhase.RECALL) {

                                    "${
                                        stringResource(
                                            R.string.game_progress_format,
                                            selectedCount,
                                            targetCount
                                        )
                                    } • ${secondsLeft}s"

                                } else {

                                    stringResource(
                                        R.string.game_progress_format,
                                        sequenceStep,
                                        targetCount
                                    )
                                },

                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,

                                color =
                                    if (
                                        phase == GamePhase.RECALL &&
                                        secondsLeft <= 3
                                    ) {
                                        CoralError
                                    } else {
                                        instructionColor
                                    }
                            ),

                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            )
                        )
                    }
                }

            } else {

                // =====================================================
                // NO COUNTER
                // =====================================================

                if (
                    phase == GamePhase.SUCCESS &&
                    successMessage != null
                ) {

                    Image(
                        painter = painterResource(
                            id = successMessage
                        ),
                        contentDescription =
                            stringResource(
                                R.string.cd_completed
                            ),
                        modifier = Modifier.height(
                            28.dp
                        )
                    )

                } else {

                    Text(
                        text = instructionText,

                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = instructionColor
                        ),

                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        // =========================================================
        // PROGRESS BAR
        // =========================================================

        val isProgressVisible =
            phase == GamePhase.MEMORIZE ||
                    phase == GamePhase.RECALL

        val progress = when (phase) {

            GamePhase.MEMORIZE -> {

                if (requiresStrictSequence) {

                    (
                            sequenceStep.toFloat() /
                                    targetCount.coerceAtLeast(1)
                            ).coerceIn(0f, 1f)

                } else {

                    (
                            memorizeTimeRemaining.toFloat() /
                                    memorizeTimeTotal.coerceAtLeast(1L)
                            ).coerceIn(0f, 1f)
                }
            }

            GamePhase.RECALL -> {

                (
                        recallTimeRemaining.toFloat() /
                                recallTimeTotal.coerceAtLeast(1L)
                        ).coerceIn(0f, 1f)
            }

            else -> 0f
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .alpha(
                        if (isProgressVisible) {
                            1f
                        } else {
                            0f
                        }
                    )
                    .clip(
                        RoundedCornerShape(50.dp)
                    )
                    .background(
                        MaterialTheme.colorScheme
                            .surfaceVariant
                            .copy(alpha = 0.7f)
                    )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(
                            RoundedCornerShape(50.dp)
                        )
                        .background(
                            if (
                                phase == GamePhase.RECALL &&
                                secondsLeft <= 3
                            ) {
                                CoralError
                            } else {
                                instructionColor
                            }
                        )
                )
            }
        }
    }
}