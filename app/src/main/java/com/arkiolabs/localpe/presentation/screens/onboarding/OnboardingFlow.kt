package com.arkiolabs.localpe.presentation.screens.onboarding

import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.arkiolabs.localpe.R
import com.arkiolabs.localpe.presentation.permissions.PermissionStatus
import com.arkiolabs.localpe.presentation.permissions.openAccessibilitySettings
import com.arkiolabs.localpe.presentation.permissions.openOverlaySettings
import com.arkiolabs.localpe.presentation.permissions.rememberPermissionLaunchers
import com.arkiolabs.localpe.presentation.permissions.rememberPermissionStatus
import com.arkiolabs.localpe.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.localpe.presentation.ui.theme.PhonePeColors
import kotlinx.coroutines.launch

private val bhimStep1ImageRes: Int? = R.drawable.bhim_step1
private val bhimStep2ImageRes: Int? = R.drawable.bhim_step2
private val bhimStep3ImageRes: Int? = R.drawable.bhim_step3

private const val TUTORIAL_VIDEO_URL: String =
    "https://youtube.com/playlist?list=PL6zhuU_l94t1y25MDt96Z-MltD3S6iPFj&si=GNlanTwR-IcfOBI"

/**
 * Authentic PhonePe Styled Onboarding Flow.
 *
 * Designed on pure white (#FFFFFF) with signature PhonePe purple accents,
 * Inter typography, and clean cards.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFlow(onComplete: () -> Unit) {
    val totalPages = 6
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    val permissionsState = rememberPermissionStatus()
    val permissions = permissionsState.value

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> Star99ExplainerPage()
                2 -> Star99BankingSetupPage()
                3 -> BhimSetupPage(onSkip = {
                    scope.launch { pagerState.animateScrollToPage(4) }
                })
                4 -> PermissionsPage(permissions = permissions)
                5 -> ReadyPage()
            }
        }

        Spacer(Modifier.height(8.dp))
        PageIndicator(current = pagerState.currentPage, total = totalPages)
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            val isLast = pagerState.currentPage == totalPages - 1
            val buttonText = when (pagerState.currentPage) {
                0 -> "Continue"
                1 -> "Got It"
                2 -> "Next"
                3 -> "Continue"
                4 -> if (permissions.readyForOverlayPay) "Looks Good" else "Continue Anyway"
                else -> "Start Using LocalPe"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(PhonePeColors.Primary)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        if (isLast) {
                            onComplete()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    fontSize = 15.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PageIndicator(current: Int, total: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(total) { i ->
            val isActive = i == current
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 7.dp,
                animationSpec = spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMedium),
                label = "indicator_w_$i"
            )
            Box(
                Modifier
                    .size(width = width, height = 7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isActive) PhonePeColors.Primary else Color(0xFFE0E0E0))
            )
            if (i < total - 1) Spacer(Modifier.width(6.dp))
        }
    }
}

// ── Page 1: Welcome ──

@Composable
private fun WelcomePage() {
    val context = LocalContext.current
    val transition = rememberInfiniteTransition(label = "welcome")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(12.dp)
                    .alpha(pulse)
                    .clip(CircleShape)
                    .background(PhonePeColors.Primary)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "100% OFFLINE UPI",
                fontSize = 12.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = PhonePeColors.Primary
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "LocalPe",
            fontSize = 44.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Send money over UPI without internet or mobile data. Direct cellular voice channel via *99# and VoLTE.",
            fontSize = 15.sp,
            color = Color(0xFF555555),
            lineHeight = 22.sp
        )

        TUTORIAL_VIDEO_URL?.let { url ->
            Spacer(Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF4F0FB))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(intent) }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = PhonePeColors.Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Watch tutorial video",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PhonePeColors.Primary
                )
            }
        }
    }
}

// ── Page 2: What is *99# ──

@Composable
private fun Star99ExplainerPage() {
    val cardShape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WHY *99#",
            fontSize = 12.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = PhonePeColors.Primary
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "USSD makes UPI work without internet.",
            fontSize = 22.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(Color(0xFFF9F9FB))
                .border(1.dp, Color(0xFFEEEEEE), cardShape)
                .padding(16.dp)
        ) {
            Text(
                text = "*99# is NPCI's official USSD service for offline UPI. It rides directly on your phone's cellular voice signaling channel — so it works in rural areas, underground spots, and crowded places where 4G/5G data fails.",
                fontSize = 14.sp,
                color = Color(0xFF444444),
                lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OnboardingCarrierPill("AIRTEL", supported = true, modifier = Modifier.weight(1f))
            OnboardingCarrierPill("VI", supported = true, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OnboardingCarrierPill("BSNL", supported = true, modifier = Modifier.weight(1f))
            OnboardingCarrierPill("JIO", supported = true, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun OnboardingCarrierPill(name: String, supported: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFFF9F9FB))
            .border(1.dp, Color(0xFFEEEEEE), shape)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (supported) Color(0xFF2E7D32) else Color(0xFFC62828))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
        }
    }
}

// ── Page 3: *99# Banking Setup ──

@Composable
private fun Star99BankingSetupPage() {
    val context = LocalContext.current
    val cardShape = RoundedCornerShape(12.dp)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF4F0FB))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "⚡ ONE-TIME SETUP",
                fontSize = 11.sp,
                fontFamily = ClashGrotesk,
                fontWeight = FontWeight.Bold,
                color = PhonePeColors.Primary
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Enable Banking via *99#",
            fontSize = 22.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Link your bank account to the *99# USSD channel once from your dialer.",
            fontSize = 13.sp,
            color = Color(0xFF555555)
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(Color(0xFFF9F9FB))
                .border(1.dp, Color(0xFFEEEEEE), cardShape)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OnboardingStep(1, "Open phone dialer and dial *99#")
                OnboardingStep(2, "Enter your bank name (e.g. SBI, HDFC, ICICI)")
                OnboardingStep(3, "Follow on-screen prompts to link your account")
            }
        }

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PhonePeColors.Primary)
                .clickable {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*99${Uri.encode("#")}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(dialIntent) }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Dial *99# Now",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ── Page 4: BHIM setup ──

@Composable
private fun BhimSetupPage(onSkip: () -> Unit) {
    val cardShape = RoundedCornerShape(12.dp)
    val bhimContext = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "BHIM INTEGRATION",
            fontSize = 11.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = PhonePeColors.Primary
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Enable USSD in BHIM",
            fontSize = 22.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "If you use the BHIM app, turn on *99# under Settings.",
            fontSize = 13.sp,
            color = Color(0xFF555555)
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(Color(0xFFF9F9FB))
                .border(1.dp, Color(0xFFEEEEEE), cardShape)
                .padding(16.dp)
        ) {
            Column {
                OnboardingStep(1, "Open BHIM app → tap your profile avatar (top-left)")
                OnboardingImageSlot(res = bhimStep1ImageRes)
                Spacer(Modifier.height(12.dp))
                OnboardingStep(2, "Scroll down → tap Settings")
                OnboardingImageSlot(res = bhimStep2ImageRes)
                Spacer(Modifier.height(12.dp))
                OnboardingStep(3, "Find \"USSD service (*99#)\" under Account → toggle ON")
                OnboardingImageSlot(res = bhimStep3ImageRes)
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clickable { onSkip() }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Already done? Skip →",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PhonePeColors.Primary
            )
        }
    }
}

// ── Page 5: Permissions ──

private data class OnboardingPermInfo(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val short: String,
    val why: String,
    val granted: Boolean,
    val onGrant: () -> Unit
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PermissionsPage(permissions: PermissionStatus) {
    val context = LocalContext.current
    val launchers = rememberPermissionLaunchers()

    val items = listOf(
        OnboardingPermInfo(
            key = "phone",
            icon = Icons.Default.Phone,
            title = "Phone & SIM",
            short = "Dial *99# and detect carrier.",
            why = "LocalPe dials *99# through Android's telephony API and detects your carrier (Airtel, Vi, BSNL, Jio) to select the right channel.",
            granted = permissions.phoneBundle,
            onGrant = { launchers.requestPhoneBundle() }
        ),
        OnboardingPermInfo(
            key = "camera",
            icon = Icons.Default.Camera,
            title = "Camera",
            short = "Scan UPI QR codes offline.",
            why = "Camera access is used locally on-device to decode QR codes and fill amount and UPI ID.",
            granted = permissions.camera,
            onGrant = { launchers.requestCamera() }
        ),
        OnboardingPermInfo(
            key = "accessibility",
            icon = Icons.Default.Settings,
            title = "Accessibility",
            short = "Read & reply to USSD dialogs automatically.",
            why = "In Auto mode, Accessibility fills in amounts and confirms screens automatically.",
            granted = permissions.accessibility,
            onGrant = { openAccessibilitySettings(context) }
        ),
        OnboardingPermInfo(
            key = "overlay",
            icon = Icons.Default.Layers,
            title = "Display Over Other Apps",
            short = "Show the seamless LocalPe overlay.",
            why = "Presents the clean LocalPe payment progress card directly over system carrier dialogs.",
            granted = permissions.overlay,
            onGrant = { openOverlaySettings(context) }
        )
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "PERMISSIONS",
            fontSize = 11.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = PhonePeColors.Primary
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Required for Automation",
            fontSize = 22.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )

        Spacer(Modifier.height(16.dp))

        items.forEach { info ->
            OnboardingPermissionCard(info = info)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun OnboardingPermissionCard(info: OnboardingPermInfo) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF9F9FB))
            .border(1.dp, Color(0xFFEEEEEE), shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (info.granted) Color(0xFFE8F5E9) else Color(0xFFEDE7F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = null,
                tint = if (info.granted) Color(0xFF2E7D32) else PhonePeColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = info.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = info.short,
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }

        Spacer(Modifier.width(10.dp))

        if (info.granted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "GRANTED ✓",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PhonePeColors.Primary)
                    .clickable { info.onGrant() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "GRANT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ── Page 6: Ready ──

@Composable
private fun ReadyPage() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "YOU'RE ALL SET",
            fontSize = 12.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = PhonePeColors.Primary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Ready to Pay Offline",
            fontSize = 24.sp,
            fontFamily = ClashGrotesk,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Scan any QR or enter a mobile number to start sending money with 0 internet.",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnboardingStep(index: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(PhonePeColors.Primary),
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
            color = Color(0xFF333333)
        )
    }
}

@Composable
private fun OnboardingImageSlot(res: Int?) {
    if (res != null) {
        Spacer(Modifier.height(8.dp))
        Image(
            painter = painterResource(id = res),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}
