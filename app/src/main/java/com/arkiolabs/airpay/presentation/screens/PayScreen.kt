package com.arkiolabs.airpay.presentation.screens

import android.content.Context
import android.text.format.DateUtils
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.data.TransactionEntity
import com.arkiolabs.airpay.domain.OperationMode
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.platform.ApkShareUtil
import com.arkiolabs.airpay.platform.BiometricHelper
import com.arkiolabs.airpay.platform.ContactsHelper
import com.arkiolabs.airpay.platform.SoundManager
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.PayUiState
import com.arkiolabs.airpay.presentation.PayViewModel
import com.arkiolabs.airpay.presentation.bottomsheets.BiometricSuggestionSheet
import com.arkiolabs.airpay.presentation.permissions.PermissionStatus
import com.arkiolabs.airpay.presentation.permissions.openAccessibilitySettings
import com.arkiolabs.airpay.presentation.permissions.openOverlaySettings
import com.arkiolabs.airpay.presentation.permissions.rememberPermissionLaunchers
import com.arkiolabs.airpay.presentation.ui.components.StatusBanner
import com.arkiolabs.airpay.presentation.ui.components.UpiPinScreen
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * PhonePe-Styled Contact Payment Conversation Interface.
 *
 * Implements:
 *  - Header: Contact avatar with initials, resolved contact name, phone/VPA, help icon.
 *  - Main Conversation Thread:
 *      * Shows payment history bubbles exchanged with this specific contact.
 *      * NO text messaging box — purely money sending & payment bubbles, exactly like PhonePe.
 *      * Friendly starter card if no prior payments exist with this contact.
 *      * Each bubble displays big bold amount, "✓ Paid Offline via SIM", timestamp, and "Pay Again"/"Share".
 *  - Bottom Money Sending Bar:
 *      * Quick amount chips (+₹100, +₹200, +₹500, +₹1,000, +₹2,000).
 *      * Clean currency amount field & optional note.
 *      * Prominent solid purple "PAY" button.
 *  - PIN / Biometric authentication & 100% offline USSD execution underneath.
 */
@Composable
fun PayScreen(
    viewModel: PayViewModel,
    historyViewModel: HistoryViewModel,
    onBack: () -> Unit = {},
    onNavigateScan: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateFaq: () -> Unit = {},
    permissions: PermissionStatus,
    modifier: Modifier = Modifier
) {
    val ui by viewModel.uiState.collectAsState()
    val session by viewModel.sessionState.collectAsState()
    val mode by viewModel.operationMode.collectAsState()
    val snackbar by viewModel.snackbar.collectAsState()
    val allTransactions by historyViewModel.transactions.collectAsState()

    val context = LocalContext.current
    val app = context.airpayApp
    val prefsRepo = app.prefsRepo
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current

    val biometricEnabled by prefsRepo.biometricEnabled.collectAsState(initial = false)
    val biometricSuggestionDismissed by prefsRepo.biometricSuggestionDismissed.collectAsState(initial = false)
    val savedPin by prefsRepo.savedBiometricPin.collectAsState(initial = null)
    val successSoundUri by prefsRepo.successSoundUri.collectAsState(initial = null)
    val failedSoundUri by prefsRepo.failedSoundUri.collectAsState(initial = null)

    var showPinScreen by remember { mutableStateOf(false) }
    var showBiometricSuggestion by remember { mutableStateOf(false) }

    // Resolve contact name from user's address book if saved
    val resolvedRecipientName = remember(ui.vpa, ui.payeeName) {
        ContactsHelper.resolveDisplayName(context, ui.vpa, ui.payeeName)
    }

    val formattedSubtitle = remember(ui.vpa) {
        val norm = ContactsHelper.normalizePhone(ui.vpa)
        if (norm != null && norm.length == 10) {
            "+91 ${norm.take(5)} ${norm.takeLast(5)}"
        } else {
            ui.vpa.ifBlank { "Offline Payee" }
        }
    }

    // Play sound upon success/failure and suggest biometric after first successful payment
    LaunchedEffect(session) {
        when (session) {
            is SessionState.Success -> {
                SoundManager.playSuccess(context, successSoundUri)
                if (!biometricEnabled && !biometricSuggestionDismissed && !savedPin.isNullOrBlank()) {
                    showBiometricSuggestion = true
                }
            }
            is SessionState.Failed -> {
                if (!failedSoundUri.isNullOrBlank()) {
                    SoundManager.playFailure(context, failedSoundUri)
                }
            }
            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .statusBarsPadding()
    ) {
        when (val s = session) {
            is SessionState.Idle -> {
                if (showPinScreen) {
                    UpiPinScreen(
                        amount = ui.amount,
                        recipientName = resolvedRecipientName,
                        bankName = "OFFLINE SIM BANK",
                        submitLabel = "PAY",
                        onPinSubmitted = { enteredPin ->
                            showPinScreen = false
                            scope.launch { prefsRepo.setSavedBiometricPin(enteredPin) }
                            viewModel.attemptPaymentWithPin(enteredPin)
                        },
                        onCancel = { showPinScreen = false },
                        onBiometricRequested = if (biometricEnabled) {
                            {
                                (context as? FragmentActivity)?.let { act ->
                                    BiometricHelper.prompt(
                                        activity = act,
                                        onSuccess = {
                                            showPinScreen = false
                                            if (!savedPin.isNullOrBlank()) {
                                                viewModel.attemptPaymentWithPin(savedPin!!)
                                            }
                                        },
                                        onError = { /* stay on PIN screen */ },
                                        onCancel = { /* stay on PIN screen */ }
                                    )
                                }
                            }
                        } else null
                    )
                } else {
                    PhonePeContactPaymentChat(
                        ui = ui,
                        recipientName = resolvedRecipientName,
                        recipientSubtitle = formattedSubtitle,
                        transactions = allTransactions,
                        permissions = permissions,
                        onBack = onBack,
                        onFaq = onNavigateFaq,
                        onHistory = onNavigateHistory,
                        onAmountChanged = { newAmount ->
                            viewModel.onFormFieldChanged(amount = newAmount)
                        },
                        onNoteChanged = { newNote ->
                            viewModel.onFormFieldChanged(note = newNote)
                        },
                        onProceed = {
                            keyboard?.hide()
                            if (!permissions.readyForDialerPay) {
                                // Request phone permissions
                                return@PhonePeContactPaymentChat
                            }
                            if (!permissions.accessibility) {
                                openAccessibilitySettings(context)
                                return@PhonePeContactPaymentChat
                            }
                            if (!permissions.overlay) {
                                openOverlaySettings(context)
                                return@PhonePeContactPaymentChat
                            }
                            if (mode == OperationMode.MANUAL) {
                                viewModel.attemptPayment()
                                return@PhonePeContactPaymentChat
                            }
                            // Direct Biometric Prompt if enabled and PIN is saved
                            if (biometricEnabled && !savedPin.isNullOrBlank()) {
                                (context as? FragmentActivity)?.let { act ->
                                    BiometricHelper.prompt(
                                        activity = act,
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
                    )
                }
            }
            is SessionState.Running -> AirpaySessionRunningCard(s, onCancel = viewModel::cancelSession)
            is SessionState.Success -> SessionSuccessCard(s, onDone = viewModel::dismissSession)
            is SessionState.Failed -> SessionFailedCard(
                state = s,
                onRetry = viewModel::dismissSession,
                onWhyFailed = onNavigateFaq
            )
        }

        // Floating Toast / Snackbar banner
        AirpaySnackbar(
            message = snackbar,
            onDismiss = viewModel::dismissSnackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        if (showBiometricSuggestion) {
            BiometricSuggestionSheet(
                onEnable = {
                    showBiometricSuggestion = false
                    scope.launch {
                        prefsRepo.setBiometricEnabled(true)
                        prefsRepo.setBiometricSuggestionDismissed(true)
                    }
                },
                onDismiss = {
                    showBiometricSuggestion = false
                    scope.launch {
                        prefsRepo.setBiometricSuggestionDismissed(true)
                    }
                }
            )
        }
    }
}

/**
 * Authentic PhonePe-Style Payment Conversation Screen.
 * Contains:
 *  - Recipient Top Bar
 *  - Payment Bubble Timeline (filtered for this contact)
 *  - Bottom Money Sending Bar (quick amount chips + amount box + PAY button)
 *  - NO text messaging bar! Purely money sending just like PhonePe.
 */
@Composable
private fun PhonePeContactPaymentChat(
    ui: PayUiState,
    recipientName: String,
    recipientSubtitle: String,
    transactions: List<TransactionEntity>,
    permissions: PermissionStatus,
    onBack: () -> Unit,
    onFaq: () -> Unit,
    onHistory: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onProceed: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val launchers = rememberPermissionLaunchers()

    // Filter transactions between current user and this specific contact
    val targetPhone = remember(ui.vpa) { ContactsHelper.normalizePhone(ui.vpa) }
    val targetClean = remember(ui.vpa) { ui.vpa.trim().lowercase() }

    val conversationTxns = remember(transactions, targetPhone, targetClean, recipientName) {
        transactions.filter { tx ->
            val txPhone = ContactsHelper.normalizePhone(tx.vpa)
            (targetPhone != null && txPhone == targetPhone) ||
                tx.vpa.trim().equals(targetClean, ignoreCase = true) ||
                (tx.payeeName != null && tx.payeeName.equals(recipientName, ignoreCase = true))
        }.sortedBy { it.timestamp } // oldest at top, newest at bottom
    }

    val listState = rememberLazyListState()

    // Auto-scroll to latest payment card at bottom
    LaunchedEffect(conversationTxns.size) {
        if (conversationTxns.isNotEmpty()) {
            listState.animateScrollToItem(conversationTxns.size - 1)
        }
    }

    val amountDouble = ui.amount.toDoubleOrNull() ?: 0.0
    val isValidAmount = amountDouble > 0.0

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Top Bar: Recipient Profile & Back Arrow ─────────────────
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onBack()
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

                // Avatar Initial Circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AirpayColors.PurpleTint)
                        .border(1.5.dp, AirpayColors.Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = recipientName.take(1).uppercase().ifBlank { "U" },
                        fontFamily = ClashGrotesk,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirpayColors.Primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Name & Subtitle
                Column(Modifier.weight(1f)) {
                    Text(
                        text = recipientName,
                        fontSize = 15.5.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(1.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = recipientSubtitle,
                            fontSize = 11.5.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "OFFLINE",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                // History shortcut
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onHistory() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Help shortcut
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onFaq() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_help_white),
                        contentDescription = "Help",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ── Main Conversation Area (Payment Bubbles / Empty State) ──
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (conversationTxns.isEmpty()) {
                // Starter Hero Card (PhonePe style empty conversation state)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AirpayColors.PurpleTint)
                            .border(2.dp, AirpayColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recipientName.take(1).uppercase().ifBlank { "U" },
                            fontSize = 28.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.Primary
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Send money to $recipientName",
                        fontSize = 17.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = recipientSubtitle,
                        fontSize = 12.5.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ 100% Offline UPI · Zero Internet Needed",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "No prior payments with this contact. Enter amount below and tap PAY to send money offline.",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // Starter quick amount chips
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("1", "100", "500").forEach { chipAmount ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(1.dp, AirpayColors.PurpleBorder, RoundedCornerShape(16.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        onAmountChanged(chipAmount)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Send ₹$chipAmount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AirpayColors.Primary
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    state = listState
                ) {
                    item { Spacer(Modifier.height(14.dp)) }

                    items(conversationTxns, key = { it.id }) { txn ->
                        PhonePePaymentBubble(
                            txn = txn,
                            recipientName = recipientName,
                            onPayAgain = {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onAmountChanged(txn.amount)
                            },
                            onShareReceipt = {
                                ApkShareUtil.shareTransactionReceipt(
                                    context = context,
                                    amount = txn.amount,
                                    payeeName = recipientName,
                                    vpa = txn.vpa,
                                    timestamp = txn.timestamp
                                )
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }

        // ── Warnings if Phone permission is missing ─────────────────
        if (!permissions.readyForDialerPay) {
            Box(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                StatusBanner(
                    title = "Phone Permission Required",
                    message = "Airpay needs phone permission to run offline payments.",
                    actionLabel = "Grant",
                    onAction = { launchers.requestPhoneBundle() }
                )
            }
        }

        // ── Bottom Money Sending Bar (PhonePe Style) ────────────────
        // Notice: NO text messaging box — purely money sending & chips!
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Quick Amount Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("100", "200", "500", "1000", "2000").forEach { chipAmount ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F3FB))
                                .border(1.dp, Color(0xFFE8E5F3), RoundedCornerShape(8.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    val current = ui.amount.toDoubleOrNull() ?: 0.0
                                    val addition = chipAmount.toDouble()
                                    onAmountChanged((current + addition).toInt().toString())
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+₹$chipAmount",
                                fontSize = 11.5.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.SemiBold,
                                color = AirpayColors.Primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Money Sending Row: Amount Box + PAY Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Amount Input Container
                    val boxShape = RoundedCornerShape(12.dp)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(boxShape)
                            .background(Color(0xFFF7F8FA))
                            .border(1.dp, Color(0xFFE2E4E8), boxShape)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹",
                                fontSize = 24.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )

                            Spacer(Modifier.width(6.dp))

                            Box(Modifier.weight(1f)) {
                                if (ui.amount.isEmpty()) {
                                    Text(
                                        text = "Enter amount",
                                        fontSize = 18.sp,
                                        fontFamily = ClashGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBDBDBD)
                                    )
                                }
                                BasicTextField(
                                    value = ui.amount,
                                    onValueChange = { input ->
                                        onAmountChanged(input.filter { it.isDigit() })
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        fontSize = 20.sp,
                                        fontFamily = ClashGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1C1E)
                                    ),
                                    cursorBrush = SolidColor(AirpayColors.Primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Optional Note row
                        BasicTextField(
                            value = ui.note,
                            onValueChange = onNoteChanged,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 11.5.sp,
                                color = Color(0xFF616161)
                            ),
                            cursorBrush = SolidColor(AirpayColors.Primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            decorationBox = { innerTextField ->
                                if (ui.note.isEmpty()) {
                                    Text(
                                        text = "Add note (optional)",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    // Solid Purple PAY Button
                    val btnShape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .clip(btnShape)
                            .background(if (isValidAmount) AirpayColors.Primary else AirpayColors.Primary.copy(alpha = 0.5f))
                            .clickable(enabled = isValidAmount) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onProceed()
                            }
                            .padding(horizontal = 22.dp, vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isValidAmount) "PAY ₹${ui.amount}" else "PAY",
                            fontSize = 14.5.sp,
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

/**
 * PhonePe Payment Bubble Card rendered in the chat timeline.
 */
@Composable
private fun PhonePePaymentBubble(
    txn: TransactionEntity,
    recipientName: String,
    onPayAgain: () -> Unit,
    onShareReceipt: () -> Unit
) {
    val timeFormatted = remember(txn.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(txn.timestamp))
    }

    val dateFormatted = remember(txn.timestamp) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(txn.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        val bubbleShape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 4.dp
        )

        Surface(
            color = Color.White,
            shape = bubbleShape,
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .border(1.dp, Color(0xFFE5E0F2), bubbleShape)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Top: Status & Recipient
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF00875A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Paid to $recipientName",
                        fontSize = 13.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Bold Amount
                Text(
                    text = "₹${txn.amount}",
                    fontSize = 24.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "✓ Debited via SIM • Offline USSD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00875A)
                )

                if (!txn.note.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF5F6F8))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Note: ${txn.note}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF616161)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)
                Spacer(Modifier.height(8.dp))

                // Actions row: Pay Again & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateFormatted,
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pay Again Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AirpayColors.PurpleTint)
                                .clickable { onPayAgain() }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = AirpayColors.Primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Pay Again",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.Primary
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Share receipt icon
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF4F5F7))
                                .clickable { onShareReceipt() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Receipt",
                                tint = Color(0xFF616161),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AirpaySessionRunningCard(state: SessionState.Running, onCancel: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, AirpayColors.Border, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Processing Offline Payment",
                    fontSize = 18.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.label,
                    fontSize = 14.sp,
                    color = AirpayColors.TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Step ${state.stepIndex + 1} of ${state.total}",
                    fontSize = 12.sp,
                    color = AirpayColors.TextMuted
                )
                Spacer(Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = { ((state.stepIndex + 1f) / state.total).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AirpayColors.Primary,
                    trackColor = AirpayColors.Border
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, AirpayColors.Danger, RoundedCornerShape(12.dp))
                .clickable { onCancel() }
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Cancel Transaction",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AirpayColors.Danger
            )
        }
    }
}

@Composable
private fun AirpaySnackbar(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2500)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AirpayColors.DarkNavy,
                shadowElevation = 6.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
