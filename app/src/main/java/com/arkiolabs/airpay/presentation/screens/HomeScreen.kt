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
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.arkiolabs.airpay.platform.ContactsHelper
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
    onNavigateContactChat: () -> Unit = onNavigatePay,
    onNavigateToMobile: () -> Unit = onNavigatePay,
    onNavigateToUpi: () -> Unit = onNavigatePay,
    onNavigateToBank: () -> Unit = onNavigatePay,
    onNavigateRequestMoney: () -> Unit = onNavigatePay,
    onNavigateNetworkTxns: () -> Unit = onNavigatePay,
    onNavigateRewards: () -> Unit = onNavigatePay,
    onNavigateUpiPinManage: () -> Unit = onNavigatePay,
    onNavigateChangeBank: () -> Unit = onNavigatePay,
    onNavigatePendingRequests: () -> Unit = onNavigatePay,
    onNavigateProfile: () -> Unit = onNavigatePay,
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
    val userName by app.prefsRepo.userName.collectAsState(initial = "")
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
    ) {
        // ── Main Scrollable Body ────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top Hero Banner (Edge-to-Edge with overlaid PhonePe Top Bar) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onNavigateToMobile()
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner_instant_money),
                    contentDescription = "Send & Receive Money Instantly - Get Started",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )

                // Overlaid Top Bar (Avatar on Left, Help & Settings on Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar with Tiny QR Badge + Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onNavigateProfile()
                            }
                            .padding(4.dp)
                    ) {
                        // Profile Avatar with Tiny QR Badge on Bottom-Right
                        Box(modifier = Modifier.size(42.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF5F259F), Color(0xFF7C3AED))
                                        )
                                    )
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (userName?.take(1)?.ifBlank { null } ?: "A").uppercase(),
                                    color = Color.White,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }

                            // Tiny QR Badge on Bottom-Right of Avatar
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_qr_scan),
                                    contentDescription = null,
                                    tint = Color(0xFF5F259F),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                text = userName?.ifBlank { null } ?: "Airpay User",
                                fontSize = 14.5.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isBankReady) (userBankName ?: "Offline SIM") else "Offline UPI • *99#",
                                fontSize = 11.sp,
                                color = Color(0xFFE0F2FE),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Top Right: Help ? & Settings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Help icon (? in circular button)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.92f))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    onNavigateFaq()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_help_white),
                                contentDescription = "Help",
                                tint = Color(0xFF1C1C1E),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Settings icon button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.92f))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    onNavigateSettings()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF1C1C1E),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

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

            // ── Money Transfers & Core Actions (Unified, no separate section boxes) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Money Transfers",
                        fontFamily = ClashGrotesk,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )

                    // Pill Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF3E0))
                            .border(0.8.dp, Color(0xFFFFCC80), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Offline • *99#",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Row 1: Core Money Transfers (Circular Purple PhonePe Tiles)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Action 1: To Mobile Number
                    PhonePeActionTile(
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

                    // Action 2: To Bank Account (IFSC & A/c)
                    PhonePeActionTile(
                        label = "To Bank\nAccount",
                        iconResId = R.drawable.to_bank_self_account,
                        onClick = {
                            when {
                                !isPhoneReady -> activeSheetOverride = SetupSheetType.PHONE
                                !isAccessibilityReady -> activeSheetOverride = SetupSheetType.ACCESSIBILITY
                                !isOverlayReady -> activeSheetOverride = SetupSheetType.OVERLAY
                                !isSimReady -> activeSheetOverride = SetupSheetType.SIM_PREFERENCE
                                else -> onNavigateToBank()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Action 3: To UPI ID
                    PhonePeActionTile(
                        label = "To UPI\nID",
                        iconResId = R.drawable.wallet_home,
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

                    // Action 4: Check Balance
                    PhonePeActionTile(
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
                }

                Spacer(Modifier.height(18.dp))

                // Row 2: UPI Services (Direct on page, cleanly arranged)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Service 1: Request Money
                    PhonePeServiceCardTile(
                        label = "Request\nMoney",
                        vectorIcon = Icons.AutoMirrored.Filled.CallReceived,
                        iconTint = Color(0xFF5F259F),
                        iconBg = Color(0xFFEDE7F6),
                        onClick = {
                            when {
                                !isPhoneReady -> activeSheetOverride = SetupSheetType.PHONE
                                !isAccessibilityReady -> activeSheetOverride = SetupSheetType.ACCESSIBILITY
                                !isOverlayReady -> activeSheetOverride = SetupSheetType.OVERLAY
                                else -> onNavigateRequestMoney()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Service 2: Pending Requests
                    PhonePeServiceCardTile(
                        label = "Pending\nRequests",
                        vectorIcon = Icons.Default.HourglassBottom,
                        iconTint = Color(0xFFE65100),
                        iconBg = Color(0xFFFFF3E0),
                        onClick = {
                            when {
                                !isPhoneReady -> activeSheetOverride = SetupSheetType.PHONE
                                !isAccessibilityReady -> activeSheetOverride = SetupSheetType.ACCESSIBILITY
                                !isOverlayReady -> activeSheetOverride = SetupSheetType.OVERLAY
                                else -> onNavigatePendingRequests()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Service 3: UPI PIN Settings
                    PhonePeServiceCardTile(
                        label = "UPI PIN\nSettings",
                        vectorIcon = Icons.Default.LockReset,
                        iconTint = Color(0xFF00838F),
                        iconBg = Color(0xFFE0F7FA),
                        onClick = {
                            when {
                                !isPhoneReady -> activeSheetOverride = SetupSheetType.PHONE
                                !isAccessibilityReady -> activeSheetOverride = SetupSheetType.ACCESSIBILITY
                                !isOverlayReady -> activeSheetOverride = SetupSheetType.OVERLAY
                                else -> onNavigateUpiPinManage()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Service 4: Change Bank
                    PhonePeServiceCardTile(
                        label = "Change\nBank",
                        vectorIcon = Icons.Default.AccountBalance,
                        iconTint = Color(0xFF1565C0),
                        iconBg = Color(0xFFE3F2FD),
                        onClick = {
                            when {
                                !isPhoneReady -> activeSheetOverride = SetupSheetType.PHONE
                                !isAccessibilityReady -> activeSheetOverride = SetupSheetType.ACCESSIBILITY
                                !isOverlayReady -> activeSheetOverride = SetupSheetType.OVERLAY
                                else -> onNavigateChangeBank()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Row 3: Statements & Offline Tools (Direct on page, cleanly arranged)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Service 5: Past Transactions (*99*6*1#)
                    PhonePeServiceCardTile(
                        label = "Past\nTxns",
                        vectorIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                        iconTint = Color(0xFF2E7D32),
                        iconBg = Color(0xFFE8F5E9),
                        onClick = { onNavigateNetworkTxns() },
                        modifier = Modifier.weight(1f)
                    )

                    // Service 6: View Rewards (*99*6*2#)
                    PhonePeServiceCardTile(
                        label = "View\nRewards",
                        vectorIcon = Icons.Default.CardGiftcard,
                        iconTint = Color(0xFFC2185B),
                        iconBg = Color(0xFFFCE4EC),
                        onClick = { onNavigateRewards() },
                        modifier = Modifier.weight(1f)
                    )

                    // Service 7: My QR Code
                    PhonePeServiceCardTile(
                        label = "My QR\nCode",
                        vectorIcon = Icons.Default.QrCode,
                        iconTint = Color(0xFF00838F),
                        iconBg = Color(0xFFE0F7FA),
                        onClick = onNavigateMyQr,
                        modifier = Modifier.weight(1f)
                    )

                    // Service 8: SIM Preferences
                    PhonePeServiceCardTile(
                        label = "SIM\nSlot",
                        vectorIcon = Icons.Default.SimCard,
                        iconTint = Color(0xFFEF6C00),
                        iconBg = Color(0xFFFFF3E0),
                        onClick = { activeSheetOverride = SetupSheetType.SIM_PREFERENCE },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Recent Transfers (Clean horizontal list directly on page) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                RecentTransfersSection(
                    transactions = transactions,
                    onSelectRecipient = { vpa, amount ->
                        payViewModel.prefillFromTransaction(vpa = vpa, amount = amount, note = "")
                        val digits = vpa.replace(Regex("[^0-9]"), "")
                        if (digits.length >= 10 && !vpa.contains("@")) {
                            onNavigateContactChat()
                        } else {
                            onNavigatePay()
                        }
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
 * Authentic PhonePe Circular Action Tile for Money Transfers.
 */
@Composable
private fun PhonePeActionTile(
    label: String,
    @DrawableRes iconResId: Int? = null,
    vectorIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
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
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            if (iconResId != null) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = label.replace("\n", " "),
                    modifier = Modifier.size(56.dp)
                )
            } else if (vectorIcon != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF670FC4), Color(0xFF5303A7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorIcon,
                        contentDescription = label.replace("\n", " "),
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            maxLines = 2
        )
    }
}

/**
 * Authentic PhonePe Service Tile inside Cards (e.g. Recharge & Bills, Loans).
 */
@Composable
private fun PhonePeServiceCardTile(
    label: String,
    vectorIcon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
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
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = vectorIcon,
                contentDescription = label.replace("\n", " "),
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            maxLines = 2
        )
    }
}

/**
 * Action Tile with circular 3D icon and 2-line label.
 */
@Composable
private fun AirpayActionTile(
    label: String,
    @DrawableRes iconResId: Int? = null,
    vectorIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
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
            if (vectorIcon != null) {
                Icon(
                    imageVector = vectorIcon,
                    contentDescription = label.replace("\n", " "),
                    tint = AirpayColors.Primary,
                    modifier = Modifier.size(26.dp)
                )
            } else if (iconResId != null) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = label.replace("\n", " "),
                    modifier = Modifier.size(34.dp)
                )
            }
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
            val context = LocalContext.current
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(transactions.take(8)) { txn ->
                    val displayName = remember(txn) {
                        ContactsHelper.resolveDisplayName(context, txn.vpa, txn.payeeName)
                    }
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
