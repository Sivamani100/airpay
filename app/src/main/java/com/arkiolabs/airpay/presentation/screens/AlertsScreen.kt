package com.arkiolabs.airpay.presentation.screens

import android.view.HapticFeedbackConstants
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

data class AlertNotificationItem(
    val id: String,
    @DrawableRes val iconRes: Int,
    val iconBg: Color,
    val iconTint: Color?,
    val title: String,
    val message: String,
    val actionText: String? = null,
    val time: String
)

/**
 * Authentic Airpay Styled Alerts & Notifications Screen.
 *
 * Implements the exact layout from Airpay screenshot:
 *  - Header: "Alerts" on left, Help (?) icon on right.
 *  - Flat notification rows with thin dividers (NO heavy boxed cards).
 *  - Clean close (X) and relative time indicators ("1d", "2d").
 *  - Bottom "OFFER ZONE" with three promotional cards.
 */
@Composable
fun AlertsScreen(
    onOpenFaq: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    val alerts = remember {
        mutableStateListOf(
            AlertNotificationItem(
                id = "1",
                iconRes = R.drawable.bill_notifications,
                iconBg = AirpayColors.PurpleTint,
                iconTint = AirpayColors.Primary,
                title = "SIM Voice Channel Ready",
                message = "Offline UPI is active over secure cellular SIM. Payments and balance queries work without internet.",
                actionText = "Test connection",
                time = "1d"
            ),
            AlertNotificationItem(
                id = "2",
                iconRes = R.drawable.ic_tick_ok,
                iconBg = AirpayColors.SuccessTint,
                iconTint = null,
                title = "Auto Mode Active",
                message = "Carrier USSD sessions will be handled smoothly in the background by the secure overlay.",
                actionText = "Manage settings",
                time = "1d"
            ),
            AlertNotificationItem(
                id = "3",
                iconRes = R.drawable.outline_lock,
                iconBg = AirpayColors.PurpleTint,
                iconTint = AirpayColors.Primary,
                title = "Zero-Persistence Security Guarantee",
                message = "Your UPI PIN is held strictly in volatile RAM and securely wiped within 500ms after every payment.",
                actionText = "Learn more",
                time = "2d"
            ),
            AlertNotificationItem(
                id = "4",
                iconRes = R.drawable.ic_to_bank_account,
                iconBg = AirpayColors.SkyBlueTint,
                iconTint = AirpayColors.SkyBlueDark,
                title = "One-Time Setup on Airpay",
                message = "Ensure your bank account is linked to your SIM card in your phone's UPI settings.",
                actionText = "View guide",
                time = "3d"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Top Bar Header (Airpay Style) ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Alerts",
                fontSize = 22.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onOpenFaq()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_help_white),
                    contentDescription = "Help",
                    tint = Color(0xFF1C1C1E),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ── Flat Notification List with Dividers ────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(alerts.size, key = { alerts[it].id }) { index ->
                val item = alerts[index]
                AlertNotificationRow(
                    item = item,
                    onDismiss = { alerts.removeAt(index) },
                    onActionClick = {
                        if (item.actionText == "Manage settings") onOpenSettings() else onOpenFaq()
                    }
                )
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }

            // ── OFFER ZONE Section ──────────────────────────────────
            item {
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "OFFER ZONE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575),
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OfferZoneCard(
                        title = "Offers for you",
                        subtitle = "Exciting offers",
                        icon = Icons.Default.LocalOffer,
                        iconTint = Color(0xFF00875A),
                        onClick = onOpenFaq,
                        modifier = Modifier.weight(1f)
                    )

                    OfferZoneCard(
                        title = "Most Popular",
                        subtitle = "Top picks today",
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFFFF9800),
                        onClick = onOpenFaq,
                        modifier = Modifier.weight(1f)
                    )

                    OfferZoneActionCard(
                        title = "All Offers",
                        onClick = onOpenFaq,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AlertNotificationRow(
    item: AlertNotificationItem,
    onDismiss: () -> Unit,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActionClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(item.iconBg),
            contentAlignment = Alignment.Center
        ) {
            if (item.iconTint != null) {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Image(
                    painter = painterResource(item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        // Text Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = item.message,
                fontSize = 12.sp,
                color = Color(0xFF555555),
                lineHeight = 17.sp
            )

            if (item.actionText != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.actionText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AirpayColors.Primary
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Time & Dismiss (X)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color(0xFF9E9E9E),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDismiss() }
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = item.time,
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
private fun OfferZoneCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFEEEEEE), shape)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )

        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun OfferZoneActionCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFEEEEEE), shape)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AirpayColors.PurpleTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AirpayColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AirpayColors.Primary
        )

        Text(
            text = "View details",
            fontSize = 10.sp,
            color = Color(0xFF757575)
        )
    }
}
