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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.GameHistoryEntity
import com.example.data.local.PlayerProfileEntity
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldStar

// Same glass recipe as AchievementsScreen: white 15% -> 5% fill, white 28% -> 8% border.
// `tint` lets a card pick up a subtle accent (current player, #1) without changing the look.
@Composable
private fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
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
                        if (isTinted) tint.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.15f),
                        if (isTinted) tint.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isTinted) tint.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.28f),
                        if (isTinted) tint.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.08f)
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
    topScores: List<GameHistoryEntity>,
    onUpdatePlayerName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }
    val defaultName = stringResource(id = R.string.leaderboard_default_player_name)
    val currentPlayerName = profile?.playerName.takeUnless { it.isNullOrBlank() } ?: defaultName
    var editNameText by remember { mutableStateOf(currentPlayerName) }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = Color(0xFF1A1F4D),
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

    // Transparent so the app-wide background shows through, exactly like AchievementsScreen.
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
                    .padding(top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.leaderboard_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = null,
                    tint = GoldStar,
                    modifier = Modifier.size(26.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 2.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // PROFILE CARD
                item {
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ElectricCyan.copy(alpha = 0.16f))
                                    .border(
                                        width = 1.dp,
                                        color = ElectricCyan.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentPlayerName,
                                        modifier = Modifier.weight(1f, fill = false),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = {
                                            editNameText = currentPlayerName
                                            showNameDialog = true
                                        },
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(28.dp)
                                            .testTag("edit_player_name_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(id = R.string.leaderboard_edit_name_desc),
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = stringResource(
                                        id = R.string.leaderboard_personal_best_format,
                                        profile?.bestScore ?: 0
                                    ),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Top-player badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GoldStar.copy(alpha = 0.16f))
                                    .border(1.dp, GoldStar.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = GoldStar,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.leaderboard_top_player_badge),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldStar
                                )
                            }
                        }
                    }
                }

                // SECTION TITLE
                item {
                    Text(
                        text = stringResource(id = R.string.leaderboard_section_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(start = 2.dp, top = 8.dp, bottom = 2.dp)
                    )
                }

                // LIST / EMPTY
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
    isCurrentPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    val medalColor: Color? = when (rank) {
        1 -> GoldStar
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
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
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge: same icon-tile treatment as achievement icons
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        medalColor?.copy(alpha = 0.17f) ?: Color.White.copy(alpha = 0.07f)
                    )
                    .border(
                        width = 1.dp,
                        color = medalColor?.copy(alpha = 0.40f) ?: Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = medalColor ?: Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playerName,
                    modifier = Modifier.weight(1f, fill = false),
                    fontSize = 14.sp,
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
                            .background(ElectricCyan.copy(alpha = 0.20f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "%,d".format(score),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldStar
                )
                Text(
                    text = stringResource(id = R.string.leaderboard_pts_suffix),
                    modifier = Modifier.padding(bottom = 2.dp),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
        }
    }
}