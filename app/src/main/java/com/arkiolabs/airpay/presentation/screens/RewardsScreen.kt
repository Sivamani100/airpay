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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

/**
 * Screen displaying Rewards & Offers from the carrier gateway (*99*6*2#).
 */
@Composable
fun RewardsScreen(
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val app = context.airpayApp
    val session by servicesViewModel.sessionState.collectAsState()

    val rewardsText by app.prefsRepo.lastRewardsText.collectAsState(initial = null)

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
                        text = "Rewards & Offers",
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
                                servicesViewModel.fetchRewards()
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Featured Reward Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AirpayColors.PurpleTint)
                        .border(1.dp, AirpayColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(AirpayColors.Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Offline UPI Rewards",
                                fontSize = 16.sp,
                                fontFamily = ClashGrotesk,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Earn cashback & points on every offline payment",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "LATEST GATEWAY REWARDS (*99*6*2#)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(10.dp))

                if (session is SessionState.Running) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AirpayColors.Primary)
                    }
                } else if (!rewardsText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFAFAFC))
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Carrier Rewards Output",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.Primary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = rewardsText!!,
                                fontSize = 13.sp,
                                color = Color(0xFF1C1C1E),
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFAFAFC))
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No rewards fetched yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF757575)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Tap the button below to query your rewards status from the bank.",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AirpayColors.Primary)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            servicesViewModel.fetchRewards()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CHECK LATEST REWARDS",
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
