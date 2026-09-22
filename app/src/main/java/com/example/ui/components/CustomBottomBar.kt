package com.example.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


data class CustomBottomItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val isNew: Boolean = false
)


@Composable
fun CustomBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {

    val items = listOf(

        CustomBottomItem(
            route = "home",
            title = "BONUS",
            icon = Icons.Default.GridOn,
            color = Color(0xFF4DD0F2)
        ),

        CustomBottomItem(
            route = "daily",
            title = "DAILY",
            icon = Icons.Default.CalendarToday,
            color = Color(0xFFF48BC8)
        ),

        CustomBottomItem(
            route = "achievements",
            title = "DRESS",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFFB978F0)
        ),

        CustomBottomItem(
            route = "leaderboard",
            title = "TROPHY",
            icon = Icons.Default.Leaderboard,
            color = Color(0xFFFFD21F),
            isNew = true
        )
    )


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp
                )
            ),
        color = Color.White,
        tonalElevation = 0.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 10.dp
                ),
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
