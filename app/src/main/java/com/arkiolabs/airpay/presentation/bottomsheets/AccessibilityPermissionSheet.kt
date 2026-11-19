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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import com.arkiolabs.airpay.presentation.permissions.openAccessibilitySettingsWithGuide
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Dedicated Bottom Sheet screen for the Airpay USSD Accessibility Service.
 * Automates background USSD menu navigation for smooth, hands-free offline payments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityPermissionSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BaseBottomSheetContainer(
        title = "Automated USSD Service",
        onDismissRequest = onDismiss,
        primaryButtonText = "Open Accessibility Settings",
        onPrimaryClick = {
            openAccessibilitySettingsWithGuide(context)
        },
        secondaryButtonText = "Maybe Later",
        onSecondaryClick = onDismiss,
        modifier = modifier
    ) {
        // Hero Icon Badge (Deep Dark Navy with Emerald accent)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AirpayColors.Primary)
                .border(3.dp, AirpayColors.SuccessGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = AirpayColors.SuccessGreen,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AirpayColors.SuccessTint)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "HANDS-FREE OFFLINE AUTOMATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.SuccessGreen,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Automate *99# Banking Menus",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AirpayColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Airpay Accessibility Service automatically fills recipient UPI ID and amount through carrier USSD menus in the background, so you only have to enter your UPI PIN.",
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
                text = "Eliminates tedious manual USSD number typing"
            )
            PermissionBenefitRow(
                text = "100% on-device automated session handling"
            )
            PermissionBenefitRow(
                text = "PIN is never stored: you enter it securely each time"
            )
        }

        Spacer(Modifier.height(14.dp))

        // Android 13+ Guide Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFF8E1))
                .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Android 13+ Hint: If toggle is greyed out: Settings → Apps → Airpay → ⋮ (top right) → Allow restricted settings.",
                fontSize = 11.sp,
                color = Color(0xFF5D4037),
                lineHeight = 15.sp
            )
        }
    }
}
