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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
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

data class PopularBankItem(val name: String, val code: String)

/**
 * Screen for changing the primary linked bank account (*99*4*1#).
 */
@Composable
fun ChangeBankScreen(
    servicesViewModel: ServicesViewModel,
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val app = context.airpayApp
    val session by servicesViewModel.sessionState.collectAsState()
    val currentBankName by app.prefsRepo.userBankName.collectAsState(initial = "")

    var searchQuery by remember { mutableStateOf("") }

    val popularBanks = listOf(
        PopularBankItem("State Bank of India", "SBI"),
        PopularBankItem("HDFC Bank", "HDFC"),
        PopularBankItem("ICICI Bank", "ICICI"),
        PopularBankItem("Punjab National Bank", "PNB"),
        PopularBankItem("Bank of Baroda", "BOB"),
        PopularBankItem("Axis Bank", "AXIS"),
        PopularBankItem("Canara Bank", "CANARA"),
        PopularBankItem("Kotak Mahindra", "KOTAK")
    )

    val filteredBanks = if (searchQuery.isBlank()) popularBanks else {
        popularBanks.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
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
                        text = "Change Linked Bank",
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

            when (val s = session) {
                is SessionState.Idle -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (!currentBankName.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AirpayColors.PurpleTint)
                                    .border(1.dp, AirpayColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = AirpayColors.Primary, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(text = "Currently Linked", fontSize = 11.sp, color = Color(0xFF757575))
                                        Text(text = currentBankName!!, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AirpayColors.TextPrimary)
                                    }
                                }
                            }
                            Spacer(Modifier.height(18.dp))
                        }

                        Text(
                            text = "Search or Enter Bank Name / 3 Letters",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AirpayColors.Primary
                        )
                        Spacer(Modifier.height(6.dp))

                        val shape = RoundedCornerShape(10.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .border(1.dp, Color(0xFFDDDDDD), shape)
                                .background(Color(0xFFFAFAFC))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(text = "e.g. SBI, HDFC, ICICI, PNB", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1C1C1E)),
                                    cursorBrush = SolidColor(AirpayColors.Primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (searchQuery.isNotBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AirpayColors.Primary)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        servicesViewModel.changeLinkedBank(searchQuery.trim())
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Link '${searchQuery.trim().uppercase()}' (*99*4*1#)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "POPULAR BANKS",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF757575),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        // Grid of popular banks
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredBanks.chunked(2).forEach { rowBanks ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowBanks.forEach { bank ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFAFAFC))
                                                .clickable {
                                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                    servicesViewModel.changeLinkedBank(bank.code)
                                                }
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(AirpayColors.PurpleTint),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = bank.code.take(2),
                                                        fontSize = 12.sp,
                                                        fontFamily = ClashGrotesk,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AirpayColors.Primary
                                                    )
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    text = bank.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF1C1C1E),
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "Tap to switch",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF757575)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is SessionState.Running -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = s.label,
                            fontSize = 16.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.Primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Querying bank account via NUUP (*99*4*1#)...",
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                is SessionState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = AirpayColors.SuccessGreen, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(text = "Bank Changed!", fontSize = 20.sp, fontFamily = ClashGrotesk, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
                        Spacer(Modifier.height(8.dp))
                        Text(text = s.resultText, fontSize = 13.sp, color = Color(0xFF757575), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AirpayColors.Primary)
                                .clickable { servicesViewModel.dismissSession() }
                                .padding(horizontal = 28.dp, vertical = 11.dp)
                        ) {
                            Text(text = "Done", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                is SessionState.Failed -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Bank Switch Failed", fontSize = 18.sp, fontFamily = ClashGrotesk, fontWeight = FontWeight.Bold, color = AirpayColors.DangerRed)
                        Spacer(Modifier.height(8.dp))
                        Text(text = s.message, fontSize = 13.sp, color = Color(0xFF757575), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AirpayColors.Primary)
                                .clickable { servicesViewModel.dismissSession() }
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(text = "Try Again", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
