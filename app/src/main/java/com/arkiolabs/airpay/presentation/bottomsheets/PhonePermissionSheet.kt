package com.arkiolabs.airpay.presentation.bottomsheets

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.presentation.permissions.openAppDetailsSettings
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Dedicated Bottom Sheet screen for Phone & Call state permissions.
 * Essential for dialing *99# USSD codes offline without internet access.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhonePermissionSheet(
    onGranted: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasDeniedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            onGranted()
        } else {
            hasDeniedOnce = true
        }
    }

    BaseBottomSheetContainer(
        title = "Phone & Call Permission",
        onDismissRequest = onDismiss,
        primaryButtonText = if (hasDeniedOnce) "Open App Settings" else "Grant Phone Permission",
        onPrimaryClick = {
            if (hasDeniedOnce) {
                openAppDetailsSettings(context)
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE
                    )
                )
            }
        },
        secondaryButtonText = if (hasDeniedOnce) "Try Again" else "Maybe Later",
        onSecondaryClick = {
            if (hasDeniedOnce) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE
                    )
                )
            } else {
                onDismiss()
            }
        },
        modifier = modifier
    ) {
        // Visual Hero Icon Badge (Deep Dark Navy circle with Saffron accent)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AirpayColors.Primary)
                .border(3.dp, AirpayColors.SaffronLight.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Status pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AirpayColors.SaffronTint)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "CRITICAL FOR OFFLINE UPI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.SaffronDark,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Allow Airpay to Make & Manage Calls",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AirpayColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Airpay routes payments directly through national telecom towers using *99# USSD codes with zero internet or data pack. Phone permission is required to dial payment codes securely.",
            fontSize = 13.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(18.dp))

        // Key Benefits Card
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
                text = "Dial *99# USSD codes directly without internet"
            )
            PermissionBenefitRow(
                text = "Detect linked bank SIM automatically"
            )
            PermissionBenefitRow(
                text = "100% on-device telecom routing, zero web tracking"
            )
        }

        if (hasDeniedOnce) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFEBEE))
                    .padding(10.dp),
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
                    text = "Permission was not granted. Tap 'Open App Settings' to grant Phone permissions manually.",
                    fontSize = 12.sp,
                    color = AirpayColors.DangerRed,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun PermissionBenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(AirpayColors.SuccessGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AirpayColors.TextPrimary
        )
    }
}
