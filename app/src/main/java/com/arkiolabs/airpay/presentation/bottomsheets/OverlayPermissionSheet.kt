package com.arkiolabs.airpay.presentation.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.presentation.permissions.openOverlaySettings
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Dedicated Bottom Sheet screen for Overlay Permission (SYSTEM_ALERT_WINDOW).
 * Allows Airpay to display a live payment progress card over carrier USSD popups.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayPermissionSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BaseBottomSheetContainer(
        title = "Display Over Other Apps",
        onDismissRequest = onDismiss,
        primaryButtonText = "Grant Overlay Permission",
        onPrimaryClick = {
            openOverlaySettings(context)
        },
        secondaryButtonText = "Maybe Later",
        onSecondaryClick = onDismiss,
        modifier = modifier
    ) {
        // Hero Icon Badge (Deep Dark Navy with Saffron accent)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AirpayColors.Primary)
                .border(3.dp, AirpayColors.SaffronLight.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = AirpayColors.Saffron,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AirpayColors.SaffronTint)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "CLEAN PAYMENT EXPERIENCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.SaffronDark,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Show Clean Offline Payment Card",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AirpayColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Airpay displays an elegant payment progress overlay directly over system dialer popups so your transactions feel just like a modern app instead of raw USSD screens.",
            fontSize = 13.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF9FAFB))
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PermissionBenefitRow(
                text = "Hides carrier popups during transactions"
            )
            PermissionBenefitRow(
                text = "Displays real-time transfer progress timer"
            )
            PermissionBenefitRow(
                text = "Instant 1-tap cancel button if needed"
            )
        }
    }
}
