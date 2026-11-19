package com.arkiolabs.airpay.presentation.screens

import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import com.arkiolabs.airpay.presentation.ui.components.JioRegistrationSheet
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.data.PreferencesRepository
import com.arkiolabs.airpay.domain.OperationMode
import com.arkiolabs.airpay.platform.BiometricHelper
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.permissions.PermissionStatus
import com.arkiolabs.airpay.presentation.permissions.openAccessibilitySettings
import com.arkiolabs.airpay.presentation.permissions.openOverlaySettings
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import kotlinx.coroutines.launch

/**
 * Authentic Airpay Styled Settings Screen.
 *
 * Implements the exact layout from Airpay screenshots:
 *  - Promo Banner: "Did you know? Passkeys enable OTP-free login ->"
 *  - Section "Payment Settings":
 *      - AutoPay >
 *      - Pay with Fingerprint (Biometric toggle / setup) >
 *      - International >
 *      - UPI Settings >
 *  - Section "Preferences & Security":
 *      - Preferences & Security >
 *      - Shake to Scan (Switch toggle)
 *      - Custom Transaction Sounds
 *      - Operation Mode (Auto / Manual SIM Tunnel)
 *      - Refer and earn ₹100 >
 *      - Help and support >
 *      - About Airpay >
 *  - Log out in red.
 */
@Composable
fun SettingsScreen(
    prefsRepo: PreferencesRepository,
    historyViewModel: HistoryViewModel,
    permissions: PermissionStatus,
    versionName: String,
    onClearAllData: () -> Unit,
    onOpenFaq: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenLegal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mode by prefsRepo.operationMode.collectAsState(initial = OperationMode.AUTO)
    val biometricEnabled by prefsRepo.biometricEnabled.collectAsState(initial = false)
    val successSoundUri by prefsRepo.successSoundUri.collectAsState(initial = null)
    val failedSoundUri by prefsRepo.failedSoundUri.collectAsState(initial = null)
    val jioRegistered by prefsRepo.jio123PayRegistered.collectAsState(initial = false)
    val userBankName by prefsRepo.userBankName.collectAsState(initial = null)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current

    var shakeToScan by remember { mutableStateOf(true) }
    var showJioRegistrationSheet by remember { mutableStateOf(false) }

    // Audio pickers
    val successAudioPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch { prefsRepo.setSuccessSoundUri(uri.toString()) }
            Toast.makeText(context, "Success sound updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val failedAudioPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch { prefsRepo.setFailedSoundUri(uri.toString()) }
            Toast.makeText(context, "Failed sound updated!", Toast.LENGTH_SHORT).show()
        }
    }

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onOpenFaq() },
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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Passkey Promo Banner (Airpay Style) ─────────────────
            val bannerShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(bannerShape)
                    .background(Color(0xFF1B0A33)) // Deep Airpay Purple Banner
                    .clickable { onOpenFaq() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Did you know?",
                            fontSize = 11.sp,
                            color = Color(0xFFCE93D8)
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Passkeys enable OTP-free login",
                                fontSize = 14.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("→", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 1: Payment Settings ─────────────────────────
            SectionHeader(title = "Payment Settings")

            SettingsRow(
                icon = Icons.Default.Settings,
                iconBg = AirpayColors.SaffronTint,
                iconTint = AirpayColors.SaffronDark,
                title = "AutoPay",
                subtitle = "Manage your IPOs, SIPs & recurring payments",
                onClick = onOpenFaq
            )

            SettingsRow(
                icon = Icons.Default.Fingerprint,
                iconBg = AirpayColors.SuccessTint,
                iconTint = AirpayColors.SuccessGreen,
                title = "Pay with Fingerprint",
                subtitle = if (biometricEnabled) "Enabled · Verify fingerprint without UPI PIN" else "Disabled · Tap to set up biometric pay",
                onClick = {
                    (context as? FragmentActivity)?.let { act ->
                        BiometricHelper.prompt(
                            activity = act,
                            title = "Biometric Authorization",
                            subtitle = "Verify fingerprint to toggle biometric pay",
                            onSuccess = {
                                scope.launch { prefsRepo.setBiometricEnabled(!biometricEnabled) }
                                Toast.makeText(context, if (!biometricEnabled) "Biometric Enabled" else "Biometric Disabled", Toast.LENGTH_SHORT).show()
                            },
                            onError = { Toast.makeText(context, "Verification failed", Toast.LENGTH_SHORT).show() },
                            onCancel = {}
                        )
                    }
                }
            )

            SettingsRow(
                icon = Icons.Default.Language,
                iconBg = Color(0xFFE1F5FE),
                iconTint = Color(0xFF0288D1),
                title = "International",
                subtitle = "Scan & Pay at Global Merchants",
                onClick = onOpenFaq
            )

            SettingsRow(
                icon = Icons.Default.Security,
                iconBg = Color(0xFFE8F5E9),
                iconTint = Color(0xFF2E7D32),
                title = "UPI Settings",
                subtitle = "Manage UPI IDs and UPI Number",
                onClick = onOpenHistory
            )

            SettingsRow(
                icon = Icons.Default.PhoneAndroid,
                iconBg = AirpayColors.SaffronTint,
                iconTint = AirpayColors.Primary,
                title = "Jio Offline UPI 123PAY",
                subtitle = if (jioRegistered) "Linked · ${userBankName ?: "Primary Bank"} (080 4516 3666)" else "Not Linked · Tap to link your bank account",
                onClick = { showJioRegistrationSheet = true }
            )

            // Section divider
            Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFF4F4F6)))

            // ── Section 2: Preferences & Security ───────────────────
            SectionHeader(title = "Preferences & Security")

            SettingsRow(
                icon = Icons.Default.Notifications,
                iconBg = Color(0xFFFFF3E0),
                iconTint = Color(0xFFE65100),
                title = "Preferences & Security",
                subtitle = "Languages, reminders, permissions, themes, screen lock",
                onClick = {
                    if (!permissions.accessibility) openAccessibilitySettings(context)
                    else if (!permissions.overlay) openOverlaySettings(context)
                    else Toast.makeText(context, "All required permissions granted!", Toast.LENGTH_SHORT).show()
                }
            )

            // Shake to Scan with toggle switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { shakeToScan = !shakeToScan }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F5F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shake to Scan",
                        fontSize = 14.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                }

                Switch(
                    checked = shakeToScan,
                    onCheckedChange = { shakeToScan = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AirpayColors.Primary
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // Custom Sounds row (Airpay feature)
            SettingsRow(
                icon = Icons.Default.MusicNote,
                iconBg = AirpayColors.SaffronTint,
                iconTint = AirpayColors.Primary,
                title = "Transaction Sounds",
                subtitle = if (successSoundUri != null) "Custom sound active" else "Default Airpay chime",
                onClick = { successAudioPicker.launch("audio/*") }
            )

            // Operation Mode switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val next = if (mode == OperationMode.AUTO) OperationMode.MANUAL else OperationMode.AUTO
                        scope.launch { prefsRepo.setOperationMode(next) }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 16.sp)
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Automated SIM Session",
                        fontSize = 14.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = if (mode == OperationMode.AUTO) "Auto USSD dialog handling active" else "Manual dialer fallback mode",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Switch(
                    checked = mode == OperationMode.AUTO,
                    onCheckedChange = { isAuto ->
                        scope.launch {
                            prefsRepo.setOperationMode(if (isAuto) OperationMode.AUTO else OperationMode.MANUAL)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AirpayColors.Primary
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            SettingsRow(
                icon = Icons.Default.Security,
                iconBg = Color(0xFFF6F7F9),
                iconTint = Color(0xFF1C1C1E),
                title = "Refer and earn ₹100",
                subtitle = "Invite friends to offline UPI",
                onClick = onOpenHistory
            )

            SettingsRow(
                icon = Icons.Default.Settings,
                iconBg = Color(0xFFF6F7F9),
                iconTint = Color(0xFF1C1C1E),
                title = "Help and support",
                subtitle = "Guides, FAQs, and carrier setup",
                onClick = onOpenFaq
            )

            SettingsRow(
                icon = Icons.Default.Security,
                iconBg = Color(0xFFF6F7F9),
                iconTint = Color(0xFF1C1C1E),
                title = "About Airpay",
                subtitle = "Version $versionName · 100% Offline UPI",
                onClick = onOpenLegal
            )

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // ── Log Out in Red (Airpay Style) ──────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "Offline session reset", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Log out",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "Log out",
                    fontSize = 15.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(Modifier.height(30.dp))
        }

        if (showJioRegistrationSheet) {
            JioRegistrationSheet(
                onDismiss = { showJioRegistrationSheet = false },
                onRegistrationConfirmed = { bank, lang ->
                    scope.launch {
                        prefsRepo.setJio123PayRegistered(true)
                        prefsRepo.setUserBankName(bank)
                        prefsRepo.setUserPreferredLanguage(lang)
                    }
                    showJioRegistrationSheet = false
                    Toast.makeText(context, "Bank account linked with Jio 123PAY!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontFamily = ClashGrotesk,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1C1C1E),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun SettingsRow(
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
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
                fontSize = 14.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(18.dp)
        )
    }
}
