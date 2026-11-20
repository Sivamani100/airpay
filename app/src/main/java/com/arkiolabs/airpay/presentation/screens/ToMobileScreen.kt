package com.arkiolabs.airpay.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.platform.ContactItem
import com.arkiolabs.airpay.platform.ContactsHelper
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax

/**
 * Authentic PhonePe-styled "Send Money / To Mobile Number" Screen.
 *
 * Implements:
 *  - Top bar with Back arrow, Refresh, Help (?), and 3-dots menu.
 *  - "Send Money" header with "to any UPI app".
 *  - Search bar: "#F5F3FB", search icon, "Search any contact / name".
 *  - Live 10-digit number detection with contact lookup.
 *  - "PAYMENTS & CHAT" flat list with contact avatar, UPI badge, name, phone.
 *  - Floating "+ New Payment" purple button with dedicated PhonePe-style mobile entry sheet.
 *  - No "Split expenses" row (removed per user request).
 */
@Composable
fun ToMobileScreen(
    onContactSelected: (phone: String, name: String) -> Unit,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var showNewPaymentSheet by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            contacts = ContactsHelper.loadContacts(context)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && contacts.isEmpty()) {
            contacts = ContactsHelper.loadContacts(context)
        }
    }

    val filteredContacts = remember(query, contacts) {
        if (query.isBlank()) contacts else {
            contacts.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }
    }

    val cleanDigits = query.replace(Regex("[^0-9]"), "")
    val isDirectPhone = cleanDigits.length in 10..12

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Header Actions ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Iconsax.ArrowLeft,
                        contentDescription = "Back",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                if (hasPermission) contacts = ContactsHelper.loadContacts(context)
                            }
                    )
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.outline_help_white),
                        contentDescription = "Help",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onOpenFaq() }
                    )
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ── Title & UPI apps subtitle ───────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Send Money",
                    fontSize = 20.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "to any UPI app",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "All Indian Banks · Jio · Airtel · Vi · BSNL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AirpayColors.Primary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Search Bar ──────────────────────────────────────────
            val pillShape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(pillShape)
                    .background(Color(0xFFF5F3FB))
                    .border(1.dp, Color(0xFFE8E5F3), pillShape)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search any contact / name / number",
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = Color(0xFF1C1C1E)
                            ),
                            cursorBrush = SolidColor(AirpayColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF757575),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Direct Mobile Number Pay Tile (if 10 digits entered) ─
            if (isDirectPhone) {
                val normalizedPhone = if (cleanDigits.length == 10) cleanDigits else cleanDigits.takeLast(10)
                val resolvedContactName = remember(normalizedPhone) {
                    ContactsHelper.getContactName(context, normalizedPhone)
                }
                val displayName = resolvedContactName ?: "+91 $normalizedPhone"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onContactSelected(normalizedPhone, displayName)
                        }
                        .background(AirpayColors.SaffronTint)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AirpayColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(1).uppercase(),
                            color = Color.White,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (resolvedContactName != null) resolvedContactName else "Pay to +91 $normalizedPhone",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.TextPrimary
                        )
                        Text(
                            text = if (resolvedContactName != null) "+91 $normalizedPhone · Offline UPI" else "Direct offline SIM payment",
                            fontSize = 11.5.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    Text(
                        text = "Pay →",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirpayColors.Primary
                    )
                }
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }

            // ── PAYMENTS & CHAT List Header ─────────────────────────
            Text(
                text = "PAYMENTS & CHAT",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF757575),
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            // ── Contact Permission Prompt ───────────────────────────
            if (!hasPermission) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AirpayColors.PurpleTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AirpayColors.Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Access Contacts",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.Primary
                        )
                        Text(
                            text = "Tap to sync contacts for quick offline pay",
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            // ── Contacts / Chats Flat List ──────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredContacts, key = { it.phone }) { contact ->
                    ContactAirpayRow(
                        contact = contact,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onContactSelected(contact.phone, contact.name)
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }

                if (filteredContacts.isEmpty() && hasPermission) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (query.isNotBlank()) "No contacts matching '$query'" else "No contacts found",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }

        // ── Floating "+ New Payment" Purple Button (PhonePe Style) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(AirpayColors.Primary)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        showNewPaymentSheet = true
                    }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "New Payment",
                    fontSize = 13.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // ── PhonePe-style New Payment Mobile Number Sheet ───────────
        if (showNewPaymentSheet) {
            NewPaymentSheet(
                onDismiss = { showNewPaymentSheet = false },
                onProceed = { phone, name ->
                    showNewPaymentSheet = false
                    onContactSelected(phone, name)
                }
            )
        }
    }
}

@Composable
private fun ContactAirpayRow(
    contact: ContactItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle with mini UPI badge
        Box(modifier = Modifier.size(46.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AirpayColors.PurpleTint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                    fontFamily = ClashGrotesk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.Primary
                )
            }

            // Mini UPI badge at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFDDDDDD), RoundedCornerShape(3.dp))
                    .padding(horizontal = 2.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = "UPI",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF005696)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Center: Name & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontSize = 14.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Mobile: ${contact.phone}",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }

        // Right status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32))
        )
    }
}

/**
 * Dedicated PhonePe-style Bottom Sheet for entering a new recipient mobile number.
 * Features live contact lookup so if a user types a number present in contacts,
 * the saved contact name is automatically displayed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewPaymentSheet(
    onDismiss: () -> Unit,
    onProceed: (phone: String, name: String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var inputNumber by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val digits = inputNumber.filter { it.isDigit() }
    val normalizedPhone = if (digits.length >= 10) digits.takeLast(10) else digits
    val isComplete = normalizedPhone.length == 10

    val resolvedContactName = remember(normalizedPhone) {
        if (isComplete) ContactsHelper.getContactName(context, normalizedPhone) else null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Payment",
                    fontFamily = ClashGrotesk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F5F8))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Enter any 10-digit mobile number to send money via offline UPI",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )

            Spacer(Modifier.height(18.dp))

            // Number entry box
            val borderShape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(borderShape)
                    .border(
                        1.5.dp,
                        if (isComplete) AirpayColors.Primary else Color(0xFFDDDDDD),
                        borderShape
                    )
                    .background(Color(0xFFFAFAFC))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+91",
                    fontSize = 16.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (inputNumber.isEmpty()) {
                        Text(
                            text = "Enter 10-digit number",
                            fontSize = 15.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    BasicTextField(
                        value = inputNumber,
                        onValueChange = {
                            if (it.filter { c -> c.isDigit() }.length <= 10) {
                                inputNumber = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.TextPrimary,
                            letterSpacing = 1.sp
                        ),
                        cursorBrush = SolidColor(AirpayColors.Primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (isComplete) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Valid",
                        tint = AirpayColors.SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Real-time contact match display
            if (isComplete) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (resolvedContactName != null) AirpayColors.PurpleTint else Color(0xFFF4F5F8))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (resolvedContactName != null) AirpayColors.Primary else Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (resolvedContactName != null) "Contact: $resolvedContactName" else "New recipient (+91 $normalizedPhone)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (resolvedContactName != null) AirpayColors.Primary else Color(0xFF1C1C1E)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Proceed Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isComplete) AirpayColors.Primary else Color(0xFFCCCCCC))
                    .clickable(enabled = isComplete) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        val name = resolvedContactName ?: "+91 $normalizedPhone"
                        onProceed(normalizedPhone, name)
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PROCEED TO PAY",
                    fontSize = 14.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
