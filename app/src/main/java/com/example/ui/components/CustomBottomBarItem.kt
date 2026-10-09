package com.example.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.engine.VibratorHapticManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomBottomBarItem(
    item: CustomBottomItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .shadow(
                elevation = if (selected) 5.dp else 0.dp,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                VibratorHapticManager.click(context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {

        // Hiển thị hình ảnh thay thế cho Icon và Text
        Image(
            painter = painterResource(id = item.imageRes), // item.imageRes là ID dạng Int (ví dụ: R.drawable.home_btn)
            contentDescription = item.title,
            modifier = Modifier.size(
                width = 59.dp,
                height = 64.dp
            )
        )
        // Giữ lại Badge "NEW" nếu item có thuộc tính isNew
//        if (item.isNew) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .offset(
//                        x = 5.dp,
//                        y = (-5).dp
//                    )
//                    .size(18.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFFFF3D68)),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "NEW",
//                    color = Color.White,
//                    fontSize = 5.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
    }
}