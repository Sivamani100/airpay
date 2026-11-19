package com.arkiolabs.airpay.presentation.bottomsheets

import android.view.HapticFeedbackConstants
import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.platform.BiometricHelper
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Onboarding-style suggestion bottom sheet displayed after a user's first successful payment.
 * Encourages the user to enable Pay with Fingerprint using their captured UPI PIN.
 *
 * Conforms strictly to guidelines:
 * - Exactly 3 points explaining the feature
 * - Colors drawn from app logo (Primary Navy #08101D, Saffron #F7941D, Green #00A651)
 * - Optional / dismissible via "Maybe Later"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricSuggestionSheet(
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Icon Badge (Primary Navy with Emerald Green border)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AirpayColors.Primary)
                    .border(2.5.dp, AirpayColors.SuccessGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = AirpayColors.Saffron,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Pay with Fingerprint?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Fast, one-touch offline payments without typing your UPI PIN every time.",
                fontSize = 13.sp,
                color = AirpayColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(20.dp))

            // Exactly 3 Points Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ThreePointBenefitRow(
                    pointNumber = "1",
                    text = "Authorize payments instantly with your registered fingerprint"
                )
                ThreePointBenefitRow(
                    pointNumber = "2",
                    text = "Your UPI PIN remains encrypted and secured locally on device hardware"
                )
                ThreePointBenefitRow(
                    pointNumber = "3",
                    text = "If fingerprint or PIN ever fails, manual PIN entry is always available"
                )
            }

            Spacer(Modifier.height(24.dp))

            // Primary Action Button (Navy with rounded corners)
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    (context as? FragmentActivity)?.let { activity ->
                        BiometricHelper.prompt(
                            activity = activity,
                            title = "Enable Fingerprint Pay",
                            subtitle = "Verify fingerprint to enable biometric offline payments",
                            onSuccess = {
                                onEnable()
                                Toast.makeText(context, "Pay with Fingerprint enabled!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, "Verification: $err", Toast.LENGTH_SHORT).show()
                            },
                            onCancel = {}
                        )
                    } ?: onEnable()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AirpayColors.Primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Enable Fingerprint Pay",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            // Secondary "Maybe Later" Option
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onDismiss()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Maybe Later",
                    fontSize = 14.sp,
                    color = AirpayColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
