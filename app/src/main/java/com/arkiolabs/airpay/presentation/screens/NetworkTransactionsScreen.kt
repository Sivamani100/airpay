package com.arkiolabs.airpay.presentation.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParsedNetworkTxn(
    val title: String,
    val amount: String,
    val isDebit: Boolean,
    val rawText: String
)

/**
 * Screen displaying Recent Transactions across the entire UPI network (via *99*6*1#).
 * Includes past transactions from other UPI apps and ATMs stored in local storage with timestamps.
 */
@Composable
fun NetworkTransactionsScreen(
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val app = context.airpayApp
    val session by servicesViewModel.sessionState.collectAsState()

    val rawNetworkTxns by app.prefsRepo.lastNetworkTxns.collectAsState(initial = null)
    val lastTimestamp by app.prefsRepo.lastNetworkTxnsTimestamp.collectAsState(initial = null)

    val formattedDate = remember(lastTimestamp) {
        if (lastTimestamp != null && lastTimestamp!! > 0) {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastTimestamp!!))
        } else null
    }

    // Parse lines from NUUP statement into cards
    val parsedTxns = remember(rawNetworkTxns) {
        val text = rawNetworkTxns ?: return@remember emptyList<ParsedNetworkTxn>()
        text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("welcome", ignoreCase = true) }
            .map { line ->
                val isDebit = line.contains("dr", ignoreCase = true) || line.contains("debited", ignoreCase = true) || line.contains("paid", ignoreCase = true)
                val amountMatch = Regex("(?:rs\\.?|inr|₹)?\\s*([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE).find(line)
                val amountStr = amountMatch?.groupValues?.get(1) ?: ""
                ParsedNetworkTxn(
                    title = line.substringBefore(amountStr).trim().ifBlank { "UPI Transaction" },
                    amount = if (amountStr.isNotBlank()) "₹$amountStr" else "",
                    isDebit = isDebit,
                    rawText = line
                )
            }
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
                                if (session !is SessionState.Idle) {
                                    servicesViewModel.dismissSession()
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
                        text = "Network Transactions",
                        fontSize = 18.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = AirpayColors.Primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                servicesViewModel.fetchRecentNetworkTransactions()
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

            // Sync Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "All Apps & ATM Activity (*99*6*1#)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    if (formattedDate != null) {
                        Text(
                            text = "Last synced: $formattedDate",
                            fontSize = 10.5.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                if (session is SessionState.Running) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AirpayColors.Primary, strokeWidth = 2.dp)
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // Content List
            if (parsedTxns.isEmpty() && session !is SessionState.Running) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bottom_nav_history_unselected),
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "No Network Statement Yet",
                        fontSize = 16.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Sync Network Transactions' to query your bank's recent transaction statement via offline SIM.",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AirpayColors.Primary)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                servicesViewModel.fetchRecentNetworkTransactions()
                            }
                            .padding(horizontal = 20.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = "Sync Network Transactions",
                            fontSize = 13.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                    }

                    items(parsedTxns) { txn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (txn.isDebit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (txn.isDebit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (txn.isDebit) AirpayColors.DangerRed else Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = txn.rawText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1C1C1E)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (txn.isDebit) "Debited via UPI" else "Credited via UPI",
                                    fontSize = 11.sp,
                                    color = Color(0xFF757575)
                                )
                            }

                            if (txn.amount.isNotBlank()) {
                                Text(
                                    text = txn.amount,
                                    fontSize = 14.sp,
                                    fontFamily = ClashGrotesk,
                                    fontWeight = FontWeight.Bold,
                                    color = if (txn.isDebit) AirpayColors.TextPrimary else Color(0xFF2E7D32)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF4F5F8))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    servicesViewModel.fetchRecentNetworkTransactions()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Refresh Statement (*99*6*1#)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.Primary
                            )
                        }
                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
