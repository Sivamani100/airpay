package com.arkiolabs.airpay.presentation.bottomsheets

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.presentation.permissions.openAppDetailsSettings
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Dedicated Bottom Sheet screen for Contacts Permission.
 * - Non-dismissible until granted.
 * - EXACTLY 3 points explaining why permission is required.
 * - Tapping action button ALWAYS triggers the Android permission dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPermissionSheet(
    onGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasDenied by remember { mutableStateOf(false) }
    var denialCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onGranted()
        } else {
            hasDenied = true
            denialCount++
        }
    }

    BaseBottomSheetContainer(
        title = "Contacts Access",
        primaryButtonText = "Allow Contacts Access",
        onPrimaryClick = {
            // ALWAYS re-launch the Android permission dialog
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        },
        modifier = modifier
    ) {
        // Hero Icon Badge (Deep Dark Navy with Saffron accent)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AirpayColors.Primary)
                .border(2.5.dp, AirpayColors.SaffronLight.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = AirpayColors.Saffron,
                modifier = Modifier.size(38.dp)
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
                text = "Quickly select friends and merchants from your phone contacts"
            )
            ThreePointBenefitRow(
                pointNumber = "2",
                text = "Send money directly to any mobile number without typing 10 digits"
            )
            ThreePointBenefitRow(
                pointNumber = "3",
                text = "100% private: contacts are searched strictly on-device, never uploaded"
            )
        }

        // Red denial notice
        if (hasDenied) {
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
                    text = "Contacts permission is needed to pay your phone contacts. Please tap Allow in the system dialog.",
                    fontSize = 12.sp,
                    color = AirpayColors.DangerRed,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }

            if (denialCount >= 2) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "If dialog doesn't appear, tap to open Settings",
                    fontSize = 12.sp,
                    color = AirpayColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { openAppDetailsSettings(context) }
                        .padding(4.dp)
                )
            }
        }
    }
}
