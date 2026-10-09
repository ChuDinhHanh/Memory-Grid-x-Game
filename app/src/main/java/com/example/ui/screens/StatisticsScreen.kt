package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { StatisticsTopBar(onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
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
                            value = pluralStringResource(R.plurals.streak_days, currentStreak, currentStreak),
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
        }
    }
}

@Composable
private fun StatisticsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.stats_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccuracyCard(accuracy: Int, correct: Int, incorrect: Int, attempted: Int) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        // Soft tinted wash behind the content so the hero card stands apart from the tiles
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(
                        ElectricCyan.copy(alpha = 0.14f),
                        VividViolet.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                )
            )
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = stringResource(R.string.accuracy_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(modifier = Modifier.size(116.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize().padding(6.dp)) {
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.accuracy_label),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
private fun RecentScoresCard(recentGames: List<GameHistoryEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = stringResource(R.string.recent_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.recent_subtitle, MAX_RECENT_GAMES),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            if (recentGames.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.recent_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                games.forEachIndexed { index, game ->
                    val isLatest = index == lastIndex
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = game.score.formatted(),
                            fontSize = 10.sp,
                            fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Medium,
                            color = if (isLatest) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                        Spacer(Modifier.height(6.dp))
                        val fraction = (game.score.toFloat() / maxScore).coerceIn(0.04f, 1f)
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Latest game gets the full gradient; older games are quieter
                            val barAlpha = if (isLatest) 1f else 0.45f
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 30.dp)
                                    .fillMaxWidth(0.75f)
                                    .height(maxHeight * fraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                ElectricCyan.copy(alpha = barAlpha),
                                                VividViolet.copy(alpha = barAlpha)
                                            )
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.recent_average, average.formatted()),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.recent_best, maxScore.formatted()),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TileRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun LegendRow(color: Color, label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = value.formatted(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
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
    Card(
        modifier = modifier.heightIn(min = 132.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.18f)),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}