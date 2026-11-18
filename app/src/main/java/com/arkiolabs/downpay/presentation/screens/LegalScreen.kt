package com.arkiolabs.downpay.presentation.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.arkiolabs.downpay.R
import com.arkiolabs.downpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.downpay.presentation.ui.theme.DownpayColors

/**
 * Effective date stamped on every legal screen.
 */
private const val EFFECTIVE_DATE = "18 November 2026"

/**
 * Privacy policy screen — reachable from Settings → "Legal" → "Privacy Policy".
 */
@Composable
fun PrivacyScreen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    LegalDocumentScaffold(
        title = "Privacy Policy",
        effectiveDate = EFFECTIVE_DATE,
        onClose = onClose,
        modifier = modifier
    ) {
        PrivacyContent()
    }
}

/**
 * Terms of Use screen — reachable from Settings → "Legal" → "Terms of Use".
 */
@Composable
fun TermsScreen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    LegalDocumentScaffold(
        title = "Terms of Use",
        effectiveDate = EFFECTIVE_DATE,
        onClose = onClose,
        modifier = modifier
    ) {
        TermsContent()
    }
}

/**
 * Merged legal screen — Privacy Policy + Terms of Use in a single
 * screen with a toggle at the top to switch between them.
 */
@Composable
fun LegalScreen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    var showTerms by remember { mutableStateOf(false) }
    val view = LocalView.current

    Column(
        modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Legal",
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1C1C1E)
            )
        }

        // Tab toggle
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LegalTab(
                label = "Privacy Policy",
                selected = !showTerms,
                onClick = { showTerms = false },
                modifier = Modifier.weight(1f)
            )
            LegalTab(
                label = "Terms of Use",
                selected = showTerms,
                onClick = { showTerms = true },
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Effective $EFFECTIVE_DATE",
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Plain-language version. Read this in full — it covers what we do and don't do with your data, and what you're agreeing to when you use Downpay.",
                fontSize = 13.sp,
                color = Color(0xFF555555),
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(18.dp))

            if (!showTerms) {
                PrivacyContent()
            } else {
                TermsContent()
            }
        }
    }
}

@Composable
private fun LegalTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) DownpayColors.Primary else Color(0xFFF4F0FB))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = if (selected) Color.White else Color(0xFF555555)
        )
    }
}

@Composable
private fun PrivacyContent() {
    LegalSection(
        heading = "The short version",
        body = "Downpay does not collect, transmit, sell, share or back up any of your data to us or any third party. There is no Downpay account, no analytics, no advertising. Everything you do in Downpay stays on your device. After install, the app makes zero outbound network requests."
    )
    LegalSection(
        heading = "What Downpay handles",
        body = "Downpay needs a few pieces of information to interact with the carrier gateway on your behalf and answer the carrier's prompts. Your UPI ID, payment amount and optional note live in app memory during a session, and on success are saved to an encrypted on-device history. The carrier's reply text is shown to you and recorded for successful payments. Your active SIM's carrier name is read once on launch to detect Jio (which uses cellular voice gateway). Your operation-mode preference and last balance are saved in a private on-device key-value store. Scanned or imported QR codes are decoded in memory only — they are never stored."
    )
    LegalSection(
        heading = "What Downpay does NOT collect",
        body = "We do not read your name, email, phone number, contacts, location, IMEI, IMSI, advertising ID, or device ID. We do not have access to your bank account. The accessibility service is restricted by configuration to the system USSD dialog packages and ignores every other app and screen on your device."
    )
    LegalSection(
        heading = "Your UPI PIN",
        body = "Your UPI PIN lives only in volatile memory and is wiped within 500 milliseconds of any session ending (success, failure, timeout or cancel) and on app backgrounding. It is never written to storage, never logged, never sent over the network, and is masked as four bullets in any UI surface that might display it. The carrier-side handling of your PIN is the same path Downpay and your bank's own app use."
    )
    LegalSection(
        heading = "Encrypted history",
        body = "Successful payments are saved to a local SQLite database encrypted with SQLCipher. The raw file on disk is unreadable without the app's key. The database is capped at 200 most-recent records and you can clear it any time from the History screen. Uninstalling Downpay deletes everything."
    )
    LegalSection(
        heading = "Clipboard Privacy",
        body = "Downpay accesses your system clipboard strictly to copy the recipient's VPA / UPI ID (in Manual Mode) or read imported QR code texts. Clipboard content is processed transiently in volatile memory on-device and is never written to disk, stored, or transmitted to any server."
    )
    LegalSection(
        heading = "Screen Capture and Recording Privacy",
        body = "Downpay operates completely offline and lacks permission to query other running apps or monitor your operating system for active background screen recorders, casting services (e.g. Chromecast, Miracast), or remote control tools. To protect your financial data, ensure your screen is not being shared, cast, or recorded by other apps when entering your UPI PIN."
    )
    LegalSection(
        heading = "Permissions",
        body = "Phone (CALL_PHONE): only ever used to dial carrier payment codes, never regular numbers. Camera: live QR scanning, processed on-device by ML Kit, frames are not stored or uploaded. Phone state (READ_PHONE_STATE): reads the carrier name to detect network support. Accessibility service: reads the carrier USSD dialog and types replies for you, restricted to known carrier dialog packages. Display over other apps (SYSTEM_ALERT_WINDOW): paints the Downpay UI over the carrier dialog in Auto mode. Denying any optional permission still leaves Manual mode fully usable."
    )
    LegalSection(
        heading = "Children",
        body = "Downpay is intended for users old enough to operate a personal UPI account. We do not knowingly direct Downpay at children, and we do not collect any data that would let us identify a child or anyone else."
    )
    LegalSection(
        heading = "Changes",
        body = "If we ever change how Downpay handles data, this screen will be updated, the effective date will be bumped and the change will be highlighted in the corresponding release notes."
    )
    LegalSection(
        heading = "Contact",
        body = "Questions or concerns about privacy? Contact Arkio Labs support at mallipurapusiva@gmail.com."
    )
}

@Composable
private fun TermsContent() {
    LegalSection(
        heading = "About Downpay",
        body = "Downpay is published by Arkio Labs. It is an Android client that automates carrier USSD and offline cellular payments so you can use UPI without an internet connection. Downpay is NOT affiliated with any banking corporation, telecom carrier, or external payment provider. Every transaction is processed by your bank and your carrier through the same infrastructure that powers standard offline UPI services — Downpay only automates the on-screen interaction."
    )
    LegalSection(
        heading = "Eligibility",
        body = "By using Downpay you confirm that you are old enough to operate a personal bank account in India (typically 18+), that you own and control the SIM in the device, that the UPI ID and PIN you enter are yours, and that you are using Downpay in compliance with applicable laws and your bank's terms."
    )
    LegalSection(
        heading = "What you can expect",
        body = "Downpay is provided AS IS, on a best-effort basis. We try to make it work reliably across carriers, devices and Android versions, but we cannot guarantee that every payment will succeed every time. Carrier networks, bank PSPs and Android device makers can change behaviour without notice. If a payment fails for any reason, the carrier will surface the error and Downpay will pass it on to you verbatim — but we have no ability to reverse, refund or compensate any transaction."
    )
    LegalSection(
        heading = "Your responsibilities",
        body = "You are solely responsible for the accuracy of every UPI ID and amount you enter. Offline UPI transactions are typically irreversible once your bank confirms them. Always double-check the recipient before tapping Pay. You are also responsible for keeping your UPI PIN private and your device secure. Do not share screenshots that show a PIN being entered."
    )
    LegalSection(
        heading = "Network Security and Carrier Risks",
        body = "Downpay relies entirely on the cellular network's USSD signaling channel to execute transactions. You acknowledge and agree that USSD communications are handled over cellular networks. You use the service at your own risk. Downpay is not liable for financial loss, identity theft, or unauthorized transactions resulting from carrier network security failures or device-level compromises."
    )
    LegalSection(
        heading = "Clipboard Integrity",
        body = "To facilitate manual operations, Downpay may copy payment details (such as UPI IDs) to your device's system clipboard. If your device is infected with malware or malicious third-party applications, clipboard data may be read, intercepted, or altered. You are solely responsible for ensuring your device is free from malicious software. Always verify the recipient's details (VPA and Name) as shown on the final confirmation screen before entering your UPI PIN."
    )
    LegalSection(
        heading = "Screen Recording and Casting Risks",
        body = "Your UPI PIN is sensitive financial data. You must ensure that no screen recording, screen casting, mirroring, or remote-access applications are actively capturing or transmitting your screen while using Downpay. Entering your UPI PIN during an active capture session could expose your credentials to third parties. Downpay disclaims all liability for credentials compromised due to visual or capture-based recording."
    )
    LegalSection(
        heading = "Android OS Customizations and Accessibility Limitations",
        body = "Downpay's automated mode uses Android's Accessibility Service to read and respond to standard system USSD dialogs. However, custom Android distributions and vendor-specific skins frequently alter the rendering, layout, and structure of these dialogs. These customizations may cause the automation logic to fail, time out, or input data incorrectly. Users are advised to double-check all inputs before final authorization or switch to Manual Mode if dialog behavior is inconsistent."
    )
    LegalSection(
        heading = "Carrier and bank charges",
        body = "Your telecom carrier may charge a small per-session fee for carrier USSD usage as defined by TRAI tariffs. Downpay does not see, control or share in any such fee. Your bank's UPI transaction limits and rules apply unchanged."
    )
    LegalSection(
        heading = "Things you must not do",
        body = "You must not use Downpay to send payments to anyone other than the intended recipient, to attempt fraud, money laundering, or any activity that violates Indian law or your bank's terms. You must not use the accessibility service for anything other than the intended USSD automation flow within the app itself."
    )
    LegalSection(
        heading = "Limitation of liability",
        body = "To the maximum extent permitted by law, Arkio Labs Inc. and its affiliates are not liable for any direct, indirect, incidental or consequential loss arising from your use of the app — including lost funds, missed payments, carrier downtime, locked PINs, device-specific quirks, or third-party fees. Your sole remedy if you are unhappy with Downpay is to stop using it and uninstall it."
    )
    LegalSection(
        heading = "Changes to these terms",
        body = "We may update these terms over time. Material changes will be reflected here, with the effective date bumped, and called out in release notes. Continued use of the app after a change means you accept the updated terms."
    )
    LegalSection(
        heading = "Governing law",
        body = "These terms are governed by the laws of India. Any disputes arising from the use of Downpay will be subject to the courts of competent jurisdiction in India."
    )
    LegalSection(
        heading = "Contact",
        body = "For questions about these terms, contact Arkio Labs support at mallipurapusiva@gmail.com."
    )
}

// ─── Internal layout primitives (Downpay Clean White Style) ───────────────────

@Composable
private fun LegalDocumentScaffold(
    title: String,
    effectiveDate: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    Column(
        modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1C1C1E)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Effective $effectiveDate",
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Plain-language version. Read this in full — it covers what we do and don't do with your data, and what you're agreeing to when you use Downpay.",
                fontSize = 13.sp,
                color = Color(0xFF555555),
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(18.dp))

            content()
        }
    }
}

@Composable
private fun LegalSection(
    heading: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = heading,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1C1C1E)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 13.sp,
                color = Color(0xFF555555),
                lineHeight = 18.sp
            )
        }
    }
}
