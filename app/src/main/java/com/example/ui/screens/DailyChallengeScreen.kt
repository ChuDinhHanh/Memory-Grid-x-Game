package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.PlayerProfileEntity
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintSuccess
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ============================================================
// GLASS CARD CONTAINER
// ============================================================

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

private data class DayStreakStatus(
    val dayNumber: Int,
    val dateKey: String,
    val isToday: Boolean,
    val isCompleted: Boolean,
    val isPast: Boolean
)

@Composable
fun DailyChallengeScreen(
    profile: PlayerProfileEntity?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatPattern = stringResource(id = R.string.daily_challenge_date_format)
    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayFormatted = remember(dateFormatPattern) {
        SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(Date())
    }
    val isCompletedToday = profile?.lastDailyChallengeDate == todayKey
    val currentStreak = profile?.currentStreak ?: 0

    // List of days in the current month
    val monthDays = remember {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val yearMonthPrefix = SimpleDateFormat("yyyy-MM-", Locale.getDefault()).format(Date())

        (1..maxDays).map { day ->
            val dateKey = "$yearMonthPrefix${String.format(Locale.US, "%02d", day)}"
            DayStreakStatus(
                dayNumber = day,
                dateKey = dateKey,
                isToday = day == currentDay,
                isCompleted = (day == currentDay && isCompletedToday) || (day < currentDay && day % 2 == 0),
                isPast = day < currentDay
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ====================================================
            // HEADER & BACK BUTTON
            // ====================================================
            item {
                Column {
                    Text(
                        text = stringResource(id = R.string.daily_challenge_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = todayFormatted,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // ====================================================
            // STREAK SUMMARY CARD
            // ====================================================
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AmberWarning.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = AmberWarning,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(
                                        id = R.string.daily_challenge_streak_count,
                                        currentStreak
                                    ),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(
                                        id = if (isCompletedToday) {
                                            R.string.daily_challenge_completed_today
                                        } else {
                                            R.string.daily_challenge_not_completed_today
                                        }
                                    ),
                                    fontSize = 12.sp,
                                    color = if (isCompletedToday) MintSuccess else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCompletedToday) MintSuccess.copy(alpha = 0.25f) else AmberWarning.copy(alpha = 0.25f),
                            border = BorderStroke(
                                1.dp,
                                if (isCompletedToday) MintSuccess.copy(alpha = 0.5f) else AmberWarning.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = stringResource(
                                    id = if (isCompletedToday) {
                                        R.string.daily_challenge_status_done
                                    } else {
                                        R.string.daily_challenge_status_not_done
                                    }
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompletedToday) MintSuccess else AmberWarning
                            )
                        }
                    }
                }
            }

            // ====================================================
            // GITHUB-STYLE MONTHLY CONTRIBUTION GRID
            // ====================================================
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.daily_challenge_monthly_grid_title),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.daily_challenge_month_format,
                                    Calendar.getInstance().get(Calendar.MONTH) + 1
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            items(monthDays) { day ->
                                val cellBgColor = when {
                                    day.isCompleted -> MintSuccess
                                    day.isToday -> Color.White.copy(alpha = 0.35f)
                                    day.isPast -> Color.White.copy(alpha = 0.1f)
                                    else -> Color.White.copy(alpha = 0.05f)
                                }

                                val borderColor = when {
                                    day.isToday -> Color.White
                                    day.isCompleted -> MintSuccess.copy(alpha = 0.8f)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cellBgColor)
                                        .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${day.dayNumber}",
                                            fontSize = 11.sp,
                                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (day.isToday) Color.White else Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ====================================================
            // SPECS GRID
            // ====================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = stringResource(id = R.string.daily_challenge_grid_size_title),
                            value = stringResource(id = R.string.daily_challenge_grid_size_value),
                            subtitle = stringResource(id = R.string.daily_challenge_grid_size_subtitle),
                            iconEmoji = "📐",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = stringResource(id = R.string.daily_challenge_tiles_to_remember_title),
                            value = stringResource(id = R.string.daily_challenge_tiles_to_remember_value),
                            subtitle = stringResource(id = R.string.daily_challenge_tiles_to_remember_subtitle),
                            iconEmoji = "🧠",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ====================================================
            // REWARD BANNER
            // ====================================================
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎁", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.daily_challenge_reward_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldStar
                            )
                            Text(
                                text = stringResource(id = R.string.daily_challenge_reward_subtitle),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}