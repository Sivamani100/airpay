package com.arkiolabs.airpay.presentation.bottomsheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.presentation.permissions.PermissionStatus
import kotlinx.coroutines.launch

enum class SetupSheetType {
    NONE,
    PHONE,
    SMS,
    CAMERA,
    ACCESSIBILITY,
    OVERLAY,
    SIM_PREFERENCE,
    BANK_SELECTION
}

/**
 * Orchestrates the sequential presentation of setup & permission bottom sheets
 * directly on the Home Screen.
 *
 * Sequence:
 * 1. Phone & Call Permission (Essential for *99# USSD)
 * 2. SMS Permission (Bank Transaction Confirmation)
 * 3. Camera Access (QR Code Scanning)
 * 4. Accessibility Service (Hands-free USSD Automation)
 * 5. Overlay Permission (Display over apps)
 * 6. SIM Card Preference (Which SIM to use)
 * 7. Bank Selection (Which bank account)
 */
@Composable
fun SetupBottomSheetCoordinator(
    permissions: PermissionStatus,
    activeSheetOverride: SetupSheetType? = null,
    onSheetDismissed: () -> Unit = {}
) {
    val app = LocalContext.current.airpayApp
    val scope = rememberCoroutineScope()

    val preferredSimSlot by app.prefsRepo.preferredSimSlot.collectAsState(initial = -1)
    val userBankName by app.prefsRepo.userBankName.collectAsState(initial = "")

    var currentSheet by remember { mutableStateOf(SetupSheetType.NONE) }
    var userDismissedCurrent by remember { mutableStateOf(false) }

    // Update active sheet based on override or sequential check
    LaunchedEffect(permissions, preferredSimSlot, userBankName, activeSheetOverride, userDismissedCurrent) {
        if (activeSheetOverride != null && activeSheetOverride != SetupSheetType.NONE) {
            currentSheet = activeSheetOverride
            userDismissedCurrent = false
            return@LaunchedEffect
        }

        if (userDismissedCurrent) {
            currentSheet = SetupSheetType.NONE
            return@LaunchedEffect
        }

        currentSheet = when {
            !permissions.callPhone || !permissions.readPhoneState -> SetupSheetType.PHONE
            !permissions.smsGranted -> SetupSheetType.SMS
            !permissions.camera -> SetupSheetType.CAMERA
            !permissions.accessibility -> SetupSheetType.ACCESSIBILITY
            !permissions.overlay -> SetupSheetType.OVERLAY
            preferredSimSlot == null || preferredSimSlot == -1 -> SetupSheetType.SIM_PREFERENCE
            userBankName.isNullOrBlank() -> SetupSheetType.BANK_SELECTION
            else -> SetupSheetType.NONE
        }
    }

    when (currentSheet) {
        SetupSheetType.PHONE -> {
            PhonePermissionSheet(
                onGranted = {
                    userDismissedCurrent = false
                    currentSheet = SetupSheetType.NONE
                },
                onDismiss = {
                    userDismissedCurrent = true
                    currentSheet = SetupSheetType.NONE
                    onSheetDismissed()
                }
            )
        }

        SetupSheetType.SMS -> {
            SmsPermissionSheet(
                onGranted = {
                    userDismissedCurrent = false
                    currentSheet = SetupSheetType.NONE
                },
                onDismiss = {
                    userDismissedCurrent = true
                    currentSheet = SetupSheetType.NONE
                    onSheetDismissed()
                }
            )
        }

        SetupSheetType.CAMERA -> {
            CameraPermissionSheet(
                onGranted = {
                    userDismissedCurrent = false
                    currentSheet = SetupSheetType.NONE
                },
                onDismiss = {
                    userDismissedCurrent = true
                    currentSheet = SetupSheetType.NONE
                    onSheetDismissed()
                }
            )
        }

        SetupSheetType.ACCESSIBILITY -> {
            AccessibilityPermissionSheet(
                onDismiss = {
                    userDismissedCurrent = true
                    currentSheet = SetupSheetType.NONE
                    onSheetDismissed()
                }
            )
        }

        SetupSheetType.OVERLAY -> {
            OverlayPermissionSheet(
                onDismiss = {
                    userDismissedCurrent = true
                    currentSheet = SetupSheetType.NONE
                    onSheetDismissed()
                }
            )
        }

        SetupSheetType.SIM_PREFERENCE -> {
            SimPreferenceSheet(
                currentSlot = preferredSimSlot?.takeIf { it >= 0 },
                onSimSelected = { slot, carrier, subId ->
                    scope.launch {
                        app.prefsRepo.setPreferredSim(slot, carrier, subId)
                    }
                    userDismissedCurrent = false
                    currentSheet = SetupSheetType.NONE
                },
                onDismiss = {
                    userDismissedCurrent = true
                    currentSheet = SetupSheetType.NONE
                    onSheetDismissed()
                }
            )
        }

        SetupSheetType.BANK_SELECTION -> {
            BankSelectionSheet(
                currentBank = userBankName,
                onBankSelected = { bank ->
                    scope.launch {
                        app.prefsRepo.setUserBankName(bank)
                    }
                    userDismissedCurrent = true
                    currentSheet = SetupSheetType.NONE
                },
                onDismiss = {
                    userDismissedCurrent = true
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
