package com.arkiolabs.airpay.presentation.screens

import android.view.HapticFeedbackConstants
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.data.TransactionEntity
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.PayViewModel
import com.arkiolabs.airpay.presentation.bottomsheets.SetupBottomSheetCoordinator
import com.arkiolabs.airpay.presentation.bottomsheets.SetupSheetType
import com.arkiolabs.airpay.presentation.bottomsheets.ShakeSuggestionSheet
import com.arkiolabs.airpay.presentation.permissions.PermissionStatus
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import kotlinx.coroutines.launch

/**
 * Cleaned & Streamlined Airpay Home Screen.
 *
 * - Ultra-clean top bar: App logo on left, My QR & Settings on right.
 * - Money Transfers 4 core actions (To Mobile Number, To UPI ID, Check Balance, My QR).
 * - Setup Progress Card (visible only when permissions/preferences are pending).
 * - Recent Transfers list for 1-tap offline repeat payment.
 * - All clutter, dummy banners, and unwanted sections removed.
 */
@Composable
fun HomeScreen(
    payViewModel: PayViewModel,
    historyViewModel: HistoryViewModel,
    permissions: PermissionStatus,
    onNavigatePay: () -> Unit,
    onNavigateToMobile: () -> Unit = onNavigatePay,
    onNavigateToUpi: () -> Unit = onNavigatePay,
    onNavigateScan: () -> Unit,
    onNavigateBalance: () -> Unit,
    onNavigateMyQr: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateFaq: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by historyViewModel.transactions.collectAsState()

    val context = LocalContext.current
    val view = LocalView.current
    val app = context.airpayApp
    val preferredSimSlot by app.prefsRepo.preferredSimSlot.collectAsState(initial = -1)
    val userBankName by app.prefsRepo.userBankName.collectAsState(initial = "")
    val shakeToScanEnabled by app.prefsRepo.shakeToScanEnabled.collectAsState(initial = false)
    val shakeSuggestionDismissed by app.prefsRepo.shakeSuggestionDismissed.collectAsState(initial = false)

    val scope = rememberCoroutineScope()
    var activeSheetOverride by remember { mutableStateOf<SetupSheetType?>(null) }
    var showShakeSuggestion by remember { mutableStateOf(false) }

    val isPhoneReady = permissions.callPhone && permissions.readPhoneState
    val isSmsReady = permissions.smsGranted
    val isContactsReady = permissions.contactsGranted
    val isCameraReady = permissions.camera
    val isAccessibilityReady = permissions.accessibility
    val isOverlayReady = permissions.overlay
    val isSimReady = preferredSimSlot != null && preferredSimSlot != -1
    val isBankReady = !userBankName.isNullOrBlank()

    val completedCount = listOf(
        isPhoneReady,
        isSmsReady,
        isContactsReady,
        isCameraReady,
        isAccessibilityReady,
        isOverlayReady,
        isSimReady,
        isBankReady
    ).count { it }
    val totalCount = 8

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Clean Top Header (Logo on Left, QR & Settings on Right) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-res Square Airpay Logo (No text, No mode pill)
            Image(
                painter = painterResource(id = R.drawable.ic_airpay_logo),
                contentDescription = "Airpay Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // My QR Code shortcut
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F6F8))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onNavigateMyQr()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_scan),
                        contentDescription = "My QR Code",
                        tint = AirpayColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Settings icon button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F6F8))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onNavigateSettings()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = AirpayColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Main Scrollable Body ────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Setup Progress Banner (Visible only while setup is pending) ──
            if (completedCount < totalCount) {
                AirpaySetupProgressCard(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    onClick = {
                        activeSheetOverride = when {
                            !isPhoneReady -> SetupSheetType.PHONE
                            !isSmsReady -> SetupSheetType.SMS
                            !isContactsReady -> SetupSheetType.CONTACTS
                            !isCameraReady -> SetupSheetType.CAMERA
                            !isAccessibilityReady -> SetupSheetType.ACCESSIBILITY
                            !isOverlayReady -> SetupSheetType.OVERLAY
                            !isSimReady -> SetupSheetType.SIM_PREFERENCE
                            !isBankReady -> SetupSheetType.BANK_SELECTION
                            else -> SetupSheetType.NONE
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Money Transfers: 4 Core Offline Actions ───────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Money Transfers",
                    fontFamily = ClashGrotesk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Action 1: To Mobile Number
                    AirpayActionTile(
                        label = "To Mobile\nNumber",
                        iconResId = R.drawable.to_mobile_number,
                        onClick = {
                            when {
                                !isPhoneReady -> activeSheetOverride = SetupSheetType.PHONE
                                !isContactsReady -> activeSheetOverride = SetupSheetType.CONTACTS
                                !isAccessibilityReady -> activeSheetOverride = SetupSheetType.ACCESSIBILITY
                                !isOverlayReady -> activeSheetOverride = SetupSheetType.OVERLAY
                                !isSimReady -> activeSheetOverride = SetupSheetType.SIM_PREFERENCE
                                else -> onNavigateToMobile()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Action 2: To UPI ID
                    AirpayActionTile(
                        label = "To UPI\nID",
                        iconResId = R.drawable.to_bank_self_account,
                        onClick = {
                            when {
                                !isPhoneReady -> activeSheetOverride = SetupSheetType.PHONE
                                !isAccessibilityReady -> activeSheetOverride = SetupSheetType.ACCESSIBILITY
                                !isOverlayReady -> activeSheetOverride = SetupSheetType.OVERLAY
                                !isSimReady -> activeSheetOverride = SetupSheetType.SIM_PREFERENCE
                                else -> onNavigateToUpi()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Action 3: Check Balance
                    AirpayActionTile(
                        label = "Check\nBalance",
                        iconResId = R.drawable.check_balance_v1,
                        onClick = {
                            when {
                                !isPhoneReady -> activeSheetOverride = SetupSheetType.PHONE
                                !isAccessibilityReady -> activeSheetOverride = SetupSheetType.ACCESSIBILITY
                                !isOverlayReady -> activeSheetOverride = SetupSheetType.OVERLAY
                                !isSimReady -> activeSheetOverride = SetupSheetType.SIM_PREFERENCE
                                !isBankReady -> activeSheetOverride = SetupSheetType.BANK_SELECTION
                                else -> onNavigateBalance()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Action 4: My QR Code
                    AirpayActionTile(
                        label = "My QR\nCode",
                        iconResId = R.drawable.ic_airpay_qr_icon_monochrome,
                        onClick = onNavigateMyQr,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Subtle divider
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFF4F5F8))
            )

            // ── Recent Transfers / Payees ───────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                RecentTransfersSection(
                    transactions = transactions,
                    onSelectRecipient = { vpa, amount ->
                        payViewModel.prefillFromTransaction(vpa = vpa, amount = amount, note = "")
                        onNavigatePay()
                    },
                    onViewAll = onNavigateHistory
                )
            }

            Spacer(Modifier.height(30.dp))
        }

        // ── Dedicated Bottom Sheet Coordinator for Permissions, SIM & Bank Setup ──
        SetupBottomSheetCoordinator(
            permissions = permissions,
            activeSheetOverride = activeSheetOverride,
            onSheetDismissed = { activeSheetOverride = null }
        )

        // ── Shake to Scan Suggestion Sheet (after 3 transactions) ──
        LaunchedEffect(transactions.size, completedCount, shakeToScanEnabled, shakeSuggestionDismissed) {
            if (completedCount == totalCount && transactions.size >= 3 && !shakeToScanEnabled && !shakeSuggestionDismissed) {
                showShakeSuggestion = true
            }
        }

        if (showShakeSuggestion) {
            ShakeSuggestionSheet(
                onEnable = { alsoOpenOnLaunch ->
                    showShakeSuggestion = false
                    scope.launch {
                        app.prefsRepo.setShakeToScanEnabled(true)
                        if (alsoOpenOnLaunch) {
                            app.prefsRepo.setOpenScannerOnLaunch(true)
                        }
                        app.prefsRepo.setShakeSuggestionDismissed(true)
                    }
                },
                onDismiss = {
                    showShakeSuggestion = false
                    scope.launch {
                        app.prefsRepo.setShakeSuggestionDismissed(true)
                    }
                }
            )
        }
    }
}

/**
 * Setup progress card displayed at the top when any permission, SIM, or bank preference is pending.
 */
@Composable
private fun AirpaySetupProgressCard(
    completedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AirpayColors.SaffronTint)
            .border(1.5.dp, AirpayColors.SaffronLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AirpayColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = AirpayColors.Saffron,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Complete Offline UPI Setup",
                fontSize = 13.5.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$completedCount of $totalCount configured • Tap to grant permissions",
                fontSize = 11.5.sp,
                color = AirpayColors.SaffronDark,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AirpayColors.Primary)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Setup →",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Action Tile with circular 3D icon and 2-line label.
 */
@Composable
private fun AirpayActionTile(
    label: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(AirpayColors.PurpleTint),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label.replace("\n", " "),
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AirpayColors.TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            maxLines = 2
        )
    }
}

/**
 * Recent Transfers / Beneficiaries horizontal row.
 */
@Composable
private fun RecentTransfersSection(
    transactions: List<TransactionEntity>,
    onSelectRecipient: (String, String) -> Unit,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transfers",
                fontFamily = ClashGrotesk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )

            Text(
                text = "View All →",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = AirpayColors.Primary,
                modifier = Modifier.clickable { onViewAll() }
            )
        }

        Spacer(Modifier.height(14.dp))

        if (transactions.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F6F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Iconsax.Call,
                        contentDescription = null,
                        tint = AirpayColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "No recent transfers yet",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AirpayColors.TextPrimary
                    )
                    Text(
                        text = "Send money to any mobile or UPI ID to get started.",
                        fontSize = 11.5.sp,
                        color = AirpayColors.TextSecondary
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(transactions.take(8)) { txn ->
                    val displayName = txn.payeeName?.takeIf { it.isNotBlank() } ?: txn.vpa.substringBefore("@")
                    RecipientBubbleItem(
                        recipientName = displayName,
                        vpa = txn.vpa,
                        amount = txn.amount,
                        onClick = { onSelectRecipient(txn.vpa, txn.amount) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipientBubbleItem(
    recipientName: String,
    vpa: String,
    amount: String,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val initial = recipientName.take(1).uppercase()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(62.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AirpayColors.Primary)
                .border(1.5.dp, AirpayColors.Saffron, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 17.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = recipientName,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = AirpayColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
