package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CustomProgressBar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

// Overshoot easing
fun OvershootEasing(tension: Float = 2.0f) = Easing { fraction ->
    val t = fraction - 1.0f
    t * t * ((tension + 1.0f) * t + tension) + 1.0f
}

@Composable
fun CustomSplashScreen(
    onInitialize: suspend (reportProgress: (Float) -> Unit) -> Unit,
    onTimeout: () -> Unit
) {
    // Luôn dùng phiên bản mới nhất của lambda, nhưng không restart LaunchedEffect.
    val currentOnInitialize by rememberUpdatedState(onInitialize)
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.8f) }

    // Giá trị tiến độ "thật" do onInitialize báo cáo.
    var targetProgress by remember { mutableFloatStateOf(0f) }

    // Chỉ MỘT nơi duy nhất điều khiển animation của progress bar
    // (trước đây 2 coroutine cùng gọi Animatable.animateTo nên giành nhau và làm
    // LaunchedEffect bị hủy -> onTimeout() không bao giờ được gọi).
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 250),
        label = "splashProgress"
    )

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(durationMillis = 700))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = OvershootEasing(1.5f)
                )
            )
        }

        val startedAt = System.currentTimeMillis()

        // Không cho tác vụ khởi tạo chặn Splash quá 12 giây (chốt chặn cuối).
        try {
            withTimeoutOrNull(12_000L) {
                currentOnInitialize { progress ->
                    targetProgress = progress.coerceIn(0f, 1f)
                }
            }
        } catch (e: CancellationException) {
            throw e // rời màn hình thì phải hủy đúng cách
        } catch (e: Exception) {
            // Khởi tạo lỗi -> vẫn đi tiếp vào Home.
        }

        // Đẩy thanh tiến độ về 100% và chờ animation chạy xong.
        targetProgress = 1f
        delay(300L)

        // Giữ Splash tối thiểu ~900ms để không bị chớp.
        val elapsed = System.currentTimeMillis() - startedAt
        if (elapsed < 900L) {
            delay(900L - elapsed)
        }

        currentOnTimeout()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = stringResource(
                id = R.string.splash_logo_content_description
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.TopCenter)
                .offset(y = 60.dp)
                .statusBarsPadding()
                .alpha(logoAlpha.value)
                .scale(logoScale.value),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 48.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val loadingStatus = when {
                animatedProgress < 0.4f ->
                    stringResource(id = R.string.splash_loading_memory)

                animatedProgress < 0.8f ->
                    stringResource(id = R.string.splash_loading_grid)

                else ->
                    stringResource(id = R.string.splash_loading_ready)
            }

            Text(
                text = loadingStatus,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomProgressBar(
                progress = animatedProgress,
                iconResId = R.drawable.splash_roc,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun CustomSplashScreenPreview() {
    CustomSplashScreen(
        onInitialize = { reportProgress ->
            reportProgress(0.25f)
            reportProgress(0.8f)
            reportProgress(1f)
        },
        onTimeout = {}
    )
}