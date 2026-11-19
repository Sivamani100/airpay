package com.arkiolabs.airpay.presentation.bottomsheets

import android.content.Context
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

data class SimOption(
    val slotIndex: Int,
    val subscriptionId: Int,
    val title: String,
    val carrierName: String
)

/**
 * Dedicated Bottom Sheet screen for SIM Card Preference.
 * Lets the user pick which SIM card is linked to their bank account for offline UPI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimPreferenceSheet(
    onSimSelected: (slot: Int, carrier: String?, subId: Int?) -> Unit,
    onDismiss: () -> Unit,
    currentSlot: Int? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    var detectedSims by remember { mutableStateOf<List<SimOption>>(emptyList()) }
    var selectedSlot by remember { mutableIntStateOf(currentSlot ?: 0) }

    LaunchedEffect(Unit) {
        val list = mutableListOf<SimOption>()
        try {
            val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val subs = sm?.activeSubscriptionInfoList
            if (!subs.isNullOrEmpty()) {
                subs.forEach { sub ->
                    val carrier = sub.carrierName?.toString() ?: sub.displayName?.toString() ?: "Telecom SIM"
                    list.add(
                        SimOption(
                            slotIndex = sub.simSlotIndex,
                            subscriptionId = sub.subscriptionId,
                            title = "SIM ${sub.simSlotIndex + 1}: $carrier",
                            carrierName = carrier
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            // Permission fallback
        }

        if (list.isEmpty()) {
            list.add(SimOption(0, -1, "SIM 1 (Slot 0)", "Default Carrier"))
            list.add(SimOption(1, -1, "SIM 2 (Slot 1)", "Secondary Carrier"))
        }

        detectedSims = list
        if (currentSlot == null && list.isNotEmpty()) {
            selectedSlot = list[0].slotIndex
        }
    }

    val selectedOption = detectedSims.find { it.slotIndex == selectedSlot } ?: detectedSims.firstOrNull()

    BaseBottomSheetContainer(
        title = "Select Primary SIM",
        onDismissRequest = onDismiss,
        primaryButtonText = "Confirm SIM Preference",
        onPrimaryClick = {
            onSimSelected(
                selectedOption?.slotIndex ?: 0,
                selectedOption?.carrierName,
                selectedOption?.subscriptionId?.takeIf { it >= 0 }
            )
        },
        secondaryButtonText = "Decide Later",
        onSecondaryClick = onDismiss,
        modifier = modifier
    ) {
        // Hero Icon Badge (Saffron Orange from official logo)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AirpayColors.Saffron)
                .border(3.dp, AirpayColors.SaffronLight.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AirpayColors.SaffronTint)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "BANK LINKED NUMBER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.SaffronDark,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Which SIM is Linked to Your Bank?",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AirpayColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Select the mobile number registered with your bank account. Airpay will use this SIM for all offline *99# USSD dials and balance inquiries.",
            fontSize = 13.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(20.dp))

        // SIM Options List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            detectedSims.forEach { sim ->
                val isSelected = (sim.slotIndex == selectedSlot)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) AirpayColors.SaffronTint.copy(alpha = 0.35f) else Color(0xFFF9FAFB))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) AirpayColors.Primary else Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            selectedSlot = sim.slotIndex
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AirpayColors.Primary else Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${sim.slotIndex + 1}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF4B5563)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column {
                            Text(
                                text = sim.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AirpayColors.TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (isSelected) "Active for offline payments" else "Slot ${sim.slotIndex}",
                                fontSize = 12.sp,
                                color = if (isSelected) AirpayColors.SaffronDark else Color(0xFF6B7280)
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            selectedSlot = sim.slotIndex
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AirpayColors.Primary,
                            unselectedColor = Color(0xFF9CA3AF)
                        )
                    )
                }
            }
        }
    }
}
