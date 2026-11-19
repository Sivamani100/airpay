package com.arkiolabs.airpay.presentation.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.platform.BiometricHelper
import com.arkiolabs.airpay.platform.SoundManager
import com.arkiolabs.airpay.presentation.BalanceViewModel
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.ui.components.AirpayLottieAnimation
import com.arkiolabs.airpay.presentation.ui.components.UpiPinScreen
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated Airpay Check Balance Screen.
 *
 * Implements:
 *  - Direct entry to authentic Airpay UPI PIN Screen (no intermediate unnecessary landing page).
 *  - One-tap Biometric Fingerprint authorization (auto-prompts if enabled).
 *  - Live animated offline verification with Airpay worm loader.
 *  - Dedicated full-screen Available Balance display matching screenshot with celebratory tick.
 *  - Pure, background USSD automation without visible carrier dialogs.
 */
@Composable
fun BalanceScreen(
    viewModel: BalanceViewModel,
    historyViewModel: HistoryViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session by viewModel.sessionState.collectAsState()

    val context = LocalContext.current
    val app = context.airpayApp
    val prefsRepo = app.prefsRepo
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    val biometricEnabled by prefsRepo.biometricEnabled.collectAsState(initial = false)
    val savedPin by prefsRepo.savedBiometricPin.collectAsState(initial = null)
    val successSoundUri by prefsRepo.successSoundUri.collectAsState(initial = null)
    val failedSoundUri by prefsRepo.failedSoundUri.collectAsState(initial = null)

    var hasPromptedBiometric by remember { mutableStateOf(false) }

    // Auto-prompt biometric if enabled when entering screen
    LaunchedEffect(biometricEnabled, savedPin) {
        if (biometricEnabled && !savedPin.isNullOrBlank() && !hasPromptedBiometric && session is SessionState.Idle) {
            hasPromptedBiometric = true
            (context as? FragmentActivity)?.let { act ->
                BiometricHelper.prompt(
                    activity = act,
                    title = "Check Bank Balance",
                    subtitle = "Verify fingerprint to check balance",
                    onSuccess = {
                        viewModel.attemptCheckBalanceWithPin(savedPin!!)
                    },
                    onError = { /* Keep showing UPI PIN screen */ },
                    onCancel = { /* Keep showing UPI PIN screen */ }
                )
            }
        }
    }

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
                UpiPinScreen(
                    amount = "",
                    recipientName = "Primary Bank Balance Check",
                    bankName = "OFFLINE SIM BANK",
                    submitLabel = "SUBMIT",
                    onPinSubmitted = { enteredPin ->
                        scope.launch { prefsRepo.setSavedBiometricPin(enteredPin) }
                        viewModel.attemptCheckBalanceWithPin(enteredPin)
                    },
                    onCancel = onClose,
                    onBiometricRequested = if (biometricEnabled && !savedPin.isNullOrBlank()) {
                        {
                            (context as? FragmentActivity)?.let { act ->
                                BiometricHelper.prompt(
                                    activity = act,
                                    title = "Check Bank Balance",
                                    subtitle = "Verify fingerprint to check balance",
                                    onSuccess = {
                                        viewModel.attemptCheckBalanceWithPin(savedPin!!)
                                    },
                                    onError = { /* stay on PIN screen */ },
                                    onCancel = { /* stay on PIN screen */ }
                                )
                            }
                        }
                    } else null
                )
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
                    },
                    onBack = {
                        viewModel.dismissSession()
                        onClose()
                    }
                )
            }
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
        AirpayLottieAnimation(
            assetName = "worm_loader_light.json",
            modifier = Modifier.size(110.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Checking Live Balance",
            fontSize = 18.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = AirpayColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Connecting securely to bank via SIM USSD…",
            fontSize = 13.sp,
            color = AirpayColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        LinearProgressIndicator(
            progress = {
                val total = if (state.total <= 0) 2 else state.total
                (state.stepIndex.toFloat() / total.toFloat()).coerceIn(0.1f, 0.95f)
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = AirpayColors.Primary,
            trackColor = Color(0xFFE0E0E0),
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onCancel() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancel",
                fontSize = 13.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Medium,
                color = AirpayColors.DangerRed
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
            color = AirpayColors.Surface,
            shape = cardShape,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, AirpayColors.SuccessGreen, cardShape)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AirpayLottieAnimation(
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
                    color = AirpayColors.TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Available Balance",
                    fontSize = 15.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Medium,
                    color = AirpayColors.TextPrimary
                )
                Spacer(Modifier.height(10.dp))
                if (parsed != null) {
                    Text(
                        text = parsed,
                        fontSize = 36.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.ExtraBold,
                        color = AirpayColors.TextPrimary
                    )
                } else {
                    Text(
                        text = resultText,
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        color = AirpayColors.TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Checked on ${formatTimestamp(System.currentTimeMillis())}",
                    fontSize = 11.sp,
                    color = AirpayColors.TextMuted
                )
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AirpayColors.Primary)
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
            color = AirpayColors.Surface,
            shape = cardShape,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AirpayColors.DangerRed, cardShape)
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
                    color = AirpayColors.DangerRed
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = AirpayColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF2F2F7))
                            .clickable { onBack() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Back",
                            fontSize = 13.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            color = AirpayColors.TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AirpayColors.Primary)
                            .clickable { onRetry() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Retry",
                            fontSize = 13.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extracts ₹ amount cleanly from USSD balance text.
 */
private fun parseBalance(raw: String): String? {
    val regex = Regex("(?:rs\\.?|inr|₹)\\s*([\\d,]+\\.?\\d{0,2})", RegexOption.IGNORE_CASE)
    val match = regex.find(raw)
    return match?.let { "₹${it.groupValues[1]}" }
}

private fun formatTimestamp(ts: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ts))
}
