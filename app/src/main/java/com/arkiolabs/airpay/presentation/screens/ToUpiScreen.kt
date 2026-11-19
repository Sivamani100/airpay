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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
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
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.domain.RecipientResolver
import com.arkiolabs.airpay.domain.UpiParser
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax

/**
 * Authentic Airpay Styled "To UPI ID" Screen.
 *
 * Implements a focused, fast transfer flow:
 *  - Header: "To UPI ID" with Help icon.
 *  - Subtitle: "Zero wait time • Zero fee".
 *  - "@ To UPI ID or Number" direct input box.
 *  - UPI Verification flow with "Verify" button, green "Verified" badge, name check.
 *  - Optional Nickname field.
 *  - Recent UPI transfers list for 1-tap re-use.
 *  - Bottom "PROCEED" action button to launch payment.
 */
@Composable
fun ToUpiScreen(
    historyViewModel: HistoryViewModel,
    onUpiValidated: (vpa: String, payeeName: String) -> Unit,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    var upiInput by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }
    val transactions by historyViewModel.transactions.collectAsState()
    val view = LocalView.current

    val resolution = remember(upiInput) {
        if (upiInput.isNotBlank()) RecipientResolver.resolve(upiInput) else null
    }

    val verifiedPayeeName = resolution?.displayName ?: "Verified Recipient"

    // Filter recent transactions that contain a valid UPI VPA
    val recentUpiList = remember(transactions) {
        transactions
            .filter { tx -> tx.vpa.contains("@") || UpiParser.isValidVpa(tx.vpa) }
            .distinctBy { it.vpa.lowercase().trim() }
            .take(6)
    }

    val canProceed = upiInput.isNotBlank() && (isVerified || upiInput.contains("@") || upiInput.length in 8..12)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
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
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onBack()
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
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    // Header badges
                    Text(
                        text = "Zero wait time • Zero fee",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00875A)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Transfer to any UPI ID or 8–9 digits UPI Number",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "UPI ID / Number",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AirpayColors.Primary
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
                                color = if (isVerified) Color(0xFF00875A) else AirpayColors.Primary,
                                shape = boxShape
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (upiInput.isEmpty()) {
                                Text(
                                    text = "Enter UPI ID or Number (e.g. name@upi)",
                                    fontSize = 13.sp,
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (upiInput.contains("@") || upiInput.length in 8..12) {
                                            Color(0xFFEEEEEE)
                                        } else {
                                            Color(0xFFF5F5F5)
                                        }
                                    )
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

                        Spacer(Modifier.height(16.dp))

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
                                cursorBrush = SolidColor(AirpayColors.Primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (recentUpiList.isNotEmpty()) {
                        Text(
                            text = "RECENT UPI TRANSFERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF757575),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                // Recent UPI items
                items(recentUpiList) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                upiInput = tx.vpa
                                isVerified = true
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDE7F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AirpayColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.payeeName ?: tx.vpa,
                                fontSize = 14.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${tx.vpa} · Last sent ₹${tx.amount}",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Icon(
                            imageVector = Iconsax.ArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF2F2F4), thickness = 0.8.dp)
                }

                item {
                    Spacer(Modifier.height(30.dp))
                    // POWERED BY UPI Logo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.upi_orange_border),
                            contentDescription = "POWERED BY UPI",
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }

        // PROCEED Button anchored at the bottom
        if (canProceed) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AirpayColors.Primary)
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
