package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    roundScoreEarned: Int,
    onExitClick: () -> Unit,
    modifier: Modifier = Modifier,
    sequenceStep: Int = 0,
    requiresStrictSequence: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top row: Exit button, Mode & Level, Lives
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
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .testTag("exit_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Thoát ván chơi",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Mode & Level
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = mode.displayName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Cấp $level",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (requiresStrictSequence) ElectricCyan.copy(alpha = 0.15f) else MintSuccess.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        if (requiresStrictSequence) ElectricCyan else MintSuccess
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = if (requiresStrictSequence) "⚡ Bắt buộc thứ tự 1➔2➔3" else "✨ Chọn tự do",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (requiresStrictSequence) ElectricCyan else MintSuccess,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Lives indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..maxLives) {
                    val hasLife = i <= lives
                    Icon(
                        imageVector = if (hasLife) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (hasLife) "Mạng sống $i" else "Mất mạng $i",
                        tint = if (hasLife) CoralError else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second row: Score & Combo Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Điểm: ",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "%,d".format(score),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                AnimatedVisibility(
                    visible = roundScoreEarned > 0 && phase == GamePhase.SUCCESS,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Text(
                        text = " +$roundScoreEarned",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MintSuccess
                        ),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            // Combo flame badge
            AnimatedVisibility(
                visible = combo >= 2,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberWarning.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 Combo $combo",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Phase instruction & Countdown bar
        val instructionText = when (phase) {
            GamePhase.COUNTDOWN -> "Chuẩn bị..."
            GamePhase.MEMORIZE -> {
                if (requiresStrictSequence) {
                    if (sequenceStep > 0) "Ô sáng theo thứ tự: $sequenceStep / $targetCount" else "Ghi nhớ THỨ TỰ các ô sáng..."
                } else {
                    "Ghi nhớ các ô sáng..."
                }
            }
            GamePhase.RECALL -> {
                if (requiresStrictSequence) {
                    "Chạm ĐÚNG THỨ TỰ (Bước ${selectedCount + 1} / $targetCount) • Sai là về Level 1!"
                } else {
                    "Chạm các ô đã sáng ($selectedCount / $targetCount) • Sai là về Level 1!"
                }
            }
            GamePhase.EVALUATING -> "Đang kiểm tra kết quả..."
            GamePhase.SUCCESS -> "Chính xác! Lên cấp độ tiếp theo..."
            GamePhase.FAILURE -> {
                if (requiresStrictSequence) {
                    "Sai thứ tự hoặc sai ô! Chơi lại từ Level 1..."
                } else {
                    "Chọn sai ô! Chơi lại từ Level 1..."
                }
            }
            else -> ""
        }

        val instructionColor by animateColorAsState(
            targetValue = when (phase) {
                GamePhase.MEMORIZE -> ElectricCyan
                GamePhase.RECALL -> MaterialTheme.colorScheme.primary
                GamePhase.SUCCESS -> MintSuccess
                GamePhase.FAILURE -> CoralError
                else -> MaterialTheme.colorScheme.onBackground
            },
            label = "instructionColor"
        )

        Text(
            text = instructionText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = instructionColor
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Progress bar
        if (phase == GamePhase.MEMORIZE) {
            val progress = if (requiresStrictSequence) {
                (sequenceStep.toFloat() / targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
            } else {
                (memorizeTimeRemaining.toFloat() / memorizeTimeTotal.coerceAtLeast(1L)).coerceIn(0f, 1f)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ElectricCyan,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else if (phase == GamePhase.RECALL) {
            // Selection progress bar
            val progress = (selectedCount.toFloat() / targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
