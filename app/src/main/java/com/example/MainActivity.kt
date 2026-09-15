package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.GameMode
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MemoryGridTheme
import com.example.ui.theme.ThemeSetting
import com.example.ui.viewmodel.MemoryGridViewModel

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MemoryGridViewModel = viewModel()
            val profile by viewModel.playerProfile.collectAsState()

            val themeSetting = when (profile?.themeSetting) {
                "DARK" -> ThemeSetting.DARK
                "LIGHT" -> ThemeSetting.LIGHT
                else -> ThemeSetting.SYSTEM
            }

            MemoryGridTheme(themeSetting = themeSetting) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MemoryGridApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MemoryGridApp(viewModel: MemoryGridViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val uiState by viewModel.uiState.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val recentGames by viewModel.recentGames.collectAsState()
    val topScores by viewModel.topScores.collectAsState()
    val topLevels by viewModel.topLevels.collectAsState()

    val navItems = listOf(
        BottomNavItem("home", "Chơi", Icons.Default.GridOn, "bottom_nav_home"),
        BottomNavItem("leaderboard", "Xếp Hạng", Icons.Default.Leaderboard, "bottom_nav_leaderboard"),
        BottomNavItem("daily", "Hằng Ngày", Icons.Default.CalendarToday, "bottom_nav_daily"),
        BottomNavItem("stats", "Thống Kê", Icons.Default.QueryStats, "bottom_nav_stats"),
        BottomNavItem("settings", "Cài Đặt", Icons.Default.Settings, "bottom_nav_settings")
    )

    // Hide bottom bar during active gameplay to maximize grid focus and prevent misclicks
    val showBottomBar = currentRoute != "game"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    modifier = Modifier
                        .shadow(12.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .testTag("main_bottom_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            modifier = Modifier.testTag(item.testTag),
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElectricCyan,
                                selectedTextColor = ElectricCyan,
                                indicatorColor = ElectricCyan.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    profile = profile,
                    onStartGame = {
                        viewModel.startGame(GameMode.CLASSIC)
                        navController.navigate("game")
                    },
                    onOpenLeaderboard = {
                        navController.navigate("leaderboard")
                    },
                    onOpenDailyChallenge = {
                        navController.navigate("daily")
                    },
                    onOpenStats = {
                        navController.navigate("stats")
                    },
                    onOpenAchievements = {
                        navController.navigate("achievements")
                    },
                    onOpenSettings = {
                        navController.navigate("settings")
                    }
                )
            }

            composable("game") {
                GameScreen(
                    uiState = uiState,
                    getCellState = { index -> viewModel.getCellVisualState(index) },
                    onCellClick = { index -> viewModel.onCellTapped(index) },
                    onExitClick = {
                        viewModel.exitToHome()
                        navController.popBackStack("home", inclusive = false)
                    },
                    onPlayAgain = {
                        viewModel.startGame(uiState.gameMode)
                    },
                    onViewLeaderboard = {
                        viewModel.exitToHome()
                        navController.navigate("leaderboard")
                    }
                )
            }

            composable("leaderboard") {
                LeaderboardScreen(
                    profile = profile,
                    topScores = topScores,
                    topLevels = topLevels,
                    onUpdatePlayerName = { newName -> viewModel.updatePlayerName(newName) },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("daily") {
                DailyChallengeScreen(
                    profile = profile,
                    onStartDaily = {
                        viewModel.startGame(GameMode.DAILY)
                        navController.navigate("game")
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("stats") {
                StatisticsScreen(
                    profile = profile,
                    recentGames = recentGames,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("achievements") {
                AchievementsScreen(
                    achievements = achievements,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    profile = profile,
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onSetTheme = { themeKey -> viewModel.setThemeSetting(themeKey) },
                    onResetData = { viewModel.resetAllData() },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
