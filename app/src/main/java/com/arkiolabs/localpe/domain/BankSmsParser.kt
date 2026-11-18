package com.arkiolabs.localpe.domain

/**
 * Parsed details from an incoming bank transaction SMS.
 */
data class ParsedBankSms(
    val isDebit: Boolean,
    val isSuccess: Boolean,
    val amount: String?,
    val recipient: String?,
    val refId: String?,
    val balance: String?,
    val bankName: String?
)

/**
 * Intelligent parser for transaction confirmation SMS messages from Indian banks
 * (SBI, HDFC, ICICI, Axis, PNB, Bank of Baroda, Kotak, Canara, Union Bank, etc.).
 *
 * This enables Jio users to automatically verify offline UPI 123PAY payments
 * without requiring any internet connection.
 */
object BankSmsParser {

    private val DEBIT_PATTERNS = listOf(
        // "debited by 500.00" or "debited by Rs 500.00"
        Regex("(?i)(?:debited|paid|transferred|sent|deducted)\\s+(?:by|for|with|of)\\s+(?:INR|Rs\\.?|₹)?\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)"),
        // "debited from HDFC Bank A/c **4120 on 18-NOV-26 for Rs 210.00"
        Regex("(?i)(?:debited|transferred)\\s+from\\s+[^,.]+?\\s+(?:by|for|with|of)\\s+(?:INR|Rs\\.?|₹)?\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)"),
        // "Rs 210.00 debited" or "INR 500.00 has been debited"
        Regex("(?i)(?:INR|Rs\\.?|₹)\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)\\s+(?:has\\s+been\\s+)?(?:debited|paid|transferred|sent|deducted)"),
        // "payment of INR 210.00 successful"
        Regex("(?i)payment\\s+of\\s+(?:INR|Rs\\.?|₹)?\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)"),
        // "debited: Rs 500.00"
        Regex("(?i)(?:debited|paid|deducted)[:\\s]+(?:INR|Rs\\.?|₹)?\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)")
    )

    private val REF_PATTERNS = listOf(
        Regex("(?i)(?:UPI\\s+Ref(?:\\s+no)?|Ref(?:\\s+no)?|UTR|Txn(?:\\s+ID)?|rrn)[:\\s]*([0-9]{9,16})"),
        Regex("(?i)\\bref\\s*[:\\-]?\\s*([0-9]{9,16})")
    )

    private val RECIPIENT_PATTERNS = listOf(
        Regex("(?i)(?:to|towards|vpa)\\s+(?:vpa\\s+)?([0-9]{10})"),
        Regex("(?i)(?:to|towards|vpa)\\s+(?:vpa\\s+)?([a-zA-Z0-9.\\-_]+@[a-zA-Z0-9]+)"),
        Regex("(?i)transfer\\s+to\\s+([0-9]{10})"),
        Regex("(?i)transfer\\s+to\\s+([a-zA-Z0-9.\\s]+?)(?:\\s+on|\\s+ref|\\s+avl|\\.)")
    )

    private val BALANCE_PATTERNS = listOf(
        // "Bal Rs 12450.50", "Available Balance is INR 8400.25", "Available Balance in A/c ending 1234 is INR 24,560.00"
        Regex("(?i)(?:avail(?:able)?\\s+balance|avl\\s+bal|bal(?:ance)?)(?:\\s+in\\s+[^,.]+?)?(?:\\s+is|\\s*:|\\s*=)?\\s*(?:INR|Rs\\.?|₹)\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)"),
        Regex("(?i)(?:INR|Rs\\.?|₹)\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)\\s*(?:is available|balance)")
    )

    private val FAILURE_PATTERNS = listOf(
        Regex("(?i)declined|failed|unable to process|incorrect (?:upi )?pin|insufficient (?:balance|funds)|cancelled")
    )

    fun parse(smsSender: String, smsBody: String): ParsedBankSms? {
        val cleanBody = smsBody.replace("\n", " ").trim()
        val isFail = FAILURE_PATTERNS.any { it.containsMatchIn(cleanBody) }

        var foundAmount: String? = null
        for (pattern in DEBIT_PATTERNS) {
            val match = pattern.find(cleanBody)
            if (match != null) {
                foundAmount = match.groupValues[1].replace(",", "").trim()
                break
            }
        }

        var foundRef: String? = null
        for (pattern in REF_PATTERNS) {
            val match = pattern.find(cleanBody)
            if (match != null) {
                foundRef = match.groupValues[1].trim()
                break
            }
        }

        var foundRecipient: String? = null
        for (pattern in RECIPIENT_PATTERNS) {
            val match = pattern.find(cleanBody)
            if (match != null) {
                foundRecipient = match.groupValues[1].trim()
                break
            }
        }

        var foundBalance: String? = null
        for (pattern in BALANCE_PATTERNS) {
            val match = pattern.find(cleanBody)
            if (match != null) {
                foundBalance = match.groupValues[1].replace(",", "").trim()
                break
            }
        }

        // If it's a balance or debit message, return parsed result
        if (foundAmount != null || foundBalance != null || isFail) {
            return ParsedBankSms(
                isDebit = foundAmount != null,
                isSuccess = !isFail,
                amount = foundAmount,
                recipient = foundRecipient,
                refId = foundRef,
                balance = foundBalance,
                bankName = extractBankName(smsSender)
            )
        }

        return null
    }

    private fun extractBankName(sender: String): String? {
        val upper = sender.uppercase()
        return when {
            upper.contains("SBI") -> "State Bank of India"
            upper.contains("HDFC") -> "HDFC Bank"
            upper.contains("ICICI") -> "ICICI Bank"
            upper.contains("AXIS") -> "Axis Bank"
            upper.contains("PNB") -> "Punjab National Bank"
            upper.contains("BOB") || upper.contains("BARODA") -> "Bank of Baroda"
            upper.contains("KOTAK") -> "Kotak Mahindra Bank"
            upper.contains("CANARA") || upper.contains("CNRB") -> "Canara Bank"
            upper.contains("UNION") || upper.contains("UBIN") -> "Union Bank of India"
            else -> null
        }
    }
}
