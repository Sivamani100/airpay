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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.arkiolabs.airpay.platform.ApkShareUtil
import com.arkiolabs.airpay.presentation.ServicesViewModel
import com.arkiolabs.airpay.presentation.ui.components.UpiPinScreen
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax

/**
 * Screen for sending money directly to any bank account via IFSC & Account Number (*99*1*5#).
 */
@Composable
fun ToBankScreen(
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val session by servicesViewModel.sessionState.collectAsState()

    var ifsc by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var confirmAccountNo by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showPinScreen by remember { mutableStateOf(false) }

    val cleanIfsc = ifsc.trim().uppercase()
    val cleanAccount = accountNo.trim()
    val cleanConfirm = confirmAccountNo.trim()
    val amountNum = amount.toDoubleOrNull() ?: 0.0

    val accountsMatch = cleanAccount.isNotEmpty() && cleanAccount == cleanConfirm
    val isFormValid = cleanIfsc.length in 8..11 && accountsMatch && amountNum > 0.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        if (showPinScreen) {
            UpiPinScreen(
                amount = amount,
                recipientName = "A/c ••${cleanAccount.takeLast(4)} ($cleanIfsc)",
                bankName = "BANK TRANSFER",
                submitLabel = "PAY",
                onPinSubmitted = { pin ->
                    showPinScreen = false
                    servicesViewModel.sendToBank(cleanIfsc, cleanAccount, amount, note, pin)
                },
                onCancel = { showPinScreen = false }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
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
                            text = "To Bank Account",
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
                                text = "Zero Wait Time • All Indian Banks",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00875A)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Send money directly to any bank account using IFSC & Account Number.",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )

                            Spacer(Modifier.height(18.dp))

                            // IFSC Code Field
                            Text(
                                text = "IFSC Code",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AirpayColors.Primary
                            )
                            Spacer(Modifier.height(6.dp))
                            BankInputField(
                                value = ifsc,
                                onValueChange = { ifsc = it.uppercase().take(11) },
                                placeholder = "e.g. SBIN0001234, HDFC0000001",
                                keyboardType = KeyboardType.Ascii
                            )

                            Spacer(Modifier.height(14.dp))

                            // Account Number Field
                            Text(
                                text = "Account Number",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AirpayColors.Primary
                            )
                            Spacer(Modifier.height(6.dp))
                            BankInputField(
                                value = accountNo,
                                onValueChange = { accountNo = it.filter { c -> c.isDigit() }.take(18) },
                                placeholder = "Enter recipient's account number",
                                keyboardType = KeyboardType.Number
                            )

                            Spacer(Modifier.height(14.dp))

                            // Confirm Account Number Field
                            Text(
                                text = "Re-enter Account Number",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (confirmAccountNo.isNotEmpty() && !accountsMatch) AirpayColors.DangerRed else AirpayColors.Primary
                            )
                            Spacer(Modifier.height(6.dp))
                            BankInputField(
                                value = confirmAccountNo,
                                onValueChange = { confirmAccountNo = it.filter { c -> c.isDigit() }.take(18) },
                                placeholder = "Confirm account number",
                                keyboardType = KeyboardType.Number,
                                isError = confirmAccountNo.isNotEmpty() && !accountsMatch
                            )
                            if (confirmAccountNo.isNotEmpty() && !accountsMatch) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Account numbers do not match",
                                    fontSize = 11.sp,
                                    color = AirpayColors.DangerRed
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            // Amount Field
                            Text(
                                text = "Amount (₹)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AirpayColors.Primary
                            )
                            Spacer(Modifier.height(6.dp))
                            BankInputField(
                                value = amount,
                                onValueChange = { amount = it.filter { c -> c.isDigit() }.take(6) },
                                placeholder = "Enter amount (e.g. 1000)",
                                keyboardType = KeyboardType.Number
                            )

                            Spacer(Modifier.height(8.dp))

                            // Quick Amount Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("500", "1000", "2000", "5000").forEach { chipAmt ->
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

                            Spacer(Modifier.height(14.dp))

                            // Remarks
                            Text(
                                text = "Remarks / Note (Optional)",
                                fontSize = 11.5.sp,
                                color = Color(0xFF757575)
                            )
                            Spacer(Modifier.height(6.dp))
                            BankInputField(
                                value = note,
                                onValueChange = { note = it.take(30) },
                                placeholder = "e.g. Rent, Grocery",
                                keyboardType = KeyboardType.Text
                            )

                            Spacer(Modifier.height(28.dp))

                            // Proceed Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isFormValid) AirpayColors.Primary else Color(0xFFCCCCCC))
                                    .clickable(enabled = isFormValid) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        showPinScreen = true
                                    }
                                    .padding(vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PROCEED TO PAY",
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
                                text = "Processing offline bank transfer...",
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
                                text = "Transfer Successful!",
                                fontSize = 20.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "₹$amount sent to A/c ••${cleanAccount.takeLast(4)}",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = s.resultText,
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(Modifier.height(24.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF4F5F8))
                                        .clickable {
                                            ApkShareUtil.shareTransactionReceipt(
                                                context = context,
                                                amount = amount,
                                                payeeName = "A/c ••${cleanAccount.takeLast(4)}",
                                                vpa = cleanIfsc,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = AirpayColors.Primary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(text = "Share Receipt", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AirpayColors.Primary)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AirpayColors.Primary)
                                        .clickable { servicesViewModel.dismissSession() }
                                        .padding(horizontal = 24.dp, vertical = 10.dp)
                                ) {
                                    Text(text = "Done", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
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
                                text = "Transfer Failed",
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
}

@Composable
private fun BankInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isError: Boolean = false
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                1.dp,
                if (isError) AirpayColors.DangerRed else Color(0xFFDDDDDD),
                shape
            )
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
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            cursorBrush = SolidColor(AirpayColors.Primary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
