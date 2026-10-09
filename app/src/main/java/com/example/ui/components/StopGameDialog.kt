package com.example.ui.components
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R

@Composable
fun StopGameDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmButtonRes: Int,
    cancelButtonRes: Int
) {
    Dialog(
        onDismissRequest = onConfirm,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 16.dp,
                        bottom = 18.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // =========================
                // TIÊU ĐỀ
                // =========================

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF4DD0E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.stop_game_title),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // =========================
                // NỘI DUNG
                // =========================

                Text(
                    text = stringResource(R.string.stop_game_message),
                    color = Color(0xFF777777),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                // =========================
                // 2 NÚT ẢNH
                // =========================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Nút HỦY
                    Image(
                        painter = painterResource(id = cancelButtonRes),
                        contentDescription =  stringResource(R.string.stop_game_cancel),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(width = 100.dp, height = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onCancel()
                            }
                    )

                    // Nút XÁC NHẬN
                    Image(
                        painter = painterResource(id = confirmButtonRes),
                        contentDescription =  stringResource(R.string.stop_game_confirm),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(width = 100.dp, height = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onConfirm()
                            }
                    )
                }
            }
        }
    }
}