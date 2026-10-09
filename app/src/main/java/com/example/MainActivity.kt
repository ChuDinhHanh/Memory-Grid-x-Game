package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.Utility.openPrivacyPolicy
import com.example.Utility.shareGame
import com.example.data.model.GameMode
import com.example.engine.RewardedAdManager
import com.example.ui.components.CustomBottomBar
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.CustomSplashScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MemoryGridTheme
import com.example.ui.theme.ThemeSetting
import com.example.ui.viewmodel.MemoryGridViewModel

class MainActivity : ComponentActivity() {
    private lateinit var rewardedAdManager: RewardedAdManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rewardedAdManager = RewardedAdManager(this).also { it.initializeAndLoad() }
        enableEdgeToEdge()
        setContent {
            val viewModel: MemoryGridViewModel = viewModel()
            val profile by viewModel.playerProfile.collectAsState()

            val themeSetting = when (profile?.themeSetting) {
                "DARK" -> ThemeSetting.DARK
                "LIGHT" -> ThemeSetting.LIGHT
                else -> ThemeSetting.LIGHT
            }

            MemoryGridTheme(themeSetting = themeSetting) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    MemoryGridApp(viewModel = viewModel, rewardedAdManager = rewardedAdManager)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MemoryGridApp(viewModel: MemoryGridViewModel, rewardedAdManager: RewardedAdManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val currentRoute = navBackStackEntry?.destination?.route ?: "splash"

    val uiState by viewModel.uiState.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val recentGames by viewModel.recentGames.collectAsState()
    val topScores by viewModel.topScores.collectAsState()

    val showBottomBar = currentRoute != "splash" && currentRoute != "game"

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // App background image
        if (currentRoute != "splash") {
            Image(
                painter = painterResource(R.drawable.bg_app),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(
                        initialOffsetY = { it }
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it }
                    ),
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
                modifier = Modifier
                    .fillMaxSize()
            ) {
                composable("splash") {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(R.drawable.bg_splash),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        CustomSplashScreen(onTimeout = {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }
                }

                composable("home") {
                    HomeScreen(
                        profile = profile,
                        onStartGame = {
                            viewModel.startGame(GameMode.CLASSIC)
                            navController.navigate("game")
                        },
                        onOpenLeaderboard = { navController.navigate("leaderboard") },
                        onOpenDailyChallenge = { navController.navigate("daily") },
                        onOpenStats = { navController.navigate("stats") },
                        onOpenAchievements = { navController.navigate("achievements") },
                        onOpenSettings = {
                            /* SettingsDialog component handles this modal on HomeScreen */
                        },
                        isSoundOn = profile?.soundEnabled ?: true,
                        isMusicOn = profile?.soundEnabled ?: true,
                        isVibrationOn = profile?.hapticsEnabled ?: true,

                        onSoundToggle = viewModel::toggleSound,
                        onMusicToggle = viewModel::toggleSound,
                        onVibrationToggle = viewModel::toggleHaptics
                    )
                }

                composable("game") {
                    GameScreen(
                        uiState = uiState,
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
                        },
                        onShareClick = {
                            shareGame(context)
                        },
                        onPrivacyPolicyClick = {
                            openPrivacyPolicy(context)
                        },
                        onPauseGame = viewModel::pauseGame,
                        onResumeGame = viewModel::resumeGame,

                        isSoundOn = profile?.soundEnabled ?: true,
                        isMusicOn = profile?.soundEnabled ?: true,
                        isVibrationOn = profile?.hapticsEnabled ?: true,

                        onSoundToggle = viewModel::toggleSound,
                        onMusicToggle = viewModel::toggleSound,
                        onVibrationToggle = viewModel::toggleHaptics
                    )
                }

                composable("leaderboard") {
                    LeaderboardScreen(
                        profile = profile,
                        topScores = topScores,
                        onUpdatePlayerName = { viewModel.updatePlayerName(it) }
                    )
                }

                composable("daily") {
                    DailyChallengeScreen(
                        profile = profile,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("stats") {
                    StatisticsScreen(
                        profile = profile,
                        recentGames = recentGames,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("achievements") {
                    AchievementsScreen(
                        achievements = achievements,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val image: ImageVector,
    val testTag: String
)