package com.arkiolabs.airpay.platform

import android.content.Context
import android.provider.ContactsContract
import android.util.Log

data class ContactItem(
    val name: String,
    val phone: String
)

object ContactsHelper {

    private const val TAG = "ContactsHelper"
    @Volatile
    private var cachedContacts: List<ContactItem>? = null
    @Volatile
    private var cachedMap: Map<String, String>? = null

    fun loadContacts(context: Context, forceReload: Boolean = false): List<ContactItem> {
        if (!forceReload && cachedContacts != null) {
            return cachedContacts!!
        }

        val contacts = mutableListOf<ContactItem>()
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

                    val normalizedPhone = normalizePhone(rawPhone)

                    if (normalizedPhone.isNotBlank() && !seenPhones.contains(normalizedPhone)) {
                        seenPhones.add(normalizedPhone)
                        contacts.add(ContactItem(name = name, phone = normalizedPhone))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts: ${e.message}", e)
        }

        cachedContacts = contacts
        cachedMap = contacts.associate { it.phone.takeLast(10) to it.name }
        return contacts
    }

    /**
     * Normalizes a phone number to standard 10-digit Indian mobile format if possible.
     */
    fun normalizePhone(rawPhone: String): String {
        val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
        return when {
            cleanPhone.startsWith("+91") && cleanPhone.length >= 13 -> cleanPhone.takeLast(10)
            cleanPhone.startsWith("0") && cleanPhone.length >= 11 -> cleanPhone.takeLast(10)
            cleanPhone.length >= 10 -> cleanPhone.takeLast(10)
            else -> cleanPhone
        }
    }

    /**
     * Resolves a phone number or VPA containing a phone number to the saved Contact Name.
     * Returns null if not found in device contacts.
     */
    fun getContactName(context: Context, rawPhoneOrVpa: String): String? {
        val digits = rawPhoneOrVpa.replace(Regex("[^0-9]"), "")
        if (digits.length < 10) return null
        val tenDigits = digits.takeLast(10)

        val map = cachedMap ?: run {
            loadContacts(context)
            cachedMap
        }
        return map?.get(tenDigits)
    }

    /**
     * Resolves the best display name:
     * 1. If phone/VPA matches a saved device contact, uses the contact name.
     * 2. If payeeName is an actual name (not just digits/generic), uses payeeName.
     * 3. Otherwise returns vpa or raw number.
     */
    fun resolveDisplayName(context: Context, vpa: String, payeeName: String?): String {
        val fromContact = getContactName(context, vpa)
        if (!fromContact.isNullOrBlank()) {
            return fromContact
        }

        if (!payeeName.isNullOrBlank()) {
            val fromPayeeContact = getContactName(context, payeeName)
            if (!fromPayeeContact.isNullOrBlank()) {
                return fromPayeeContact
            }
            val digitsOnly = payeeName.replace(Regex("[^0-9]"), "")
            if (payeeName != "Airpay User" && digitsOnly.length != payeeName.length) {
                return payeeName
            }
        }

        return vpa
    }
}
