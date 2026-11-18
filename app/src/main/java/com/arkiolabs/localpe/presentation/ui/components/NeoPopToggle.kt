package com.arkiolabs.localpe.presentation.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.localpe.presentation.ui.theme.PhonePeColors

/**
 * PhonePe style segmented toggle control: rounded pill with purple selection.
 */
@Composable
fun <T> NeoPopToggle(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val containerShape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(PhonePeColors.PurpleTint)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, (value, label) ->
            val isActive = value == selected
            val pillShape = RoundedCornerShape(10.dp)
            val bg by animateColorAsState(
                targetValue = if (isActive) PhonePeColors.Primary else Color.Transparent,
                label = "toggle_bg_$index"
            )
            val fg by animateColorAsState(
                targetValue = if (isActive) Color.White else PhonePeColors.TextSecondary,
                label = "toggle_fg_$index"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(pillShape)
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isActive) {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onSelect(value)
                        }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    color = fg,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
