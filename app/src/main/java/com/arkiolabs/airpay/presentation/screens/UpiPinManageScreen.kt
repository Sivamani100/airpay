package com.arkiolabs.airpay.presentation.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.presentation.ServicesViewModel
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax

/**
 * Screen for managing UPI PIN:
 *  - Option 1: Set or Reset UPI PIN using Debit Card (*99*7*1#)
 *  - Option 2: Change Existing UPI PIN (*99*7*2#)
 */
@Composable
fun UpiPinManageScreen(
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val session by servicesViewModel.sessionState.collectAsState()

    var isResetTab by remember { mutableStateOf(true) }

    // Option 1: Reset with Debit Card
    var cardDigits by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var resetNewPin by remember { mutableStateOf("") }
    var resetConfirmPin by remember { mutableStateOf("") }

    // Option 2: Change Existing PIN
    var oldPin by remember { mutableStateOf("") }
    var changeNewPin by remember { mutableStateOf("") }
    var changeConfirmPin by remember { mutableStateOf("") }

    val isResetValid = cardDigits.length == 6 &&
        cardExpiry.length == 4 &&
        resetNewPin.length in 4..6 &&
        resetNewPin == resetConfirmPin

    val isChangeValid = oldPin.length in 4..6 &&
        changeNewPin.length in 4..6 &&
        changeNewPin == changeConfirmPin

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (session !is SessionState.Idle) {
                                    servicesViewModel.dismissSession()
                                } else {
                                    onBack()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Iconsax.ArrowLeft,
                            contentDescription = "Back",
                            tint = Color(0xFF1C1C1E),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "UPI PIN Settings",
                        fontSize = 18.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onOpenFaq() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_help_white),
                        contentDescription = "Help",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            when (val s = session) {
                is SessionState.Idle -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // 2 Tabs: Reset with Debit Card vs Change Existing PIN
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF4F5F8))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isResetTab) Color.White else Color.Transparent)
                                    .clickable { isResetTab = true }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Reset with Card",
                                    fontSize = 13.sp,
                                    fontWeight = if (isResetTab) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isResetTab) AirpayColors.Primary else Color(0xFF757575)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isResetTab) Color.White else Color.Transparent)
                                    .clickable { isResetTab = false }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Change Existing PIN",
                                    fontSize = 13.sp,
                                    fontWeight = if (!isResetTab) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isResetTab) AirpayColors.Primary else Color(0xFF757575)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        if (isResetTab) {
                            // ── Option 1: Reset with Debit Card (*99*7*1#) ───
                            Text(
                                text = "Reset UPI PIN (*99*7*1#)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Enter the last 6 digits of your debit card and expiry date to set a new PIN.",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(text = "Debit Card Last 6 Digits", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = AirpayColors.Primary)
                            Spacer(Modifier.height(6.dp))
                            PinInputField(
                                value = cardDigits,
                                onValueChange = { cardDigits = it.filter { c -> c.isDigit() }.take(6) },
                                placeholder = "XXXXXX",
                                keyboardType = KeyboardType.Number
                            )

                            Spacer(Modifier.height(14.dp))

                            Text(text = "Card Expiry (MMYY)", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = AirpayColors.Primary)
                            Spacer(Modifier.height(6.dp))
                            PinInputField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it.filter { c -> c.isDigit() }.take(4) },
                                placeholder = "e.g. 0528 for May 2028",
                                keyboardType = KeyboardType.Number
                            )

                            Spacer(Modifier.height(14.dp))

                            Text(text = "Enter New UPI PIN (4 or 6 digits)", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = AirpayColors.Primary)
                            Spacer(Modifier.height(6.dp))
                            PinInputField(
                                value = resetNewPin,
                                onValueChange = { resetNewPin = it.filter { c -> c.isDigit() }.take(6) },
                                placeholder = "••••••",
                                keyboardType = KeyboardType.NumberPassword,
                                isPassword = true
                            )

                            Spacer(Modifier.height(14.dp))

                            Text(text = "Confirm New UPI PIN", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = AirpayColors.Primary)
                            Spacer(Modifier.height(6.dp))
                            PinInputField(
                                value = resetConfirmPin,
                                onValueChange = { resetConfirmPin = it.filter { c -> c.isDigit() }.take(6) },
                                placeholder = "••••••",
                                keyboardType = KeyboardType.NumberPassword,
                                isPassword = true
                            )

                            Spacer(Modifier.height(26.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isResetValid) AirpayColors.Primary else Color(0xFFCCCCCC))
                                    .clickable(enabled = isResetValid) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        servicesViewModel.resetUpiPin(cardDigits, cardExpiry, resetNewPin, resetConfirmPin)
                                    }
                                    .padding(vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SET NEW UPI PIN",
                                    fontSize = 14.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            // ── Option 2: Change Existing PIN (*99*7*2#) ──────
                            Text(
                                text = "Change UPI PIN (*99*7*2#)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Enter your current PIN followed by your new UPI PIN.",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(text = "Current / Old UPI PIN", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = AirpayColors.Primary)
                            Spacer(Modifier.height(6.dp))
                            PinInputField(
                                value = oldPin,
                                onValueChange = { oldPin = it.filter { c -> c.isDigit() }.take(6) },
                                placeholder = "Enter current PIN",
                                keyboardType = KeyboardType.NumberPassword,
                                isPassword = true
                            )

                            Spacer(Modifier.height(14.dp))

                            Text(text = "New UPI PIN (4 or 6 digits)", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = AirpayColors.Primary)
                            Spacer(Modifier.height(6.dp))
                            PinInputField(
                                value = changeNewPin,
                                onValueChange = { changeNewPin = it.filter { c -> c.isDigit() }.take(6) },
                                placeholder = "Enter new PIN",
                                keyboardType = KeyboardType.NumberPassword,
                                isPassword = true
                            )

                            Spacer(Modifier.height(14.dp))

                            Text(text = "Confirm New UPI PIN", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = AirpayColors.Primary)
                            Spacer(Modifier.height(6.dp))
                            PinInputField(
                                value = changeConfirmPin,
                                onValueChange = { changeConfirmPin = it.filter { c -> c.isDigit() }.take(6) },
                                placeholder = "Re-enter new PIN",
                                keyboardType = KeyboardType.NumberPassword,
                                isPassword = true
                            )

                            Spacer(Modifier.height(26.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isChangeValid) AirpayColors.Primary else Color(0xFFCCCCCC))
                                    .clickable(enabled = isChangeValid) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        servicesViewModel.changeUpiPin(oldPin, changeNewPin, changeConfirmPin)
                                    }
                                    .padding(vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "CHANGE UPI PIN",
                                    fontSize = 14.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                is SessionState.Running -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = s.label,
                            fontSize = 16.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.Primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Communicating securely with bank gateway...",
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                is SessionState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = AirpayColors.SuccessGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "PIN Updated!",
                            fontSize = 20.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = s.resultText.ifBlank { "Your UPI PIN has been successfully updated on the bank network." },
                            fontSize = 13.sp,
                            color = Color(0xFF757575),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AirpayColors.Primary)
                                .clickable { servicesViewModel.dismissSession() }
                                .padding(horizontal = 28.dp, vertical = 11.dp)
                        ) {
                            Text(text = "Done", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                is SessionState.Failed -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "PIN Update Failed",
                            fontSize = 18.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.DangerRed
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = s.message,
                            fontSize = 13.sp,
                            color = Color(0xFF757575),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AirpayColors.Primary)
                                .clickable { servicesViewModel.dismissSession() }
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(text = "Try Again", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color(0xFFDDDDDD), shape)
            .background(Color(0xFFFAFAFC))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, fontSize = 13.sp, color = Color(0xFF9E9E9E))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1C1C1E)),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            cursorBrush = SolidColor(AirpayColors.Primary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
