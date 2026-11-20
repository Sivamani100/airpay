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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.ui.text.style.TextAlign
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

/**
 * Screen displaying Pending Collect Requests (*99*5#).
 * Stores past requests in local storage and shows with timestamps.
 */
@Composable
fun PendingRequestsScreen(
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val app = context.airpayApp
    val session by servicesViewModel.sessionState.collectAsState()

    val pendingText by app.prefsRepo.lastPendingRequestsText.collectAsState(initial = null)
    val lastTimestamp by app.prefsRepo.lastPendingRequestsTimestamp.collectAsState(initial = null)

    val formattedDate = remember(lastTimestamp) {
        if (lastTimestamp != null && lastTimestamp!! > 0) {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastTimestamp!!))
        } else null
    }

    val parsedRequests = remember(pendingText) {
        val text = pendingText ?: return@remember emptyList<String>()
        text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("welcome", ignoreCase = true) }
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
                        text = "Pending Requests",
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
                                servicesViewModel.fetchPendingRequests()
                            }
                    )
                    Spacer(Modifier.width(14.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.outline_help_white),
                        contentDescription = "Help",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(24.dp)
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
                        text = "Incoming Collect Requests (*99*5#)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    if (formattedDate != null) {
                        Text(
                            text = "Last checked: $formattedDate",
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

            if (parsedRequests.isEmpty() && session !is SessionState.Running) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "No Pending Requests",
                        fontSize = 16.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "You have no incoming payment requests from other users waiting for approval.",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AirpayColors.Primary)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                servicesViewModel.fetchPendingRequests()
                            }
                            .padding(horizontal = 20.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = "Check Gateway (*99*5#)",
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

                    items(parsedRequests) { reqText ->
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
                                    .background(AirpayColors.PurpleTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = AirpayColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reqText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1C1C1E)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "NUUP Collect Request",
                                    fontSize = 11.sp,
                                    color = Color(0xFF757575)
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
                                    servicesViewModel.fetchPendingRequests()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Refresh Pending Requests (*99*5#)",
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
