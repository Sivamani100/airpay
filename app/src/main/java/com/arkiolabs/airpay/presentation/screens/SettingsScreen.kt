package com.arkiolabs.airpay.presentation.screens

import android.app.Activity
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.data.PreferencesRepository
import com.arkiolabs.airpay.platform.ApkShareUtil
import com.arkiolabs.airpay.platform.BiometricHelper
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.bottomsheets.LogoutConfirmationSheet
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import kotlinx.coroutines.launch

/**
 * Streamlined Airpay Settings Screen.
 * Strictly presents the 6 requested features:
 *  1. Pay with Fingerprint
 *  2. Shake to Scan (+ Open Scanner on Launch)
 *  3. Share with a Friend
 *  4. Support Us
 *  5. About Airpay
 *  6. Log Out (with Confirmation Bottom Sheet)
 */
@Composable
fun SettingsScreen(
    prefsRepo: PreferencesRepository,
    historyViewModel: HistoryViewModel,
    onOpenSupport: () -> Unit,
    onOpenAbout: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val biometricEnabled by prefsRepo.biometricEnabled.collectAsState(initial = false)
    val shakeToScanEnabled by prefsRepo.shakeToScanEnabled.collectAsState(initial = false)
    val openScannerOnLaunch by prefsRepo.openScannerOnLaunch.collectAsState(initial = false)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current

    var showLogoutSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Top Header ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F5F8))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AirpayColors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Item 1: Pay with Fingerprint ────────────────────────
            SettingsToggleRow(
                icon = Icons.Default.Fingerprint,
                iconBg = AirpayColors.SuccessTint,
                iconTint = AirpayColors.SuccessGreen,
                title = "Pay with Fingerprint",
                subtitle = if (biometricEnabled) {
                    "Enabled · Verify fingerprint to authorize offline payments"
                } else {
                    "Disabled · Tap to authorize payments with your fingerprint"
                },
                checked = biometricEnabled,
                onCheckedChange = { targetChecked ->
                    if (targetChecked) {
                        (context as? FragmentActivity)?.let { activity ->
                            BiometricHelper.prompt(
                                activity = activity,
                                title = "Enable Biometric Pay",
                                subtitle = "Verify fingerprint to enable biometric offline payments",
                                onSuccess = {
                                    scope.launch { prefsRepo.setBiometricEnabled(true) }
                                    Toast.makeText(context, "Pay with Fingerprint enabled!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, "Biometric error: $err", Toast.LENGTH_SHORT).show()
                                },
                                onCancel = {}
                            )
                        } ?: run {
                            scope.launch { prefsRepo.setBiometricEnabled(true) }
                        }
                    } else {
                        scope.launch { prefsRepo.setBiometricEnabled(false) }
                        Toast.makeText(context, "Biometric pay disabled", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)

            // ── Item 2: Shake to Scan ───────────────────────────────
            SettingsToggleRow(
                icon = Icons.Default.Vibration,
                iconBg = AirpayColors.SaffronTint,
                iconTint = AirpayColors.Primary,
                title = "Shake to Scan",
                subtitle = "Shake phone anywhere to open QR scanner instantly",
                checked = shakeToScanEnabled,
                onCheckedChange = { checked ->
                    scope.launch { prefsRepo.setShakeToScanEnabled(checked) }
                }
            )

            // Sub-item: Open Scanner on App Launch
            SettingsToggleRow(
                icon = Icons.Default.Vibration,
                iconBg = Color(0xFFF4F5F8),
                iconTint = Color(0xFF757575),
                title = "Open Scanner on Launch",
                subtitle = "Open QR scanner automatically when app opens",
                checked = openScannerOnLaunch,
                onCheckedChange = { checked ->
                    scope.launch { prefsRepo.setOpenScannerOnLaunch(checked) }
                },
                isSubItem = true
            )

            HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)

            // ── Item 3: Share with a Friend ─────────────────────────
            SettingsClickableRow(
                icon = Icons.Default.Share,
                iconBg = Color(0xFFE1F5FE),
                iconTint = Color(0xFF0288D1),
                title = "Share with a Friend",
                subtitle = "Share Airpay APK for 100% offline payments",
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    ApkShareUtil.shareInstalledApk(context)
                }
            )

            HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)

            // ── Item 4: Support Us ──────────────────────────────────
            SettingsClickableRow(
                icon = Icons.Default.Favorite,
                iconBg = Color(0xFFFFEBEE),
                iconTint = Color(0xFFE91E63),
                title = "Support Us",
                subtitle = "Help India pay offline, merchant advocacy & feedback",
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onOpenSupport()
                }
            )

            HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)

            // ── Item 5: About Airpay ────────────────────────────────
            SettingsClickableRow(
                icon = Icons.Default.Info,
                iconBg = Color(0xFFEDE7F6),
                iconTint = Color(0xFF512DA8),
                title = "About Airpay",
                subtitle = "Version 2.4.0 · Zero-Internet Architecture & Privacy",
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onOpenAbout()
                }
            )

            HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)

            // ── Item 6: Log Out in Red ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        showLogoutSheet = true
                    }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Log out",
                        tint = AirpayColors.DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Log Out",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = AirpayColors.DangerRed
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Clear all data and reset app as a fresh installation",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        // ── Logout Confirmation Bottom Sheet ───────────────────────
        if (showLogoutSheet) {
            LogoutConfirmationSheet(
                onConfirmLogout = {
                    showLogoutSheet = false
                    scope.launch {
                        // 1. Wipe all DataStore preferences
                        prefsRepo.clearAllData()

                        // 2. Wipe fast cache SharedPreferences
                        val cachedPrefs = context.getSharedPreferences("airpay_fast_cache", Context.MODE_PRIVATE)
                        cachedPrefs.edit().clear().commit()

                        // 3. Clear Room database transaction records
                        historyViewModel.clearHistory()

                        Toast.makeText(context, "Airpay reset successfully", Toast.LENGTH_SHORT).show()

                        // 4. Cleanly exit the app so next launch opens fresh as new
                        (context as? Activity)?.finishAffinity()
                    }
                },
                onDismiss = { showLogoutSheet = false }
            )
        }
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = AirpayColors.TextSecondary,
                lineHeight = 16.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isSubItem: Boolean = false
) {
    val view = LocalView.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onCheckedChange(!checked)
            }
            .padding(
                start = if (isSubItem) 28.dp else 18.dp,
                end = 18.dp,
                top = 13.dp,
                bottom = 13.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isSubItem) 34.dp else 40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(if (isSubItem) 17.dp else 20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = if (isSubItem) 13.5.sp else 14.5.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = AirpayColors.TextSecondary,
                lineHeight = 15.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AirpayColors.Primary,
                uncheckedTrackColor = Color(0xFFE0E0E0),
                uncheckedThumbColor = Color.White
            )
        )
    }
}
