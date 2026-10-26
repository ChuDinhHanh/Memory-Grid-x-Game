package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.PlayerProfileEntity
import com.example.data.repository.LeaderboardEntry
import com.example.ui.components.AvatarSelectionDialog
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldStar

@Composable
private fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    tint: Color = Color.White,
    content: @Composable () -> Unit
) {
    val isTinted = tint != Color.White
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isTinted) tint.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.14f),
                        if (isTinted) tint.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isTinted) tint.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.35f),
                        if (isTinted) tint.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

@Composable
fun LeaderboardScreen(
    profile: PlayerProfileEntity?,
    topScores: List<LeaderboardEntry>,
    onUpdatePlayerName: (String) -> Unit,
    onUpdateAvatar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    val defaultName = stringResource(id = R.string.leaderboard_default_player_name)
    val currentPlayerName = profile?.playerName.takeUnless { it.isNullOrBlank() } ?: defaultName
    val currentAvatar = profile?.avatar.takeUnless { it.isNullOrBlank() } ?: "🐶"
    var editNameText by remember { mutableStateOf(currentPlayerName) }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = { selectedEmoji ->
                onUpdateAvatar(selectedEmoji)
            }
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = Color(0xFF161A3D),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = stringResource(id = R.string.leaderboard_dialog_edit_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(id = R.string.leaderboard_dialog_edit_subtitle),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { if (it.length <= 20) editNameText = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        label = { Text(stringResource(id = R.string.leaderboard_dialog_name_label)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = ElectricCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_name_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editNameText.trim()
                        if (trimmed.isNotEmpty()) onUpdatePlayerName(trimmed)
                        showNameDialog = false
                    },
                    modifier = Modifier.testTag("confirm_name_button")
                ) {
                    Text(
                        text = stringResource(id = R.string.leaderboard_dialog_save),
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text(
                        text = stringResource(id = R.string.leaderboard_dialog_cancel),
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.leaderboard_title),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = stringResource(id = R.string.leaderboard_subtitle),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GoldStar.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PROFILE CARD
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        tint = ElectricCyan
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(ElectricCyan.copy(alpha = 0.4f), Color.Transparent)
                                        )
                                    )
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.linearGradient(listOf(ElectricCyan, Color.White)),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { showAvatarDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentAvatar, fontSize = 30.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentPlayerName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .clickable {
                                                editNameText = currentPlayerName
                                                showNameDialog = true
                                            }
                                            .testTag("edit_player_name_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${stringResource(id = R.string.leaderboard_personal_best_prefix)}${profile?.bestScore ?: 0} PTS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(id = R.string.leaderboard_section_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
                    )
                }

                if (topScores.isEmpty()) {
                    item {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "🏆", fontSize = 38.sp)
                                Text(
                                    text = stringResource(id = R.string.leaderboard_empty_title),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(id = R.string.leaderboard_empty_subtitle),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(topScores) { index, record ->
                        SimpleLeaderboardRow(
                            rank = index + 1,
                            playerName = record.playerName,
                            score = record.score,
                            avatar = record.avatar.ifBlank { "🐶" },
                            isCurrentPlayer = record.playerName == currentPlayerName
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleLeaderboardRow(
    rank: Int,
    playerName: String,
    score: Int,
    avatar: String,
    isCurrentPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    val medalColor: Color? = when (rank) {
        1 -> GoldStar
        2 -> Color(0xFFE0E0E0)
        3 -> Color(0xFFD47A3B)
        else -> null
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_$rank"),
        tint = if (isCurrentPlayer) ElectricCyan else Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(medalColor?.copy(alpha = 0.2f) ?: Color.White.copy(alpha = 0.08f))
                    .border(
                        width = 1.dp,
                        color = medalColor?.copy(alpha = 0.5f) ?: Color.White.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = medalColor ?: Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = avatar,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 10.dp)
            )

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playerName,
                    modifier = Modifier.weight(1f, fill = false),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isCurrentPlayer) {
                    Text(
                        text = stringResource(id = R.string.leaderboard_you_tag),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricCyan.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "%,d".format(score),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldStar
                )
                Text(
                    text = stringResource(id = R.string.leaderboard_pts_label),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}