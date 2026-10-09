
package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CustomProgressBar
import kotlinx.coroutines.launch

// Overshoot easing
fun OvershootEasing(tension: Float = 2.0f) = Easing { fraction ->
    val t = fraction - 1.0f
    t * t * ((tension + 1.0f) * t + tension) + 1.0f
}

@Composable
fun CustomSplashScreen(
    onTimeout: () -> Unit
) {
    // Logo animations
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.8f) }

    // Loading progress animation
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run logo animations concurrently
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }

        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1200,
                    easing = OvershootEasing(1.5f)
                )
            )
        }

        // Animate loading progress over 2.5 seconds
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2500,
                easing = LinearEasing
            )
        )

        // Navigate when loading is complete
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // GAME LOGO
        Image(
            painter = painterResource(
                id = R.drawable.splash_logo
            ),
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

        // LOADING SECTION
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 48.dp,
                    vertical = 40.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dynamic loading status
            val loadingStatus = when {
                progressAnim.value < 0.4f -> {
                    stringResource(
                        id = R.string.splash_loading_memory
                    )
                }

                progressAnim.value < 0.8f -> {
                    stringResource(
                        id = R.string.splash_loading_grid
                    )
                }

                else -> {
                    stringResource(
                        id = R.string.splash_loading_ready
                    )
                }
            }

            Text(
                text = loadingStatus,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // Progress bar with rocket icon
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
    CustomSplashScreen(
        onTimeout = {}
    )
}