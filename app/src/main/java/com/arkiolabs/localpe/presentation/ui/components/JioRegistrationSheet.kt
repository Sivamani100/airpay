package com.arkiolabs.localpe.presentation.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.arkiolabs.localpe.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.localpe.presentation.ui.theme.Iconsax
import com.arkiolabs.localpe.presentation.ui.theme.PhonePeColors

private val POPULAR_BANKS = listOf(
    "State Bank of India",
    "HDFC Bank",
    "ICICI Bank",
    "Axis Bank",
    "Punjab National Bank",
    "Bank of Baroda",
    "Canara Bank",
    "Union Bank of India",
    "Kotak Mahindra Bank"
)

private val POPULAR_LANGUAGES = listOf(
    "English" to "2",
    "Hindi" to "1",
    "Telugu" to "4",
    "Kannada" to "3",
    "Tamil" to "5",
    "Marathi" to "6"
)

/**
 * BottomSheet prompting Jio SIM users to link/register their bank account
 * with the official NPCI UPI 123PAY voice gateway (08045163666).
 *
 * This prevents automated payment failures caused by unlinked accounts
 * and guides the user through the 1-time setup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JioRegistrationSheet(
    onDismiss: () -> Unit,
    onRegistrationConfirmed: (bankName: String, language: String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedBank by remember { mutableStateOf(POPULAR_BANKS.first()) }
    var selectedLanguage by remember { mutableStateOf(POPULAR_LANGUAGES.first()) }
    var isCallStepActive by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEDE7F6))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "JIO OFFLINE UPI 123PAY SETUP",
                    fontSize = 11.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = PhonePeColors.Primary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Link Bank Account for Jio",
                fontSize = 20.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Before making offline payments on Jio, NPCI requires your bank account to be linked once with UPI 123PAY (080 4516 3666).",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(18.dp))

            if (!isCallStepActive) {
                // Step 1: Select Bank
                Text(
                    text = "1. Select Your Primary Bank",
                    fontSize = 14.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    POPULAR_BANKS.take(5).forEach { bank ->
                        val isSelected = bank == selectedBank
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PhonePeColors.Primary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(if (isSelected) Color(0xFFF5F3FB) else Color.White)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    selectedBank = bank
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = bank,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PhonePeColors.Primary else Color(0xFF1E293B)
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(PhonePeColors.Primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Iconsax.TickCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Step 2: Preferred Language
                Text(
                    text = "2. Preferred Voice Language",
                    fontSize = 14.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(POPULAR_LANGUAGES) { (lang, code) ->
                        val isSelected = selectedLanguage.first == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PhonePeColors.Primary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) Color(0xFFEDE7F6) else Color.White)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    selectedLanguage = Pair(lang, code)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PhonePeColors.Primary else Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Actions
                // A) Call to register now
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PhonePeColors.Primary)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            isCallStepActive = true
                            dialRegistrationCall(context, selectedLanguage.second)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Call 080 4516 3666 to Link",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(10.dp))

                // B) Already registered option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onRegistrationConfirmed(selectedBank, selectedLanguage.first)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "I Have Already Linked My Bank",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }
            } else {
                // Post-call confirmation view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Registration in Progress",
                            fontSize = 14.sp,
                            fontFamily = ClashGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Follow the voice call on 080 4516 3666 to confirm your language and link $selectedBank.",
                            fontSize = 12.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PhonePeColors.SuccessGreen)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onRegistrationConfirmed(selectedBank, selectedLanguage.first)
                            onDismiss()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Yes, Bank Account Linked! ✓",
                        fontSize = 15.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable {
                            dialRegistrationCall(context, selectedLanguage.second)
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Call Again (080 4516 3666)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

private fun dialRegistrationCall(context: Context, languageCode: String) {
    try {
        // Dial 08045163666 with automated pause for language selection
        val dialUri = Uri.parse("tel:08045163666,,$languageCode")
        val intent = Intent(Intent.ACTION_CALL, dialUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08045163666")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
        } catch (_: Exception) {}
    }
}
