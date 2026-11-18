package com.arkiolabs.airpay.presentation.screens

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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.data.TransactionEntity
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Authentic Airpay Styled Search Screen.
 *
 * Implements the exact layout from Airpay screenshot:
 *  - Title: "Search" with Help (?) icon.
 *  - Search pill with search icon, placeholder "Search for 'bills'", and Mic icon.
 *  - "Popular" 4 squircle icons (Credit Score, Mobile Recharge, Loan Repayment, FASTag Recharge).
 *  - "New for you" modern promotional cards.
 *  - Search results in clean flat list with subtle dividers.
 */
@Composable
fun SearchScreen(
    historyViewModel: HistoryViewModel,
    onNavigatePay: (String) -> Unit,
    onNavigateScan: () -> Unit,
    onNavigateBalance: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val transactions by historyViewModel.transactions.collectAsState()
    val view = LocalView.current

    val filteredTxns = remember(query, transactions) {
        if (query.isBlank()) emptyList() else {
            transactions.filter {
                it.vpa.contains(query, ignoreCase = true) ||
                    (it.payeeName?.contains(query, ignoreCase = true) == true) ||
                    (it.note?.contains(query, ignoreCase = true) == true) ||
                    it.amount.contains(query)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Top Bar Header (Airpay Style) ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search",
                fontSize = 22.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onOpenFaq()
                    },
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

        // ── Search Pill Input Bar (Exact Airpay Style) ──────────────
        val pillShape = RoundedCornerShape(24.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(pillShape)
                .background(Color(0xFFF5F3FB))
                .border(1.dp, Color(0xFFE8E5F3), pillShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.search_icon),
                    contentDescription = null,
                    tint = Color(0xFF1C1C1E),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(10.dp))

                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search for 'bills' or any UPI ID",
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

                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice search",
                    tint = Color(0xFF1C1C1E),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        if (query.isNotBlank()) {
            // Search Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (filteredTxns.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No results for \"$query\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Tap below to pay this number or UPI ID directly",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AirpayColors.Primary)
                                    .clickable { onNavigatePay(query) }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Pay $query →",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTxns) { txn ->
                        SearchResultRow(txn = txn, onClick = { onNavigatePay(txn.vpa) })
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    }
                }
            }
        } else {
            // Default Explore Content matching Airpay Screenshot
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Section: Popular
                item {
                    Text(
                        text = "Popular",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SearchPopularItem(
                            label = "To Mobile\nNumber",
                            badge = null,
                            iconResId = R.drawable.to_mobile_number,
                            onClick = { onNavigatePay("") },
                            modifier = Modifier.weight(1f)
                        )

                        SearchPopularItem(
                            label = "To Bank &\nSelf A/c",
                            badge = null,
                            iconResId = R.drawable.to_bank_self_account,
                            onClick = { onNavigatePay("") },
                            modifier = Modifier.weight(1f)
                        )

                        SearchPopularItem(
                            label = "Check\nBalance",
                            badge = null,
                            iconResId = R.drawable.check_balance_v1,
                            onClick = onNavigateBalance,
                            modifier = Modifier.weight(1f)
                        )

                        SearchPopularItem(
                            label = "Offline\nGuide",
                            badge = "Help",
                            iconResId = R.drawable.home_referandearn,
                            onClick = onOpenFaq,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }

                // Section: New for you
                item {
                    Text(
                        text = "New for you",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NewForYouCard(
                            title = "Check offline guide",
                            desc = "Learn how SIM payments work without internet",
                            bannerBg = Color(0xFF263238),
                            onClick = onOpenFaq,
                            modifier = Modifier.weight(1f)
                        )

                        NewForYouCard(
                            title = "Track transactions",
                            desc = "View real-time USSD balance and receipts",
                            bannerBg = Color(0xFF1A237E),
                            onClick = onNavigateBalance,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchPopularItem(
    label: String,
    badge: String?,
    @androidx.annotation.DrawableRes iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF9F9FB))
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(34.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF9800))
                        .padding(horizontal = 4.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun NewForYouCard(
    title: String,
    desc: String,
    bannerBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFEEEEEE), shape)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(bannerBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_airpay_qr_icon_monochrome),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(36.dp)
            )
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color(0xFF757575),
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    txn: TransactionEntity,
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
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF4F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.txn_outflow),
                contentDescription = null,
                tint = Color(0xFF1C1C1E),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = txn.payeeName?.takeIf { it.isNotBlank() } ?: txn.vpa,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = txn.vpa,
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }

        Text(
            text = "₹${txn.amount}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )
    }
}
