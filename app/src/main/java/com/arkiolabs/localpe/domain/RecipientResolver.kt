package com.arkiolabs.localpe.domain

import com.arkiolabs.localpe.platform.ContactItem

data class ResolvedRecipient(
    val recipient: String,
    val displayName: String,
    val isPhone: Boolean,
    val isValid: Boolean,
    val errorMessage: String? = null
)

object RecipientResolver {

    private val PHONE_PATTERN = Regex("^[6-9]\\d{9}$")
    private val VPA_PATTERN = Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$")

    // Common genuine Indian UPI PSP handles
    val COMMON_UPI_HANDLES = setOf(
        "okhdfcbank", "oksbi", "okaxis", "okicici",
        "ybl", "ibl", "axl", "paytm", "upi", "apl",
        "idfcbank", "postbank", "barodampay", "federal",
        "rbl", "kotak", "indus", "pnb", "cnrb", "aubank",
        "jupiteraxis", "slice", "freecharge", "airtel", "yesbank"
    )

    fun resolve(
        raw: String,
        contacts: List<ContactItem> = emptyList()
    ): ResolvedRecipient {
        val trimmed = raw.trim()

        if (trimmed.isEmpty()) {
            return ResolvedRecipient(
                recipient = "",
                displayName = "",
                isPhone = false,
                isValid = false,
                errorMessage = "Enter a UPI ID or 10-digit mobile number"
            )
        }

        // Check if 10-digit mobile number
        val digitsOnly = trimmed.replace(Regex("[^0-9]"), "")
        val normalizedPhone = when {
            digitsOnly.startsWith("91") && digitsOnly.length == 12 -> digitsOnly.substring(2)
            digitsOnly.startsWith("0") && digitsOnly.length == 11 -> digitsOnly.substring(1)
            digitsOnly.length == 10 -> digitsOnly
            else -> null
        }

        if (normalizedPhone != null && PHONE_PATTERN.matches(normalizedPhone)) {
            val contactMatch = contacts.find { it.phone == normalizedPhone }
            val name = contactMatch?.name ?: "Mobile: +91 $normalizedPhone"
            return ResolvedRecipient(
                recipient = normalizedPhone,
                displayName = name,
                isPhone = true,
                isValid = true,
                errorMessage = null
            )
        }

        // Check if UPI ID
        if (trimmed.contains("@")) {
            val parts = trimmed.split("@")
            if (parts.size == 2 && parts[0].length >= 2 && parts[1].length >= 2 && VPA_PATTERN.matches(trimmed)) {
                val username = parts[0]
                val handle = parts[1].lowercase()

                // Check in contacts if someone matches this
                val contactMatch = contacts.find { it.name.contains(username, ignoreCase = true) }
                val formattedName = contactMatch?.name ?: formatNameFromUsername(username)

                return ResolvedRecipient(
                    recipient = trimmed,
                    displayName = formattedName,
                    isPhone = false,
                    isValid = true,
                    errorMessage = null
                )
            } else {
                return ResolvedRecipient(
                    recipient = trimmed,
                    displayName = trimmed,
                    isPhone = false,
                    isValid = false,
                    errorMessage = "Invalid UPI ID format (e.g. name@okhdfcbank)"
                )
            }
        }

        return ResolvedRecipient(
            recipient = trimmed,
            displayName = trimmed,
            isPhone = false,
            isValid = false,
            errorMessage = "Please enter a valid UPI ID (e.g. user@oksbi) or 10-digit number"
        )
    }

    private fun formatNameFromUsername(username: String): String {
        return username
            .replace(Regex("[._-]"), " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            .ifEmpty { username }
    }
}
