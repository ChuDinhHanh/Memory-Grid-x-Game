package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameHistoryEntity
import com.example.data.local.PlayerProfileEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldStar
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.VividViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LeaderboardScreen(
    profile: PlayerProfileEntity?,
    topScores: List<GameHistoryEntity>,
    topLevels: List<GameHistoryEntity>,
    onUpdatePlayerName: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf(profile?.playerName ?: "Người chơi") }

    val displayedList = if (selectedTab == 0) topScores else topLevels
    val currentPlayerName = profile?.playerName ?: "Người chơi"

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = {
                Text(
                    text = "Đổi tên hiển thị",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tên này sẽ xuất hiện trên bảng xếp hạng kỷ lục:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { if (it.length <= 20) editNameText = it },
                        singleLine = true,
                        label = { Text("Tên của bạn") },
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
                        if (trimmed.isNotEmpty()) {
                            onUpdatePlayerName(trimmed)
                        }
                        showNameDialog = false
                    },
                    modifier = Modifier.testTag("confirm_name_button")
                ) {
                    Text("Lưu tên")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("leaderboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Bảng Xếp Hạng",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Player Profile Bar with Edit Name Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(ElectricCyan, VividViolet))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentPlayerName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = {
                                            editNameText = currentPlayerName
                                            showNameDialog = true
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .padding(start = 4.dp)
                                            .testTag("edit_player_name_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Sửa tên",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Kỷ lục: %,d đ • Cấp %d".format(profile?.bestScore ?: 0, profile?.bestLevel ?: 1),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldStar.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldStar.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = GoldStar,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Top Player",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldStar
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Tab Switcher
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectricCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "🏆 Điểm Cao Nhất",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        modifier = Modifier.testTag("tab_top_scores")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "⭐ Cấp Độ Đạt Được",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        modifier = Modifier.testTag("tab_top_levels")
                    )
                }
            }

            // Top 3 Podium
            if (displayedList.isNotEmpty()) {
                item {
                    LeaderboardPodium(
                        items = displayedList.take(3),
                        isScoreTab = selectedTab == 0,
                        currentPlayerName = currentPlayerName
                    )
                }
            }

            // Section Header
            item {
                Text(
                    text = "Bảng Xếp Hạng Chi Tiết",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (displayedList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🎮", fontSize = 40.sp)
                            Text(
                                text = "Chưa có lượt chơi nào",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Hãy bắt đầu chơi Thang Độ Khó để ghi danh vào bảng vàng!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(displayedList) { index, record ->
                    LeaderboardRow(
                        rank = index + 1,
                        record = record,
                        isScoreTab = selectedTab == 0,
                        isCurrentPlayer = record.playerName == currentPlayerName
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardPodium(
    items: List<GameHistoryEntity>,
    isScoreTab: Boolean,
    currentPlayerName: String,
    modifier: Modifier = Modifier
) {
    // Standard podium ordering: 2nd place (left), 1st place (center), 3rd place (right)
    val first = items.getOrNull(0)
    val second = items.getOrNull(1)
    val third = items.getOrNull(2)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place (Silver)
        PodiumCol(
            rank = 2,
            record = second,
            pedestalHeight = 110.dp,
            badgeColor = Color(0xFFC0C0C0),
            isScoreTab = isScoreTab,
            isCurrentPlayer = second?.playerName == currentPlayerName,
            modifier = Modifier.weight(1f)
        )

        // 1st Place (Gold)
        PodiumCol(
            rank = 1,
            record = first,
            pedestalHeight = 140.dp,
            badgeColor = GoldStar,
            isScoreTab = isScoreTab,
            isCurrentPlayer = first?.playerName == currentPlayerName,
            modifier = Modifier.weight(1.15f)
        )

        // 3rd Place (Bronze)
        PodiumCol(
            rank = 3,
            record = third,
            pedestalHeight = 90.dp,
            badgeColor = Color(0xFFCD7F32),
            isScoreTab = isScoreTab,
            isCurrentPlayer = third?.playerName == currentPlayerName,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PodiumCol(
    rank: Int,
    record: GameHistoryEntity?,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    badgeColor: Color,
    isScoreTab: Boolean,
    isCurrentPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (record != null) {
            // Crown or Medal for rank
            Text(
                text = when (rank) {
                    1 -> "👑"
                    2 -> "🥈"
                    else -> "🥉"
                },
                fontSize = if (rank == 1) 28.sp else 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = record.playerName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isCurrentPlayer) FontWeight.Black else FontWeight.Bold,
                    color = if (isCurrentPlayer) ElectricCyan else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isScoreTab) "%,d đ".format(record.score) else "Cấp ${record.levelReached}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Pedestal block
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (rank == 1) 0.85f else 0.5f),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                badgeColor.copy(alpha = if (rank == 1) 0.8f else 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = badgeColor,
                        fontSize = if (rank == 1) 28.sp else 22.sp
                    )
                )
                if (record != null) {
                    Text(
                        text = if (isScoreTab) "Cấp ${record.levelReached}" else "%,d đ".format(record.score),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(
    rank: Int,
    record: GameHistoryEntity,
    isScoreTab: Boolean,
    isCurrentPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    val rankBadgeColor = when (rank) {
        1 -> GoldStar
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateString = remember(record.timestamp) { dateFormatter.format(Date(record.timestamp)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_$rank"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlayer) {
                ElectricCyan.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentPlayer) ElectricCyan.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (rank <= 3) rankBadgeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = if (rank <= 3) rankBadgeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Info & Date
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.playerName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentPlayer) ElectricCyan else MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrentPlayer) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricCyan.copy(alpha = 0.2f),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "BẠN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = ElectricCyan,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Cấp ${record.levelReached} • Độ chính xác ${record.accuracy}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "• $dateString",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Score / Level Display
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isScoreTab) "%,d".format(record.score) else "Cấp ${record.levelReached}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isScoreTab) GoldStar else MintSuccess
                    )
                )
                Text(
                    text = if (isScoreTab) "điểm" else "%,d đ".format(record.score),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
