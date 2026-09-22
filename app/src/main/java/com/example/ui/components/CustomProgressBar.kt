package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GameAmber
import com.example.ui.theme.GameGold

@Composable
fun CustomProgressBar(
    progress: Float,
    iconResId: Int,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "ProgressAnimation"
    )

    // Tăng độ lớn tên lửa (2.0f là tỉ lệ vừa đẹp)
    val iconSize = height * 2.0f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Tính khoảng cách tối đa mà rocket có thể di chuyển trong lòng khung
        // Giúp rocket dừng lại chính xác ở mép phải khi progress = 1.0f
        val maxTravelDistance = maxWidth - iconSize
        val iconOffsetX = maxTravelDistance * animatedProgress

        // 1. SHADOW (BÓNG ĐỔ DƯỚI)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 4.dp)
                .background(
                    color = Color(0x33000000),
                    shape = CircleShape
                )
        )

        // 2. KHUNG TRẮNG
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFE0E0E0),
                    shape = CircleShape
                )
                .padding(4.dp)
        ) {
            // 3. THANH NỀN
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(GameAmber)
            ) {
                // 4. PROGRESS
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(CircleShape)
                        .background(GameGold)
                )
            }
        }

        // 5. ROCKET / ICON
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .requiredSize(iconSize)
                .align(Alignment.CenterStart)
                .offset(x = iconOffsetX) // Dịch chuyển chuẩn trong giới hạn khung
        )
    }
}