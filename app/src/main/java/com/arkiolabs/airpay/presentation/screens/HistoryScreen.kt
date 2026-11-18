package com.arkiolabs.airpay.presentation.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import com.arkiolabs.airpay.data.TransactionEntity
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authentic Airpay Styled Transaction History Screen.
 *
 * Implements the exact layout from Airpay screenshot:
 *  - Header: "History" title on left, "My Statements" pill on right, Help (?) icon.
 *  - Search bar: Lavender tint (#F5F3FB) with search icon and filter icon.
 *  - Month summary header: "November 2026" on left, "+ ₹3,913.2 >" on right.
 *  - Flat transaction rows with hairline dividers (NO boxed cards).
 *  - Circular icon with diagonal debit arrow (↗), bold black name, timestamp, bold amount, and "Debited from [Bank]".
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onPay: () -> Unit,
    onPayAgain: (TransactionEntity) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val txns by viewModel.transactions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val view = LocalView.current

    val filteredTxns = remember(searchQuery, txns) {
        if (searchQuery.isBlank()) txns else {
            txns.filter {
                it.vpa.contains(searchQuery, ignoreCase = true) ||
                    it.amount.contains(searchQuery) ||
                    (it.payeeName?.contains(searchQuery, ignoreCase = true) == true) ||
                    (it.note?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    val totalAmount = remember(filteredTxns) {
        filteredTxns.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Top Header Row (Airpay Style) ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                fontSize = 22.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // "My Statements" pill button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(20.dp))
                        .clickable { /* Statements */ }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color(0xFF1C1C1E),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "My Statements",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E)
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Help (?) Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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
        }

        // ── Search & Filter Bar (Airpay Style) ─────────────────────
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
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
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
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filter",
                    tint = Color(0xFF1C1C1E),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Month Summary Header (Airpay Style) ────────────────────
        val currentMonth = remember {
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFAFAFC))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentMonth,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (totalAmount > 0) "+ ₹${String.format(Locale.getDefault(), "%,.1f", totalAmount)}" else "₹0.00",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.SuccessGreen
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // ── Flat Transactions List (NO cards, clean dividers) ───────
        if (filteredTxns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F3FB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.txn_outflow),
                            contentDescription = null,
                            tint = AirpayColors.Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "No transactions yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Make your first offline payment or balance check.",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredTxns, key = { it.id }) { txn ->
                    AirpayHistoryRow(
                        txn = txn,
                        onPayAgain = { onPayAgain(txn) }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun AirpayHistoryRow(
    txn: TransactionEntity,
    onPayAgain: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val formattedTime = remember(txn.timestamp) {
        val diff = System.currentTimeMillis() - txn.timestamp
        when {
            diff < 3600_000 -> "${diff / 60_000} mins ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            else -> SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(txn.timestamp))
        }
    }

    val displayName = txn.payeeName?.takeIf { it.isNotBlank() } ?: txn.vpa.uppercase()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Diagonal Arrow Circle Icon (↗)
            Box(
                modifier = Modifier
                    .size(42.dp)
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

            Spacer(Modifier.width(14.dp))

            // Center details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Paid to",
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )
                Text(
                    text = displayName,
                    fontSize = 14.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E),
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )
            }

            // Right amount & Debited info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${txn.amount}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Debited from",
                        fontSize = 10.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF005696)), // SBI Blue dot
                        contentAlignment = Alignment.Center
                    ) {
                        Text("i", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Expandable details (Pay Again shortcut + USSD raw reply)
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                if (txn.carrierReply.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF6F7F9))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "USSD Response: ${txn.carrierReply}",
                            fontSize = 11.sp,
                            color = Color(0xFF555555)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(AirpayColors.PurpleTint)
                            .clickable { onPayAgain() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            tint = AirpayColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Pay Again",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.Primary
                        )
                    }
                }
            }
        }
    }
}
