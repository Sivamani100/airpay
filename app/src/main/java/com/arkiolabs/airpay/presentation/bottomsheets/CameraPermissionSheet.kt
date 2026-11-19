package com.arkiolabs.airpay.presentation.bottomsheets

import android.Manifest
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.presentation.permissions.openAppDetailsSettings
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Dedicated Bottom Sheet screen for Camera permission.
 * Enables scanning merchant Bharat QR and peer UPI QR codes offline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPermissionSheet(
    onGranted: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasDeniedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onGranted()
        } else {
            hasDeniedOnce = true
        }
    }

    BaseBottomSheetContainer(
        title = "Camera Access for QR Scan",
        onDismissRequest = onDismiss,
        primaryButtonText = if (hasDeniedOnce) "Open App Settings" else "Grant Camera Access",
        onPrimaryClick = {
            if (hasDeniedOnce) {
                openAppDetailsSettings(context)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        secondaryButtonText = if (hasDeniedOnce) "Try Again" else "Maybe Later",
        onSecondaryClick = {
            if (hasDeniedOnce) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                onDismiss()
            }
        },
        modifier = modifier
    ) {
        // Hero Icon Badge (Emerald Green from official logo)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AirpayColors.SuccessGreen)
                .border(3.dp, Color(0xFF81C784).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_qr_scan),
                contentDescription = null,
                tint = Color.White,
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
                text = "FAST 1-TAP OFFLINE PAYMENTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.SuccessGreen,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Scan Any UPI QR Code Offline",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AirpayColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Airpay scans merchant and friend QR codes on-device to instantly parse payment UPI IDs without needing any internet connection.",
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
                text = "Scan any shop or merchant Bharat QR code"
            )
            PermissionBenefitRow(
                text = "Extract recipient UPI address instantly offline"
            )
            PermissionBenefitRow(
                text = "Zero photos saved, 100% private on-device camera"
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
                    text = "Camera permission was not granted. Tap 'Open App Settings' to grant camera access manually.",
                    fontSize = 12.sp,
                    color = AirpayColors.DangerRed,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
