package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * LevelTag thiết kế dạng 3D pill chuẩn UI game.
 */
@Composable
fun LevelTag(
    level: Int,
    modifier: Modifier = Modifier
) {
    // 1. Box ngoài cùng làm lớp chân bóng 3D phía dưới
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFE0E0E0), // Màu chân bóng 3D
                shape = CircleShape
            )
            .padding(bottom = 3.dp) // Nhô chân 3D 3dp phía dưới
    ) {
        // 2. Box chính chứa nền tím + viền trắng
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFFA24BFF), // Màu tím chủ đạo
                    shape = CircleShape
                )
                .border(
                    width = 2.5.dp,            // Viền trắng nổi bật
                    color = Color.White,
                    shape = CircleShape
                )
                .padding(horizontal = 20.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.game_level_format, level),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}