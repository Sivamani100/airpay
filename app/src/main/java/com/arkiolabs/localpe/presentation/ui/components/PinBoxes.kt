package com.arkiolabs.localpe.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkiolabs.localpe.presentation.ui.theme.PhonePeColors

/**
 * PhonePe style UPI PIN display row.
 * Rounded boxes with vibrant purple accent when active/filled, and a crisp
 * security bullet dot for filled digits.
 */
@Composable
fun PinBoxes(
    value: String,
    modifier: Modifier = Modifier,
    length: Int = 6
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pin_active")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin_pulse_alpha"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(length) { index ->
            val filled = index < value.length
            val isActive = index == value.length
            val boxShape = RoundedCornerShape(12.dp)

            val borderColor by animateColorAsState(
                targetValue = when {
                    filled -> PhonePeColors.Primary
                    isActive -> PhonePeColors.Primary
                    else -> PhonePeColors.Border
                },
                label = "pin_border_$index"
            )

            val fillColor = when {
                filled -> PhonePeColors.PurpleTint
                isActive -> PhonePeColors.Surface
                else -> PhonePeColors.SurfaceVariant
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(boxShape)
                    .background(fillColor)
                    .border(
                        width = if (isActive || filled) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = boxShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (filled) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(PhonePeColors.Primary)
                    )
                } else if (isActive) {
                    Box(
                        Modifier
                            .size(14.dp, 2.dp)
                            .alpha(pulseAlpha)
                            .background(PhonePeColors.Primary)
                    )
                }
            }
        }
    }
}
