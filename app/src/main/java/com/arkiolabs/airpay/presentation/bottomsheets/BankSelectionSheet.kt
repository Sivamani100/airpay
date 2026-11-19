package com.arkiolabs.airpay.presentation.bottomsheets

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

val ALL_INDIAN_BANKS: List<String> = listOf(
    "State Bank of India (SBI)",
    "HDFC Bank",
    "ICICI Bank",
    "Axis Bank",
    "Punjab National Bank (PNB)",
    "Bank of Baroda",
    "Kotak Mahindra Bank",
    "Canara Bank",
    "Union Bank of India",
    "Bank of India",
    "IndusInd Bank",
    "Yes Bank",
    "IDFC FIRST Bank",
    "Central Bank of India",
    "Indian Bank",
    "UCO Bank",
    "Federal Bank",
    "Indian Overseas Bank"
)

/**
 * Dedicated Bottom Sheet screen for Bank Selection.
 * - Multiple choice list with radio button selection.
 * - Clean search bar to filter instantly.
 * - No extra paragraphs or clutter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSelectionSheet(
    onBankSelected: (bankName: String) -> Unit,
    currentBank: String? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf(currentBank ?: ALL_INDIAN_BANKS.first()) }

    val filteredBanks = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ALL_INDIAN_BANKS
        } else {
            ALL_INDIAN_BANKS.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    BaseBottomSheetContainer(
        title = "Select Your Bank",
        primaryButtonText = "Confirm Bank",
        primaryButtonEnabled = selectedBank.isNotBlank(),
        onPrimaryClick = {
            if (selectedBank.isNotBlank()) {
                onBankSelected(selectedBank)
            }
        },
        modifier = modifier
    ) {
        // Search Bar Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF4F5F7))
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = AirpayColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search bank name...",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(Modifier.height(14.dp))

        // Multiple Choice Radio Button Bank List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredBanks) { bank ->
                val isSelected = (bank == selectedBank)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) AirpayColors.SaffronTint.copy(alpha = 0.45f) else Color(0xFFF9FAFB))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) AirpayColors.Primary else Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            selectedBank = bank
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = bank,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AirpayColors.Primary else AirpayColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            selectedBank = bank
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
