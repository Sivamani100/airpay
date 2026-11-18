package com.arkiolabs.localpe.presentation.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.localpe.R
import com.arkiolabs.localpe.domain.OperationMode
import com.arkiolabs.localpe.domain.SessionState
import com.arkiolabs.localpe.localPeApp
import com.arkiolabs.localpe.platform.BiometricHelper
import com.arkiolabs.localpe.platform.SoundManager
import com.arkiolabs.localpe.presentation.BalanceViewModel
import com.arkiolabs.localpe.presentation.HistoryViewModel
import com.arkiolabs.localpe.presentation.ui.components.LocalPeLottieAnimation
import com.arkiolabs.localpe.presentation.ui.components.UpiPinScreen
import com.arkiolabs.localpe.presentation.ui.components.JioRegistrationSheet
import com.arkiolabs.localpe.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.localpe.presentation.ui.theme.Iconsax
import com.arkiolabs.localpe.presentation.ui.theme.LocalPeColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated LocalPe Check Balance Screen.
 *
 * Implements:
 *  - Fresh check flow (does not display stale cached balances).
 *  - One-tap Biometric Fingerprint authorization.
 *  - NPCI UPI PIN Screen matching user screenshot.
 *  - Live animated USSD verification with LocalPe worm loader.
 *  - Dedicated full-screen Available Balance display with celebratory tick.
 *  - Zero visible *99# or carrier internal codes.
 */
@Composable
fun BalanceScreen(
    viewModel: BalanceViewModel,
    historyViewModel: HistoryViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session by viewModel.sessionState.collectAsState()
    val mode by viewModel.operationMode.collectAsState()

    val context = LocalContext.current
    val app = context.localPeApp
    val prefsRepo = app.prefsRepo
    val scope = rememberCoroutineScope()
    val view = LocalView.current

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
            .background(LocalPeColors.Background)
            .statusBarsPadding()
    ) {
        when (val s = session) {
            is SessionState.Idle -> {
                if (showPinScreen) {
                    UpiPinScreen(
                        amount = "",
                        recipientName = "Primary Bank Balance Check",
                        bankName = "OFFLINE SIM BANK",
                        submitLabel = "SUBMIT",
                        onPinSubmitted = { enteredPin ->
                            showPinScreen = false
                            if (biometricEnabled) {
                                scope.launch { prefsRepo.setSavedBiometricPin(enteredPin) }
                            }
                            viewModel.attemptCheckBalanceWithPin(enteredPin)
                        },
                        onCancel = { showPinScreen = false },
                        onBiometricRequested = if (biometricEnabled) {
                            {
                                (context as? FragmentActivity)?.let { act ->
                                    BiometricHelper.prompt(
                                        activity = act,
                                        title = "Check Bank Balance",
                                        subtitle = "Verify fingerprint to check balance",
                                        onSuccess = {
                                            showPinScreen = false
                                            if (!savedPin.isNullOrBlank()) {
                                                viewModel.attemptCheckBalanceWithPin(savedPin!!)
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
                    BalanceInitialScreen(
                        onInitiateCheck = {
                            if (isJio && !jioRegistered) {
                                showJioRegistrationSheet = true
                                return@BalanceInitialScreen
                            }
                            if (mode == OperationMode.MANUAL) {
                                viewModel.attemptCheckBalance()
                                return@BalanceInitialScreen
                            }
                            if (biometricEnabled && !savedPin.isNullOrBlank()) {
                                (context as? FragmentActivity)?.let { act ->
                                    BiometricHelper.prompt(
                                        activity = act,
                                        title = "Check Bank Balance",
                                        subtitle = "Verify fingerprint to query balance",
                                        onSuccess = {
                                            viewModel.attemptCheckBalanceWithPin(savedPin!!)
                                        },
                                        onError = { showPinScreen = true },
                                        onCancel = { showPinScreen = true }
                                    )
                                } ?: run {
                                    showPinScreen = true
                                }
                            } else {
                                showPinScreen = true
                            }
                        },
                        onBack = onClose
                    )
                }
            }

            is SessionState.Running -> {
                BalanceRunningView(state = s, onCancel = viewModel::cancelSession)
            }

            is SessionState.Success -> {
                BalanceSuccessDedicatedView(
                    resultText = s.resultText,
                    onDone = {
                        viewModel.dismissSession()
                        onClose()
                    }
                )
            }

            is SessionState.Failed -> {
                BalanceFailedView(
                    message = s.message,
                    onRetry = {
                        viewModel.dismissSession()
                        showPinScreen = true
                    },
                    onBack = {
                        viewModel.dismissSession()
                        onClose()
                    }
                )
            }
        }

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
private fun BalanceInitialScreen(
    onInitiateCheck: () -> Unit,
    onBack: () -> Unit
) {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Top Bar Header (LocalPe Style) ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Iconsax.ArrowLeft,
                        contentDescription = "Back",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Check Balance",
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
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    },
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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── Promo Banner (Navy Blue Card with Gold Jar) ───────────
            val bannerShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bannerShape)
                    .background(Color(0xFF0C192E))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Build ₹1,200 in 1 month",
                                fontSize = 15.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("→", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Save ₹40/day in Gold",
                            fontSize = 12.sp,
                            color = Color(0xFF90A4AE)
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.coin_without_stars),
                        contentDescription = "Gold Coins",
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Flat Payment Method Items (Exact LocalPe Screenshot) ──
            // 1. Primary Bank Account (State Bank of India)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onInitiateCheck()
                    }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF005696)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "State Bank of India - 7188",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Bank Account",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Icon(
                    imageVector = Iconsax.ArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(18.dp)
                )
            }

            // 2. LocalPe / LocalPe Wallet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Wallet */ }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.wallet_home),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LocalPe Wallet",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "ⓘ Upgrade now to pay on any QR",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                }

                Text(
                    text = "Activate",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalPeColors.Primary
                )
            }

            // 3. RuPay Credit Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Link card */ }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0288D1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("RuPay", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RuPay Credit Card",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                }

                Text(
                    text = "Link Now",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalPeColors.Primary
                )
            }

            // 4. Add new payment method
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Add */ }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF1C1C1E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add new payment method",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Bank Account, UPI Lite & more",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Icon(
                    imageVector = Iconsax.ArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(36.dp))

            // Carousel dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF1C1C1E)))
                Spacer(Modifier.width(6.dp))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFCCCCCC)))
                Spacer(Modifier.width(6.dp))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFCCCCCC)))
            }

            Spacer(Modifier.height(24.dp))

            // POWERED BY UPI Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.upi_orange_border),
                    contentDescription = "POWERED BY UPI",
                    modifier = Modifier.height(24.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun BalanceRunningView(state: SessionState.Running, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LocalPeLottieAnimation(
            assetName = "worm_loader_light.json",
            modifier = Modifier.size(110.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Checking Live Balance",
            fontSize = 18.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = LocalPeColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Querying your bank through cellular network...",
            fontSize = 13.sp,
            color = LocalPeColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        LinearProgressIndicator(
            progress = { ((state.stepIndex + 1f) / state.total).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = LocalPeColors.Primary,
            trackColor = LocalPeColors.PurpleTint
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(LocalPeColors.DangerTint)
                .clickable { onCancel() }
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancel",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalPeColors.DangerRed
            )
        }
    }
}

@Composable
private fun BalanceSuccessDedicatedView(resultText: String, onDone: () -> Unit) {
    val cardShape = RoundedCornerShape(20.dp)
    val parsed = parseBalance(resultText)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = LocalPeColors.Surface,
            shape = cardShape,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, LocalPeColors.SuccessGreen, cardShape)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LocalPeLottieAnimation(
                    assetName = "tick_success.json",
                    modifier = Modifier.size(90.dp),
                    iterations = 1
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "PRIMARY BANK OF INDIA",
                    fontSize = 12.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    color = LocalPeColors.TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Available Balance",
                    fontSize = 15.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Medium,
                    color = LocalPeColors.TextPrimary
                )
                Spacer(Modifier.height(10.dp))
                if (parsed != null) {
                    Text(
                        text = parsed,
                        fontSize = 36.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.ExtraBold,
                        color = LocalPeColors.TextPrimary
                    )
                } else {
                    Text(
                        text = resultText,
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        color = LocalPeColors.TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Checked on ${formatTimestamp(System.currentTimeMillis())}",
                    fontSize = 11.sp,
                    color = LocalPeColors.TextMuted
                )
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalPeColors.Primary)
                        .clickable { onDone() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DONE",
                        fontSize = 14.sp,
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
private fun BalanceFailedView(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = LocalPeColors.Surface,
            shape = cardShape,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LocalPeColors.DangerRed, cardShape)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Balance Check Failed",
                    fontSize = 17.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = LocalPeColors.DangerRed
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = LocalPeColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LocalPeColors.PurpleTint)
                            .clickable { onBack() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Back",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalPeColors.Primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LocalPeColors.Primary)
                            .clickable { onRetry() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Try Again",
                            fontSize = 13.sp,
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

private fun parseBalance(raw: String): String? {
    val regex = Regex("""(?i)(?:₹|rs\.?|inr|bal(?:ance)?[:\s=]*|balance\s+is[:\s]*)\s*(?:₹|rs\.?|inr)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""")
    val match = regex.find(raw)
    if (match != null) {
        val amount = match.groupValues[1]
        return "₹$amount"
    }
    val rawDigits = raw.filter { it.isDigit() || it == '.' || it == ',' }
    if (rawDigits.isNotEmpty() && rawDigits.any { it.isDigit() }) {
        return "₹${rawDigits.trim(',', '.')}"
    }
    return null
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "Recently"
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
