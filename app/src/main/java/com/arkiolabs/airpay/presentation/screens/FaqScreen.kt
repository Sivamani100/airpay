package com.arkiolabs.airpay.presentation.screens

import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.presentation.permissions.openAccessibilitySettings
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

private val step1ImageRes: Int? = null
private val step2ImageRes: Int? = null
private val step3ImageRes: Int? = null

/**
 * Airpay Help Center Screen.
 *
 *  - Header: Back arrow, "Help Center", language selector dropdown pill ("English ▾").
 *  - Diagnostic cards and recent transaction assistance.
 *  - Offline UPI guides and carrier setup documentation.
 */
@Composable
fun FaqScreen(
    onClose: () -> Unit,
    onCheckPermissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val context = LocalContext.current
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Top Bar Header (Airpay Style) ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_arrow_back_black),
                        contentDescription = "Back",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "Help Center",
                    fontSize = 20.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }

            // Language Selector Pill ("English ▾")
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(20.dp))
                    .clickable { /* Language Picker */ }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "English",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1C1E)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "▾",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ── Diagnostic / View All Tickets Card (Airpay Style) ──────
            val cardShape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), cardShape)
                    .clickable { /* Tickets */ }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF3E5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bottom_nav_history_selected),
                            contentDescription = null,
                            tint = AirpayColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "View all tickets & queries",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = "Track status of offline transactions",
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Section: Need help with a recent transaction? ───────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Need help with a recent transaction?",
                    fontSize = 14.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )

                Text(
                    text = "View All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AirpayColors.Primary,
                    modifier = Modifier.clickable { onClose() }
                )
            }

            Spacer(Modifier.height(10.dp))

            // Recent Transaction Help Card (Matching Screenshot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEEEEE), cardShape)
                    .clickable { /* Recent Txn Help */ }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF4F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↗",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Paid to",
                            fontSize = 10.sp,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = "SIRISHA PALIVELA",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = "3 hours ago",
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹5",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Debited from",
                            fontSize = 10.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D47A1))
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── MyUPI Row (Airpay Style) ───────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* MyUPI */ }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "पे",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "MyUPI Offline Services",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = "Direct SIM dialing, secure routing & payment automation",
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            Spacer(Modifier.height(20.dp))

            // ── Section: Help Topics ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Help Topics",
                    fontSize = 15.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )

                Text(
                    text = "View All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AirpayColors.Primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // ACCORDION Q&A TOPICS (Airpay Guides)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            ExpandableHelpTopic(
                question = "How can I keep my UPI payments safe and secure?",
                badge = "Security"
            ) {
                Text(
                    text = "Airpay keeps your transactions 100% private and offline:\n\n" +
                        "• Volatile Memory PIN: Your UPI PIN is cleared within 500ms of session completion and is never written to disk or logs.\n" +
                        "• Zero Internet: Airpay communicates strictly over secure encrypted cellular channels, protecting against WiFi snooping.\n" +
                        "• Screen Privacy: Never share your screen via Zoom, AnyDesk or TeamViewer while entering your UPI PIN.",
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    lineHeight = 18.sp
                )
            }

            ExpandableHelpTopic(
                question = "How do I enable offline cellular banking?",
                badge = "ONE-TIME"
            ) {
                Text(
                    text = "Before using Airpay, link your bank to the offline cellular channel once from your phone dialer.",
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
                Spacer(Modifier.height(10.dp))
                AirpayNumberedStep(1, "Open dialer to activate cellular banking")
                AirpayNumberedStep(2, "Enter your primary bank name")
                AirpayNumberedStep(3, "Follow the on-screen prompts to link your account")
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AirpayColors.Primary)
                        .clickable {
                            val i = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:*99${Uri.encode("#")}")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(i) }
                        }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Activate Cellular Channel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            ExpandableHelpTopic(question = "How do I enable offline payments in Airpay?") {
                Text(
                    text = "In Airpay, enable the offline payment service under Settings. Once done, you're all set.",
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
                Spacer(Modifier.height(10.dp))
                AirpayNumberedStep(1, "Open Airpay → tap your profile avatar (top-left)")
                AirpayImageSlot(res = step1ImageRes)
                Spacer(Modifier.height(8.dp))
                AirpayNumberedStep(2, "Scroll down → tap Settings")
                AirpayImageSlot(res = step2ImageRes)
                Spacer(Modifier.height(8.dp))
                AirpayNumberedStep(3, "Find \"Payment automation\" → toggle ON")
                AirpayImageSlot(res = step3ImageRes)
            }

            ExpandableHelpTopic(question = "Which SIM carriers are supported offline?") {
                Text(
                    text = "Airtel, Vodafone (Vi), BSNL, and Reliance Jio — all four major Indian carriers are fully supported! Airpay uses direct, high-speed offline cellular channels across all networks, processing payments directly without requiring internet data.",
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AirpayCarrierTag("Airtel ✓", true)
                    AirpayCarrierTag("Vi ✓", true)
                    AirpayCarrierTag("BSNL ✓", true)
                    AirpayCarrierTag("Jio ✓", true)
                }
            }

            ExpandableHelpTopic(question = "How do I enable the Accessibility Service?") {
                AirpayNumberedStep(1, "Open phone Settings → Accessibility → Installed apps")
                AirpayNumberedStep(2, "Find Airpay → toggle ON → confirm the dialog")
                AirpayNumberedStep(3, "Return to Airpay. Automation is now active")
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF4F4F6))
                        .clickable { openAccessibilitySettings(context) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Open Accessibility Settings →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AirpayColors.Primary
                    )
                }
            }

            ExpandableHelpTopic(
                question = "Can't enable accessibility? (Android 13+ restricted settings fix)",
                badge = "Fix"
            ) {
                Text(
                    text = "On Android 13+, sideloaded apps require one-time approval for Accessibility:\n\n" +
                        "1. Open Settings → Apps → Airpay\n" +
                        "2. Tap the ⋮ three dots menu (top right)\n" +
                        "3. Tap \"Allow restricted settings\" → confirm with PIN/fingerprint\n" +
                        "4. Return to Airpay and turn on Accessibility.",
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF4F4F6))
                        .clickable {
                            openAccessibilitySettings(context)
                        }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Open Accessibility Settings →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AirpayColors.Primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))



            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9F9FB))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onCheckPermissions()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = AirpayColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Review Offline UPI Permissions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9F9FB))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = AirpayColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Official Support Email",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = "mallipurapusiva@gmail.com",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ExpandableHelpTopic(
    question: String,
    badge: String? = null,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "qa_arrow"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                expanded = !expanded
            }
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1C1E),
                    lineHeight = 18.sp
                )

                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE8EAF6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AirpayColors.Primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Color(0xFF757575),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 2.dp, end = 2.dp)
            ) {
                content()
            }
        }

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFF4F4F6), thickness = 1.dp)
    }
}

@Composable
private fun AirpayNumberedStep(index: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(AirpayColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF424242)
        )
    }
}

@Composable
private fun AirpayCarrierTag(name: String, supported: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (supported) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (supported) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}

@Composable
private fun AirpayImageSlot(res: Int?) {
    if (res != null) {
        Spacer(Modifier.height(8.dp))
        Image(
            painter = painterResource(id = res),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}
