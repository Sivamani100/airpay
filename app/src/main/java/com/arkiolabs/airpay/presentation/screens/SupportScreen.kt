package com.arkiolabs.airpay.presentation.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.platform.ApkShareUtil
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Dedicated "Support Us" Screen.
 * Provides merchant advocacy, community sharing, and official feedback email.
 */
@Composable
fun SupportScreen(
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
                text = "Support Us",
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ── Hero Banner ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(AirpayColors.Primary, Color(0xFF14243B))
                        )
                    )
                    .padding(22.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AirpayColors.Saffron.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = AirpayColors.Saffron,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Empowering Offline India",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.Saffron
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Help 1.4 Billion People Pay Without Internet",
                        fontSize = 18.sp,
                        fontFamily = ClashGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 24.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Airpay was created to ensure that digital financial access in India never stops when mobile data is down, servers crash, or you are in a remote basement or rural area.",
                        fontSize = 13.sp,
                        color = Color(0xFFCFD8DC),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Action 1: Spread the Word & Share APK ───────────────
            SupportCard(
                icon = Icons.Default.Share,
                iconBg = AirpayColors.SaffronTint,
                iconTint = AirpayColors.Primary,
                title = "Share Airpay with Friends",
                description = "Send the offline payment app directly via Bluetooth, WhatsApp, or Quick Share so your friends can pay even when data plans expire.",
                buttonText = "Share Airpay APK",
                onButtonClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    ApkShareUtil.shareInstalledApk(context)
                }
            )

            Spacer(Modifier.height(16.dp))

            // ── Action 2: Merchant Advocacy ─────────────────────────
            SupportCard(
                icon = Icons.Default.Store,
                iconBg = Color(0xFFE8F5E9),
                iconTint = AirpayColors.SuccessGreen,
                title = "Advocate for Local Merchants",
                description = "Tell local vegetable vendors, tea stalls, and small retail shops about Airpay. They can receive offline payments with zero monthly machine fees or Wi-Fi dependencies.",
                buttonText = "Share Merchant Guide",
                onButtonClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Accept Offline UPI with Airpay")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Support your small business with 100% Offline UPI! Airpay enables customers to scan your Bharat QR and pay even without internet. Download Airpay today."
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share with Merchants"))
                }
            )

            Spacer(Modifier.height(16.dp))

            // ── Action 3: Official Support & Inquiries ──────────────
            SupportCard(
                icon = Icons.Default.Email,
                iconBg = Color(0xFFE3F2FD),
                iconTint = Color(0xFF1976D2),
                title = "Get in Touch with Arkio Labs",
                description = "Have suggestions, feature requests, or encountered a bank issue? Our engineering team reads every message directly.",
                buttonText = "Email Developer",
                onButtonClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    sendSupportEmail(context)
                }
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SupportCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = AirpayColors.TextPrimary
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = description,
            fontSize = 13.sp,
            color = AirpayColors.TextSecondary,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AirpayColors.Primary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = buttonText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun sendSupportEmail(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:mallipurapusiva@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Airpay Support & Feedback")
            putExtra(Intent.EXTRA_TEXT, "Hi Airpay Team,\n\nI am writing to share feedback regarding Airpay Offline UPI:\n\n")
        }
        context.startActivity(Intent.createChooser(intent, "Send Email via"))
    } catch (_: Exception) {
        Toast.makeText(context, "Support email: mallipurapusiva@gmail.com", Toast.LENGTH_LONG).show()
    }
}
