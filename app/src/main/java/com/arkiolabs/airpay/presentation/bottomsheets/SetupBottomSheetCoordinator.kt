package com.arkiolabs.airpay.presentation.bottomsheets

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.platform.UssdAccessibilityService
import com.arkiolabs.airpay.presentation.permissions.PermissionStatus
import kotlinx.coroutines.launch

enum class SetupSheetType {
    NONE,
    PHONE,
    SMS,
    CONTACTS,
    CAMERA,
    ACCESSIBILITY,
    OVERLAY,
    SIM_PREFERENCE,
    BANK_SELECTION
}

/**
 * High-speed, zero-delay coordinator for all required permissions, accessibility, overlay, SIM, and bank setup bottom sheets.
 * - Non-dismissible until configured.
 * - Transitions between sheets instantly (0 ms delay) upon granting each permission.
 */
@Composable
fun SetupBottomSheetCoordinator(
    permissions: PermissionStatus,
    activeSheetOverride: SetupSheetType? = null,
    onSheetDismissed: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val app = context.airpayApp
    val scope = rememberCoroutineScope()
    val cachedPrefs = remember { context.getSharedPreferences("airpay_fast_cache", Context.MODE_PRIVATE) }

    val preferredSimSlot by app.prefsRepo.preferredSimSlot.collectAsState(initial = -1)
    val userBankName by app.prefsRepo.userBankName.collectAsState(initial = "")

    fun checkAccessibility(): Boolean {
        val expected = "${context.packageName}/${UssdAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(":").any { it.equals(expected, ignoreCase = true) }
    }

    fun determineNextStep(): SetupSheetType {
        val phoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val smsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val contactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val accessibilityGranted = checkAccessibility()
        val overlayGranted = Settings.canDrawOverlays(context)
        val cachedSim = cachedPrefs.getInt("preferred_sim_slot", -1)
        val cachedBank = cachedPrefs.getString("user_bank_name", null)

        return when {
            !phoneGranted -> SetupSheetType.PHONE
            !smsGranted -> SetupSheetType.SMS
            !contactsGranted -> SetupSheetType.CONTACTS
            !cameraGranted -> SetupSheetType.CAMERA
            !accessibilityGranted -> SetupSheetType.ACCESSIBILITY
            !overlayGranted -> SetupSheetType.OVERLAY
            cachedSim == -1 -> SetupSheetType.SIM_PREFERENCE
            cachedBank.isNullOrBlank() -> SetupSheetType.BANK_SELECTION
            else -> SetupSheetType.NONE
        }
    }

    var currentSheet by remember { mutableStateOf(determineNextStep()) }

    // On resume, re-check current step (e.g. if returning from Accessibility Settings or Overlay Settings)
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val next = determineNextStep()
                if (currentSheet == SetupSheetType.ACCESSIBILITY && checkAccessibility()) {
                    currentSheet = next
                } else if (currentSheet == SetupSheetType.OVERLAY && Settings.canDrawOverlays(context)) {
                    currentSheet = next
                } else if (currentSheet == SetupSheetType.PHONE || currentSheet == SetupSheetType.SMS || currentSheet == SetupSheetType.CONTACTS || currentSheet == SetupSheetType.CAMERA) {
                    val phoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    val smsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                    val contactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                    val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if ((currentSheet == SetupSheetType.PHONE && phoneGranted) ||
                        (currentSheet == SetupSheetType.SMS && smsGranted) ||
                        (currentSheet == SetupSheetType.CONTACTS && contactsGranted) ||
                        (currentSheet == SetupSheetType.CAMERA && cameraGranted)
                    ) {
                        currentSheet = next
                    }
                }
                if (next == SetupSheetType.NONE && currentSheet != SetupSheetType.NONE) {
                    currentSheet = SetupSheetType.NONE
                    onSheetDismissed()
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // Respond immediately to external overrides (e.g. user tapping action on Home screen)
    LaunchedEffect(activeSheetOverride) {
        if (activeSheetOverride != null && activeSheetOverride != SetupSheetType.NONE) {
            currentSheet = activeSheetOverride
        }
    }

    when (currentSheet) {
        SetupSheetType.PHONE -> {
            PhonePermissionSheet(
                onGranted = {
                    currentSheet = determineNextStep()
                }
            )
        }

        SetupSheetType.SMS -> {
            SmsPermissionSheet(
                onGranted = {
                    currentSheet = determineNextStep()
                }
            )
        }

        SetupSheetType.CONTACTS -> {
            ContactsPermissionSheet(
                onGranted = {
                    currentSheet = determineNextStep()
                }
            )
        }

        SetupSheetType.CAMERA -> {
            CameraPermissionSheet(
                onGranted = {
                    currentSheet = determineNextStep()
                }
            )
        }

        SetupSheetType.ACCESSIBILITY -> {
            AccessibilityPermissionSheet(
                onConfigured = {
                    currentSheet = determineNextStep()
                }
            )
        }

        SetupSheetType.OVERLAY -> {
            OverlayPermissionSheet(
                onConfigured = {
                    currentSheet = determineNextStep()
                }
            )
        }

        SetupSheetType.SIM_PREFERENCE -> {
            val initialSim = preferredSimSlot?.takeIf { it >= 0 } ?: cachedPrefs.getInt("preferred_sim_slot", -1).takeIf { it >= 0 }
            SimPreferenceSheet(
                currentSlot = initialSim,
                onSimSelected = { slot, carrier, subId ->
                    cachedPrefs.edit().putInt("preferred_sim_slot", slot).apply()
                    scope.launch {
                        app.prefsRepo.setPreferredSim(slot, carrier, subId)
                    }
                    val cachedBank = cachedPrefs.getString("user_bank_name", null)
                    if (cachedBank.isNullOrBlank()) {
                        currentSheet = SetupSheetType.BANK_SELECTION
                    } else {
                        currentSheet = SetupSheetType.NONE
                        onSheetDismissed()
                    }
                }
            )
        }

        SetupSheetType.BANK_SELECTION -> {
            val initialBank = userBankName?.takeIf { it.isNotBlank() } ?: cachedPrefs.getString("user_bank_name", null)
            BankSelectionSheet(
                currentBank = initialBank,
                onBankSelected = { bank ->
                    cachedPrefs.edit().putString("user_bank_name", bank).apply()
                    scope.launch {
                        app.prefsRepo.setUserBankName(bank)
                    }
                    currentSheet = SetupSheetType.NONE
                    onSheetDismissed()
                }
            )
        }

        SetupSheetType.NONE -> {
            // Nothing to show
        }
    }
}
