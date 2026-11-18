package com.arkiolabs.airpay.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State representing the Jio Offline VoLTE Voice Gateway session.
 */
sealed class JioGatewayState {
    object Idle : JioGatewayState()
    data class Connecting(val recipient: String, val amount: String, val statusLabel: String) : JioGatewayState()
    data class Dialed(val recipient: String, val amount: String, val promptPin: Boolean = false) : JioGatewayState()
    data class Completed(val recipient: String, val amount: String, val refId: String) : JioGatewayState()
    data class Failed(val error: String) : JioGatewayState()
}

/**
 * Manages offline voice gateway calls for Reliance Jio SIM holders.
 * Because Jio lacks 2G USSD, this utilizes the voice cellular gateway over VoLTE voice calling (zero internet data required).
 */
class JioVoiceGateway(private val context: Context) {

    private val _gatewayState = MutableStateFlow<JioGatewayState>(JioGatewayState.Idle)
    val gatewayState: StateFlow<JioGatewayState> = _gatewayState.asStateFlow()

    companion object {
        // Jio Voice Primary Single-Call National Gateways
        const val PRIMARY_GATEWAY_NUMBER = "08045163666"
        const val BACKUP_GATEWAY_NUMBER = "08045163581"
        const val GUPSHUP_GATEWAY_NUMBER = "6366200200"

        // National Digital Payments Helpline
        const val DIGISAATHI_HELPLINE = "14431"
    }

    /**
     * Builds the fast calibrated automated DTMF dialer string for an offline payment.
     * Uses calibrated pauses (,) for fast VoLTE transmission on 08045163666:
     * - ,, (wait ~4s for call setup) -> 1 (Select Money Transfer)
     * - ,  (wait ~2.5s for recipient prompt) -> $cleanPhone (10-digit phone)
     * - ,  (wait ~2.5s for amount prompt) -> $cleanAmount (amount digits)
     * - ,, (wait ~4.5s for confirmation prompt) -> 1 (Confirm beneficiary and amount)
     * - ,  (wait ~2.5s for PIN prompt) -> UPI PIN
     * - # (%23) (terminate entry)
     */
    fun buildPaymentDialUri(recipientPhone: String, amount: String, pin: String? = null): Uri {
        val cleanPhone = recipientPhone.filter { it.isDigit() }.takeLast(10)
        val cleanAmount = amount.filter { it.isDigit() || it == '.' }.takeWhile { it != '.' }

        // Note: # in tel: URI must be encoded as %23 so Android's Uri parser doesn't treat it as a fragment
        val full = if (!pin.isNullOrBlank()) {
            "tel:$PRIMARY_GATEWAY_NUMBER,,1,$cleanPhone,$cleanAmount,,1,$pin%23"
        } else {
            "tel:$PRIMARY_GATEWAY_NUMBER,,1,$cleanPhone,$cleanAmount,,1"
        }
        return Uri.parse(full)
    }

    /**
     * Builds the fast calibrated automated DTMF dialer string for checking balance.
     * On 08045163666: Option 1 = Money Transfer, Option 2 = Balance Check!
     * - ,, (wait ~4s for call setup) -> 2 (Select Balance Check)
     * - ,  (wait ~2.5s for PIN prompt) -> UPI PIN
     * - # (%23) (terminate entry)
     */
    fun buildBalanceDialUri(pin: String? = null): Uri {
        val full = if (!pin.isNullOrBlank()) {
            "tel:$PRIMARY_GATEWAY_NUMBER,,2,$pin%23"
        } else {
            "tel:$PRIMARY_GATEWAY_NUMBER,,2"
        }
        return Uri.parse(full)
    }

    /**
     * Ends the active call once transaction completes or if user cancels.
     */
    fun endActiveCall() {
        try {
            setSpeakerphone(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                telecomManager?.endCall()
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Controls loudspeaker output so the user can audibly hear the IVR confirmation
     * or spoken account balance while the dialer screen stays hidden behind the overlay.
     */
    fun setSpeakerphone(enabled: Boolean) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.mode = android.media.AudioManager.MODE_IN_CALL
            audioManager?.isSpeakerphoneOn = enabled
        } catch (_: Exception) {}
    }

    fun isSpeakerphoneOn(): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.isSpeakerphoneOn ?: false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Initiates the offline voice call for payment on Jio.
     */
    fun initiatePaymentCall(recipientPhone: String, amount: String, pin: String? = null): Boolean {
        return try {
            val uri = buildPaymentDialUri(recipientPhone, amount, pin)
            val intent = Intent(Intent.ACTION_CALL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            bringAppToFront()
            ensureSilentBackgroundAudio()
            _gatewayState.value = JioGatewayState.Dialed(recipientPhone, amount, promptPin = pin.isNullOrBlank())
            true
        } catch (e: SecurityException) {
            // Fallback to ACTION_DIAL if CALL_PHONE is not yet granted
            try {
                val uri = buildPaymentDialUri(recipientPhone, amount, pin)
                val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                bringAppToFront()
                ensureSilentBackgroundAudio()
                _gatewayState.value = JioGatewayState.Dialed(recipientPhone, amount, promptPin = true)
                true
            } catch (ex: Exception) {
                _gatewayState.value = JioGatewayState.Failed(ex.message ?: "Could not place call")
                false
            }
        } catch (e: Exception) {
            _gatewayState.value = JioGatewayState.Failed(e.message ?: "Could not place call")
            false
        }
    }

    /**
     * Initiates the offline voice call for balance inquiry on Jio.
     */
    fun initiateBalanceCall(pin: String? = null): Boolean {
        return try {
            val uri = buildBalanceDialUri(pin)
            val intent = Intent(Intent.ACTION_CALL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            bringAppToFront()
            ensureSilentBackgroundAudio()
            true
        } catch (e: Exception) {
            try {
                val uri = buildBalanceDialUri(pin)
                val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                bringAppToFront()
                ensureSilentBackgroundAudio()
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    private fun bringAppToFront() {
        try {
            val bringIntent = Intent(context, com.arkiolabs.airpay.presentation.MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(bringIntent)
            // Re-assert front focus after a brief delay in case OEM dialer launched asynchronously
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    context.startActivity(bringIntent)
                } catch (_: Exception) {}
            }, 350L)
        } catch (_: Exception) {}
    }

    private fun ensureSilentBackgroundAudio() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.isSpeakerphoneOn = false
        } catch (_: Exception) {}
    }

    fun resetState() {
        _gatewayState.value = JioGatewayState.Idle
    }
}
