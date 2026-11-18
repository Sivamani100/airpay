package com.arkiolabs.localpe.presentation.screens

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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.localpe.R
import com.arkiolabs.localpe.data.TransactionEntity
import com.arkiolabs.localpe.domain.OperationMode
import com.arkiolabs.localpe.presentation.HistoryViewModel
import com.arkiolabs.localpe.presentation.PayViewModel
import com.arkiolabs.localpe.presentation.permissions.PermissionStatus
import com.arkiolabs.localpe.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.localpe.presentation.ui.theme.Iconsax
import com.arkiolabs.localpe.presentation.ui.theme.LocalPeColors

/**
 * Authentic LocalPe Home Screen.
 *
 * Designed exactly to mirror the LocalPe screenshots:
 *  - Clean pure white background (#FFFFFF) with NO heavy boxed card wrappers.
 *  - Top hero banner area with Profile Avatar (mini QR badge), Mode Pill, and Help (?) icon.
 *  - "Money Transfers" 4 circular purple action items directly on white background.
 *  - "Refer -> ₹100" peach badge with golden coin.
 *  - Quick explore pills row side-by-side.
 *  - "Recharge & Bills" squircle grid with SIM delivery/status pill and "More ->" button.
 *  - "Recent Transfers" recipient bubbles without card borders.
 *  - 100% Offline & Private cellular voice guarantees with LocalPe security badges.
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
    val mode by payViewModel.operationMode.collectAsState()
    val transactions by historyViewModel.transactions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Main Scrollable Content ─────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top Bar & Hero Promotional Banner Area (LocalPe Style) ────
            HomeHeaderWithHeroBanner(
                mode = mode,
                onProfileClick = onNavigateMyQr,
                onHelpClick = onNavigateFaq,
                onBannerClick = onNavigateFaq
            )

            // ── Money Transfers Section (NO Card Wrapper, flat on white) ──
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                MoneyTransfersSection(
                    onToMobileClick = onNavigateToMobile,
                    onToBankClick = onNavigateToUpi,
                    onToSelfClick = onNavigateToUpi,
                    onCheckBalanceClick = onNavigateBalance
                )
            }

            // ── Quick Explore Pills Row (LocalPe side-by-side pills) ─────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionPill(
                    iconRes = R.drawable.ic_localpe_qr_icon_monochrome,
                    iconTint = LocalPeColors.Primary,
                    title = "Explore Offline UPI",
                    onClick = onNavigateFaq,
                    modifier = Modifier.weight(1f)
                )

                QuickActionPill(
                    iconRes = R.drawable.to_mobile_number,
                    iconTint = null,
                    title = "Daily SIM Pay @ ₹0 Fee",
                    onClick = onNavigateToMobile,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Subtle divider between sections
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFF4F4F6))
            )

            // ── Offline Utilities & Tools Section (Clean Flat LocalPe Grid) ──
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                OfflineUtilitiesSection(
                    onRechargeClick = onNavigateToMobile,
                    onQrClick = onNavigateMyQr,
                    onSettingsClick = onNavigateSettings,
                    onHelpClick = onNavigateFaq
                )
            }

            // Subtle divider between sections
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFF4F4F6))
            )

            // ── Recent Transfers / Beneficiaries ──────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                RecentTransfersSection(
                    transactions = transactions,
                    onSelectRecipient = { vpa, amount ->
                        payViewModel.prefillFromTransaction(vpa = vpa, amount = amount, note = "")
                        onNavigatePay()
                    },
                    onViewAll = onNavigateHistory
                )
            }

            // Subtle divider between sections
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFF4F4F6))
            )

            // ── 100% Offline & Private Security Guarantees ────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                SecurityAndCarrierSection(
                    mode = mode,
                    onManageSettings = onNavigateSettings
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Top App Bar + Hero Promotional Banner (Signature LocalPe Sky Blue Banner).
 */
@Composable
private fun HomeHeaderWithHeroBanner(
    mode: OperationMode,
    onProfileClick: () -> Unit,
    onHelpClick: () -> Unit,
    onBannerClick: () -> Unit
) {
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        // Hero Banner Image / Background
        Image(
            painter = painterResource(id = R.drawable.localpe_home_hero_banner),
            contentDescription = "LocalPe Offline UPI Banner",
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onBannerClick()
                },
            contentScale = ContentScale.Crop
        )

        // Top Row Overlay: Profile with mini QR badge, title/mode pill, and Help icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar with Mini QR Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onProfileClick()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.localpe_user_avatar),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Mini QR badge overlay
                Box(
                    modifier = Modifier
                        .size(17.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEEEEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_scan),
                        contentDescription = null,
                        tint = LocalPeColors.Primary,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Brand & Mode Badge
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LocalPe",
                    fontFamily = ClashGrotesk,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (mode == OperationMode.AUTO) "⚡ Auto SIM" else "⚪ Manual",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Help Icon (?) White Outline Circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onHelpClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_help_white),
                    contentDescription = "Help",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Signature LocalPe "Money Transfers" Section:
 *  - Flat on white background (NO card container, NO drop shadow).
 *  - Header: "Money Transfers" on left, "Refer -> ₹100" peach pill on right.
 *  - 4 authentic purple circular action buttons with labels below.
 */
@Composable
private fun MoneyTransfersSection(
    onToMobileClick: () -> Unit,
    onToBankClick: () -> Unit,
    onToSelfClick: () -> Unit,
    onCheckBalanceClick: () -> Unit
) {
    val view = LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Row: "Money Transfers" on left, "Refer -> ₹100" peach pill on right
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

            // Exact LocalPe Refer -> ₹100 peach pill with golden coin (shares APK via Bluetooth/Nearby/Files)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF3E0))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        com.arkiolabs.localpe.platform.ApkShareUtil.shareInstalledApk(context)
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coin_without_stars),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Refer → ₹100",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE65100)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // 4 Circular Buttons Row (Exact LocalPe 3D Vector Icons, Flat on White)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LocalPeActionTile(
                label = "To Mobile\nNumber",
                iconResId = R.drawable.to_mobile_number,
                onClick = onToMobileClick,
                modifier = Modifier.weight(1f)
            )

            LocalPeActionTile(
                label = "To Bank &\nSelf A/c",
                iconResId = R.drawable.to_bank_self_account,
                onClick = onToBankClick,
                modifier = Modifier.weight(1f)
            )

            LocalPeActionTile(
                label = "LocalPe\nWallet",
                badge = "Offer",
                iconResId = R.drawable.wallet_home,
                onClick = onToSelfClick,
                modifier = Modifier.weight(1f)
            )

            LocalPeActionTile(
                label = "Check\nBalance",
                iconResId = R.drawable.check_balance_v1,
                onClick = onCheckBalanceClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Authentic LocalPe Action Tile (Circular 3D Icon + Centered 2-line Label).
 */
@Composable
private fun LocalPeActionTile(
    label: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
    badge: String? = null,
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
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )

            // Optional Top Badge (e.g. "Offer" orange rounded badge)
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-4).dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF6D00))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            maxLines = 2
        )
    }
}

/**
 * Quick Action Mini Card Pill (LocalPe Explore style).
 */
@Composable
private fun QuickActionPill(
    @DrawableRes iconRes: Int,
    iconTint: Color?,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFEEEEEE), shape)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconTint != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1C1C1E),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Offline Utilities & Services Section: 4 squircle icons + 2 horizontal pills below.
 * Clean, flat LocalPe grid using LocalPe's own offline payment features.
 */
@Composable
private fun OfflineUtilitiesSection(
    onRechargeClick: () -> Unit,
    onQrClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Offline Utilities & Services",
            fontFamily = ClashGrotesk,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )

        Spacer(Modifier.height(14.dp))

        // 4 Squircle Items Row (LocalPe Features with Authentic LocalPe Vectors)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HomeSquircleItem(
                label = "Mobile\nRecharge",
                iconResId = R.drawable.home_mobile_recharge,
                onClick = onRechargeClick,
                modifier = Modifier.weight(1f)
            )

            HomeSquircleItem(
                label = "Receive\nvia QR",
                iconResId = R.drawable.ic_localpe_qr_icon_monochrome,
                onClick = onQrClick,
                modifier = Modifier.weight(1f)
            )

            HomeSquircleItem(
                label = "SIM &\nNetwork",
                iconResId = R.drawable.home_fastag,
                onClick = onSettingsClick,
                modifier = Modifier.weight(1f)
            )

            HomeSquircleItem(
                label = "Offline\nGuide",
                badge = "Help",
                iconResId = R.drawable.home_referandearn,
                onClick = onHelpClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // Two Pills below: SIM Delivery / Status + More ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pill: Carrier voice status (Exact LocalPe status style)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9F9FB))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                    .clickable { onSettingsClick() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIM Voice Channel: 100% Offline & Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1C1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(6.dp))

                Image(
                    painter = painterResource(id = R.drawable.home_mobile_recharge),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Right Pill: Settings ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9F9FB))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                    .clickable { onSettingsClick() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LocalPeColors.Primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "→",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalPeColors.Primary
                )
            }
        }
    }
}

/**
 * Authentic LocalPe Squircle Item with optional badge overlay.
 */
@Composable
private fun HomeSquircleItem(
    label: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
    badge: String? = null,
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
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF9F9FB))
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(34.dp),
                contentScale = ContentScale.Fit
            )

            // Optional Badge on top (e.g. "Latest" orange badge)
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF9800))
                        .padding(horizontal = 4.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            maxLines = 2
        )
    }
}

/**
 * Recent Transfers / Beneficiaries Section (Flat on white).
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
                color = Color(0xFF1C1C1E)
            )

            Text(
                text = "View All →",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalPeColors.Primary,
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LocalPeColors.PurpleTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Iconsax.Call,
                        contentDescription = null,
                        tint = LocalPeColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "No recent transfers yet",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "Send money to any mobile or UPI ID to get started.",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
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
    val initial = recipientName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
            .width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LocalPeColors.PurpleTint)
                .border(1.dp, LocalPeColors.Primary.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontFamily = ClashGrotesk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LocalPeColors.Primary
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = recipientName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "₹$amount",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF757575)
        )
    }
}

/**
 * 100% Offline & Private Security Guarantees + LocalPe Trust Badges.
 */
@Composable
private fun SecurityAndCarrierSection(
    mode: OperationMode,
    onManageSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Iconsax.ShieldSecurity,
                    contentDescription = null,
                    tint = LocalPeColors.SuccessGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "100% Offline & Private",
                    fontFamily = ClashGrotesk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }

            Text(
                text = "Settings →",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalPeColors.Primary,
                modifier = Modifier.clickable { onManageSettings() }
            )
        }

        Spacer(Modifier.height(10.dp))

        // Carrier readiness pills (All 4 Major Indian Networks Supported)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CarrierPill("Airtel", supported = true, modifier = Modifier.weight(1f))
            CarrierPill("Vi", supported = true, modifier = Modifier.weight(1f))
            CarrierPill("BSNL", supported = true, modifier = Modifier.weight(1f))
            CarrierPill("Jio", supported = true, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        GuaranteeItem(
            iconResId = R.drawable.ic_lock,
            title = "UPI PIN never persists",
            desc = "Held only in volatile memory during session, wiped within 500ms."
        )

        Spacer(Modifier.height(8.dp))

        GuaranteeItem(
            iconResId = R.drawable.ic_tick_ok,
            title = "Zero outbound network requests",
            desc = "All communication is routed strictly through secure cellular voice signaling."
        )

        Spacer(Modifier.height(14.dp))

        // LocalPe Offline Security Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF6F7F9))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Iconsax.ShieldSecurity,
                contentDescription = null,
                tint = LocalPeColors.Primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "LocalPe Secure • 100% Offline Cellular Protection",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF424242)
            )
        }
    }
}

@Composable
private fun CarrierPill(name: String, supported: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (supported) LocalPeColors.SuccessTint else LocalPeColors.DangerTint)
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (supported) "$name ✓" else "$name ✗",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (supported) LocalPeColors.SuccessGreen else LocalPeColors.DangerRed
        )
    }
}

@Composable
private fun GuaranteeItem(@DrawableRes iconResId: Int, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(15.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = Color(0xFF757575),
                lineHeight = 14.sp
            )
        }
    }
}
