package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.GameHistoryEntity
import com.example.data.local.PlayerProfileEntity
import com.example.engine.ScoringEngine
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.VividViolet
import java.text.NumberFormat

private const val MAX_RECENT_GAMES = 8

private fun Int.formatted(): String = NumberFormat.getIntegerInstance().format(this)

/**
 * Liquid-glass surface, same look as the one in AchievementsScreen,
 * with an optional accent tint for hero / stat cards.
 */
@Composable
private fun StatsGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(22.dp),
    tint: Color? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .then(
                if (tint != null) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(tint.copy(alpha = 0.14f), Color.Transparent)
                        )
                    )
                } else Modifier
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        (tint ?: Color.White).copy(alpha = if (tint != null) 0.45f else 0.28f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

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
    val accuracy = ScoringEngine.calculateAccuracy(totalCorrect, totalAttempted).coerceIn(0, 100)
    val dailyCompleted = profile?.totalDailyCompleted ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            StatisticsHeader(onBack)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 2.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    AccuracyCard(
                        accuracy = accuracy,
                        correct = totalCorrect,
                        incorrect = totalIncorrect,
                        attempted = totalAttempted
                    )
                }

                item {
                    SectionLabel(stringResource(R.string.section_overview))
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TileRow {
                            StatTile(
                                emoji = "🎮",
                                title = stringResource(R.string.tile_games_title),
                                value = totalGames.formatted(),
                                subtitle = stringResource(R.string.tile_games_sub),
                                accent = ElectricCyan,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                emoji = "🏆",
                                title = stringResource(R.string.tile_best_score_title),
                                value = bestScore.formatted(),
                                subtitle = stringResource(R.string.tile_best_score_sub),
                                accent = AmberWarning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        TileRow {
                            StatTile(
                                emoji = "⭐",
                                title = stringResource(R.string.tile_best_level_title),
                                value = stringResource(R.string.tile_best_level_value, bestLevel),
                                subtitle = stringResource(R.string.tile_best_level_sub),
                                accent = VividViolet,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                emoji = "🔥",
                                title = stringResource(R.string.tile_combo_title),
                                value = stringResource(R.string.tile_combo_value, bestCombo),
                                subtitle = stringResource(R.string.tile_combo_sub),
                                accent = CoralError,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        TileRow {
                            StatTile(
                                emoji = "📅",
                                title = stringResource(R.string.tile_streak_title),
                                value = pluralStringResource(
                                    R.plurals.streak_days,
                                    currentStreak,
                                    currentStreak
                                ),
                                subtitle = stringResource(R.string.tile_streak_sub, longestStreak),
                                accent = MintSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                emoji = "🎯",
                                title = stringResource(R.string.tile_daily_title),
                                value = dailyCompleted.formatted(),
                                subtitle = stringResource(R.string.tile_daily_sub),
                                accent = AmberWarning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item { RecentScoresCard(recentGames) }
                item {
                    Spacer(modifier = Modifier.padding(bottom = 50.dp))
                }
            }
        }
    }
}

@Composable
private fun StatisticsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
                .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_btn),
                contentDescription = stringResource(R.string.cd_back),
                )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.stats_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.stats_subtitle),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.68f)
            )
        }
    }
}

@Composable
private fun AccuracyCard(accuracy: Int, correct: Int, incorrect: Int, attempted: Int) {
    val trackColor = Color.White.copy(alpha = 0.10f)

    StatsGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tint = ElectricCyan
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.accuracy_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(modifier = Modifier.size(116.dp), contentAlignment = Alignment.Center) {
                    Canvas(
                        Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    ) {
                        val strokeWidth = 12.dp.toPx()
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        if (accuracy > 0) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(ElectricCyan, MintSuccess, ElectricCyan)
                                ),
                                startAngle = -90f,
                                sweepAngle = accuracy / 100f * 360f,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.accuracy_percent, accuracy),
                            fontSize = 28.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.accuracy_label),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.62f)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendRow(MintSuccess, stringResource(R.string.legend_correct), correct)
                    LegendRow(CoralError, stringResource(R.string.legend_incorrect), incorrect)
                    LegendRow(AmberWarning, stringResource(R.string.legend_total), attempted)
                }
            }
        }
    }
}

@Composable
private fun RecentScoresCard(recentGames: List<GameHistoryEntity>) {
    StatsGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.recent_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.recent_subtitle, MAX_RECENT_GAMES),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(20.dp))

            if (recentGames.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.recent_empty),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                }
                return@Column
            }

            val games = recentGames.take(MAX_RECENT_GAMES).reversed()
            val maxScore = games.maxOf { it.score }.coerceAtLeast(1)
            val average = games.map { it.score }.average().toInt()
            val lastIndex = games.lastIndex

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                games.forEachIndexed { index, game ->
                    val isLatest = index == lastIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = game.score.formatted(),
                            fontSize = 10.sp,
                            fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Medium,
                            color = if (isLatest) Color.White else Color.White.copy(alpha = 0.55f),
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                        Spacer(Modifier.height(6.dp))
                        val fraction = (game.score.toFloat() / maxScore).coerceIn(0.04f, 1f)
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val barAlpha = if (isLatest) 1f else 0.45f
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 30.dp)
                                    .fillMaxWidth(0.75f)
                                    .height(maxHeight * fraction)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 8.dp,
                                            topEnd = 8.dp,
                                            bottomStart = 3.dp,
                                            bottomEnd = 3.dp
                                        )
                                    )
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                ElectricCyan.copy(alpha = barAlpha),
                                                VividViolet.copy(alpha = barAlpha)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = if (isLatest) 0.30f else 0.10f),
                                        shape = RoundedCornerShape(
                                            topStart = 8.dp,
                                            topEnd = 8.dp,
                                            bottomStart = 3.dp,
                                            bottomEnd = 3.dp
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_axis),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
                Text(
                    text = stringResource(R.string.recent_average, average.formatted()),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.recent_best, maxScore.formatted()),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
        }
    }
}

@Composable
private fun TileRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}

@Composable
private fun LegendRow(color: Color, label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White.copy(alpha = 0.68f)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = value.formatted(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            color = Color.White
        )
    }
}

@Composable
private fun StatTile(
    emoji: String,
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    StatsGlassCard(
        modifier = modifier.heightIn(min = 132.dp),
        shape = RoundedCornerShape(22.dp),
        tint = accent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.18f))
                        .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 18.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.68f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.weight(1f, fill = false))
            Text(
                text = value,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = Color.White.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}