package com.arkiolabs.airpay.presentation.bottomsheets

import android.provider.Settings
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.arkiolabs.airpay.platform.UssdAccessibilityService
import com.arkiolabs.airpay.presentation.permissions.openAccessibilitySettingsWithGuide
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Dedicated Bottom Sheet screen for Automated Payment Service.
 * - Non-dismissible until configured.
 * - EXACTLY 3 points explaining why service is required.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityPermissionSheet(
    onConfigured: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var hasAttempted by remember { mutableStateOf(false) }

    fun checkEnabled(): Boolean {
        val expected = "${context.packageName}/${UssdAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(":").any { it.equals(expected, ignoreCase = true) }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (checkEnabled()) {
                    onConfigured()
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    BaseBottomSheetContainer(
        title = "Automated Fast Payments",
        primaryButtonText = if (hasAttempted) "Open Accessibility Settings Again" else "Open Accessibility Settings",
        onPrimaryClick = {
            hasAttempted = true
            openAccessibilitySettingsWithGuide(context)
        },
        modifier = modifier
    ) {
        // Hero Icon Badge (Deep Dark Navy with Emerald accent)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AirpayColors.Primary)
                .border(2.5.dp, AirpayColors.SuccessGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = AirpayColors.SuccessGreen,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // EXACTLY 3 points
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF9FAFB))
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ThreePointBenefitRow(
                pointNumber = "1",
                text = "Automates payment steps seamlessly in the background"
            )
            ThreePointBenefitRow(
                pointNumber = "2",
                text = "Enters recipient UPI ID and amount automatically"
            )
            ThreePointBenefitRow(
                pointNumber = "3",
                text = "You only enter your secure UPI PIN to confirm"
            )
        }

        if (hasAttempted && !checkEnabled()) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFEBEE))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AirpayColors.DangerRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Accessibility service is not active yet. Please find Airpay Fast Payment Automation in settings and toggle it ON.",
                    fontSize = 12.sp,
                    color = AirpayColors.DangerRed,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
