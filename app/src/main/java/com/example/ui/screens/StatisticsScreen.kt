package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameHistoryEntity
import com.example.data.local.PlayerProfileEntity
import com.example.engine.ScoringEngine
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.VividViolet

@Composable
fun StatisticsScreen(
    profile: PlayerProfileEntity?,
    recentGames: List<GameHistoryEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalGames = profile?.totalGamesPlayed ?: 0
    val bestScore = profile?.bestScore ?: 0
    val bestLevel = profile?.bestLevel ?: 1
    val bestCombo = profile?.bestCombo ?: 0
    val currentStreak = profile?.currentStreak ?: 0
    val longestStreak = profile?.longestStreak ?: 0
    val totalCorrect = profile?.totalCorrectCells ?: 0
    val totalAttempted = profile?.totalCellsAttempted ?: 0
    val totalIncorrect = (totalAttempted - totalCorrect).coerceAtLeast(0)
    val accuracy = ScoringEngine.calculateAccuracy(totalCorrect, totalAttempted)
    val dailyCompleted = profile?.totalDailyCompleted ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .testTag("stats_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Thống Kê Trí Nhớ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accuracy Donut Meter Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Donut Canvas
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val trackColor = MaterialTheme.colorScheme.surfaceVariant

                            Canvas(modifier = Modifier.size(100.dp)) {
                                val strokeWidth = 12.dp.toPx()
                                // Background circle
                                drawArc(
                                    color = trackColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                // Active sweep
                                val sweep = (accuracy / 100f) * 360f
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(ElectricCyan, MintSuccess, ElectricCyan)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$accuracy%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Độ chính xác",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }

                        // Counts breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MintSuccess)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đúng: %,d ô".format(totalCorrect),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CoralError)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sai: %,d ô".format(totalIncorrect),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AmberWarning)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tổng chọn: %,d".format(totalAttempted),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Overview 2x2 Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Trận Đã Chơi",
                        value = "%,d".format(totalGames),
                        subtitle = "Tổng số trận",
                        iconEmoji = "🎮",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Điểm Cao Nhất",
                        value = "%,d".format(bestScore),
                        subtitle = "Kỷ lục cá nhân",
                        iconEmoji = "🏆",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Cấp Cao Nhất",
                        value = "Cấp $bestLevel",
                        subtitle = "Màn cao nhất",
                        iconEmoji = "⭐",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Chuỗi Combo",
                        value = "${bestCombo}x",
                        subtitle = "Vòng đúng liên tiếp",
                        iconEmoji = "🔥",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Chuỗi Ngày",
                        value = "$currentStreak Ngày",
                        subtitle = "Kỷ lục: $longestStreak Ngày",
                        iconEmoji = "📅",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Thử Thách Ngày",
                        value = "$dailyCompleted",
                        subtitle = "Đã hoàn thành",
                        iconEmoji = "🎯",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Games History Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Lịch Sử Điểm Số Gần Đây",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (recentGames.isEmpty()) {
                            Text(
                                text = "Chưa có dữ liệu ván chơi gần đây. Hãy chơi ngay một lượt!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(vertical = 18.dp)
                            )
                        } else {
                            val maxRecentScore = recentGames.maxOfOrNull { it.score }?.coerceAtLeast(100) ?: 100
                            val gamesToShow = recentGames.take(8).reversed()

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                val barWidth = 22.dp.toPx()
                                val spacing = (size.width - (gamesToShow.size * barWidth)) / (gamesToShow.size + 1)

                                gamesToShow.forEachIndexed { index, game ->
                                    val barHeight = (game.score.toFloat() / maxRecentScore) * (size.height - 20.dp.toPx())
                                    val x = spacing + index * (barWidth + spacing)
                                    val y = size.height - barHeight

                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(ElectricCyan, VividViolet)
                                        ),
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Các ván gần nhất",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "Cao nhất: %,d đ".format(maxRecentScore),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
