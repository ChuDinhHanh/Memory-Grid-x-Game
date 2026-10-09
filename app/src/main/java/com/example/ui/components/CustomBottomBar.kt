package com.example.ui.components

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

data class CustomBottomItem(
    val route: String,
    val title: String,
    @DrawableRes val imageRes: Int,
    val isNew: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        CustomBottomItem(
            route = "home",
            title = "BONUS",
            imageRes = R.drawable.home_btn
        ),
        CustomBottomItem(
            route = "daily",
            title = "DAILY",
            imageRes = R.drawable.daily_btn
        ),
        CustomBottomItem(
            route = "achievements",
            title = "DRESS",
            imageRes = R.drawable.achie_btn
        ),
        CustomBottomItem(
            route = "leaderboard",
            title = "TROPHY",
            imageRes = R.drawable.rank_btn,
            isNew = true
        )
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        // DÙNG SHADOW VÀ SHAPE CHUẨN CỦA SURFACE (KHÔNG NÊN DÙNG Modifier.shadow)
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // ĐẨY CONTENT LÊN TRÊN THANH 3 NÚT HỆ THỐNG
                .padding(horizontal = 12.dp, vertical = 6.dp), // CHỈNH PADDING NHỎ LẠI CHO ĐẸP
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                CustomBottomBarItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = {
                        onNavigate(item.route)
                    }
                )
            }
        }
    }
}