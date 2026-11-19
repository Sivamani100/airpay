package com.arkiolabs.airpay.platform

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat

data class ContactItem(
    val name: String,
    val phone: String
)

object ContactsHelper {

    private const val TAG = "ContactsHelper"

    @Volatile
    private var cachedContacts: List<ContactItem>? = null

    @Volatile
    private var phoneToNameMap: Map<String, String>? = null

    fun loadContacts(context: Context, forceRefresh: Boolean = false): List<ContactItem> {
        if (!forceRefresh && cachedContacts != null) {
            return cachedContacts!!
        }

        // Check permission before querying
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return cachedContacts ?: emptyList()
        }

        val contacts = mutableListOf<ContactItem>()
        val phoneMap = mutableMapOf<String, String>()
        val seenPhones = mutableSetOf<String>()

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use { c ->
                val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (c.moveToNext()) {
                    val name = if (nameIdx >= 0) c.getString(nameIdx) ?: "Contact" else "Contact"
                    val rawPhone = if (numIdx >= 0) c.getString(numIdx) ?: "" else ""

                    val norm = normalizePhone(rawPhone)
                    if (!norm.isNullOrBlank() && !seenPhones.contains(norm)) {
                        seenPhones.add(norm)
                        contacts.add(ContactItem(name = name, phone = norm))
                        phoneMap[norm] = name
                    }

                    // Also index raw digits if different
                    val rawDigits = rawPhone.filter { it.isDigit() }
                    if (rawDigits.isNotBlank()) {
                        phoneMap[rawDigits] = name
                        if (rawDigits.length > 10) {
                            phoneMap[rawDigits.takeLast(10)] = name
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts: ${e.message}", e)
        }

        cachedContacts = contacts
        phoneToNameMap = phoneMap
        return contacts
    }

    /**
     * Normalizes a phone number string to a 10-digit standard Indian mobile number if possible.
     */
    fun normalizePhone(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.filter { it.isDigit() }
        return when {
            clean.length == 10 -> clean
            clean.length == 11 && clean.startsWith("0") -> clean.substring(1)
            clean.length == 12 && clean.startsWith("91") -> clean.substring(2)
            clean.length == 13 && clean.startsWith("091") -> clean.substring(3)
            clean.length > 10 -> clean.takeLast(10)
            else -> clean.takeIf { it.isNotBlank() }
        }
    }

    /**
     * Extracts a 10-digit phone number from either a raw phone string or a VPA (e.g. "9876543210@ybl").
     */
    fun extractPhone(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val candidate = if (raw.contains("@")) raw.substringBefore("@") else raw
        val norm = normalizePhone(candidate)
        return if (norm != null && norm.length == 10 && norm.all { it.isDigit() }) norm else null
    }

    /**
     * Looks up the saved contact display name for a given phone number or VPA.
     * Returns null if no matching contact is saved in the user's phonebook.
     */
    fun getContactName(context: Context, rawPhoneOrVpa: String?): String? {
        if (rawPhoneOrVpa.isNullOrBlank()) return null

        if (phoneToNameMap == null) {
            loadContacts(context)
        }
        val map = phoneToNameMap ?: return null

        // 1. Direct normalized phone lookup
        val norm = normalizePhone(rawPhoneOrVpa)
        if (norm != null && map.containsKey(norm)) {
            return map[norm]
        }

        // 2. Extracted phone from VPA
        val extracted = extractPhone(rawPhoneOrVpa)
        if (extracted != null && map.containsKey(extracted)) {
            return map[extracted]
        }

        // 3. Raw digits fallback
        val digits = rawPhoneOrVpa.filter { it.isDigit() }
        if (digits.isNotBlank()) {
            if (map.containsKey(digits)) return map[digits]
            if (digits.length >= 10 && map.containsKey(digits.takeLast(10))) {
                return map[digits.takeLast(10)]
            }
        }

        return null
    }

    /**
     * Resolves the best human-readable display name for transactions, cards, and UI.
     * Prioritizes saved phonebook contact names, then explicit payee names, then cleaned VPA.
     */
    fun resolveDisplayName(context: Context, vpa: String?, payeeName: String?): String {
        // 1. Check if VPA or phone resolves to a saved contact
        val fromVpa = getContactName(context, vpa)
        if (!fromVpa.isNullOrBlank()) return fromVpa

        // 2. Check if payeeName contains a phone or VPA that resolves to a saved contact
        val fromPayee = getContactName(context, payeeName)
        if (!fromPayee.isNullOrBlank()) return fromPayee

        // 3. If payeeName is an actual name (not generic "Mobile: +91..." or pure digits)
        if (!payeeName.isNullOrBlank()) {
            val trimmed = payeeName.trim()
            val isGenericMobile = trimmed.startsWith("Mobile:", ignoreCase = true)
            val isPureDigits = trimmed.all { it.isDigit() || it == '+' || it == ' ' || it == '-' }
            if (!isGenericMobile && !isPureDigits) {
                return trimmed
            }
        }

        // 4. Fallback to VPA or "Recipient"
        return vpa?.takeIf { it.isNotBlank() } ?: "Recipient"
    }

    fun clearCache() {
        cachedContacts = null
        phoneToNameMap = null
    }
}
