package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.GameEndSummary
import com.example.data.model.GameMode
import com.example.ui.theme.MintSuccess

@Composable
fun GameOverScreen(
    summary: GameEndSummary,
    onPlayAgain: () -> Unit,
    onRewardedContinue: () -> Unit,
    canUseRewardedContinue: Boolean,
    onGoHome: () -> Unit,
    onViewLeaderboard: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember {
        Animatable(0.88f)
    }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f, animationSpec = tween(
                durationMillis = 400, easing = FastOutSlowInEasing
            )
        )
    }

    val isDaily = summary.gameMode == GameMode.DAILY

    // =========================================================
    // RIBBON
    // =========================================================

    val ribbonPainter = painterResource(
        id = R.drawable.header_model
    )

    val ribbonRatio = ribbonPainter.intrinsicSize.let { size ->
        if (size.width > 0f && size.height > 0f && size.width.isFinite() && size.height.isFinite()) {
            size.width / size.height
        } else {
            2.5f
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter
    ) {

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value)
                .padding(vertical = 16.dp)
        ) {

            // =================================================
            // RIBBON SIZE
            // =================================================

            val ribbonWidth = maxWidth
            val ribbonHeight = ribbonWidth / ribbonRatio

            // Ribbon overlap percentage on top of card
            val ribbonOverlapFraction = 0.25f

            // Card offset top
            val cardTop = ribbonHeight * (1f - ribbonOverlapFraction)

            // Content padding inside card to avoid overlap
            val contentTopPadding = ribbonHeight * ribbonOverlapFraction + 4.dp

            // =================================================
            // CARD
            // =================================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp)
                    .padding(top = cardTop)
                    .shadow(
                        elevation = 4.dp, shape = RoundedCornerShape(26.dp), clip = false
                    )
                    .clip(
                        RoundedCornerShape(26.dp)
                    )
                    .background(Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = contentTopPadding, bottom = 16.dp
                        ), horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // =================================================
                    // REWARD
                    // =================================================

                    Text(
                        text = stringResource(id = R.string.game_over_reward_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2387D7),
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Container tổng để định vị Badge XP đè lên góc ngoài của Capsule
                    Box(
                        modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.TopEnd
                    ) {
                        // 1. Capsule chứa điểm + icon coins
                        Row(
                            modifier = Modifier
                                .padding(
                                    top = 6.dp, end = 6.dp
                                ) // Tạo khoảng trống góc trên phải cho Badge XP
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE8F8FF))
                                .padding(vertical = 8.dp, horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "%,d".format(summary.finalScore.coerceAtLeast(0)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E75BB)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Image(
                                painter = painterResource(id = R.drawable.coins_img),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // 2. Badge XP nổi lên góc trên phải ngoài Capsule (không đè icon coin)
                        Text(
                            text = stringResource(
                                id = R.string.game_over_xp_format, summary.xpEarned
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MintSuccess,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-3).dp)
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 1.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    // =================================================
                    // STATS
                    // =================================================

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF4F8FC))
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        GameOverStat(
                            title = stringResource(id = R.string.game_over_stat_level),
                            value = "${summary.levelReached}"
                        )

                        GameOverStat(
                            title = stringResource(id = R.string.game_over_stat_accuracy),
                            value = stringResource(
                                id = R.string.game_over_stat_accuracy_format,
                                summary.accuracyPercentage
                            )
                        )

                        GameOverStat(
                            title = stringResource(id = R.string.game_over_stat_combo),
                            value = stringResource(
                                id = R.string.game_over_stat_combo_format, summary.longestCombo
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    // =================================================
                    // KEEP PLAYING
                    // =================================================

                    if (canUseRewardedContinue) {

                        GameOverImageButton(
                            imageRes = R.drawable.keeplay_btn,
                            contentDescription = stringResource(id = R.string.game_over_cd_keep_playing),
                            onClick = onRewardedContinue
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )
                    }

                    // =================================================
                    // REPLAY
                    // =================================================

                    val replayContentDescription = if (isDaily) {
                        stringResource(id = R.string.game_over_cd_replay_daily)
                    } else {
                        stringResource(id = R.string.game_over_cd_replay_classic)
                    }

                    GameOverImageButton(
                        imageRes = R.drawable.replay_btn,
                        contentDescription = replayContentDescription,
                        onClick = onPlayAgain
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    // =================================================
                    // LEADERBOARD
                    // =================================================

                    if (onViewLeaderboard != null) {

                        GameOverImageButton(
                            imageRes = R.drawable.go_leaderboard_btn,
                            contentDescription = stringResource(id = R.string.game_over_cd_leaderboard),
                            onClick = onViewLeaderboard
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )
                    }

                    // =================================================
                    // HOME
                    // =================================================

                    GameOverImageButton(
                        imageRes = R.drawable.go_hom_btn,
                        contentDescription = stringResource(id = R.string.game_over_cd_home),
                        onClick = onGoHome
                    )
                }
            }

            // =================================================
            // RIBBON
            // =================================================

            Image(
                painter = ribbonPainter,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ribbonRatio)
                    .align(Alignment.TopCenter)
            )
        }
    }
}

// =============================================================
// IMAGE BUTTON
// =============================================================

@Composable
private fun GameOverImageButton(
    imageRes: Int, contentDescription: String, onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = onClick
            )
    )
}

// =============================================================
// STAT
// =============================================================

@Composable
private fun GameOverStat(
    title: String, value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E99A6)
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF2C3E50)
        )
    }
}

// =============================================================
// PREVIEW
// =============================================================

@Preview(
    showBackground = true, backgroundColor = 0xFFF2F2F2, widthDp = 360, heightDp = 800
)
@Composable
private fun GameOverScreenPreview() {

    GameOverScreen(
        summary = GameEndSummary(
        finalScore = 12500,
        xpEarned = 250,
        levelReached = 8,
        accuracyPercentage = 92,
        longestCombo = 15,
        gameMode = GameMode.CLASSIC,
        bestScore = 10000,
        bestLevel = 7,
        isNewHighScore = true
    ),
        onPlayAgain = {},
        onRewardedContinue = {},
        canUseRewardedContinue = true,
        onGoHome = {},
        onViewLeaderboard = {})
}