package com.arkiolabs.localpe.platform

import android.content.Context
import android.provider.ContactsContract
import android.util.Log

data class ContactItem(
    val name: String,
    val phone: String
)

object ContactsHelper {

    private const val TAG = "ContactsHelper"

    fun loadContacts(context: Context): List<ContactItem> {
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

                    // Clean phone number: remove spaces, dashes, parentheses
                    val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
                    val normalizedPhone = when {
                        cleanPhone.startsWith("+91") && cleanPhone.length == 13 -> cleanPhone.substring(3)
                        cleanPhone.startsWith("0") && cleanPhone.length == 11 -> cleanPhone.substring(1)
                        cleanPhone.length == 10 -> cleanPhone
                        else -> cleanPhone
                    }

                    if (normalizedPhone.isNotBlank() && !seenPhones.contains(normalizedPhone)) {
                        seenPhones.add(normalizedPhone)
                        contacts.add(ContactItem(name = name, phone = normalizedPhone))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts: ${e.message}", e)
        }

        return contacts
    }
}
