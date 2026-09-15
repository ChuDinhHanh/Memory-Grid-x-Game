package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameEndSummary
import com.example.data.model.GameMode
import com.example.ui.components.PrimaryButton
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.VividViolet

@Composable
fun GameOverScreen(
    summary: GameEndSummary,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit,
    onViewLeaderboard: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New High Score Banner or Title
            if (summary.isNewHighScore) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GoldStar.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldStar)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎉", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KỶ LỤC MỚI!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = GoldStar,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            // Title
            Text(
                text = if (summary.gameMode == GameMode.DAILY) {
                    "HOÀN THÀNH THỬ THÁCH"
                } else {
                    "CHỌN SAI Ô!"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = if (summary.gameMode == GameMode.DAILY) MintSuccess else CoralError
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = if (summary.gameMode == GameMode.DAILY) {
                    "Bạn đã hoàn thành thử thách ghi nhớ hôm nay!"
                } else {
                    "Chỉ một sai sót nhỏ! Bạn đã dừng lại ở Cấp ${summary.levelReached}."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            // Main Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ĐIỂM SỐ",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "%,d".format(summary.finalScore),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MintSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+${summary.xpEarned} XP Kinh Nghiệm",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MintSuccess
                            )
                        )
                    }
                }
            }

            // Grid Stats breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Cấp Đạt Được",
                    value = "Cấp ${summary.levelReached}",
                    subtitle = "Kỷ lục: Cấp ${summary.bestLevel}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Độ Chính Xác",
                    value = "${summary.accuracyPercentage}%",
                    subtitle = "Chuỗi: ${summary.longestCombo}x",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryButton(
                    text = if (summary.gameMode == GameMode.DAILY) "CHƠI LẠI THỬ THÁCH" else "CHƠI LẠI TỪ LEVEL 1",
                    onClick = onPlayAgain,
                    gradientColors = listOf(ElectricCyan, VividViolet),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    modifier = Modifier.testTag("game_over_play_again_button")
                )

                if (onViewLeaderboard != null) {
                    OutlinedButton(
                        onClick = onViewLeaderboard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("game_over_leaderboard_button"),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldStar.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = GoldStar
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "XEM BẢNG XẾP HẠNG",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldStar
                            )
                        )
                    }
                }

                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("game_over_home_button"),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VỀ TRANG CHỦ",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}
