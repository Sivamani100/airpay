package com.arkiolabs.airpay.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkiolabs.airpay.data.PreferencesRepository
import com.arkiolabs.airpay.domain.ActionEvent
import com.arkiolabs.airpay.domain.ActionRunner
import com.arkiolabs.airpay.domain.Actions
import com.arkiolabs.airpay.domain.InputValidator
import com.arkiolabs.airpay.domain.OperationMode
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.platform.OverlayController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The most recent successful balance check, persisted across app kills.
 */
data class BalanceResult(val text: String, val timestamp: Long)

/**
 * UI state for the Balance check screen.
 *
 * PIN is captured inline on the form (a 6-box section is the screen's
 * hero) and lives here so it survives recompositions.
 */
data class BalanceUiState(
    val pin: String = "",
    val pinError: String? = null,
    val isSessionActive: Boolean = false
)

/**
 * ViewModel for the Balance check screen.
 *
 * Flow:
 *  1. User enters their PIN inline → screen calls [attemptCheckBalance]
 *     either on the 6th digit (auto-fire) or via the explicit "Check Now"
 *     CTA at 4-5 digits.
 *  2. [attemptCheckBalance] validates the PIN and starts the CheckBalance
 *     action via [runCheck].
 *  3. On success the carrier reply is persisted to PreferencesRepository
 *     so the last result survives app kill.
 *
 * @param clipboardWriter only used by MANUAL mode where the screen falls
 *   back to the dialer with offline code prefilled. Unused in ADVANCED/AUTO.
 */
class BalanceViewModel(
    private val actionRunner: ActionRunner,
    private val prefsRepo: PreferencesRepository,
    private val carrierDetector: com.arkiolabs.airpay.platform.CarrierDetector? = null,
    private val jioVoiceGateway: com.arkiolabs.airpay.platform.JioVoiceGateway? = null,
    private val overlayController: OverlayController? = null,
    private val onDialerFallback: (String) -> Unit = {},
    private val clipboardWriter: (String) -> Unit = {},
    /**
     * System-level Toast hook — shows over other apps including the
     * dialer once we hand off in MANUAL mode. Wired in MainScaffold.
     */
    private val systemToast: (String) -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(BalanceUiState())
    val uiState: StateFlow<BalanceUiState> = _uiState.asStateFlow()

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()

    /**
     * Last persisted balance result. Hydrated from DataStore so the screen
     * renders the prior balance immediately on launch.
     */
    val lastResult: StateFlow<BalanceResult?> = combine(
        prefsRepo.lastBalanceText,
        prefsRepo.lastBalanceTimestamp
    ) { text, ts ->
        if (text != null && ts != null) BalanceResult(text, ts) else null
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val operationMode: StateFlow<OperationMode> = prefsRepo.operationMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, OperationMode.AUTO)

    private var sessionJob: Job? = null

    /** Inline PIN editing — strips non-digits, caps at 6, clears any error. */
    fun onPinChanged(pin: String) {
        val digits = pin.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(pin = digits, pinError = null) }
    }

    /**
     * Validates the entered PIN and starts a balance-check session.
     *
     *  - Manual mode: opens the dialer with offline balance code prefilled. PIN entry is
     *    deferred to the carrier dialog itself.
     *  - Advanced/Auto: requires a valid 4-6 digit PIN, then runs the
     *    CheckBalance action via [runCheck].
     */
    fun attemptCheckBalance() {
        val mode = operationMode.value

        if (mode == OperationMode.MANUAL) {
            _snackbar.value = "Opening dialer for balance check"
            onDialerFallback("*99*3#")
            // System Toast — visible on top of the dialer so the user
            // remembers what's queued. The in-app snackbar above only
            // renders inside our activity which is about to lose focus.
            systemToast("Opening dialer for balance check")
            _sessionState.value = SessionState.Idle
            return
        }

        val pin = _uiState.value.pin
        val validation = InputValidator.validatePin(pin)
        if (!validation.isValid) {
            _uiState.update { it.copy(pinError = validation.errorMessage) }
            return
        }

        runCheck(pin)
    }

    fun attemptCheckBalanceWithPin(pin: String) {
        onPinChanged(pin)
        attemptCheckBalance()
    }

    /**
     * Runs the CheckBalance action with the captured PIN.
     * Routes dynamically by carrier:
     *  - Airtel / Vi / BSNL: Native USSD Engine automated in the background.
     *  - Reliance Jio: Dedicated Phone Voice Gateway (08045163581) over VoLTE.
     */
    private fun runCheck(pin: String) {
        viewModelScope.launch {
            val isJio = carrierDetector?.isCurrentSimJio() ?: false
            if (!isJio && !actionRunner.isServiceEnabled()) {
                _sessionState.value = SessionState.Failed(
                    message = "Accessibility service is disabled. Please enable it in Settings.",
                    resultText = ""
                )
                return@launch
            }

            if (isJio) {
                runJioPhoneBalanceCheck(pin)
            } else {
                runUssdBalanceCheck(pin)
            }
        }
    }

    private fun runJioPhoneBalanceCheck(pin: String) {
        _uiState.update { it.copy(pinError = null, isSessionActive = true) }
        _sessionState.value = SessionState.Running(
            label = "Connecting to Jio Voice Gateway (08045163666)...",
            stepIndex = 1,
            total = 3
        )

        // Show the 100% OPAQUE Jio VoLTE Balance Check overlay
        overlayController?.showJioAutoInject(
            recipient = "",
            amount = "",
            stepNumber = 1,
            stepTitle = "Jio VoLTE Balance Check",
            stepSubtitle = "Connecting to Jio Voice Gateway (08045163666)"
        )
        overlayController?.onCancel = { cancelSession() }

        val gateway = jioVoiceGateway
        overlayController?.onSpeakerToggle = {
            val isNowOn = !(gateway?.isSpeakerphoneOn() ?: false)
            gateway?.setSpeakerphone(isNowOn)
            isNowOn
        }

        val sessionStartTime = System.currentTimeMillis() - 5000L
        val initiated = gateway?.initiateBalanceCall(pin) ?: false
        if (!initiated) {
            _sessionState.value = SessionState.Failed(
                message = "Could not place Jio balance call",
                resultText = "Please verify voice signal is active."
            )
            overlayController?.hide()
            onSessionEnd()
            return
        }

        sessionJob = viewModelScope.launch {
            fun handleBalanceSms(sms: com.arkiolabs.airpay.domain.ParsedBankSms): Boolean {
                if (sms.balance != null) {
                    val balText = "₹${sms.balance}"
                    _sessionState.value = SessionState.Success(resultText = "Your bank balance is $balText")
                    launch {
                        prefsRepo.setLastBalance(
                            text = "Your bank balance is $balText",
                            timestamp = System.currentTimeMillis()
                        )
                    }
                    gateway.endActiveCall()
                    overlayController?.hide()
                    onSessionEnd()
                    return true
                } else if (!sms.isSuccess) {
                    _sessionState.value = SessionState.Failed(
                        message = "Could not fetch balance from bank",
                        resultText = "Bank declined balance request"
                    )
                    gateway.endActiveCall()
                    overlayController?.hide()
                    onSessionEnd()
                    return true
                }
                return false
            }

            val smsJob = launch {
                com.arkiolabs.airpay.platform.BankSmsReceiver.bankSmsEvents.collect { sms ->
                    val current = _sessionState.value
                    if (current is SessionState.Success || current is SessionState.Failed) {
                        return@collect
                    }
                    handleBalanceSms(sms)
                }
            }

            delay(4_000L) // After call setup -> Option 2 (Balance Check)
            if (_sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Running(
                    label = "Option 2 (Balance Check) injected",
                    stepIndex = 1,
                    total = 3
                )
                overlayController?.updateJioAutoInject(1, "Authorizing PIN...", "Entering Bank UPI PIN securely")
            }

            delay(2_500L) // After PIN prompt -> PIN#
            if (_sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Running(
                    label = "UPI PIN authorized",
                    stepIndex = 2,
                    total = 3
                )
                overlayController?.updateJioAutoInject(2, "Verifying Balance...", "Querying bank for account balance")
            }

            // Poll SMS inbox periodically for bank balance SMS
            var elapsed = 0L
            val pollInterval = 1_500L
            val maxWait = 15_000L
            while (elapsed < maxWait && _sessionState.value is SessionState.Running) {
                delay(pollInterval)
                elapsed += pollInterval
                val recentSms = com.arkiolabs.airpay.platform.BankSmsReceiver.pollRecentBankSms(sessionStartTime)
                if (recentSms != null) {
                    if (handleBalanceSms(recentSms)) break
                }
            }

            smsJob.cancel()
            gateway.endActiveCall()
            overlayController?.hide()

            val current = _sessionState.value
            if (current is SessionState.Running) {
                _sessionState.value = SessionState.Failed(
                    message = "Could not verify balance",
                    resultText = "No balance confirmation SMS received from your bank. Please check your SMS inbox or try again."
                )
                onSessionEnd()
            }
        }
    }

    private fun runUssdBalanceCheck(pin: String) {
        val mode = operationMode.value

        _uiState.update { it.copy(pinError = null, isSessionActive = true) }
        _sessionState.value = SessionState.Running(
            label = "Checking balance",
            stepIndex = 0,
            total = Actions.CheckBalance.steps.size
        )

        // Always show the full 100% opaque branded overlay to completely cover carrier dialogs
        overlayController?.show(
            title = "Checking Balance",
            subtitle = "Airpay is querying your bank…",
            stepLabel = "STARTING"
        )
        overlayController?.onCancel = { cancelSession() }

        val vars = mapOf("pin" to pin)
        val actionRun = actionRunner.runAction(Actions.CheckBalance, vars, viewModelScope)

        sessionJob = viewModelScope.launch {
            launch {
                actionRun.events.collect { event ->
                    val current = _sessionState.value
                    if (current is SessionState.Success || current is SessionState.Failed) {
                        return@collect
                    }
                    when (event) {
                        is ActionEvent.Progress -> {
                            _sessionState.value = SessionState.Running(
                                label = event.label ?: "Processing",
                                stepIndex = event.stepIndex,
                                total = event.total
                            )
                            overlayController?.update(
                                title = "Checking Balance",
                                subtitle = "Airpay is querying your bank…",
                                stepLabel = (event.label ?: "Processing").uppercase()
                            )
                        }
                        is ActionEvent.Done -> {
                            _sessionState.value = SessionState.Success(resultText = event.resultText)
                            overlayController?.hide()
                            launch {
                                prefsRepo.setLastBalance(
                                    text = event.resultText,
                                    timestamp = System.currentTimeMillis()
                                )
                            }
                            onSessionEnd()
                        }
                        is ActionEvent.Error -> {
                            _sessionState.value = SessionState.Failed(
                                message = event.message,
                                resultText = event.resultText
                            )
                            overlayController?.showError(
                                title = "Check failed",
                                message = event.message
                            )
                            if (event.message.contains("pin", ignoreCase = true) &&
                                (event.message.contains("incorrect", ignoreCase = true) ||
                                 event.message.contains("invalid", ignoreCase = true) ||
                                 event.message.contains("wrong", ignoreCase = true) ||
                                 event.message.contains("mismatch", ignoreCase = true))) {
                                launch { prefsRepo.clearBiometricPin() }
                            }
                            onSessionEnd()
                        }
                        else -> { /* Frame and Reply events — no UI state change */ }
                    }
                }
            }

            val result = actionRun.result.await()
            if (!result.success && _sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Failed(
                    message = "Balance check failed",
                    resultText = result.resultText
                )
                if (result.resultText.contains("pin", ignoreCase = true) ||
                    result.resultText.contains("incorrect", ignoreCase = true) ||
                    result.resultText.contains("wrong", ignoreCase = true)) {
                    launch { prefsRepo.clearBiometricPin() }
                }
                onSessionEnd()
            }
        }
    }

    /**
     * Cancels the current balance check session.
     */
    fun cancelSession() {
        sessionJob?.cancel()
        sessionJob = null
        jioVoiceGateway?.endActiveCall()
        overlayController?.onSpeakerToggle = null
        overlayController?.hide()
        _sessionState.value = SessionState.Idle
        onSessionEnd()
    }

    fun dismissSession() {
        _sessionState.value = SessionState.Idle
        // Clear PIN so a retry forces fresh entry.
        _uiState.update { it.copy(pin = "") }
    }

    fun dismissSnackbar() {
        _snackbar.value = null
    }

    private fun onSessionEnd() {
        _uiState.update { it.copy(isSessionActive = false) }
        viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(pin = "") }
        }
    }
}
