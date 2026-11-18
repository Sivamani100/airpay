package com.arkiolabs.airpay.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.arkiolabs.airpay.domain.BankSmsParser
import com.arkiolabs.airpay.domain.ParsedBankSms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for incoming bank transaction SMS messages.
 * Automatically verifies offline UPI 123PAY payments and balance queries on Jio.
 */
class BankSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        for (sms in messages) {
            val sender = sms.displayOriginatingAddress ?: ""
            val body = sms.displayMessageBody ?: ""

            val parsed = BankSmsParser.parse(sender, body)
            if (parsed != null) {
                // Emit event on application scope
                coroutineScope.launch {
                    _bankSmsEvents.emit(parsed)
                }
            }
        }
    }

    companion object {
        private var appContext: Context? = null
        private val coroutineScope = CoroutineScope(Dispatchers.IO)
        private val _bankSmsEvents = MutableSharedFlow<ParsedBankSms>(extraBufferCapacity = 16)
        val bankSmsEvents: SharedFlow<ParsedBankSms> = _bankSmsEvents.asSharedFlow()

        fun init(context: Context) {
            appContext = context.applicationContext
        }

        fun pollRecentBankSms(sinceTimestamp: Long): ParsedBankSms? {
            val ctx = appContext ?: return null
            try {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        ctx,
                        android.Manifest.permission.READ_SMS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }
                val uri = Telephony.Sms.Inbox.CONTENT_URI
                val projection = arrayOf(
                    Telephony.Sms.Inbox.ADDRESS,
                    Telephony.Sms.Inbox.BODY,
                    Telephony.Sms.Inbox.DATE
                )
                val selection = "${Telephony.Sms.Inbox.DATE} >= ?"
                val selectionArgs = arrayOf(sinceTimestamp.toString())
                val sortOrder = "${Telephony.Sms.Inbox.DATE} DESC"

                ctx.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                    val addressIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
                    val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY)
                    while (cursor.moveToNext()) {
                        val address = cursor.getString(addressIdx) ?: ""
                        val body = cursor.getString(bodyIdx) ?: ""
                        val parsed = BankSmsParser.parse(address, body)
                        if (parsed != null) {
                            coroutineScope.launch {
                                _bankSmsEvents.emit(parsed)
                            }
                            return parsed
                        }
                    }
                }
            } catch (_: Exception) {}
            return null
        }
    }
}
