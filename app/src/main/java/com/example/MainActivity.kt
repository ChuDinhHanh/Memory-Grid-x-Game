package com.example

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.Utility.openPrivacyPolicy
import com.example.Utility.shareGame
import com.example.data.model.GameMode
import com.example.engine.AdsConfig
import com.example.engine.BannerAd
import com.example.engine.ReviewHelper
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
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume


class MainActivity : ComponentActivity() {
    private lateinit var rewardedAdManager: RewardedAdManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Khởi tạo manager trước khi tải quảng cáo.
        rewardedAdManager = RewardedAdManager(this)

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
                    modifier = Modifier.fillMaxSize(), color = Color.Transparent
                ) {
                    MemoryGridApp(
                        viewModel = viewModel,
                        rewardedAdManager = rewardedAdManager,
                        onInitializeForSplash = { reportProgress ->
                            initializeForSplash(viewModel, reportProgress)
                        }
                    )
                }
            }
        }
    }

    /**
     * Chạy trong lúc Splash hiển thị. Hàm này chỉ return khi dữ liệu, Remote Config,
     * Mobile Ads SDK và quảng cáo thưởng đã tải xong (hoặc hết timeout từng bước),
     * lúc đó Splash mới chuyển sang Home.
     */
    private suspend fun initializeForSplash(
        viewModel: MemoryGridViewModel,
        onProgress: (Float) -> Unit
    ) = coroutineScope {
        val totalSteps = 3
        var doneSteps = 0
        fun stepDone() {
            doneSteps++
            onProgress(doneSteps / totalSteps.toFloat())
        }

        // 1) Dữ liệu người chơi (Room) đã sẵn sàng
        val data = async {
            withTimeoutOrNull(STEP_TIMEOUT_MS) {
                viewModel.playerProfile.first { it != null }
            }
            stepDone()
        }

        // 2) Firebase Remote Config
        val config = async {
            runCatching { withTimeoutOrNull(STEP_TIMEOUT_MS) { awaitRemoteConfig() } }
                .onFailure { Log.w("Startup", "Remote Config fetch failed", it) }
            stepDone()
        }

        // 3) Mobile Ads SDK, rồi quảng cáo thưởng.
        // Chờ Remote Config xong trước, vì AdsConfig.reviveEnabled quyết định có tải quảng cáo hay không.
        val ads = async {
            config.await()
            runCatching {
                withTimeoutOrNull(STEP_TIMEOUT_MS) { awaitMobileAds() }
                withTimeoutOrNull(STEP_TIMEOUT_MS) { awaitRewardedAd() }
            }.onFailure { Log.w("Startup", "Ads initialization failed", it) }
            stepDone()
        }

        awaitAll(data, config, ads)
    }

    /** Chờ Remote Config tải xong (cả thành công lẫn thất bại). */
    private suspend fun awaitRemoteConfig() = suspendCancellableCoroutine<Unit> { cont ->
        AdsConfig.fetch {
            if (cont.isActive) cont.resume(Unit)
        }
    }

    /** Khởi tạo Mobile Ads SDK trên background thread, chờ callback hoàn tất. */
    private suspend fun awaitMobileAds() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { cont ->
            MobileAds.initialize(this@MainActivity) { status ->
                Log.d("RewardedAd", "Mobile Ads SDK initialized: $status")
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    /** Chờ quảng cáo thưởng tải xong (cả thành công lẫn thất bại). load() chạy trên main thread. */
    private suspend fun awaitRewardedAd() = suspendCancellableCoroutine<Unit> { cont ->
        rewardedAdManager.load {
            if (cont.isActive) cont.resume(Unit)
        }
    }

    private companion object {
        const val STEP_TIMEOUT_MS = 5_000L
    }
}

/**
 * Bọc nội dung của các màn hình CÓ banner.
 * Chừa sẵn một khoảng cố định ở trên (status bar + chiều cao banner + khoảng cách)
 * ngay từ đầu, nên khi banner load xong hay khi chuyển màn hình thì nội dung KHÔNG bị đẩy xuống.
 */
@Composable
private fun WithBannerSpace(topSpace: Dp, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = topSpace)
    ) {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MemoryGridApp(
    viewModel: MemoryGridViewModel,
    rewardedAdManager: RewardedAdManager,
    onInitializeForSplash: suspend ((Float) -> Unit) -> Unit
) {
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

    // Điều kiện hiển thị banner ở trên đầu
    val showTopBanner = currentRoute != "splash" && currentRoute != "game"

    // Chiều cao banner adaptive tính trước, để dành chỗ cố định (không chờ ad load xong mới có chiều cao)
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val bannerHeight: Dp = remember(screenWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
            .height.dp
    }
    // Khoảng trống phía trên nội dung = banner + 12dp (0 nếu tắt banner từ remote config)
    val topSpace: Dp = if (AdsConfig.bannerEnabled) bannerHeight + 12.dp else 0.dp

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
                        initialOffsetY = { it }),
                    exit = slideOutVertically(
                        targetOffsetY = { it }),
                ) {
                    Column {
                        CustomBottomBar(
                            currentRoute = currentRoute, onNavigate = { route ->
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
                            })
                    }
                }
            }) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.fillMaxSize()
            ) {
                // Splash + Game: toàn màn hình, không chừa chỗ banner
                composable("splash") {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(R.drawable.bg_splash),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        CustomSplashScreen(
                            onInitialize = { reportProgress ->
                                onInitializeForSplash(reportProgress)
                            },
                            onTimeout = {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable("home") {
                    WithBannerSpace(topSpace) {
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
                }

                composable("game") {
                    GameScreen(
                        uiState = uiState,
                        onCellClick = { index -> viewModel.onCellTapped(index) },
                        onExitClick = {
                            viewModel.exitToHome()
                            navController.popBackStack("home", inclusive = false)
                            ReviewHelper.onGameFinished(context)
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
                    // Lấy dữ liệu từ ViewModel qua State
                    val firebaseTopScores by viewModel.firebaseLeaderboard.collectAsState()

                    WithBannerSpace(topSpace) {
                        LeaderboardScreen(
                            profile = profile,
                            topScores = firebaseTopScores, // Truyền danh sách từ Firebase vào đây
                            onUpdatePlayerName = { viewModel.updatePlayerName(it) },
                            onUpdateAvatar = { viewModel.updatePlayerAvatar(it) }
                        )
                    }
                }

                composable("daily") {
                    WithBannerSpace(topSpace) {
                        DailyChallengeScreen(
                            profile = profile, onBack = { navController.popBackStack() })
                    }
                }

                composable("stats") {
                    WithBannerSpace(topSpace) {
                        StatisticsScreen(
                            profile = profile,
                            recentGames = recentGames,
                            onBack = { navController.popBackStack() })
                    }
                }

                composable("achievements") {
                    WithBannerSpace(topSpace) {
                        AchievementsScreen(
                            achievements = achievements, onBack = { navController.popBackStack() })
                    }
                }
            }
        }

        // Banner vẽ đè lên vùng trống đã chừa sẵn ở trên cùng; chỉ fade in/out, không đẩy layout.
        AnimatedVisibility(
            visible = showTopBanner,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight) // Chiều cao cố định, ad load xong cũng không đổi
            ) {
                BannerAd()
            }
        }
    }
}

data class BottomNavItem(
    val route: String, val title: String, val image: ImageVector, val testTag: String
)