package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AchievementEntity
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintSuccess

@Composable
private fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
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
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
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
fun AchievementsScreen(
    achievements: List<AchievementEntity>,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val unlockedCount = achievements.count { it.isUnlocked }

    val overallProgress = if (achievements.isEmpty()) {
        0f
    } else {
        unlockedCount.toFloat() / achievements.size
    }

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

            // HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.achievements_screen_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = stringResource(
                        id = R.string.achievements_unlocked_badges_format,
                        unlockedCount,
                        achievements.size
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.68f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // SUMMARY CARD
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GoldStar.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.achievements_summary_trophy_emoji),
                            fontSize = 23.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.achievements_progress_format,
                                unlockedCount,
                                achievements.size
                            ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = stringResource(id = R.string.achievements_summary_trophy),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.62f)
                        )
                    }

                    Text(
                        text = stringResource(
                            id = R.string.achievements_progress_percent,
                            (overallProgress * 100).toInt()
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldStar
                    )
                }
            }

            // ACHIEVEMENT LIST
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    top = 2.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = achievements,
                    key = { it.id }
                ) { achievement ->

                    val maxProgress = achievement.maxProgress.coerceAtLeast(1)
                    val progress = (achievement.currentProgress.toFloat() / maxProgress).coerceIn(0f, 1f)

                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("achievement_card_${achievement.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 13.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // ACHIEVEMENT ICON
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (achievement.isUnlocked) {
                                            GoldStar.copy(alpha = 0.17f)
                                        } else {
                                            Color.White.copy(alpha = 0.07f)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (achievement.isUnlocked) {
                                            GoldStar.copy(alpha = 0.35f)
                                        } else {
                                            Color.White.copy(alpha = 0.08f)
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = achievement.icon,
                                    fontSize = 23.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                // TITLE + UNLOCKED STATUS
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = achievement.title,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        lineHeight = 19.sp
                                    )

                                    if (achievement.isUnlocked) {
                                        Spacer(modifier = Modifier.width(6.dp))

                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = stringResource(
                                                id = R.string.achievements_icon_unlocked_desc
                                            ),
                                            tint = MintSuccess,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = achievement.description,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.65f),
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(11.dp))

                                // PROGRESS BAR
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = if (achievement.isUnlocked) {
                                        GoldStar
                                    } else {
                                        ElectricCyan
                                    },
                                    trackColor = Color.White.copy(alpha = 0.10f)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // PROGRESS STATUS
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (achievement.isUnlocked) {
                                            stringResource(id = R.string.achievements_status_completed)
                                        } else {
                                            stringResource(
                                                id = R.string.achievements_progress_format,
                                                achievement.currentProgress,
                                                achievement.maxProgress
                                            )
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (achievement.isUnlocked) {
                                            MintSuccess
                                        } else {
                                            Color.White.copy(alpha = 0.55f)
                                        }
                                    )

                                    if (!achievement.isUnlocked) {
                                        Text(
                                            text = stringResource(
                                                id = R.string.achievements_progress_percent,
                                                (progress * 100).toInt()
                                            ),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ElectricCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}