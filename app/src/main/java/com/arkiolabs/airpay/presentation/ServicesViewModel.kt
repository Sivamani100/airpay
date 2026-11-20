package com.arkiolabs.airpay.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkiolabs.airpay.AirpayApplication
import com.arkiolabs.airpay.data.HistoryRepository
import com.arkiolabs.airpay.data.PreferencesRepository
import com.arkiolabs.airpay.data.TransactionEntity
import com.arkiolabs.airpay.domain.ActionEvent
import com.arkiolabs.airpay.domain.ActionRun
import com.arkiolabs.airpay.domain.ActionRunner
import com.arkiolabs.airpay.domain.Actions
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.platform.OverlayController
import com.arkiolabs.airpay.platform.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * ViewModel managing extended NUUP (*99#) services:
 *  - Send Money to Bank Account (IFSC) (*99*1*5#)
 *  - Request Money (Mobile or UPI ID) (*99*2*1# / *99*2*2#)
 *  - View Network Transactions (*99*6*1#)
 *  - View Rewards (*99*6*2#)
 *  - Set / Reset UPI PIN (*99*7*1#)
 *  - Change Existing UPI PIN (*99*7*2#)
 *  - Change Linked Bank Account (*99*4*1#)
 *  - View Pending Requests (*99*5#)
 *  - Fetch Profile / My Details (*99*4#)
 */
class ServicesViewModel(
    private val actionRunner: ActionRunner,
    private val prefsRepo: PreferencesRepository,
    private val historyRepo: HistoryRepository,
    private val overlayController: OverlayController? = null,
    private val context: Context? = null
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _lastOperationResult = MutableStateFlow<String?>(null)
    val lastOperationResult: StateFlow<String?> = _lastOperationResult.asStateFlow()

    private var activeRun: ActionRun? = null
    private var sessionJob: Job? = null

    fun dismissSession() {
        sessionJob?.cancel()
        sessionJob = null
        activeRun = null
        overlayController?.hide()
        _sessionState.value = SessionState.Idle
    }

    fun cancelSession() {
        viewModelScope.launch {
            activeRun?.cancel?.invoke()
            dismissSession()
        }
    }

    /**
     * 1. Send Money to Bank Account using IFSC & Account Number (*99*1*5#).
     */
    fun sendToBank(
        ifsc: String,
        account: String,
        amount: String,
        note: String,
        pin: String
    ) {
        val cleanIfsc = ifsc.trim().uppercase()
        val cleanAccount = account.trim()
        val cleanAmount = amount.trim()
        val cleanNote = note.trim().ifBlank { "Bank Transfer" }

        runServiceAction(
            action = Actions.SendToBank,
            vars = mapOf(
                "ifsc" to cleanIfsc,
                "account" to cleanAccount,
                "amount" to cleanAmount,
                "note" to cleanNote,
                "pin" to pin
            ),
            overlayTitle = "Sending ₹$cleanAmount",
            overlaySubtitle = "to A/c ••${cleanAccount.takeLast(4)} ($cleanIfsc)",
            stepLabel = "CONNECTING TO BANK",
            onSuccess = { reply ->
                viewModelScope.launch {
                    historyRepo.recordTransaction(
                        vpa = "A/c: $cleanAccount ($cleanIfsc)",
                        payeeName = "Bank Transfer ($cleanIfsc)",
                        amount = cleanAmount,
                        note = cleanNote.ifBlank { null },
                        carrierReply = reply
                    )
                }
            }
        )
    }

    /**
     * 2. Request Money via Mobile or UPI ID (*99*2*1# or *99*2*2#).
     */
    fun requestMoney(
        isPhone: Boolean,
        target: String,
        amount: String,
        note: String
    ) {
        val cleanTarget = target.trim()
        val cleanAmount = amount.trim()
        val cleanNote = note.trim().ifBlank { "Collect" }

        val action = if (isPhone) Actions.RequestMoneyByPhone else Actions.RequestMoneyByVpa
        val vars = if (isPhone) {
            mapOf("phone" to cleanTarget, "amount" to cleanAmount, "note" to cleanNote)
        } else {
            mapOf("vpa" to cleanTarget, "amount" to cleanAmount, "note" to cleanNote)
        }

        runServiceAction(
            action = action,
            vars = vars,
            overlayTitle = "Requesting ₹$cleanAmount",
            overlaySubtitle = "from $cleanTarget",
            stepLabel = "SENDING COLLECT REQUEST"
        )
    }

    /**
     * 3. View Recent Transactions across the UPI Network (*99*6*1#).
     */
    fun fetchRecentNetworkTransactions() {
        runServiceAction(
            action = Actions.ViewRecentTransactions,
            vars = emptyMap(),
            overlayTitle = "Fetching Statement",
            overlaySubtitle = "Connecting to UPI Network",
            stepLabel = "QUERYING RECENT TRANSACTIONS",
            onSuccess = { reply ->
                viewModelScope.launch {
                    prefsRepo.setLastNetworkTxns(reply, System.currentTimeMillis())
                }
            }
        )
    }

    /**
     * 4. View Rewards Information (*99*6*2#).
     */
    fun fetchRewards() {
        runServiceAction(
            action = Actions.ViewRewards,
            vars = emptyMap(),
            overlayTitle = "Checking Rewards",
            overlaySubtitle = "Connecting to Gateway",
            stepLabel = "FETCHING REWARDS & OFFERS",
            onSuccess = { reply ->
                viewModelScope.launch {
                    prefsRepo.setLastRewardsText(reply)
                }
            }
        )
    }

    /**
     * 5. Set or Reset UPI PIN using Debit Card (*99*7*1#).
     */
    fun resetUpiPin(
        debitDigits: String,
        expiry: String,
        newPin: String,
        confirmPin: String
    ) {
        runServiceAction(
            action = Actions.ResetUpiPin,
            vars = mapOf(
                "debitDigits" to debitDigits.trim(),
                "expiry" to expiry.trim(),
                "newPin" to newPin.trim(),
                "confirmPin" to confirmPin.trim()
            ),
            overlayTitle = "Setting UPI PIN",
            overlaySubtitle = "Verifying with your Debit Card",
            stepLabel = "UPDATING SECURITY PIN"
        )
    }

    /**
     * 6. Change Existing UPI PIN (*99*7*2#).
     */
    fun changeUpiPin(
        oldPin: String,
        newPin: String,
        confirmPin: String
    ) {
        runServiceAction(
            action = Actions.ChangeUpiPin,
            vars = mapOf(
                "oldPin" to oldPin.trim(),
                "newPin" to newPin.trim(),
                "confirmPin" to confirmPin.trim()
            ),
            overlayTitle = "Changing UPI PIN",
            overlaySubtitle = "Verifying Existing PIN",
            stepLabel = "UPDATING SECURITY PIN"
        )
    }

    /**
     * 7. Change Linked Bank Account (*99*4*1#).
     */
    fun changeLinkedBank(bankName: String) {
        val cleanBank = bankName.trim()
        runServiceAction(
            action = Actions.ChangeLinkedBank,
            vars = mapOf("bankName" to cleanBank),
            overlayTitle = "Switching Bank",
            overlaySubtitle = "Searching for $cleanBank",
            stepLabel = "LINKING BANK ACCOUNT",
            onSuccess = {
                viewModelScope.launch {
                    prefsRepo.setUserBankName(cleanBank)
                }
            }
        )
    }

    /**
     * 8. Fetch Pending Requests (*99*5#).
     */
    fun fetchPendingRequests() {
        runServiceAction(
            action = Actions.PendingRequests,
            vars = emptyMap(),
            overlayTitle = "Pending Requests",
            overlaySubtitle = "Checking for Collect Approvals",
            stepLabel = "FETCHING PENDING REQUESTS",
            onSuccess = { reply ->
                viewModelScope.launch {
                    prefsRepo.setLastPendingRequestsText(reply, System.currentTimeMillis())
                }
            }
        )
    }

    /**
     * 9. Fetch Profile / My Details (*99*4#).
     * Parses Name, UPI ID, Bank Name, and Account Number.
     */
    fun fetchMyDetails() {
        runServiceAction(
            action = Actions.FetchMyDetails,
            vars = emptyMap(),
            overlayTitle = "Fetching Profile",
            overlaySubtitle = "Reading details from SIM bank link",
            stepLabel = "SYNCHRONIZING PROFILE",
            onSuccess = { reply ->
                viewModelScope.launch {
                    parseAndSaveProfileDetails(reply)
                }
            }
        )
    }

    /**
     * Parses raw carrier profile output and updates PreferencesRepository.
     */
    private suspend fun parseAndSaveProfileDetails(reply: String) {
        _lastOperationResult.value = reply
        val lines = reply.lines().map { it.trim() }.filter { it.isNotBlank() }

        // Find UPI ID (contains '@')
        val upiId = lines.firstOrNull { it.contains("@") }
            ?.let { line ->
                val match = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9]+").find(line)
                match?.value ?: line.substringAfter(":").trim()
            }
        if (!upiId.isNullOrBlank()) {
            prefsRepo.setUserUpiId(upiId)
        }

        // Find Account Number (numbers with 4+ digits or 'X' / '*' masked)
        val accLine = lines.firstOrNull { line ->
            line.contains("a/c", ignoreCase = true) ||
                line.contains("account", ignoreCase = true) ||
                line.contains("xx", ignoreCase = true) ||
                line.contains("••", ignoreCase = true)
        }
        if (accLine != null) {
            val digits = accLine.filter { it.isDigit() }
            if (digits.length >= 4) {
                val last4 = digits.takeLast(4)
                prefsRepo.setUserAccountMasked("•••• •••• $last4")
                prefsRepo.setUserAccountFull(digits)
            }
        }

        // Find Name (lines with 'name' or 1st non-menu line)
        val nameLine = lines.firstOrNull { line ->
            line.contains("name", ignoreCase = true) && !line.contains("bank", ignoreCase = true)
        }
        val extractedName = if (nameLine != null) {
            nameLine.substringAfter(":").substringAfter("-").trim()
        } else {
            lines.firstOrNull { l ->
                !l.contains("select", ignoreCase = true) &&
                    !l.contains("welcome", ignoreCase = true) &&
                    !l.contains("@") &&
                    !l.all { it.isDigit() } &&
                    l.length in 3..25
            }
        }

        if (!extractedName.isNullOrBlank() && extractedName.length >= 2) {
            prefsRepo.setUserName(extractedName)
        }

        // Find Bank Name
        val bankLine = lines.firstOrNull { line ->
            line.contains("bank", ignoreCase = true) ||
                line.contains("sbi", ignoreCase = true) ||
                line.contains("hdfc", ignoreCase = true) ||
                line.contains("icici", ignoreCase = true) ||
                line.contains("pnb", ignoreCase = true) ||
                line.contains("axis", ignoreCase = true)
        }
        if (bankLine != null) {
            val cleanBank = bankLine.substringAfter(":").trim()
            prefsRepo.setUserBankName(cleanBank)
        }
    }

    /**
     * Common execution wrapper for all NUUP actions.
     * Manages branded overlay, session states, and audio feedback.
     */
    private fun runServiceAction(
        action: com.arkiolabs.airpay.domain.Action,
        vars: Map<String, String> = emptyMap(),
        overlayTitle: String,
        overlaySubtitle: String,
        stepLabel: String,
        onSuccess: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (!actionRunner.isServiceEnabled()) {
                _sessionState.value = SessionState.Failed(
                    message = "Accessibility service is disabled. Enable it in Settings.",
                    resultText = ""
                )
                return@launch
            }

            _sessionState.value = SessionState.Running(
                label = stepLabel,
                stepIndex = 0,
                total = action.steps.size
            )

            // Engage 100% full branded overlay covering carrier dialog
            overlayController?.show(
                title = overlayTitle,
                subtitle = overlaySubtitle,
                stepLabel = stepLabel
            )
            overlayController?.onCancel = { cancelSession() }

            val run = actionRunner.runAction(action, vars, viewModelScope)
            activeRun = run

            sessionJob = launch {
                launch {
                    run.events.collect { event ->
                        when (event) {
                            is ActionEvent.Progress -> {
                                _sessionState.value = SessionState.Running(
                                    label = event.label ?: "Processing",
                                    stepIndex = event.stepIndex,
                                    total = event.total
                                )
                                overlayController?.update(
                                    title = overlayTitle,
                                    subtitle = overlaySubtitle,
                                    stepLabel = (event.label ?: "Processing").uppercase()
                                )
                            }
                            is ActionEvent.Done -> {
                                com.arkiolabs.airpay.platform.UssdAccessibilityService.instance?.dismissDialog()
                                _lastOperationResult.value = event.resultText
                                onSuccess?.invoke(event.resultText)
                                _sessionState.value = SessionState.Success(
                                    resultText = event.resultText
                                )
                                context?.let { ctx ->
                                    val sound = prefsRepo.successSoundUri.firstOrNull()
                                    SoundManager.playSuccess(ctx, sound)
                                }
                                overlayController?.hide()
                            }
                            is ActionEvent.Error -> {
                                com.arkiolabs.airpay.platform.UssdAccessibilityService.instance?.dismissDialog()
                                _sessionState.value = SessionState.Failed(
                                    message = event.message,
                                    resultText = event.resultText
                                )
                                context?.let { ctx ->
                                    val sound = prefsRepo.failedSoundUri.firstOrNull()
                                    if (!sound.isNullOrBlank()) {
                                        SoundManager.playFailure(ctx, sound)
                                    }
                                }
                                overlayController?.hide()
                            }
                            else -> Unit
                        }
                    }
                }

                val outcome = run.result.await()
                if (outcome.success) {
                    val current = _sessionState.value
                    if (current !is SessionState.Success) {
                        com.arkiolabs.airpay.platform.UssdAccessibilityService.instance?.dismissDialog()
                        _lastOperationResult.value = outcome.resultText
                        onSuccess?.invoke(outcome.resultText)
                        _sessionState.value = SessionState.Success(
                            resultText = outcome.resultText
                        )
                        context?.let { ctx ->
                            val sound = prefsRepo.successSoundUri.firstOrNull()
                            SoundManager.playSuccess(ctx, sound)
                        }
                    }
                } else {
                    val current = _sessionState.value
                    if (current !is SessionState.Failed) {
                        com.arkiolabs.airpay.platform.UssdAccessibilityService.instance?.dismissDialog()
                        _sessionState.value = SessionState.Failed(
                            message = outcome.resultText.ifBlank { "Operation Failed" },
                            resultText = outcome.resultText
                        )
                    }
                }
                overlayController?.hide()
            }
        }
    }
}
