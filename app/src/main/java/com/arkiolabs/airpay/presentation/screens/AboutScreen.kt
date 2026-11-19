package com.arkiolabs.airpay.presentation.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Dedicated "About Airpay" Screen.
 * Showcases the square app logo, offline architectural guarantees, and contact info.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFD))
            .statusBarsPadding()
    ) {
        // ── Top App Bar ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AirpayColors.TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = "About Airpay",
                fontSize = 19.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            // ── Centered Square App Logo ────────────────────────────
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFEEEEEE), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_airpay_logo),
                    contentDescription = "Airpay Logo",
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Airpay",
                fontSize = 24.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Version 2.4.0 (Offline Edition)",
                fontSize = 13.sp,
                color = AirpayColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "100% Offline UPI Payments · Made in India",
                fontSize = 12.sp,
                color = AirpayColors.SaffronDark,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(24.dp))

            // ── Architectural Guarantees Card ───────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(18.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Zero-Internet Architecture",
                    fontSize = 15.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )

                AboutFeatureRow(
                    icon = Icons.Default.PublicOff,
                    iconBg = AirpayColors.SaffronTint,
                    iconTint = AirpayColors.SaffronDark,
                    title = "No Internet Connection Needed",
                    body = "Payments execute directly through your telecom provider's SIM infrastructure to NPCI. Mobile data and Wi-Fi are never accessed."
                )

                HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)

                AboutFeatureRow(
                    icon = Icons.Default.Security,
                    iconBg = Color(0xFFE8F5E9),
                    iconTint = AirpayColors.SuccessGreen,
                    title = "Zero External Cloud Servers",
                    body = "Airpay operates with no middleman servers. Everything is processed locally on your phone hardware and through bank telecom lines."
                )

                HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)

                AboutFeatureRow(
                    icon = Icons.Default.Lock,
                    iconBg = Color(0xFFEDE7F6),
                    iconTint = Color(0xFF512DA8),
                    title = "Volatile PIN Security",
                    body = "Your UPI PIN is never sent over internet channels and is handled strictly in accordance with official NPCI guidelines."
                )

                HorizontalDivider(color = Color(0xFFF0F0F2), thickness = 0.8.dp)

                AboutFeatureRow(
                    icon = Icons.Default.SimCard,
                    iconBg = Color(0xFFE1F5FE),
                    iconTint = Color(0xFF0288D1),
                    title = "Universal All-Carrier Support",
                    body = "Automated offline session routing for Airtel, Vi, BSNL, and dedicated VoLTE phone gateway for Reliance Jio."
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Creator & Contact Card ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "Arkio Labs",
                    fontSize = 15.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Engineered to deliver uninterrupted financial autonomy across India.",
                    fontSize = 12.sp,
                    color = AirpayColors.TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF5F7FA))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            sendDeveloperEmail(context)
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = AirpayColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Official Developer Contact",
                            fontSize = 11.sp,
                            color = AirpayColors.TextSecondary
                        )
                        Text(
                            text = "mallipurapusiva@gmail.com",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AirpayColors.Primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "© 2026 Arkio Labs · All Rights Reserved",
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AboutFeatureRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    body: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = body,
                fontSize = 12.sp,
                color = AirpayColors.TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

private fun sendDeveloperEmail(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:mallipurapusiva@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Airpay Feedback")
        }
        context.startActivity(Intent.createChooser(intent, "Send Email via"))
    } catch (_: Exception) {
        Toast.makeText(context, "mallipurapusiva@gmail.com", Toast.LENGTH_LONG).show()
    }
}
