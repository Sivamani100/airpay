package com.arkiolabs.localpe.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkiolabs.localpe.presentation.ui.theme.PhonePeColors

/**
 * PhonePe style card: rounded white card with subtle border and gentle shadow.
 */
@Composable
fun NeoPopCard(
    modifier: Modifier = Modifier,
    surfaceColor: Color = PhonePeColors.Surface,
    rightColor: Color = Color.Transparent,
    bottomColor: Color = Color.Transparent,
    borderColor: Color = PhonePeColors.Border,
    depth: Dp = 0.dp,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = shape)
            .clip(shape)
            .background(surfaceColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * PhonePe style accent card: white card with state-tinted border/surface.
 */
@Composable
fun NeoPopAccentCard(
    accent: Color,
    modifier: Modifier = Modifier,
    depth: Dp = 0.dp,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = shape)
            .clip(shape)
            .background(PhonePeColors.Surface)
            .border(width = 1.5.dp, color = accent, shape = shape)
            .padding(contentPadding)
    ) {
        content()
    }
}
