package com.arkiolabs.downpay.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkiolabs.downpay.data.HistoryRepository
import com.arkiolabs.downpay.data.PreferencesRepository
import com.arkiolabs.downpay.domain.ActionEvent
import com.arkiolabs.downpay.domain.ActionRun
import com.arkiolabs.downpay.domain.ActionRunner
import com.arkiolabs.downpay.domain.Actions
import com.arkiolabs.downpay.domain.FormField
import com.arkiolabs.downpay.domain.InputValidator
import com.arkiolabs.downpay.domain.OperationMode
import com.arkiolabs.downpay.domain.SessionState
import com.arkiolabs.downpay.domain.UpiData
import com.arkiolabs.downpay.domain.UpiParser
import com.arkiolabs.downpay.platform.CarrierDetector
import com.arkiolabs.downpay.platform.OverlayController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Pay screen form. PIN is captured inline on the form
 * itself in a highlighted "ENTER UPI PIN" section — no dedicated screen.
 */
data class PayUiState(
    val vpa: String = "",
    val payeeName: String = "",
    val amount: String = "",
    val note: String = "",
    val pin: String = "",
    val errors: Map<FormField, String> = emptyMap(),
    val isSessionActive: Boolean = false
)

/**
 * ViewModel for the Pay screen. Manages form state, QR autofill,
 * input validation, USSD session lifecycle, and PIN security.
 *
 * Pay flow:
 *  1. User fills VPA + amount + inline PIN.
 *  2. Either auto-fires when 6 digits are typed (the screen debounces and
 *     calls [attemptPayment]) or the user taps the Pay button at 4-5 digits.
 *  3. [attemptPayment] validates everything; in MANUAL mode it copies the
 *     VPA to clipboard and opens the dialer; in ADVANCED/AUTO it runs the
 *     ActionRunner.
 *  4. PIN is held in volatile memory only and cleared <500ms after session
 *     end (security guarantee from the original spec).
 *
 * @param clipboardWriter callback used by MANUAL mode to copy the VPA to
 *   the system clipboard. Wired in the activity layer so the ViewModel
 *   itself stays free of Android Context dependencies.
 */
class PayViewModel(
    private val actionRunner: ActionRunner,
    private val historyRepo: HistoryRepository,
    private val prefsRepo: PreferencesRepository,
    private val carrierDetector: CarrierDetector,
    private val jioVoiceGateway: com.arkiolabs.downpay.platform.JioVoiceGateway? = null,
    private val overlayController: OverlayController? = null,
    private val onDialerFallback: (String) -> Unit = {},
    private val clipboardWriter: (String) -> Unit = {},
    /**
     * System-level Toast hook — shows over other apps, including the
     * native dialer once we hand off in MANUAL mode. Snackbars only
     * render inside our own activity, so they're useless once the user
     * is on the carrier dialog. Wired in MainScaffold via the activity
     * context.
     */
    private val systemToast: (String) -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(PayUiState())
    val uiState: StateFlow<PayUiState> = _uiState.asStateFlow()

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()

    /** Currently selected operation mode, observed for routing payments. */
    val operationMode: StateFlow<OperationMode> = prefsRepo.operationMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, OperationMode.AUTO)

    private var activeRun: ActionRun? = null
    private var sessionJob: Job? = null

    /**
     * Re-prefills the form from a past transaction (used by "Pay again"
     * in the History screen). Drops any in-memory PIN so the user must
     * re-enter it; we never silently re-execute payments.
     */
    fun prefillFromTransaction(
        vpa: String,
        amount: String,
        note: String?
    ) {
        _uiState.update { current ->
            current.copy(
                vpa = vpa,
                amount = amount,
                note = note ?: "",
                pin = "",
                errors = emptyMap()
            )
        }
    }

    /**
     * Parses QR scanned data and autofills form fields for all non-null values.
     */
    fun onQrScanned(raw: String) {
        val upiData: UpiData = UpiParser.parse(raw) ?: return
        _uiState.update { current ->
            current.copy(
                vpa = upiData.vpa,
                payeeName = upiData.payeeName ?: current.payeeName,
                amount = upiData.amount ?: current.amount,
                note = upiData.transactionNote ?: current.note,
                errors = emptyMap()
            )
        }
    }

    /**
     * Updates one or more form fields. Pass `null` to leave a field unchanged.
     * Editing a field clears its validation error so the highlight goes away
     * as soon as the user starts fixing it.
     */
    fun onFormFieldChanged(
        vpa: String? = null,
        amount: String? = null,
        note: String? = null
    ) {
        _uiState.update { current ->
            val newErrors = current.errors.toMutableMap()
            if (vpa != null) newErrors.remove(FormField.VPA)
            if (amount != null) newErrors.remove(FormField.AMOUNT)
            current.copy(
                vpa = vpa ?: current.vpa,
                amount = amount ?: current.amount,
                note = note ?: current.note,
                errors = newErrors
            )
        }
    }

    /**
     * Inline PIN editing. Strips non-digits and caps at 6. Clears any
     * existing PIN error so the user sees the highlight clear as they type.
     */
    fun onPinChanged(pin: String) {
        val digits = pin.filter { it.isDigit() }.take(6)
        _uiState.update { current ->
            current.copy(
                pin = digits,
                errors = current.errors - FormField.PIN
            )
        }
    }

    /**
     * Attempts to start a payment. Validates the form, then either:
     *  - Manual mode: copies VPA to clipboard, opens dialer, resets state.
     *  - Advanced/Auto: runs the ActionRunner via [runPayment].
     *
     * Auto-fires once the user types a 6-digit PIN (the screen calls this).
     * Manual taps of the Pay button at 4-5 digits also reach here.
     */
    fun attemptPayment() {
        val state = _uiState.value
        val mode = operationMode.value

        // Validate VPA and amount up front for every mode (manual still
        // needs a valid VPA — that's what we copy to the clipboard).
        val errors = mutableMapOf<FormField, String>()
        val trimmedVpa = state.vpa.trim()
        val isMobile = trimmedVpa.all { it.isDigit() } && (trimmedVpa.length == 10 || trimmedVpa.length == 12)
        if (!isMobile) {
            InputValidator.validateVpa(trimmedVpa).also {
                if (!it.isValid) errors[FormField.VPA] = it.errorMessage!!
            }
        }
        InputValidator.validateAmount(state.amount).also {
            if (!it.isValid) errors[FormField.AMOUNT] = it.errorMessage!!
        }

        // PIN is required only for automated modes; manual mode lets the
        // user enter the PIN themselves in the dialer.
        if (mode != OperationMode.MANUAL) {
            InputValidator.validatePin(state.pin).also {
                if (!it.isValid) errors[FormField.PIN] = it.errorMessage!!
            }
        }

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        _uiState.update { it.copy(errors = emptyMap()) }

        val cleanedVpa = if (isMobile && trimmedVpa.length == 12 && trimmedVpa.startsWith("91")) trimmedVpa.substring(2) else trimmedVpa
        val cleanedAmount = state.amount.trim()
        val cleanedNote = state.note

        if (mode == OperationMode.MANUAL) {
            clipboardWriter(cleanedVpa)
            _snackbar.value = "Recipient copied — opening dialer"
            onDialerFallback(if (isMobile) "*99*1*1#" else "*99*1*3#")
            systemToast("Recipient copied — please paste on the phone prompt")
            _sessionState.value = SessionState.Idle
            return
        }

        runPayment(cleanedVpa, cleanedAmount, cleanedNote, state.pin)
    }

    fun attemptPaymentWithPin(pin: String) {
        onPinChanged(pin)
        attemptPayment()
    }

    /**
     * Validates inputs and starts the payment session.
     * Routes dynamically by carrier:
     *  - Airtel / Vi / BSNL: Native USSD Engine automated in the background.
     *  - Reliance Jio: Dedicated Phone Voice Gateway (08045163581) over VoLTE.
     */
    private fun runPayment(vpa: String, amount: String, note: String, pin: String) {
        viewModelScope.launch {
            val isJio = carrierDetector.isCurrentSimJio()

            // For USSD carriers (Airtel, Vi, BSNL), accessibility service is mandatory to automate
            if (!isJio && !actionRunner.isServiceEnabled()) {
                _sessionState.value = SessionState.Failed(
                    message = "Accessibility service is disabled. Enable it in Settings.",
                    resultText = ""
                )
                return@launch
            }

            if (isJio) {
                runJioPhonePayment(vpa, amount, note, pin)
            } else {
                runUssdPayment(vpa, amount, note, pin)
            }
        }
    }

    /**
     * Dedicated Phone Voice Gateway payment for Reliance Jio SIM holders.
     * Initiates official voice call (08045163666) over Jio VoLTE
     * with automated DTMF tones and listens for genuine bank debit confirmation SMS.
     * Zero fake simulations — genuine verification only!
     */
    private fun runJioPhonePayment(vpa: String, amount: String, note: String, pin: String) {
        val cleanPhone = if (vpa.all { it.isDigit() } && vpa.length == 12 && vpa.startsWith("91")) vpa.substring(2) else vpa
        val cleanAmount = amount.filter { it.isDigit() || it == '.' }.takeWhile { it != '.' }

        _uiState.update { it.copy(errors = emptyMap(), isSessionActive = true) }
        _sessionState.value = SessionState.Running(
            label = "Calling Jio Voice Gateway (08045163666)...",
            stepIndex = 1,
            total = 5
        )

        // Show the 100% OPAQUE overlay so the system phone call dialer is completely hidden from view!
        overlayController?.showJioAutoInject(
            recipient = cleanPhone,
            amount = cleanAmount,
            stepNumber = 1,
            stepTitle = "Jio VoLTE Offline Gateway",
            stepSubtitle = "Connecting to Jio Voice Gateway"
        )
        overlayController?.onCancel = { cancelSession() }

        val gateway = jioVoiceGateway
        overlayController?.onSpeakerToggle = {
            val isNowOn = !(gateway?.isSpeakerphoneOn() ?: false)
            gateway?.setSpeakerphone(isNowOn)
            isNowOn
        }

        val sessionStartTime = System.currentTimeMillis() - 5000L
        val initiated = gateway?.initiatePaymentCall(cleanPhone, cleanAmount, pin) ?: false

        if (!initiated) {
            _sessionState.value = SessionState.Failed(
                message = "Could not connect to Jio Voice Gateway",
                resultText = "Please verify cellular voice signal and CALL_PHONE permission."
            )
            overlayController?.hide()
            onSessionEnded()
            return
        }

        sessionJob = viewModelScope.launch {
            fun handleSmsConfirmation(sms: com.arkiolabs.downpay.domain.ParsedBankSms): Boolean {
                if (sms.isSuccess && sms.isDebit) {
                    val refText = sms.refId?.let { "Ref: $it" } ?: "Completed"
                    val resultMsg = "Payment of ₹$cleanAmount to $cleanPhone successful. $refText"
                    _sessionState.value = SessionState.Success(resultText = resultMsg)
                    overlayController?.hide()
                    gateway.endActiveCall()
                    onSessionEnded()

                    launch {
                        historyRepo.recordTransaction(
                            vpa = cleanPhone,
                            payeeName = _uiState.value.payeeName.ifBlank { null },
                            amount = cleanAmount,
                            note = note.ifBlank { null },
                            carrierReply = resultMsg
                        )
                    }
                    return true
                } else if (!sms.isSuccess) {
                    val errorMsg = if (sms.amount == null && sms.refId == null) {
                        "Transaction could not be completed by bank"
                    } else {
                        "Bank declined payment: ${sms.refId ?: "Unknown error"}"
                    }
                    _sessionState.value = SessionState.Failed(
                        message = errorMsg,
                        resultText = "Bank declined transaction"
                    )
                    overlayController?.hide()
                    gateway.endActiveCall()
                    onSessionEnded()
                    return true
                }
                return false
            }

            // Listen for genuine bank confirmation SMS
            val smsJob = launch {
                com.arkiolabs.downpay.platform.BankSmsReceiver.bankSmsEvents.collect { sms ->
                    val current = _sessionState.value
                    if (current is SessionState.Success || current is SessionState.Failed) {
                        return@collect
                    }
                    handleSmsConfirmation(sms)
                }
            }

            // Progression status updates for user guidance (in sync with calibrated fast DTMF: ,,1,phone,amount,,1,pin#)
            delay(4_000L) // After call setup -> Option 1 (Money Transfer)
            if (_sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Running(
                    label = "Option 1 (Transfer) injected",
                    stepIndex = 1,
                    total = 5
                )
                overlayController?.updateJioAutoInject(1, "Injecting Recipient...", "Sending $cleanPhone")
            }

            delay(2_500L) // After recipient prompt -> recipient phone
            if (_sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Running(
                    label = "Recipient phone injected",
                    stepIndex = 2,
                    total = 5
                )
                overlayController?.updateJioAutoInject(2, "Injecting Amount...", "Sending ₹$cleanAmount")
            }

            delay(2_500L) // After amount prompt -> amount
            if (_sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Running(
                    label = "Amount injected",
                    stepIndex = 3,
                    total = 5
                )
                overlayController?.updateJioAutoInject(3, "Confirming Details...", "Confirming recipient & amount (1)")
            }

            delay(4_000L) // After confirmation prompt -> Option 1 (Confirm)
            if (_sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Running(
                    label = "Details confirmed",
                    stepIndex = 4,
                    total = 5
                )
                overlayController?.updateJioAutoInject(4, "Authorizing PIN...", "Entering Bank UPI PIN securely")
            }

            delay(2_500L) // After PIN prompt -> PIN#
            if (_sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Running(
                    label = "UPI PIN authorized",
                    stepIndex = 5,
                    total = 5
                )
                overlayController?.updateJioAutoInject(5, "Verifying with Bank...", "All inputs injected. Awaiting bank confirmation SMS")
            }

            // Poll inbox periodically in case SMS broadcast was delayed
            var elapsed = 0L
            val pollInterval = 2_000L
            val maxWait = 25_000L
            while (elapsed < maxWait && _sessionState.value is SessionState.Running) {
                delay(pollInterval)
                elapsed += pollInterval
                val recentSms = com.arkiolabs.downpay.platform.BankSmsReceiver.pollRecentBankSms(sessionStartTime)
                if (recentSms != null) {
                    if (handleSmsConfirmation(recentSms)) break
                }
            }

            smsJob.cancel()
            gateway.endActiveCall()
            overlayController?.hide()

            val current = _sessionState.value
            if (current is SessionState.Running) {
                // Zero fake simulation! Only verified debit triggers Success.
                _sessionState.value = SessionState.Failed(
                    message = "Awaiting bank confirmation",
                    resultText = "Payment call ended. If money was debited, your bank SMS will confirm shortly."
                )
                onSessionEnded()
            }
        }
    }

    private fun runUssdPayment(vpa: String, amount: String, note: String, pin: String) {
        val mode = operationMode.value
        val cleanRecipient = if (vpa.all { it.isDigit() } && vpa.length == 12 && vpa.startsWith("91")) vpa.substring(2) else vpa
        val isMobile = cleanRecipient.all { it.isDigit() } && cleanRecipient.length == 10
        val targetAction = if (isMobile) Actions.SendToMobile else Actions.SendUpi

        _uiState.update { it.copy(errors = emptyMap(), isSessionActive = true) }
        _sessionState.value = SessionState.Running(
            label = "Starting payment",
            stepIndex = 0,
            total = targetAction.steps.size
        )

        // Always show the full 100% opaque branded overlay to completely cover carrier dialogs
        overlayController?.show(
            title = "Paying ₹$amount",
            subtitle = "to $cleanRecipient",
            stepLabel = "CONNECTING TO BANK"
        )
        overlayController?.onCancel = { cancelSession() }

        val vars = mapOf(
            "vpa" to cleanRecipient,
            "amount" to amount,
            "note" to note.ifBlank { "Payment" },
            "pin" to pin
        )

        val run = actionRunner.runAction(targetAction, vars, viewModelScope)
        activeRun = run

        sessionJob = viewModelScope.launch {
            launch {
                run.events.collect { event ->
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
                                title = "Paying ₹$amount",
                                subtitle = "to $cleanRecipient",
                                stepLabel = (event.label ?: "Processing").uppercase()
                            )
                        }
                        is ActionEvent.Done -> {
                            _sessionState.value = SessionState.Success(resultText = event.resultText)
                            overlayController?.hide()
                            onSessionEnded()
                            launch {
                                historyRepo.recordTransaction(
                                    vpa = cleanRecipient,
                                    payeeName = _uiState.value.payeeName.ifBlank { null },
                                    amount = amount,
                                    note = note.ifBlank { null },
                                    carrierReply = event.resultText
                                )
                            }
                        }
                        is ActionEvent.Error -> {
                            _sessionState.value = SessionState.Failed(
                                message = event.message,
                                resultText = event.resultText
                            )
                            overlayController?.showError(
                                title = "Payment failed",
                                message = event.message
                            )
                            if (event.message.contains("pin", ignoreCase = true) &&
                                (event.message.contains("incorrect", ignoreCase = true) ||
                                 event.message.contains("invalid", ignoreCase = true) ||
                                 event.message.contains("wrong", ignoreCase = true) ||
                                 event.message.contains("mismatch", ignoreCase = true))) {
                                launch { prefsRepo.clearBiometricPin() }
                            }
                            onSessionEnded()
                        }
                        else -> { /* Frame/Reply — no UI state change needed */ }
                    }
                }
            }

            val result = run.result.await()
            if (!result.success && _sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Failed(
                    message = result.resultText,
                    resultText = result.resultText
                )
                overlayController?.showError(title = "Payment failed", message = result.resultText)
                onSessionEnded()
            }
        }
    }

    /** Cancel the active session. */
    fun cancelSession() {
        sessionJob?.cancel()
        sessionJob = null
        jioVoiceGateway?.endActiveCall()
        overlayController?.onSpeakerToggle = null
        viewModelScope.launch {
            activeRun?.cancel?.invoke()
            overlayController?.hide()
            _sessionState.value = SessionState.Idle
            onSessionEnded()
        }
    }

    /** Dismiss a terminal (success/failed) session card and return to the form. */
    fun dismissSession() {
        val wasSuccess = _sessionState.value is SessionState.Success
        _sessionState.value = SessionState.Idle
        if (wasSuccess) {
            // After success, clear the form so the user starts fresh.
            _uiState.update { PayUiState() }
        } else {
            // On failure, just clear the PIN so the user can re-enter it.
            _uiState.update { it.copy(pin = "") }
        }
    }

    fun clearFieldError(field: FormField) {
        _uiState.update { current ->
            if (current.errors.containsKey(field)) {
                current.copy(errors = current.errors - field)
            } else current
        }
    }

    fun dismissSnackbar() {
        _snackbar.value = null
    }

    fun onNavigateAway() {
        // Defensive: if the user backs out mid-form, drop the in-memory PIN.
        _uiState.update { it.copy(pin = "") }
    }

    fun onBackground() {
        _uiState.update { it.copy(pin = "") }
    }

    /**
     * Called whenever the session reaches a terminal state. Marks the
     * session as inactive and schedules a wipe of the PIN within 500ms so
     * it never lingers in memory longer than necessary.
     */
    private fun onSessionEnded() {
        _uiState.update { it.copy(isSessionActive = false) }
        activeRun = null
        viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(pin = "") }
        }
    }
}
