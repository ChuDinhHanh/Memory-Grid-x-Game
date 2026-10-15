package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.PlayerProfileEntity
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldStar

private const val PRIVACY_POLICY_URL = "https://qr-scan-gilt.vercel.app/#privacy"

// ============================================================
// GLASS CARD (Liquid effect recipe)
// White 15% -> 5% fill, white 28% -> 8% border.
// ============================================================

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    onClick: (() -> Unit)? = null,
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        content()
    }
}

// ============================================================
// HOME SCREEN
// ============================================================

@Composable
fun HomeScreen(
    profile: PlayerProfileEntity?,
    onStartGame: () -> Unit,
    onOpenLeaderboard: () -> Unit = {},
    onOpenDailyChallenge: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    onOpenAchievements: () -> Unit = {},
    onOpenSettings: () -> Unit = {},

    isSoundOn: Boolean = true,
    isMusicOn: Boolean = true,
    isVibrationOn: Boolean = true,
    onSoundToggle: () -> Unit = {},
    onMusicToggle: () -> Unit = {},
    onVibrationToggle: () -> Unit = {},

    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    val currentXp = profile?.currentXp ?: 0
    val streak = profile?.currentStreak ?: 0
    val bestScore = profile?.bestScore ?: 0
    val bestLevel = profile?.bestLevel ?: 1

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ---------------- HEADER ----------------
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.home_title),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "🧠", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stringResource(R.string.home_tagline),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.68f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = {
                                onOpenSettings()
                                showSettingsDialog = true
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .testTag("home_settings_button")
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.settings_btn),
                                contentDescription = stringResource(R.string.home_settings_desc),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // ---------------- STREAK + XP (FULL GLASS/LIQUID STYLE) ----------------
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // 1. STREAK CARD
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AmberWarning.copy(alpha = 0.17f))
                                        .border(1.dp, AmberWarning.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🔥", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = context.resources.getQuantityString(
                                        R.plurals.home_streak_days, streak, streak
                                    ),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        // 2. XP CARD (GLASS LIQUID CUSTOM)
                        val level = (currentXp / 500) + 1
                        val currentLevelXp = currentXp % 500
                        val progress = currentLevelXp.toFloat() / 500f

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Vòng tròn level chuẩn style Liquid Cyan
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan.copy(alpha = 0.20f))
                                        .border(1.5.dp, ElectricCyan.copy(alpha = 0.50f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$level",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.home_best_level_value, level).uppercase(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "$currentLevelXp / 500 XP",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Thanh progress bar phong cách Glass
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color.White.copy(alpha = 0.12f))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(5.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(
                                                            ElectricCyan,
                                                            Color(0xFF00E5FF)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ---------------- HIGH SCORE + BEST LEVEL ----------------
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_high_score_title),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(text = "🏆", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "%,d".format(bestScore),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = stringResource(R.string.home_high_score_subtitle),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF76FF03)
                                )
                            }
                        }

                        GlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_best_level_title),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(text = "⭐", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = stringResource(R.string.home_best_level_value, bestLevel),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = stringResource(R.string.home_best_level_subtitle),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF76FF03)
                                )
                            }
                        }
                    }
                }

                // ---------------- STATISTICS + ACHIEVEMENTS ----------------
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShortcutCard(
                            icon = Icons.Default.QueryStats,
                            label = stringResource(R.string.home_statistics),
                            accent = ElectricCyan,
                            onClick = onOpenStats,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_stats_button")
                        )
                        ShortcutCard(
                            icon = Icons.Default.EmojiEvents,
                            label = stringResource(R.string.home_achievements),
                            accent = GoldStar,
                            onClick = onOpenAchievements,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_achievements_button")
                        )
                    }
                }

                // ---------------- START GAME ----------------
                item {
                    PrimaryButton(
                        text = stringResource(R.string.home_start_game),
                        onClick = onStartGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_play_button")
                    )
                }
            }

            // ---------------- SETTINGS DIALOG OVERLAY ----------------
            AnimatedVisibility(
                visible = showSettingsDialog,
                enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.85f),
                exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.85f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showSettingsDialog = false }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SettingsDialog(
                        isSoundOn = isSoundOn,
                        isMusicOn = isMusicOn,
                        isVibrationOn = isVibrationOn,
                        onSoundToggle = onSoundToggle,
                        onMusicToggle = onMusicToggle,
                        onVibrationToggle = onVibrationToggle,
                        onShareClick = {
                            showSettingsDialog = false
                            shareApp(context)
                        },
                        onPrivacyPolicyClick = {
                            showSettingsDialog = false
                            openPrivacyPolicy(context)
                        },
                        onDismissRequest = { showSettingsDialog = false },
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    )
                }
            }
        }
    }
}

/** Glass shortcut tile: tinted rounded-square icon + label */
@Composable
private fun ShortcutCard(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.17f))
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun openPrivacyPolicy(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.home_error_open_link), Toast.LENGTH_SHORT).show()
    }
}

private fun shareApp(context: Context) {
    try {
        val playStoreUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.home_share_message, playStoreUrl))
            type = "text/plain"
        }
        context.startActivity(
            Intent.createChooser(sendIntent, context.getString(R.string.home_share_chooser_title))
        )
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.home_error_share), Toast.LENGTH_SHORT).show()
    }
}