package com.arkiolabs.localpe.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.localpe.presentation.ui.theme.PhonePeColors

/**
 * PhonePe styled text field: clean rounded card, light border with vibrant
 * purple accent border on focus, helper label and inline error.
 */
@Composable
fun NeoPopTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    error: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    textStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = PhonePeColors.TextPrimary
    )
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val hasError = error != null

    val borderColor by animateColorAsState(
        targetValue = when {
            hasError -> PhonePeColors.DangerRed
            focused -> PhonePeColors.Primary
            else -> PhonePeColors.Border
        },
        label = "field_border"
    )

    val labelColor = when {
        hasError -> PhonePeColors.DangerRed
        focused -> PhonePeColors.Primary
        else -> PhonePeColors.TextSecondary
    }

    val shape = RoundedCornerShape(12.dp)

    Column(modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )

        Box(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(PhonePeColors.Surface)
                .border(
                    width = if (focused || hasError) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = shape
                )
                .padding(horizontal = 14.dp, vertical = 13.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (focused) PhonePeColors.Primary else PhonePeColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }

                Box(Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = PhonePeColors.TextMuted
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        singleLine = singleLine,
                        textStyle = textStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        visualTransformation = visualTransformation,
                        cursorBrush = SolidColor(PhonePeColors.Primary),
                        interactionSource = interactionSource
                    )
                }

                if (trailingContent != null) {
                    Spacer(Modifier.width(8.dp))
                    trailingContent()
                }
            }
        }

        if (hasError) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = PhonePeColors.DangerRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = PhonePeColors.DangerRed
                )
            }
        }
    }
}
