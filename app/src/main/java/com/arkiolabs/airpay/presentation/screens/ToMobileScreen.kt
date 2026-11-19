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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.platform.ContactItem
import com.arkiolabs.airpay.platform.ContactsHelper
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Authentic Airpay Styled "Send Money / To Mobile Number" Screen.
 *
 * Implements the exact layout from Airpay screenshots:
 *  - Top bar with Back arrow, Refresh, Help (?), and 3-dots menu.
 *  - "Send Money" header with "to any UPI app [Airpay] [Airpay] [online UPI] [online wallets]".
 *  - Search bar: "#F5F3FB", search icon, "Search any contact / name".
 *  - "PAYMENTS & CHAT" flat list with contact avatar, UPI badge, last activity, green dot, time.
 *  - Bottom floating "+ New Payment" purple button.
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
                                text = "Search any contact / name",
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

            Spacer(Modifier.height(12.dp))

            // ── Direct Mobile Number Pay Tile (if 10 digits entered) ─
            if (isDirectPhone) {
                val normalizedPhone = if (cleanDigits.length == 10) cleanDigits else cleanDigits.takeLast(10)
                val savedName = ContactsHelper.getContactName(context, normalizedPhone)
                val title = savedName ?: "Pay to +91 $normalizedPhone"
                val subtitle = if (savedName != null) "+91 $normalizedPhone · Saved Contact" else "Direct offline SIM payment"
                val displayNameToPass = savedName ?: "+91 $normalizedPhone"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onContactSelected(normalizedPhone, displayNameToPass)
                        }
                        .background(AirpayColors.SaffronTint)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AirpayColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (savedName != null) {
                            Text(
                                text = savedName.take(1).uppercase(),
                                color = Color.White,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 14.5.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.Primary
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.5.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    Text(
                        text = "Pay →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirpayColors.Primary
                    )
                }
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

                // Sample recent chats if contacts empty
                if (filteredContacts.isEmpty() && hasPermission) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No contacts found",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }

        // ── Floating "+ New Payment" Purple Button (Airpay Style) ──
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
                        // Trigger direct pay input
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
