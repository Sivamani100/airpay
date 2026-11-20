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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.presentation.ServicesViewModel
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax

/**
 * Screen for Requesting Money via Mobile Number (*99*2*1#) or UPI ID (*99*2*2#).
 */
@Composable
fun RequestMoneyScreen(
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val session by servicesViewModel.sessionState.collectAsState()

    var isPhoneTab by remember { mutableStateOf(true) }
    var targetInput by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val cleanTarget = targetInput.trim()
    val cleanDigits = cleanTarget.filter { it.isDigit() }
    val isValidTarget = if (isPhoneTab) {
        cleanDigits.length == 10
    } else {
        cleanTarget.contains("@") && cleanTarget.length >= 5
    }
    val amountNum = amount.toDoubleOrNull() ?: 0.0
    val isFormValid = isValidTarget && amountNum > 0.0

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
                        text = "Request Money",
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
                        Text(
                            text = "Send a Payment Collect Request",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00875A)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "The payer will receive an offline approval prompt on their phone.",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )

                        Spacer(Modifier.height(18.dp))

                        // Toggle Tab: Mobile vs UPI ID
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
                                    .background(if (isPhoneTab) Color.White else Color.Transparent)
                                    .clickable {
                                        isPhoneTab = true
                                        targetInput = ""
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Mobile Number",
                                    fontSize = 13.sp,
                                    fontWeight = if (isPhoneTab) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPhoneTab) AirpayColors.Primary else Color(0xFF757575)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isPhoneTab) Color.White else Color.Transparent)
                                    .clickable {
                                        isPhoneTab = false
                                        targetInput = ""
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "UPI ID / VPA",
                                    fontSize = 13.sp,
                                    fontWeight = if (!isPhoneTab) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isPhoneTab) AirpayColors.Primary else Color(0xFF757575)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Target Input Box
                        Text(
                            text = if (isPhoneTab) "Payer's Mobile Number" else "Payer's UPI ID",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AirpayColors.Primary
                        )
                        Spacer(Modifier.height(6.dp))

                        val targetShape = RoundedCornerShape(10.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(targetShape)
                                .border(1.dp, Color(0xFFDDDDDD), targetShape)
                                .background(Color(0xFFFAFAFC))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isPhoneTab) {
                                Text(
                                    text = "+91 ",
                                    fontSize = 14.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = AirpayColors.TextPrimary
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (targetInput.isEmpty()) {
                                    Text(
                                        text = if (isPhoneTab) "Enter 10-digit number" else "e.g. mobile@upi, name@okhdfc",
                                        fontSize = 13.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                                BasicTextField(
                                    value = targetInput,
                                    onValueChange = {
                                        if (isPhoneTab) {
                                            targetInput = it.filter { c -> c.isDigit() }.take(10)
                                        } else {
                                            targetInput = it.trim().lowercase()
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1C1C1E)
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = if (isPhoneTab) KeyboardType.Phone else KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    cursorBrush = SolidColor(AirpayColors.Primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Amount Field
                        Text(
                            text = "Amount (₹)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AirpayColors.Primary
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(targetShape)
                                .border(1.dp, Color(0xFFDDDDDD), targetShape)
                                .background(Color(0xFFFAFAFC))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            if (amount.isEmpty()) {
                                Text(text = "Enter amount", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                            }
                            BasicTextField(
                                value = amount,
                                onValueChange = { amount = it.filter { c -> c.isDigit() }.take(6) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1C1E)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                cursorBrush = SolidColor(AirpayColors.Primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Quick Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("100", "200", "500", "1000").forEach { chipAmt ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF4F5F8))
                                        .clickable { amount = chipAmt }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "+₹$chipAmt",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AirpayColors.Primary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Note Field
                        Text(
                            text = "Note / Remarks (Optional)",
                            fontSize = 11.5.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(targetShape)
                                .border(1.dp, Color(0xFFDDDDDD), targetShape)
                                .background(Color(0xFFFAFAFC))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            if (note.isEmpty()) {
                                Text(text = "e.g. Dinner, Bill split", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                            }
                            BasicTextField(
                                value = note,
                                onValueChange = { note = it.take(30) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF1C1C1E)),
                                cursorBrush = SolidColor(AirpayColors.Primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(28.dp))

                        // Send Collect Request Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFormValid) AirpayColors.Primary else Color(0xFFCCCCCC))
                                .clickable(enabled = isFormValid) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    servicesViewModel.requestMoney(
                                        isPhone = isPhoneTab,
                                        target = if (isPhoneTab) cleanDigits else cleanTarget,
                                        amount = amount,
                                        note = note
                                    )
                                }
                                .padding(vertical = 15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SEND REQUEST",
                                fontSize = 14.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
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
                            text = "Sending collect request to payer...",
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
                            text = "Request Sent!",
                            fontSize = 20.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Payment request of ₹$amount sent to $cleanTarget",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
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
                            text = "Request Failed",
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
