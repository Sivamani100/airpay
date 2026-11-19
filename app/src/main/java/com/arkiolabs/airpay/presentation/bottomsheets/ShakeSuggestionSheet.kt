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
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Suggestion bottom sheet shown when the user has completed 3 or more transactions.
 * Suggests enabling "Shake to Scan" and optionally "Open Scanner on Launch".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShakeSuggestionSheet(
    onEnable: (openOnLaunch: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var alsoOpenOnLaunch by remember { mutableStateOf(false) }

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
            // Hero Icon Badge (Saffron accent)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AirpayColors.SaffronTint)
                    .border(2.5.dp, AirpayColors.Saffron, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = AirpayColors.Primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Enable Shake to Scan?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Shake your phone anywhere in the app to immediately open the offline QR scanner.",
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
                    text = "Shake your phone gently to open the camera QR scanner in a split second"
                )
                ThreePointBenefitRow(
                    pointNumber = "2",
                    text = "Skip menu clicks and pay instantly at busy merchant counters"
                )
                ThreePointBenefitRow(
                    pointNumber = "3",
                    text = "Option to also open the camera scanner automatically when app launches"
                )
            }

            Spacer(Modifier.height(16.dp))

            // Optional Checkbox: Open Scanner on App Launch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F7FA))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        alsoOpenOnLaunch = !alsoOpenOnLaunch
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = alsoOpenOnLaunch,
                    onCheckedChange = { alsoOpenOnLaunch = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AirpayColors.Primary,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Also open scanner automatically on app launch",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AirpayColors.TextPrimary
                )
            }

            Spacer(Modifier.height(20.dp))

            // Primary Action Button
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onEnable(alsoOpenOnLaunch)
                    Toast.makeText(context, "Shake to Scan enabled!", Toast.LENGTH_SHORT).show()
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
                    text = "Turn On Shake to Scan",
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
