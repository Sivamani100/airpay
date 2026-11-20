package com.arkiolabs.airpay.presentation.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.data.TransactionEntity
import com.arkiolabs.airpay.domain.RecipientResolver
import com.arkiolabs.airpay.platform.ContactsHelper
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax

/**
 * Authentic Airpay Styled "To UPI ID / Number" Screen.
 *
 * Implements:
 *  - Header with Back arrow, "To UPI ID", and Help (?) icon.
 *  - Clean UPI ID / Number input box with real-time format checking.
 *  - "Verify" button with green "Verified" badge and account holder resolution.
 *  - Optional Nickname field.
 *  - Purple "PROCEED TO PAY" button.
 *  - Recent UPI Recipients list with 1-tap selection.
 *  - "POWERED BY UPI" footer logo.
 *  - Removed all "To Account Number & IFSC" tabs/screens.
 */
@Composable
fun ToUpiScreen(
    historyViewModel: HistoryViewModel,
    onUpiValidated: (vpa: String, payeeName: String) -> Unit,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    var upiInput by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }

    val transactions by historyViewModel.transactions.collectAsState()

    val resolution = remember(upiInput) {
        if (upiInput.isNotBlank()) RecipientResolver.resolve(upiInput) else null
    }

    val verifiedPayeeName = remember(upiInput, resolution) {
        ContactsHelper.resolveDisplayName(context, upiInput, resolution?.displayName)
    }

    // Filter recent unique UPI transactions
    val recentUpiTxns = remember(transactions) {
        transactions
            .filter { it.vpa.isNotBlank() }
            .distinctBy { it.vpa.lowercase() }
            .take(6)
    }

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

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "To UPI ID",
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Transfer to any UPI app • Zero fee",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00875A)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Enter any UPI ID (e.g. mobile@upi, name@okhdfcbank) or 8–9 digits UPI Number.",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "UPI ID / Number",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AirpayColors.Primary
                )

                Spacer(Modifier.height(6.dp))

                // Input Box with "Verify" or "Verified" badge
                val boxShape = RoundedCornerShape(12.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(boxShape)
                        .border(
                            width = 1.5.dp,
                            color = if (isVerified) Color(0xFF00875A) else AirpayColors.Primary,
                            shape = boxShape
                        )
                        .background(Color(0xFFFAFAFC))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (upiInput.isEmpty()) {
                            Text(
                                text = "Enter UPI ID or UPI Number",
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
                            cursorBrush = SolidColor(AirpayColors.Primary),
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
                        val canVerify = upiInput.contains("@") || upiInput.length in 8..12
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (canVerify) AirpayColors.PurpleTint else Color(0xFFF5F5F5))
                                .clickable(enabled = canVerify) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    isVerified = true
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Verify",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canVerify) AirpayColors.Primary else Color(0xFF9E9E9E)
                            )
                        }
                    }
                }

                // When Verified: show holder name and Nickname field
                if (isVerified) {
                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Verified Recipient",
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
                            .background(Color.White)
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
                            cursorBrush = SolidColor(AirpayColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Proceed button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AirpayColors.Primary)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                val finalPayee = nickname.ifBlank { verifiedPayeeName }
                                onUpiValidated(upiInput, finalPayee)
                            }
                            .padding(vertical = 15.dp),
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
                }

                Spacer(Modifier.height(28.dp))
            }

            // ── Recent UPI IDs Section ──────────────────────────────
            if (recentUpiTxns.isNotEmpty()) {
                item {
                    Text(
                        text = "RECENT UPI TRANSFERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
                }

                items(recentUpiTxns, key = { it.id }) { txn ->
                    val displayName = remember(txn) {
                        ContactsHelper.resolveDisplayName(context, txn.vpa, txn.payeeName)
                    }
                    RecentUpiRow(
                        vpa = txn.vpa,
                        payeeName = displayName,
                        onClick = {
                            upiInput = txn.vpa
                            isVerified = true
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }
            }

            // ── Powered by UPI Footer ───────────────────────────────
            item {
                Spacer(Modifier.height(36.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.upi_orange_border),
                        contentDescription = "POWERED BY UPI",
                        modifier = Modifier.height(24.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RecentUpiRow(
    vpa: String,
    payeeName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(AirpayColors.PurpleTint)
                .border(1.dp, AirpayColors.Primary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = payeeName.take(1).uppercase(),
                fontSize = 16.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.Primary
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payeeName,
                fontSize = 14.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = vpa,
                fontSize = 12.sp,
                color = Color(0xFF757575),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "Select →",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AirpayColors.Primary
        )
    }
}
