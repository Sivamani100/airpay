package com.arkiolabs.airpay.presentation.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Authentic Airpay Fullscreen UPI PIN Entry Interface.
 * Matches layout, colors, amber summary banner, and custom numeric keypad.
 */
@Composable
fun UpiPinScreen(
    amount: String,
    recipientName: String,
    bankName: String = "PRIMARY BANK OF INDIA",
    pinLength: Int = 6,
    submitLabel: String = if (amount.isNotBlank()) "PAY" else "SUBMIT",
    onPinSubmitted: (String) -> Unit,
    onCancel: () -> Unit,
    onBiometricRequested: (() -> Unit)? = null,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Top Bar: UPI Logo + Bank Name + Close (X) ───────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.upi_orange_border),
                        contentDescription = "UPI",
                        modifier = Modifier.height(18.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = bankName.uppercase(),
                    fontSize = 13.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Iconsax.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Amber Payment Summary Banner (from screenshot) ───────────
        val bannerShape = RoundedCornerShape(12.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(bannerShape)
                .background(Color(0xFFFEF3C7))
                .border(1.dp, Color(0xFFFDE68A), bannerShape)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (amount.isNotBlank()) "Pay ₹$amount" else "Check Balance",
                        fontSize = 18.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "To $recipientName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }

                // Account icon badge on right
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Iconsax.Profile,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Center Section: PIN Prompt, Dots & Advisory ─────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ENTER UPI PIN",
                fontSize = 14.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = when {
                    pin.length in 4..5 -> "✓ 4 digits entered — tap $submitLabel or continue for 6 digits"
                    pin.length == 6 -> "✓ 6-digit UPI PIN entered"
                    else -> "Enter your 4 or 6-digit Bank UPI PIN"
                },
                fontSize = 12.sp,
                fontFamily = ClashGrotesk,
                fontWeight = if (pin.length >= 4) FontWeight.SemiBold else FontWeight.Normal,
                color = if (pin.length >= 4) AirpayColors.SuccessGreen else Color(0xFF64748B)
            )

            Spacer(Modifier.height(18.dp))

            // Large Circular PIN Dots (6 dot placeholders, with 5th and 6th dot showing flexible optional styling)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val isFilled = i < pin.length
                    val isOptional = i >= 4
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) Color(0xFF1E1B4B) else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) Color(0xFF1E1B4B)
                                       else if (isOptional) Color(0xFFCBD5E1)
                                       else Color(0xFF94A3B8),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Sub-indicator checkmark
            Icon(
                imageVector = Iconsax.TickCircle,
                contentDescription = null,
                tint = if (pin.length in 4..6) AirpayColors.SuccessGreen else Color.Transparent,
                modifier = Modifier.size(20.dp)
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    fontSize = 12.sp,
                    color = AirpayColors.DangerRed,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // Advisory warning notice (from screenshot)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Iconsax.InfoCircle,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Never enter your UPI PIN to receive money",
                    fontSize = 12.sp,
                    color = Color(0xFFB45309),
                    fontWeight = FontWeight.Medium
                )
            }

            if (onBiometricRequested != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AirpayColors.PurpleTint)
                        .clickable { onBiometricRequested() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Iconsax.Fingerprint,
                        contentDescription = "Biometric",
                        tint = AirpayColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Use Biometric Fingerprint",
                        fontSize = 12.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = AirpayColors.Primary
                    )
                }
            }
        }

        val isSubmitEnabled = pin.length in 4..6

        // ── Custom Numeric Keypad (from screenshot) ─────────────────
        CustomUpiKeypad(
            onDigitClick = { digit ->
                if (pin.length < 6) {
                    val newPin = pin + digit
                    pin = newPin
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (newPin.length == 6) {
                        onPinSubmitted(newPin)
                    }
                }
            },
            onBackspaceClick = {
                if (pin.isNotEmpty()) {
                    pin = pin.dropLast(1)
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            },
            onSubmitClick = {
                if (pin.length in 4..6) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onPinSubmitted(pin)
                }
            },
            isSubmitEnabled = isSubmitEnabled,
            submitLabel = submitLabel
        )
    }
}

@Composable
private fun CustomUpiKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onSubmitClick: () -> Unit,
    isSubmitEnabled: Boolean = false,
    submitLabel: String = "PAY"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEEF2F6))
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KeypadButton(text = "1", onClick = { onDigitClick("1") }, modifier = Modifier.weight(1f))
            KeypadButton(text = "2", onClick = { onDigitClick("2") }, modifier = Modifier.weight(1f))
            KeypadButton(text = "3", onClick = { onDigitClick("3") }, modifier = Modifier.weight(1f))
        }

        // Row 2: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KeypadButton(text = "4", onClick = { onDigitClick("4") }, modifier = Modifier.weight(1f))
            KeypadButton(text = "5", onClick = { onDigitClick("5") }, modifier = Modifier.weight(1f))
            KeypadButton(text = "6", onClick = { onDigitClick("6") }, modifier = Modifier.weight(1f))
        }

        // Row 3: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KeypadButton(text = "7", onClick = { onDigitClick("7") }, modifier = Modifier.weight(1f))
            KeypadButton(text = "8", onClick = { onDigitClick("8") }, modifier = Modifier.weight(1f))
            KeypadButton(text = "9", onClick = { onDigitClick("9") }, modifier = Modifier.weight(1f))
        }

        // Row 4: Backspace, 0, PAY
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Backspace pill (soft slate lavender)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFCBD5E1))
                    .clickable { onBackspaceClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Iconsax.ArrowLeft,
                    contentDescription = "Backspace",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(22.dp)
                )
            }

            // 0
            KeypadButton(text = "0", onClick = { onDigitClick("0") }, modifier = Modifier.weight(1f))

            // PAY / SUBMIT pill (highlights enabled at 4-6 digits with bright green tick)
            val isEnabled = isSubmitEnabled
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isEnabled) Color(0xFF1E293B) else Color(0xFFCBD5E1))
                    .clickable(enabled = isEnabled) { onSubmitClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = submitLabel,
                        color = if (isEnabled) Color.White else Color(0xFF64748B),
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (isEnabled) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Iconsax.TickCircle,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(100.dp)
    Surface(
        color = Color.White,
        shape = shape,
        shadowElevation = 0.5.dp,
        modifier = modifier
            .height(54.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 22.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}
