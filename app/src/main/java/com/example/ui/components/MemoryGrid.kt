package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.CellVisualState
import com.example.data.model.GamePhase
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintSuccess
import kotlin.math.roundToInt

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

@Composable
fun MemoryGrid(
    gridSize: Int,
    phase: GamePhase,
    getCellState: (Int) -> CellVisualState,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeStepNumberMap: Map<Int, Int> = emptyMap()
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(phase) {
        if (phase == GamePhase.FAILURE) {
            // Rapid shake animation
            shakeOffset.animateTo(
                targetValue = 18f,
                animationSpec = tween(durationMillis = 50)
            )
            shakeOffset.animateTo(
                targetValue = -16f,
                animationSpec = tween(durationMillis = 60)
            )
            shakeOffset.animateTo(
                targetValue = 12f,
                animationSpec = tween(durationMillis = 60)
            )
            shakeOffset.animateTo(
                targetValue = -8f,
                animationSpec = tween(durationMillis = 50)
            )
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 50)
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val totalSpacing = 8.dp * (gridSize - 1)
        val availableSize = maxWidth - totalSpacing
        val cellSize = availableSize / gridSize
        val cornerRadius = when {
            gridSize <= 3 -> 16.dp
            gridSize == 4 -> 14.dp
            else -> 10.dp
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            for (row in 0 until gridSize) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until gridSize) {
                        val index = row * gridSize + col
                        val state = getCellState(index)
                        val stepNumber = activeStepNumberMap[index]

                        MemoryCell(
                            index = index,
                            size = cellSize,
                            cornerRadius = cornerRadius,
                            state = state,
                            stepNumber = stepNumber,
                            isClickable = phase == GamePhase.RECALL,
                            onClick = { onCellClick(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryCell(
    index: Int,
    size: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
    state: CellVisualState,
    stepNumber: Int?,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(state) {
        when (state) {
            CellVisualState.LIGHTING_UP -> {
                scaleAnim.snapTo(0.92f)
                scaleAnim.animateTo(1.18f, spring(dampingRatio = 0.45f, stiffness = 600f))
                scaleAnim.animateTo(1.04f, spring(dampingRatio = 0.6f, stiffness = 400f))
            }
            CellVisualState.HIGHLIGHTED, CellVisualState.SELECTED, CellVisualState.CORRECT_REVEAL -> {
                scaleAnim.animateTo(1.08f, spring(dampingRatio = 0.5f, stiffness = 400f))
                scaleAnim.animateTo(1.0f, spring(dampingRatio = 0.6f, stiffness = 300f))
            }
            CellVisualState.WRONG_REVEAL -> {
                scaleAnim.animateTo(1.15f, spring(dampingRatio = 0.4f, stiffness = 600f))
                scaleAnim.animateTo(1.0f, spring(dampingRatio = 0.5f, stiffness = 400f))
            }
            else -> {
                scaleAnim.animateTo(1.0f, tween(150))
            }
        }
    }

    // Glowing pulse for highlight phase
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    val targetBackground: Color = when (state) {
        CellVisualState.IDLE -> if (isDark) Color(0xFF1B2845) else Color(0xFFE2EBF8)
        CellVisualState.LIGHTING_UP -> Color(0xFF00E5FF) // Electric Cyan spotlight
        CellVisualState.HIGHLIGHTED -> if (isDark) ElectricCyan.copy(alpha = pulseAlpha) else Color(0xFF0077B6)
        CellVisualState.SELECTED -> MintSuccess
        CellVisualState.CORRECT_REVEAL -> MintSuccess
        CellVisualState.WRONG_REVEAL -> CoralError
        CellVisualState.MISSED_REVEAL -> AmberWarning
    }

    val targetBorderColor: Color = when (state) {
        CellVisualState.IDLE -> if (isDark) Color(0xFF2A3C66) else Color(0xFFCBDDF5)
        CellVisualState.LIGHTING_UP -> Color.White
        CellVisualState.HIGHLIGHTED -> Color.White.copy(alpha = 0.9f)
        CellVisualState.SELECTED -> Color.White
        CellVisualState.CORRECT_REVEAL -> Color.White
        CellVisualState.WRONG_REVEAL -> Color.White
        CellVisualState.MISSED_REVEAL -> Color.White
    }

    val animatedBg by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(durationMillis = 180),
        label = "cellBg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 180),
        label = "cellBorder"
    )

    val accessibilityDesc = when (state) {
        CellVisualState.IDLE -> "Ô nhớ số ${index + 1}, chưa chọn"
        CellVisualState.LIGHTING_UP -> "Ô đang sáng thứ $stepNumber"
        CellVisualState.HIGHLIGHTED -> "Ô cần ghi nhớ thứ $stepNumber"
        CellVisualState.SELECTED -> "Ô số ${index + 1} đã chọn"
        CellVisualState.CORRECT_REVEAL -> "Ô số ${index + 1} chính xác"
        CellVisualState.WRONG_REVEAL -> "Ô số ${index + 1} sai"
        CellVisualState.MISSED_REVEAL -> "Ô số ${index + 1} đã bỏ sót"
    }

    Box(
        modifier = Modifier
            .size(size)
            .scale(scaleAnim.value)
            .shadow(
                elevation = when (state) {
                    CellVisualState.LIGHTING_UP -> 12.dp
                    CellVisualState.HIGHLIGHTED, CellVisualState.SELECTED -> 8.dp
                    CellVisualState.IDLE -> 2.dp
                    else -> 6.dp
                },
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (state == CellVisualState.LIGHTING_UP) {
                        listOf(Color.White, Color(0xFF00E5FF))
                    } else {
                        listOf(animatedBg, animatedBg.copy(alpha = 0.85f))
                    }
                )
            )
            .border(
                width = when (state) {
                    CellVisualState.LIGHTING_UP -> 3.5.dp
                    CellVisualState.IDLE -> 1.dp
                    else -> 2.5.dp
                },
                color = animatedBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                enabled = isClickable,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .testTag("memory_cell_$index")
            .semantics {
                contentDescription = accessibilityDesc
            },
        contentAlignment = Alignment.Center
    ) {
        // Icon / number hint
        when (state) {
            CellVisualState.LIGHTING_UP, CellVisualState.HIGHLIGHTED -> {
                if (stepNumber != null) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = if (size > 65.dp) 24.sp else 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state == CellVisualState.LIGHTING_UP) Color(0xFF0D1B2A) else Color.White
                        )
                    )
                }
            }
            CellVisualState.SELECTED, CellVisualState.CORRECT_REVEAL -> {
                if (stepNumber != null) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = if (size > 65.dp) 24.sp else 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Chính xác",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.45f)
                    )
                }
            }
            CellVisualState.WRONG_REVEAL -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Sai",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.45f)
                )
            }
            CellVisualState.MISSED_REVEAL -> {
                if (stepNumber != null) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            else -> {}
        }
    }
}
