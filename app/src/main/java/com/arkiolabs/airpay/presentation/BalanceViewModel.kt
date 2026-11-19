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
 */
data class BalanceUiState(
    val pin: String = "",
    val pinError: String? = null,
    val isSessionActive: Boolean = false
)

/**
 * ViewModel for the Balance check screen.
 */
class BalanceViewModel(
    private val actionRunner: ActionRunner,
    private val prefsRepo: PreferencesRepository,
    private val carrierDetector: com.arkiolabs.airpay.platform.CarrierDetector? = null,
    private val overlayController: OverlayController? = null,
    private val onDialerFallback: (String) -> Unit = {},
    private val clipboardWriter: (String) -> Unit = {},
    private val systemToast: (String) -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(BalanceUiState())
    val uiState: StateFlow<BalanceUiState> = _uiState.asStateFlow()

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()

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
     */
    fun attemptCheckBalance() {
        val mode = operationMode.value

        if (mode == OperationMode.MANUAL) {
            _snackbar.value = "Opening dialer for balance check"
            onDialerFallback("*99*3#")
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
     * Runs the pure USSD CheckBalance action with the captured PIN in the background.
     */
    private fun runCheck(pin: String) {
        viewModelScope.launch {
            if (!actionRunner.isServiceEnabled()) {
                _sessionState.value = SessionState.Failed(
                    message = "Accessibility service is disabled. Please enable it in Settings.",
                    resultText = ""
                )
                return@launch
            }
            runUssdBalanceCheck(pin)
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
                        else -> {}
                    }
                }
            }

            val result = actionRun.result.await()
            if (!result.success && _sessionState.value is SessionState.Running) {
                _sessionState.value = SessionState.Failed(
                    message = result.resultText,
                    resultText = result.resultText
                )
                overlayController?.showError(title = "Check failed", message = result.resultText)
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
        overlayController?.hide()
        _sessionState.value = SessionState.Idle
        onSessionEnd()
    }

    fun dismissSession() {
        _sessionState.value = SessionState.Idle
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
