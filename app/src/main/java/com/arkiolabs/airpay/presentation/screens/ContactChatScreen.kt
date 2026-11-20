package com.arkiolabs.airpay.presentation.screens

import android.app.Activity
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.data.TransactionEntity
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.platform.BiometricHelper
import com.arkiolabs.airpay.platform.ContactsHelper
import com.arkiolabs.airpay.platform.SoundManager
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.PayViewModel
import com.arkiolabs.airpay.presentation.permissions.PermissionStatus
import com.arkiolabs.airpay.presentation.permissions.openAccessibilitySettings
import com.arkiolabs.airpay.presentation.permissions.openOverlaySettings
import com.arkiolabs.airpay.presentation.permissions.rememberPermissionLaunchers
import com.arkiolabs.airpay.presentation.ui.components.UpiPinScreen
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authentic PhonePe-style Contact Chat Payment Screen.
 *
 * Implements:
 *  - 1-on-1 Contact Header: Avatar, Name, Mobile Number / VPA, Verified badge.
 *  - Chat Timeline: Sent money bubbles (Paid ₹X, status, date & time) or clean empty state.
 *  - No casual texting: Only Money Sending interface at the bottom ("just like PhonePe").
 *  - Quick amount suggestion chips (+₹100, +₹500, +₹1,000, +₹2,000).
 *  - Amount input dock with Big "PAY" button.
 *  - Biometric / UPI PIN prompt and USSD offline execution.
 */
@Composable
fun ContactChatScreen(
    viewModel: PayViewModel,
    historyViewModel: HistoryViewModel,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    permissions: PermissionStatus,
    modifier: Modifier = Modifier
) {
    val ui by viewModel.uiState.collectAsState()
    val session by viewModel.sessionState.collectAsState()
    val allTransactions by historyViewModel.transactions.collectAsState()

    val context = LocalContext.current
    val app = context.airpayApp
    val prefsRepo = app.prefsRepo
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val keyboard = LocalSoftwareKeyboardController.current
    val launchers = rememberPermissionLaunchers()

    val biometricEnabled by prefsRepo.biometricEnabled.collectAsState(initial = false)
    val savedPin by prefsRepo.savedBiometricPin.collectAsState(initial = null)
    val successSoundUri by prefsRepo.successSoundUri.collectAsState(initial = null)
    val failedSoundUri by prefsRepo.failedSoundUri.collectAsState(initial = null)

    var showPinScreen by remember { mutableStateOf(false) }

    // Resolve name from contacts or payeeName
    val resolvedContactName = remember(ui.vpa, ui.payeeName) {
        ContactsHelper.resolveDisplayName(context, ui.vpa, ui.payeeName)
    }

    val initial = remember(resolvedContactName) {
        resolvedContactName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    }

    val rawDigits = ui.vpa.replace(Regex("[^0-9]"), "")
    val normalized10Digit = if (rawDigits.length >= 10) rawDigits.takeLast(10) else ""

    val formattedPhoneOrVpa = remember(ui.vpa, normalized10Digit) {
        if (normalized10Digit.length == 10) {
            "+91 ${normalized10Digit.substring(0, 5)} ${normalized10Digit.substring(5)}"
        } else {
            ui.vpa
        }
    }

    // Filter transactions for this specific contact
    val contactTxns = remember(allTransactions, ui.vpa, normalized10Digit) {
        allTransactions.filter { txn ->
            val txnDigits = txn.vpa.replace(Regex("[^0-9]"), "").takeLast(10)
            txn.vpa.equals(ui.vpa, ignoreCase = true) ||
                (normalized10Digit.isNotEmpty() && txnDigits == normalized10Digit)
        }.sortedBy { it.timestamp }
    }

    val listState = rememberLazyListState()

    // Scroll to bottom when transactions update
    LaunchedEffect(contactTxns.size) {
        if (contactTxns.isNotEmpty()) {
            listState.animateScrollToItem(contactTxns.size - 1)
        }
    }

    // Sound effect on transaction state change
    LaunchedEffect(session) {
        when (session) {
            is SessionState.Success -> {
                SoundManager.playSuccess(context, successSoundUri)
                viewModel.onFormFieldChanged(amount = "", note = "")
                Toast.makeText(context, "Payment Successful!", Toast.LENGTH_SHORT).show()
                viewModel.dismissSession()
            }
            is SessionState.Failed -> {
                if (!failedSoundUri.isNullOrBlank()) {
                    SoundManager.playFailure(context, failedSoundUri)
                }
            }
            else -> Unit
        }
    }

    fun initiatePay() {
        keyboard?.hide()
        val amountNum = ui.amount.toDoubleOrNull() ?: 0.0
        if (amountNum <= 0) {
            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (!permissions.readyForDialerPay) {
            launchers.requestPhoneBundle()
            return
        }
        if (!permissions.accessibility) {
            openAccessibilitySettings(context)
            return
        }
        if (!permissions.overlay) {
            openOverlaySettings(context)
            return
        }

        // Biometric fast path
        if (biometricEnabled && !savedPin.isNullOrBlank()) {
            (context as? FragmentActivity)?.let { activity ->
                BiometricHelper.prompt(
                    activity = activity,
                    title = "Confirm Offline Payment",
                    subtitle = "Pay ₹${ui.amount} to $resolvedContactName",
                    onSuccess = {
                        viewModel.attemptPaymentWithPin(savedPin!!)
                    },
                    onError = {
                        showPinScreen = true
                    },
                    onCancel = {
                        showPinScreen = true
                    }
                )
            } ?: run {
                showPinScreen = true
            }
        } else {
            showPinScreen = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F9))
            .statusBarsPadding()
    ) {
        if (showPinScreen) {
            UpiPinScreen(
                amount = ui.amount,
                recipientName = resolvedContactName,
                bankName = "OFFLINE SIM BANK",
                submitLabel = "PAY",
                onPinSubmitted = { pin ->
                    showPinScreen = false
                    scope.launch { prefsRepo.setSavedBiometricPin(pin) }
                    viewModel.attemptPaymentWithPin(pin)
                },
                onCancel = { showPinScreen = false },
                onBiometricRequested = if (biometricEnabled && !savedPin.isNullOrBlank()) {
                    {
                        (context as? FragmentActivity)?.let { act ->
                            BiometricHelper.prompt(
                                activity = act,
                                onSuccess = {
                                    showPinScreen = false
                                    viewModel.attemptPaymentWithPin(savedPin!!)
                                },
                                onError = {},
                                onCancel = {}
                            )
                        }
                    }
                } else null
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // ── Top PhonePe-style Header ────────────────────────
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF4F5F8))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    onBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = AirpayColors.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(AirpayColors.Primary, Color(0xFF8B5CF6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                fontSize = 18.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = resolvedContactName,
                                fontSize = 15.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.TextPrimary
                            )
                            Spacer(Modifier.height(1.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formattedPhoneOrVpa,
                                    fontSize = 11.5.sp,
                                    color = AirpayColors.TextSecondary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "• Offline UPI",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF00875A)
                                )
                            }
                        }

                        // Help button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onOpenFaq() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Iconsax.InfoCircle,
                                contentDescription = "Help",
                                tint = AirpayColors.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ── Chat / Transaction History Canvas ────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (contactTxns.isEmpty()) {
                        // Empty Conversation State (PhonePe Style)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(AirpayColors.PurpleTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    fontSize = 28.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = AirpayColors.Primary
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = resolvedContactName,
                                fontSize = 17.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formattedPhoneOrVpa,
                                fontSize = 13.sp,
                                color = AirpayColors.TextSecondary
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AirpayColors.Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Send money directly from your SIM card. 100% Offline.",
                                    fontSize = 11.sp,
                                    color = AirpayColors.TextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(contactTxns, key = { it.id }) { txn ->
                                PaymentChatBubble(
                                    txn = txn,
                                    payeeName = resolvedContactName
                                )
                            }
                        }
                    }
                }

                // ── Bottom PhonePe-Style Money Sending Dock ──────────
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Quick Amount Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("100", "500", "1000", "2000").forEach { chipAmount ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF4F5F8))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                            viewModel.onFormFieldChanged(amount = chipAmount)
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+₹$chipAmount",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AirpayColors.TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Amount Input Bar (Chat Style)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF6F7F9))
                                .border(1.5.dp, if (ui.amount.isNotEmpty()) AirpayColors.Primary else Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹",
                                fontSize = 22.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.Primary
                            )

                            Spacer(Modifier.width(10.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (ui.amount.isEmpty()) {
                                    Text(
                                        text = "Enter amount to pay",
                                        fontSize = 15.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                                BasicTextField(
                                    value = ui.amount,
                                    onValueChange = { newVal ->
                                        val digits = newVal.filter { it.isDigit() }.take(6)
                                        viewModel.onFormFieldChanged(amount = digits)
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = ClashGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        color = AirpayColors.TextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { initiatePay() }
                                    ),
                                    cursorBrush = SolidColor(AirpayColors.Primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Optional Note button
                            if (ui.note.isNotEmpty()) {
                                Text(
                                    text = "Note: ${ui.note.take(12)}",
                                    fontSize = 11.sp,
                                    color = AirpayColors.TextSecondary
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Large Solid Purple "PAY" Button
                        val hasValidAmount = (ui.amount.toDoubleOrNull() ?: 0.0) > 0.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (hasValidAmount) AirpayColors.Primary else Color(0xFFC7B8E8))
                                .clickable(enabled = hasValidAmount) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    initiatePay()
                                },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (hasValidAmount) "PAY ₹${ui.amount}" else "ENTER AMOUNT TO PAY",
                                fontSize = 15.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Authentic PhonePe-style payment bubble aligned to the right.
 */
@Composable
private fun PaymentChatBubble(
    txn: TransactionEntity,
    payeeName: String
) {
    val formattedTime = remember(txn.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(txn.timestamp))
    }
    val formattedDate = remember(txn.timestamp) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(txn.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        // Date pill above first bubble
        Text(
            text = formattedDate,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9E9E9E),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 4.dp)
        )

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paid to $payeeName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AirpayColors.TextSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = AirpayColors.SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "₹${txn.amount}",
                    fontSize = 24.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )

                if (!txn.note.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Note: ${txn.note}",
                        fontSize = 11.sp,
                        color = AirpayColors.TextSecondary
                    )
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 0.8.dp)
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Offline SIM UPI",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00875A)
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 10.5.sp,
                        color = AirpayColors.TextSecondary
                    )
                }
            }
        }
    }
}
