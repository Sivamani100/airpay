package com.arkiolabs.airpay.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.presentation.ServicesViewModel
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax

/**
 * Profile / My Details Screen (Dedicated Bottom Navigation Tab).
 *
 * Implements:
 *  - Auto-fetches details from *99*4# under branded overlay if not yet populated.
 *  - Displays User Name, UPI ID (with copy), and Bank Name.
 *  - Masked Account Number with Eye icon to toggle full view.
 *  - Syncs User Name directly to the My QR screen.
 */
@Composable
fun ProfileScreen(
    servicesViewModel: ServicesViewModel,
    onNavigateMyQr: () -> Unit,
    onNavigateSettings: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val app = context.airpayApp
    val scope = rememberCoroutineScope()
    val session by servicesViewModel.sessionState.collectAsState()

    val userName by app.prefsRepo.userName.collectAsState(initial = null)
    val userUpiId by app.prefsRepo.userUpiId.collectAsState(initial = null)
    val userBankName by app.prefsRepo.userBankName.collectAsState(initial = null)
    val userAccountMasked by app.prefsRepo.userAccountMasked.collectAsState(initial = null)
    val userAccountFull by app.prefsRepo.userAccountFull.collectAsState(initial = null)
    val lastProfileTimestamp by app.prefsRepo.lastProfileTimestamp.collectAsState(initial = null)

    var isAccountUnmasked by remember { mutableStateOf(false) }
    var showEditAccountDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    // 4-Day Cache Cycle:
    // When newly opened (no local data) OR if last sync was > 4 days ago, auto-fetch profile.
    // Otherwise, immediately use the saved offline local storage details.
    val fourDaysMs = 4L * 24 * 60 * 60 * 1000L
    LaunchedEffect(lastProfileTimestamp, userName, userUpiId) {
        val now = System.currentTimeMillis()
        val isDataEmpty = userName.isNullOrBlank() && userUpiId.isNullOrBlank()
        val isExpired = lastProfileTimestamp == null || (now - lastProfileTimestamp!!) > fourDaysMs
        if (isDataEmpty || isExpired) {
            servicesViewModel.fetchMyDetails()
        }
    }

    val displayName = userName?.ifBlank { null } ?: "Airpay User"
    val displayUpi = userUpiId?.ifBlank { null } ?: "Not Registered"
    val displayBank = userBankName?.ifBlank { null } ?: "Offline SIM Bank"

    // Account display formatting:
    val displayAccount = if (isAccountUnmasked) {
        if (!userAccountFull.isNullOrBlank() && userAccountFull!!.length > 4) {
            userAccountFull!!.chunked(4).joinToString(" ")
        } else {
            userAccountMasked ?: "•••• •••• ••••"
        }
    } else {
        userAccountMasked ?: "•••• •••• ••••"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Profile & Details",
                    fontSize = 20.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Details",
                        tint = AirpayColors.Primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                servicesViewModel.fetchMyDetails()
                            }
                    )
                    Spacer(Modifier.width(14.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.outline_help_white),
                        contentDescription = "Help",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onOpenFaq() }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Profile Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(AirpayColors.Primary, Color(0xFF8B5CF6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.take(1).uppercase(),
                                fontSize = 30.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    showEditNameDialog = true
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = displayName,
                                fontSize = 18.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.TextPrimary
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = AirpayColors.Primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // UPI ID with copy chip
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF4F5F8))
                                .clickable {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("UPI ID", displayUpi))
                                    Toast.makeText(context, "UPI ID copied!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayUpi,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AirpayColors.Primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = AirpayColors.Primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Linked Bank Account Card with Eye Toggle Icon
                Text(
                    text = "LINKED BANK ACCOUNT",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFAFAFC))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AirpayColors.PurpleTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = AirpayColors.Primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = displayBank,
                                    fontSize = 15.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1C1E)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Primary UPI Account",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF757575)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                        Spacer(Modifier.height(12.dp))

                        // Account Number Row with Eye Toggle & Edit Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Account Number",
                                    fontSize = 11.sp,
                                    color = Color(0xFF757575)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = displayAccount,
                                    fontSize = 14.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1C1E),
                                    letterSpacing = 1.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Eye Icon Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF4F5F8))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                            if (!isAccountUnmasked) {
                                                if (userAccountFull.isNullOrBlank() || userAccountFull!!.length <= 4) {
                                                    showEditAccountDialog = true
                                                } else {
                                                    isAccountUnmasked = true
                                                }
                                            } else {
                                                isAccountUnmasked = false
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isAccountUnmasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isAccountUnmasked) "Hide Account Number" else "Show Full Account Number",
                                        tint = AirpayColors.Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                // Edit Account Number Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF4F5F8))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                            showEditAccountDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Full Account Number",
                                        tint = AirpayColors.Primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Actions List
                Text(
                    text = "PROFILE ACTIONS",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(10.dp))

                // Action 1: View My QR
                ProfileRowAction(
                    icon = Icons.Default.QrCode,
                    title = "My QR Code",
                    subtitle = "Show your offline QR code to receive payments",
                    onClick = onNavigateMyQr
                )

                Spacer(Modifier.height(8.dp))

                // Action 2: Settings
                ProfileRowAction(
                    icon = Icons.Default.Settings,
                    title = "Settings & SIM Preferences",
                    subtitle = "Manage biometric auth, SIM slot, and sounds",
                    onClick = onNavigateSettings
                )

                Spacer(Modifier.height(18.dp))

                // 4-Day Cache Status Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF4F5F8))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AirpayColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        val daysAgo = lastProfileTimestamp?.let {
                            val diff = (System.currentTimeMillis() - it) / (1000L * 60 * 60 * 24)
                            if (diff == 0L) "today" else "$diff days ago"
                        } ?: "just now"
                        Text(
                            text = "Stored offline in local storage (synced $daysAgo). Automatically refreshes every 4 days.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF616161)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Refresh Details Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AirpayColors.Primary)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            servicesViewModel.fetchMyDetails()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (session is SessionState.Running) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = if (session is SessionState.Running) "FETCHING DETAILS..." else "REFRESH PROFILE FROM SIM (*99*4#)",
                            fontSize = 13.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }

    // Edit Bank Account Number Dialog
    if (showEditAccountDialog) {
        var inputAccount by remember { mutableStateOf(userAccountFull ?: "") }
        AlertDialog(
            onDismissRequest = { showEditAccountDialog = false },
            title = {
                Text(
                    text = "Bank Account Number",
                    fontWeight = FontWeight.Bold,
                    fontFamily = ClashGrotesk,
                    color = Color(0xFF1C1C1E)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Carrier masks your account digits for security. Enter your complete account number to view or unmask it anytime offline.",
                        fontSize = 13.sp,
                        color = Color(0xFF616161)
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = inputAccount,
                        onValueChange = { inputAccount = it.filter { ch -> ch.isDigit() }.take(20) },
                        label = { Text("Complete Account Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = inputAccount.trim()
                        if (clean.isNotBlank()) {
                            scope.launch {
                                app.prefsRepo.setUserAccountFull(clean)
                                app.prefsRepo.setUserAccountMasked("•••• •••• ${clean.takeLast(4)}")
                                isAccountUnmasked = true
                            }
                        }
                        showEditAccountDialog = false
                    }
                ) {
                    Text("Save", color = AirpayColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAccountDialog = false }) {
                    Text("Cancel", color = Color(0xFF757575))
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        var inputName by remember { mutableStateOf(userName ?: "") }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = "Edit Your Name",
                    fontWeight = FontWeight.Bold,
                    fontFamily = ClashGrotesk,
                    color = Color(0xFF1C1C1E)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your name is displayed on your offline QR code and used for UPI transfers.",
                        fontSize = 13.sp,
                        color = Color(0xFF616161)
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it.take(40) },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = inputName.trim()
                        if (clean.isNotBlank()) {
                            scope.launch {
                                app.prefsRepo.setUserName(clean)
                            }
                        }
                        showEditNameDialog = false
                    }
                ) {
                    Text("Save", color = AirpayColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = Color(0xFF757575))
                }
            }
        )
    }
}

@Composable
private fun ProfileRowAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFAFAFC))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AirpayColors.PurpleTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AirpayColors.Primary, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontFamily = ClashGrotesk, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.5.sp, color = Color(0xFF757575))
        }

        Icon(imageVector = Iconsax.ArrowRight, contentDescription = null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(16.dp))
    }
}
