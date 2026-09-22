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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.GameMode
import com.example.engine.RewardedAdManager
import com.example.ui.components.CustomBottomBar
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.CustomSplashScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MemoryGridTheme
import com.example.ui.theme.ThemeSetting
import com.example.ui.viewmodel.MemoryGridViewModel


class MainActivity : ComponentActivity() {
    private lateinit var rewardedAdManager: RewardedAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // ĐÃ GỠ BỎ installSplashScreen() ĐỂ BỎ SPLASH CŨ CỦA HỆ THỐNG
        super.onCreate(savedInstanceState)
        rewardedAdManager = RewardedAdManager(this).also { it.initializeAndLoad() }
        enableEdgeToEdge()
        setContent {
            val viewModel: MemoryGridViewModel = viewModel()
            val profile by viewModel.playerProfile.collectAsState()

            val themeSetting = when (profile?.themeSetting) {
                "DARK" -> ThemeSetting.DARK
                "LIGHT" -> ThemeSetting.LIGHT
                // The game art direction is intentionally the bright purple
                // game theme, regardless of the device's system theme.
                else -> ThemeSetting.LIGHT
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
    
    // Mặc định khởi đầu bằng màn hình "splash" lung linh bạn vừa tạo
    val currentRoute = navBackStackEntry?.destination?.route ?: "splash"

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

    val showBottomBar = currentRoute != "splash" && currentRoute != "game"

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        bottomBar = {

            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it }
                )
            ) {

                CustomBottomBar(
                    currentRoute = currentRoute,

                    onNavigate = { route ->

                        if (currentRoute != route) {

                            navController.navigate(route) {

                                popUpTo(
                                    navController.graph.findStartDestination().id
                                ) {
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

    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                CustomSplashScreen(onTimeout = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }

            composable("home") {
                HomeScreen(
                    profile = profile,
                    onStartGame = { viewModel.startGame(GameMode.CLASSIC); navController.navigate("game") },
                    onOpenLeaderboard = { navController.navigate("leaderboard") },
                    onOpenDailyChallenge = { navController.navigate("daily") },
                    onOpenStats = { navController.navigate("stats") },
                    onOpenAchievements = { navController.navigate("achievements") },
                    onOpenSettings = { navController.navigate("settings") }
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
                    onPlayAgain = { viewModel.startGame(uiState.gameMode) },
                    onRewardedContinue = {
                        rewardedAdManager.show { viewModel.continueCurrentLevelAfterReward() }
                    },
                    onViewLeaderboard = {
                        viewModel.exitToHome()
                        navController.navigate("leaderboard")
                    }
                )
            }

            composable("leaderboard") {
                LeaderboardScreen(profile, topScores, topLevels, { viewModel.updatePlayerName(it) }, { navController.popBackStack() })
            }

            composable("daily") {
                DailyChallengeScreen(profile, { viewModel.startGame(GameMode.DAILY); navController.navigate("game") }, { navController.popBackStack() })
            }

            composable("stats") {
                StatisticsScreen(profile, recentGames, { navController.popBackStack() })
            }

            composable("achievements") {
                AchievementsScreen(achievements, { navController.popBackStack() })
            }

            composable("settings") {
                SettingsScreen(
                    profile = profile,
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onSetTheme = { themeKey -> viewModel.setThemeSetting(themeKey) },
                    onResetData = { viewModel.resetAllData() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector, val testTag: String)
