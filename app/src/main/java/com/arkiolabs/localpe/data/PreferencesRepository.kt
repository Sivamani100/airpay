package com.arkiolabs.localpe.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.arkiolabs.localpe.domain.OperationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PreferencesKeys {
    val OPERATION_MODE = stringPreferencesKey("operation_mode")
    val BATTERY_WARNING_DISMISSED = booleanPreferencesKey("battery_warning_dismissed")
    val FIRST_LAUNCH_COMPLETE = booleanPreferencesKey("first_launch_complete")
    val LAST_BALANCE_TEXT = stringPreferencesKey("last_balance_text")
    val LAST_BALANCE_TIMESTAMP = longPreferencesKey("last_balance_timestamp")

    // Biometric PIN bypass
    val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    val BIOMETRIC_SAVED_PIN = stringPreferencesKey("biometric_saved_pin")

    // Custom Transaction Audio Sounds
    val SUCCESS_SOUND_URI = stringPreferencesKey("success_sound_uri")
    val FAILED_SOUND_URI = stringPreferencesKey("failed_sound_uri")

    // Dynamic QR Profile UPI ID (fetched via SIM profile lookup)
    val USER_UPI_ID = stringPreferencesKey("user_upi_id")

    // Jio 123PAY 1-time Bank Registration Status
    val JIO_123PAY_REGISTERED = booleanPreferencesKey("jio_123pay_registered")
    val USER_BANK_NAME = stringPreferencesKey("user_bank_name")
    val USER_PREFERRED_LANGUAGE = stringPreferencesKey("user_preferred_language")
}

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val operationMode: Flow<OperationMode> = dataStore.data.map { preferences ->
        val stored = preferences[PreferencesKeys.OPERATION_MODE]
        if (stored != null) {
            try {
                OperationMode.valueOf(stored)
            } catch (_: IllegalArgumentException) {
                when (stored) {
                    "MINIMAL" -> OperationMode.ADVANCED
                    "DIALER" -> OperationMode.MANUAL
                    "OVERLAY", "ADVANCED" -> OperationMode.AUTO
                    else -> OperationMode.AUTO
                }
            }
        } else {
            OperationMode.AUTO
        }
    }

    suspend fun setOperationMode(mode: OperationMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.OPERATION_MODE] = mode.name
        }
    }

    val batteryWarningDismissed: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BATTERY_WARNING_DISMISSED] ?: false
    }

    suspend fun setBatteryWarningDismissed(dismissed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BATTERY_WARNING_DISMISSED] = dismissed
        }
    }

    val firstLaunchComplete: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_LAUNCH_COMPLETE] ?: false
    }

    suspend fun setFirstLaunchComplete(complete: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH_COMPLETE] = complete
        }
    }

    /** Last successful balance check — full carrier reply text. */
    val lastBalanceText: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_BALANCE_TEXT]
    }

    /** Epoch millis of the last successful balance check. */
    val lastBalanceTimestamp: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_BALANCE_TIMESTAMP]
    }

    suspend fun setLastBalance(text: String, timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BALANCE_TEXT] = text
            preferences[PreferencesKeys.LAST_BALANCE_TIMESTAMP] = timestamp
        }
    }

    suspend fun clearLastBalance() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LAST_BALANCE_TEXT)
            preferences.remove(PreferencesKeys.LAST_BALANCE_TIMESTAMP)
        }
    }

    // ── Biometric UPI PIN ──────────────────────────────────────────

    val biometricEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
            if (!enabled) {
                preferences.remove(PreferencesKeys.BIOMETRIC_SAVED_PIN)
            }
        }
    }

    val savedBiometricPin: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIOMETRIC_SAVED_PIN]
    }

    suspend fun setSavedBiometricPin(pin: String?) {
        dataStore.edit { preferences ->
            if (pin != null) {
                preferences[PreferencesKeys.BIOMETRIC_SAVED_PIN] = pin
            } else {
                preferences.remove(PreferencesKeys.BIOMETRIC_SAVED_PIN)
            }
        }
    }

    suspend fun clearBiometricPin() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.BIOMETRIC_SAVED_PIN)
        }
    }

    // ── Custom Sounds ──────────────────────────────────────────────

    val successSoundUri: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SUCCESS_SOUND_URI]
    }

    suspend fun setSuccessSoundUri(uri: String?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[PreferencesKeys.SUCCESS_SOUND_URI] = uri
            } else {
                preferences.remove(PreferencesKeys.SUCCESS_SOUND_URI)
            }
        }
    }

    val failedSoundUri: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FAILED_SOUND_URI]
    }

    suspend fun setFailedSoundUri(uri: String?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[PreferencesKeys.FAILED_SOUND_URI] = uri
            } else {
                preferences.remove(PreferencesKeys.FAILED_SOUND_URI)
            }
        }
    }

    // ── Dynamic Real UPI ID ────────────────────────────────────────

    val userUpiId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_UPI_ID]
    }

    suspend fun setUserUpiId(upiId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_UPI_ID] = upiId
        }
    }

    // ── Jio 123PAY Registration ────────────────────────────────────

    val jio123PayRegistered: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.JIO_123PAY_REGISTERED] ?: false
    }

    suspend fun setJio123PayRegistered(registered: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.JIO_123PAY_REGISTERED] = registered
        }
    }

    val userBankName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_BANK_NAME]
    }

    suspend fun setUserBankName(bank: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_BANK_NAME] = bank
        }
    }

    val userPreferredLanguage: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_PREFERRED_LANGUAGE]
    }

    suspend fun setUserPreferredLanguage(lang: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_PREFERRED_LANGUAGE] = lang
        }
    }
}
