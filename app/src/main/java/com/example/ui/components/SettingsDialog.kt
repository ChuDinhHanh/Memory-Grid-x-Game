package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun SettingsDialog(
    isSoundOn: Boolean,
    isMusicOn: Boolean,
    isVibrationOn: Boolean,
    onSoundToggle: () -> Unit,
    onMusicToggle: () -> Unit,
    onVibrationToggle: () -> Unit,
    onShareClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onRateUsClick: () -> Unit,
    onDismissRequest: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dialogBgColor = Color.White
    val textColor = Color(0xFF1E293B)
    val subTextColor = Color(0xFF64748B)

    Surface(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = dialogBgColor,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // =====================================================
            // HEADER
            // =====================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.settings_btn),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "SETTINGS",
                            fontSize = 22.sp, // Chữ to hơn
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "CUSTOMIZE YOUR GAME",
                            fontSize = 12.sp, // Chữ to hơn
                            fontWeight = FontWeight.Bold,
                            color = subTextColor
                        )
                    }
                }

                if (onDismissRequest != null) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = subTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =====================================================
            // 3 NÚT ĐẦU (NẰM NGANG)
            // =====================================================
            Text(
                text = "GAME CONTROL",
                fontSize = 13.sp, // Chữ to hơn
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0EA5E9),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SquareToggleItem(
                    modifier = Modifier.weight(1f),
                    icon = if (isSoundOn) R.drawable.active_sound else R.drawable.un_sound,
                    title = "SOUND",
                    enabled = isSoundOn,
                    onClick = onSoundToggle
                )

                SquareToggleItem(
                    modifier = Modifier.weight(1f),
                    icon = if (isMusicOn) R.drawable.active_music else R.drawable.un_music,
                    title = "MUSIC",
                    enabled = isMusicOn,
                    onClick = onMusicToggle
                )

                SquareToggleItem(
                    modifier = Modifier.weight(1f),
                    icon = if (isVibrationOn) R.drawable.active_vibrate else R.drawable.un_vibrate,
                    title = "VIBRATE",
                    enabled = isVibrationOn,
                    onClick = onVibrationToggle
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =====================================================
            // 2 NÚT DƯỚI (ẢNH DÀI - ĐÃ XÓA TIÊU ĐỀ MORE)
            // =====================================================
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nút Share Dài
                ImageActionButton(
                    icon = R.drawable.share_btn,
                    onClick = onShareClick
                )

                // Nút Privacy Policy Dài
                ImageActionButton(
                    icon = R.drawable.privacy_policy,
                    onClick = onPrivacyPolicyClick
                )


                // Nút đánh giá
                ImageActionButton(
                    icon = R.drawable.rate_us,
                    onClick = onRateUsClick
                )
            }
        }
    }
}

// =============================================================
// SUB-COMPONENTS
// =============================================================

@Composable
private fun SquareToggleItem(
    icon: Int,
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (enabled) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
        animationSpec = tween(200),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (enabled) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
        animationSpec = tween(200),
        label = "borderColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = title,
            modifier = Modifier.size(46.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 13.sp, // Chữ to hơn
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = if (enabled) "ON" else "OFF",
            fontSize = 12.sp, // Chữ to hơn
            fontWeight = FontWeight.ExtraBold,
            color = if (enabled) Color(0xFF0284C7) else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun ImageActionButton(
    icon: Int,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    )
}