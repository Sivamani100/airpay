package com.arkiolabs.airpay.presentation.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Replay
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
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.data.TransactionEntity
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clean, streamlined Offline Transaction History Screen.
 * Shows offline payment records stored on this device without unwanted clutter.
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction History",
                fontSize = 20.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${filteredTxns.size} payments",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AirpayColors.TextSecondary
            )
        }

        // ── Clean Search Bar ────────────────────────────────────────
        val pillShape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(pillShape)
                .background(Color(0xFFF6F7F9))
                .border(1.dp, Color(0xFFE5E7EB), pillShape)
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AirpayColors.TextSecondary,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(10.dp))

                Box(Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search by name, UPI ID, or amount",
                            fontSize = 13.5.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.5.sp,
                            color = AirpayColors.TextPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(AirpayColors.Primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDDDDDD))
                            .clickable { searchQuery = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Clean Transaction List ──────────────────────────────────
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
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F7FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.txn_outflow),
                            contentDescription = null,
                            tint = AirpayColors.TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "No offline transactions yet" else "No matching transactions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirpayColors.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) {
                            "Completed offline payments via SIM will appear here."
                        } else {
                            "Try searching with a different name or number."
                        },
                        fontSize = 12.sp,
                        color = AirpayColors.TextSecondary
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
                    CleanHistoryItem(
                        txn = txn,
                        onPayAgain = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onPayAgain(txn)
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)
                }
            }
        }
    }
}

@Composable
private fun CleanHistoryItem(
    txn: TransactionEntity,
    onPayAgain: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedDate = remember(txn.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(txn.timestamp))
    }

    val displayName = txn.payeeName?.takeIf { it.isNotBlank() } ?: txn.vpa

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
            // Outflow Circle Icon
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
                    tint = AirpayColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            // Details (Name / VPA & Timestamp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontSize = 14.5.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    fontSize = 11.5.sp,
                    color = AirpayColors.TextSecondary
                )
            }

            // Amount (Debited)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${txn.amount}",
                    fontSize = 16.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Successful",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AirpayColors.SuccessGreen
                )
            }
        }

        // Expanded Options: Details & Pay Again button
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "To: ${txn.vpa}",
                            fontSize = 12.sp,
                            color = AirpayColors.TextSecondary
                        )
                        if (!txn.note.isNullOrBlank()) {
                            Text(
                                text = "Note: ${txn.note}",
                                fontSize = 11.sp,
                                color = AirpayColors.TextSecondary
                            )
                        }
                    }

                    // Pay Again Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AirpayColors.Primary)
                            .clickable { onPayAgain() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Pay Again",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
