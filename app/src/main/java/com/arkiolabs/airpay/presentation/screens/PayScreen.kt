package com.arkiolabs.airpay.presentation.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.domain.FormField
import com.arkiolabs.airpay.domain.OperationMode
import com.arkiolabs.airpay.domain.RecipientResolver
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.platform.BiometricHelper
import com.arkiolabs.airpay.platform.SoundManager
import com.arkiolabs.airpay.presentation.PayUiState
import com.arkiolabs.airpay.presentation.PayViewModel
import com.arkiolabs.airpay.presentation.permissions.PermissionStatus
import com.arkiolabs.airpay.presentation.permissions.openAccessibilitySettings
import com.arkiolabs.airpay.presentation.permissions.openOverlaySettings
import com.arkiolabs.airpay.presentation.permissions.rememberPermissionLaunchers
import com.arkiolabs.airpay.presentation.ui.components.AirpayLottieAnimation
import com.arkiolabs.airpay.presentation.ui.components.StatusBanner
import com.arkiolabs.airpay.presentation.ui.components.UpiPinScreen
import com.arkiolabs.airpay.presentation.ui.components.JioRegistrationSheet
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import kotlinx.coroutines.launch

/**
 * Authentic Airpay Styled Send Money (Pay) Screen.
 *
 * Implements:
 *  - Header with Back arrow, "Send Money", "to any UPI app", Help (?) icon.
 *  - Recipient Identity Card with verified Payee Name & avatar initials.
 *  - Prominent Amount Card with ClashGrotesk ₹ symbol and quick-amount chips (+100, +500, +1000).
 *  - One-tap Biometric Authentication (Fingerprint/Face) to bypass PIN.
 *  - Fullscreen authentic Airpay UPI PIN Screen.
 *  - Custom transaction success and failure sound playback.
 */
@Composable
fun PayScreen(
    viewModel: PayViewModel,
    onNavigateScan: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateFaq: () -> Unit,
    permissions: PermissionStatus,
    modifier: Modifier = Modifier
) {
    val ui by viewModel.uiState.collectAsState()
    val session by viewModel.sessionState.collectAsState()
    val mode by viewModel.operationMode.collectAsState()
    val snackbar by viewModel.snackbar.collectAsState()

    val context = LocalContext.current
    val app = context.airpayApp
    val prefsRepo = app.prefsRepo
    val scope = rememberCoroutineScope()

    val biometricEnabled by prefsRepo.biometricEnabled.collectAsState(initial = false)
    val savedPin by prefsRepo.savedBiometricPin.collectAsState(initial = null)
    val successSoundUri by prefsRepo.successSoundUri.collectAsState(initial = null)
    val failedSoundUri by prefsRepo.failedSoundUri.collectAsState(initial = null)
    val jioRegistered by prefsRepo.jio123PayRegistered.collectAsState(initial = false)

    var isJio by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isJio = app.carrierDetector.isCurrentSimJio()
    }

    var showPinScreen by remember { mutableStateOf(false) }
    var showJioRegistrationSheet by remember { mutableStateOf(false) }

    // Play custom sounds upon success or failure
    LaunchedEffect(session) {
        when (session) {
            is SessionState.Success -> SoundManager.playSuccess(context, successSoundUri)
            is SessionState.Failed -> {
                if (!failedSoundUri.isNullOrBlank()) {
                    SoundManager.playFailure(context, failedSoundUri)
                }
            }
            else -> Unit
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(AirpayColors.Background)
            .statusBarsPadding()
    ) {
        when (val s = session) {
            is SessionState.Idle -> {
                if (showPinScreen) {
                    val resolved = RecipientResolver.resolve(ui.vpa)
                    UpiPinScreen(
                        amount = ui.amount,
                        recipientName = ui.payeeName.ifBlank { resolved.displayName },
                        bankName = "OFFLINE SIM BANK",
                        submitLabel = "PAY",
                        onPinSubmitted = { enteredPin ->
                            showPinScreen = false
                            if (biometricEnabled) {
                                scope.launch { prefsRepo.setSavedBiometricPin(enteredPin) }
                            }
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
                    AirpayPayForm(
                        ui = ui,
                        mode = mode,
                        permissions = permissions,
                        onProceed = {
                            if (isJio && !jioRegistered) {
                                showJioRegistrationSheet = true
                                return@AirpayPayForm
                            }
                            if (!isJio && !permissions.accessibility) {
                                openAccessibilitySettings(context)
                                return@AirpayPayForm
                            }
                            if (!permissions.overlay) {
                                openOverlaySettings(context)
                                return@AirpayPayForm
                            }
                            if (mode == OperationMode.MANUAL) {
                                viewModel.attemptPayment()
                                return@AirpayPayForm
                            }
                            // If biometric is enabled and saved PIN exists, prompt fingerprint directly!
                            if (biometricEnabled && !savedPin.isNullOrBlank()) {
                                (context as? FragmentActivity)?.let { act ->
                                    BiometricHelper.prompt(
                                        activity = act,
                                        onSuccess = {
                                            viewModel.attemptPaymentWithPin(savedPin!!)
                                        },
                                        onError = {
                                            // Fallback to on-screen UPI PIN
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
                        },
                        onScan = onNavigateScan,
                        onHistory = onNavigateHistory,
                        onFaq = onNavigateFaq,
                        onVpa = { v -> viewModel.onFormFieldChanged(vpa = v) },
                        onAmount = { v -> viewModel.onFormFieldChanged(amount = v) },
                        onNote = { v -> viewModel.onFormFieldChanged(note = v) }
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

        if (showJioRegistrationSheet) {
            JioRegistrationSheet(
                onDismiss = { showJioRegistrationSheet = false },
                onRegistrationConfirmed = { bank, lang ->
                    scope.launch {
                        prefsRepo.setJio123PayRegistered(true)
                        prefsRepo.setUserBankName(bank)
                        prefsRepo.setUserPreferredLanguage(lang)
                    }
                    showJioRegistrationSheet = false
                    showPinScreen = true
                }
            )
        }
    }
}

@Composable
private fun AirpayPayForm(
    ui: PayUiState,
    mode: OperationMode,
    permissions: PermissionStatus,
    onProceed: () -> Unit,
    onScan: () -> Unit,
    onHistory: () -> Unit,
    onFaq: () -> Unit,
    onVpa: (String) -> Unit,
    onAmount: (String) -> Unit,
    onNote: (String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val launchers = rememberPermissionLaunchers()
    val keyboard = LocalSoftwareKeyboardController.current

    val resolved = remember(ui.vpa) { RecipientResolver.resolve(ui.vpa) }
    val amountLooksValid = ui.amount.toDoubleOrNull()?.let { it > 0.0 } == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // ── Top Navigation Bar (Airpay Style) ───────────────────
        Surface(
            color = AirpayColors.Surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onHistory() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bottom_nav_history_selected),
                        contentDescription = "History",
                        tint = AirpayColors.TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Send Money",
                        fontFamily = ClashGrotesk,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirpayColors.TextPrimary
                    )
                    Text(
                        text = "Direct Offline SIM Network",
                        fontSize = 11.sp,
                        color = AirpayColors.TextSecondary
                    )
                }

                // QR Scanner button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onScan() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_scan),
                        contentDescription = "Scan QR",
                        tint = AirpayColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Help icon (?)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onFaq() },
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

        // ── Scrollable Form Area ─────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Permission Warnings
            if (!permissions.readyForDialerPay) {
                StatusBanner(
                    title = "Phone Permission Required",
                    message = "Airpay needs phone permission to run offline payments.",
                    actionLabel = "Grant",
                    onAction = { launchers.requestPhoneBundle() }
                )
                Spacer(Modifier.height(10.dp))
            }

            if (mode == OperationMode.AUTO && !permissions.readyForOverlayPay) {
                if (!permissions.accessibility) {
                    StatusBanner(
                        title = "Accessibility Permission",
                        message = "Enable Accessibility for seamless auto payments.",
                        actionLabel = "Settings",
                        onAction = { openAccessibilitySettings(context) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
                if (!permissions.overlay) {
                    StatusBanner(
                        title = "Overlay Permission",
                        message = "Enable Overlay to hide system popups.",
                        actionLabel = "Settings",
                        onAction = { openOverlaySettings(context) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            // ── Recipient Card ───────────────────────────────────────
            val cardShape = RoundedCornerShape(16.dp)
            Surface(
                color = AirpayColors.Surface,
                shape = cardShape,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AirpayColors.Border, cardShape)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transfer to",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AirpayColors.TextSecondary
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(AirpayColors.PurpleTint)
                                .border(1.5.dp, AirpayColors.Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (ui.payeeName.ifBlank { resolved.displayName }).take(1).uppercase().ifBlank { "?" },
                                fontSize = 18.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.Primary
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = ui.payeeName.ifBlank { resolved.displayName },
                                fontSize = 15.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            BasicTextField(
                                value = ui.vpa,
                                onValueChange = onVpa,
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = AirpayColors.TextSecondary,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(AirpayColors.Primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (resolved.isValid) {
                            Icon(
                                imageVector = Iconsax.TickCircle,
                                contentDescription = "Verified",
                                tint = AirpayColors.SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (ui.errors[FormField.VPA] != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = ui.errors[FormField.VPA] ?: "",
                            fontSize = 12.sp,
                            color = AirpayColors.DangerRed
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Amount Card ──────────────────────────────────────────
            Surface(
                color = AirpayColors.Surface,
                shape = cardShape,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AirpayColors.Border, cardShape)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Enter Amount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AirpayColors.TextSecondary
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 34.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.TextPrimary
                        )

                        Spacer(Modifier.width(8.dp))

                        Box(Modifier.weight(1f)) {
                            if (ui.amount.isEmpty()) {
                                Text(
                                    text = "0",
                                    fontSize = 34.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = AirpayColors.TextMuted
                                )
                            }
                            BasicTextField(
                                value = ui.amount,
                                onValueChange = onAmount,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    fontSize = 34.sp,
                                    fontFamily = ClashGrotesk,
                                    color = AirpayColors.TextPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(AirpayColors.Primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (ui.errors[FormField.AMOUNT] != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = ui.errors[FormField.AMOUNT] ?: "",
                            fontSize = 12.sp,
                            color = AirpayColors.DangerRed
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Quick Amount Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("100", "500", "1000", "2000").forEach { chipAmount ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AirpayColors.PurpleTint)
                                    .border(1.dp, AirpayColors.PurpleBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        val current = ui.amount.toDoubleOrNull() ?: 0.0
                                        val addition = chipAmount.toDouble()
                                        onAmount((current + addition).toInt().toString())
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "+₹$chipAmount",
                                    fontSize = 12.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AirpayColors.Primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Note input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AirpayColors.Background)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        if (ui.note.isEmpty()) {
                            Text(
                                text = "Add a note (e.g. Dinner, Groceries)",
                                fontSize = 13.sp,
                                color = AirpayColors.TextSecondary
                            )
                        }
                        BasicTextField(
                            value = ui.note,
                            onValueChange = onNote,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = AirpayColors.TextPrimary
                            ),
                            cursorBrush = SolidColor(AirpayColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Fixed Bottom Button: PROCEED TO PAY ───────────────────
        Surface(
            color = AirpayColors.Surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                val btnShape = RoundedCornerShape(12.dp)
                val isEnabled = permissions.readyForDialerPay && ui.vpa.isNotBlank() && amountLooksValid

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(btnShape)
                        .background(if (isEnabled) AirpayColors.Primary else AirpayColors.Primary.copy(alpha = 0.5f))
                        .clickable(enabled = isEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            keyboard?.hide()
                            onProceed()
                        }
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (amountLooksValid) "PAY ₹${ui.amount}" else "PROCEED TO PAY",
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
            color = AirpayColors.Surface,
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
