package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.OvershootEasing
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CustomProgressBar
import kotlinx.coroutines.launch

@Composable
fun CustomSplashScreen(onTimeout: () -> Unit) {
    // 1. Biến hiệu ứng cho Logo (Hiện dần + Phóng to)
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.8f) }
    
    // 2. Biến hiệu ứng cho Thanh Progress
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Chạy đồng thời các hiệu ứng
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(1000))
        }
        launch {
            // Hiệu ứng phóng to nhẹ (bung ra) bằng OvershootEasing
            logoScale.animateTo(
                targetValue = 1f, 
                animationSpec = tween(1200, easing = OvershootEasing(1.5f))
            )
        }

        // Thanh tiến trình chạy trong 2.5 giây
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )

        // Sau khi thanh load chạy xong thì chuyển màn hình
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. HÌNH NỀN CỦA BẠN (Phủ kín màn hình)
        Image(
            painter = painterResource(id = R.drawable.bg_splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 2. LOGO GAME (Căn trên, có hiệu ứng bung ra)
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = "Game Logo",
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.TopCenter)
                .offset(y = 60.dp)
                .statusBarsPadding()
                .alpha(logoAlpha.value)
                .scale(logoScale.value),
            contentScale = ContentScale.FillWidth
        )

        // 3. KHU VỰC LOADING (Căn dưới)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 48.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tùy chỉnh chữ hiển thị trạng thái loading theo phần trăm thanh load
            val loadingStatus = when {
                progressAnim.value < 0.4f -> "Đang khởi tạo bộ nhớ..."
                progressAnim.value < 0.8f -> "Đang tải bản đồ lưới..."
                else -> "Sẵn sàng chiến đấu!"
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

            // Thanh Progress với Rocket
            CustomProgressBar(
                progress = progressAnim.value,
                iconResId = R.drawable.splash_roc,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun CustomSplashScreenPreview() {
    CustomSplashScreen(onTimeout = {})
}
