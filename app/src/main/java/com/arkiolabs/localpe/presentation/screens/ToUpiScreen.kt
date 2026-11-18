package com.arkiolabs.localpe.presentation.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.localpe.R
import com.arkiolabs.localpe.domain.RecipientResolver
import com.arkiolabs.localpe.presentation.HistoryViewModel
import com.arkiolabs.localpe.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.localpe.presentation.ui.theme.Iconsax
import com.arkiolabs.localpe.presentation.ui.theme.LocalPeColors

enum class ToUpiTab {
    DASHBOARD,
    UPI_ID_ENTRY,
    BANK_ACCOUNTS
}

/**
 * Authentic LocalPe Styled "To Bank & Self A/c / To UPI ID" Screen.
 *
 * Implements the exact layout from LocalPe screenshots:
 *  - Header: "Send Money" / "Zero wait time • Zero fee".
 *  - Two action cards: "To Self Bank Account" & "To Account Number & IFSC".
 *  - "@ To UPI ID or Number" entry row.
 *  - UPI Verification flow with "Verify" button, green "Verified" badge, name check, and "PROCEED" button.
 *  - Saved accounts list with "+ Bank Account" / "+ UPI ID / Number" FABs.
 */
@Composable
fun ToUpiScreen(
    historyViewModel: HistoryViewModel,
    onUpiValidated: (vpa: String, payeeName: String) -> Unit,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ToUpiTab.DASHBOARD) }
    var upiInput by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }
    val transactions by historyViewModel.transactions.collectAsState()
    val view = LocalView.current

    val resolution = remember(upiInput) {
        if (upiInput.isNotBlank()) RecipientResolver.resolve(upiInput) else null
    }

    val verifiedPayeeName = resolution?.displayName ?: "Verified Recipient"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Top Header Bar ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (activeTab != ToUpiTab.DASHBOARD) {
                                activeTab = ToUpiTab.DASHBOARD
                            } else {
                                onBack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Iconsax.ArrowLeft,
                        contentDescription = "Back",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = when (activeTab) {
                        ToUpiTab.DASHBOARD -> "Send Money"
                        ToUpiTab.UPI_ID_ENTRY -> "To new UPI ID/Number"
                        ToUpiTab.BANK_ACCOUNTS -> "To Account Number & IFSC"
                    },
                    fontSize = 18.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }

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

        when (activeTab) {
            // ── TAB 1: Main "Send Money" Dashboard ──────────────────
            ToUpiTab.DASHBOARD -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Zero wait time header
                    Text(
                        text = "Zero wait time • Zero fee",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00875A)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Add recipient and pay up to ₹1 lakh per day instantly.",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(Modifier.height(20.dp))

                    // Two Big Action Cards Row (Exact LocalPe Screenshot)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card 1: To Self Bank Account
                        SendMoneyOptionCard(
                            iconRes = R.drawable.to_bank_self_account,
                            title = "To Self Bank Account",
                            onClick = { activeTab = ToUpiTab.BANK_ACCOUNTS },
                            modifier = Modifier.weight(1f)
                        )

                        // Card 2: To Account Number & IFSC
                        SendMoneyOptionCard(
                            iconRes = R.drawable.ic_to_bank_account,
                            title = "To Account Number\n& IFSC",
                            onClick = { activeTab = ToUpiTab.BANK_ACCOUNTS },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    Spacer(Modifier.height(8.dp))

                    // Row: @ To UPI ID or Number
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeTab = ToUpiTab.UPI_ID_ENTRY
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "@",
                                fontSize = 20.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = LocalPeColors.Primary
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "To UPI ID or Number",
                                fontSize = 15.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Transfer to any UPI app",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Icon(
                            imageVector = Iconsax.ArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    Spacer(Modifier.weight(1f))

                    // POWERED BY UPI Logo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.upi_orange_border),
                            contentDescription = "POWERED BY UPI",
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }

            // ── TAB 2: "To new UPI ID/Number" Flow ───────────────────
            ToUpiTab.UPI_ID_ENTRY -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Enter any UPI ID or 8–9 digits UPI Number",
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "UPI ID / Number",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LocalPeColors.Primary
                        )

                        Spacer(Modifier.height(6.dp))

                        // Input Box with "Verify" or "Verified" badge
                        val boxShape = RoundedCornerShape(10.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(boxShape)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isVerified) Color(0xFF00875A) else LocalPeColors.Primary,
                                    shape = boxShape
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (upiInput.isEmpty()) {
                                    Text(
                                        text = "Enter UPI ID/UPI Number",
                                        fontSize = 14.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                                BasicTextField(
                                    value = upiInput,
                                    onValueChange = {
                                        upiInput = it.trim().lowercase()
                                        isVerified = false
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1C1C1E)
                                    ),
                                    cursorBrush = SolidColor(LocalPeColors.Primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (isVerified) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Verified",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (upiInput.contains("@") || upiInput.length in 8..12) Color(0xFFEEEEEE) else Color(0xFFF5F5F5))
                                        .clickable(enabled = upiInput.isNotBlank()) {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            isVerified = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Verify",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (upiInput.isNotBlank()) Color(0xFF1C1C1E) else Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }

                        // When Verified: show holder name and Nickname field
                        if (isVerified) {
                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = "Verified Account Holder Name",
                                fontSize = 11.sp,
                                color = Color(0xFF757575)
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = verifiedPayeeName,
                                    fontSize = 16.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1C1E)
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF00875A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = "Nickname",
                                fontSize = 11.sp,
                                color = Color(0xFF757575)
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                if (nickname.isEmpty()) {
                                    Text(
                                        text = "Add Nickname (Optional)",
                                        fontSize = 13.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                                BasicTextField(
                                    value = nickname,
                                    onValueChange = { nickname = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF1C1C1E)),
                                    cursorBrush = SolidColor(LocalPeColors.Primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // PROCEED Button at bottom (Exact LocalPe Screenshot)
                    if (isVerified) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LocalPeColors.Primary)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        onUpiValidated(upiInput, verifiedPayeeName)
                                    }
                                    .padding(vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PROCEED",
                                    fontSize = 14.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── TAB 3: Saved Bank Accounts & IFSC ────────────────────
            ToUpiTab.BANK_ACCOUNTS -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "SAVED BANK ACCOUNTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF757575),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        SavedBankRow(
                            bankName = "Principal Seshadri Rao Gudlaval...",
                            accountInfo = "State Bank of India ••5882",
                            extra = "Sent: ₹200",
                            date = "01 Mar 2026",
                            onClick = { onUpiValidated("sbi5882@upi", "Principal Seshadri") }
                        )
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                        SavedBankRow(
                            bankName = "CMR HACKATHON",
                            accountInfo = "HDFC Bank ••8035",
                            extra = "Sent: ₹3,300",
                            date = "05 Dec 2025",
                            onClick = { onUpiValidated("hdfc8035@upi", "CMR HACKATHON") }
                        )
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                        SavedBankRow(
                            bankName = "Godavari Institute Of Engineerin...",
                            accountInfo = "Punjab National Bank ••5134",
                            extra = "Bank account added",
                            date = "12 Feb 2025",
                            onClick = { onUpiValidated("pnb5134@upi", "Godavari Institute") }
                        )
                    }

                    // Floating "+ Bank Account" purple FAB (LocalPe Style)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(LocalPeColors.Primary)
                                .clickable {
                                    activeTab = ToUpiTab.UPI_ID_ENTRY
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
                                text = "Bank Account",
                                fontSize = 13.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SendMoneyOptionCard(
    @DrawableRes iconRes: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFFFAFAFC))
            .border(1.dp, Color(0xFFEEEEEE), shape)
            .clickable { onClick() }
            .padding(18.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SavedBankRow(
    bankName: String,
    accountInfo: String,
    extra: String,
    date: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF005696)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bankName,
                fontSize = 14.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = accountInfo,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(Modifier.height(2.dp))
            Row {
                Text(
                    text = extra,
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = date,
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(20.dp)
        )
    }
}
