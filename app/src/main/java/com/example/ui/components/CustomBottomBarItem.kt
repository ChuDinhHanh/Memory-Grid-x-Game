package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomBottomBarItem(
    item: CustomBottomItem,
    selected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(
                width = 58.dp,
                height = 54.dp
            )
            .shadow(
                elevation = if (selected) 5.dp else 3.dp,
                shape = RoundedCornerShape(10.dp)
            )
            .clip(
                RoundedCornerShape(10.dp)
            )
            .background(item.color)
            .border(2.dp, Color.White, RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = item.title,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }


        if (item.isNew) {

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = 5.dp,
                        y = (-5).dp
                    )
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFFFF3D68)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "NEW",
                    color = Color.White,
                    fontSize = 5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
