package com.arkiolabs.downpay.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.arkiolabs.downpay.platform.QrScannerManager
import com.arkiolabs.downpay.presentation.permissions.openAppDetailsSettings
import androidx.compose.ui.res.painterResource
import com.arkiolabs.downpay.R
import com.arkiolabs.downpay.presentation.ui.components.DownpayLottieAnimation
import com.arkiolabs.downpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.downpay.presentation.ui.theme.DownpayColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Downpay Styled QR Scanner Screen.
 *
 * Implements the exact layout and placement from Downpay screenshot:
 *  - Top bar: Back arrow, "Scan any QR" (bold white), Help (?) icon.
 *  - Viewfinder with signature vibrant purple corner brackets.
 *  - Scrim cutout over camera preview.
 *  - Two bottom circular translucent buttons: "Upload QR" (gallery) and "Torch" (flashlight).
 *  - Bottom "Downpay | UPI" / "Offline Cellular" badge.
 */
@Composable
fun ScanScreen(
    qrManager: QrScannerManager,
    onResult: (String) -> Unit,
    onClose: () -> Unit,
    onOpenFaq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) permissionDenied = true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) cameraLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            hasCameraPermission -> DownpayCameraContent(
                qrManager = qrManager,
                onResult = onResult,
                onClose = onClose,
                onOpenFaq = onOpenFaq
            )
            else -> ScannerPermissionDenied(
                permanentlyDenied = permissionDenied,
                onGrant = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                onOpenSettings = { openAppDetailsSettings(context) },
                onClose = onClose
            )
        }
    }
}

@Composable
private fun DownpayCameraContent(
    qrManager: QrScannerManager,
    onResult: (String) -> Unit,
    onClose: () -> Unit,
    onOpenFaq: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var torchOn by remember { mutableStateOf(false) }
    var detected by remember { mutableStateOf(false) }
    var focusTap by remember { mutableStateOf<Offset?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                qrManager.decodeFromUri(context, uri)?.let { raw ->
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    detected = true
                    delay(180)
                    onResult(raw)
                }
            }
        }
    }

    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        qrManager.bindToLifecycle(lifecycleOwner, previewView) { raw ->
            if (!detected) {
                detected = true
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                scope.launch {
                    delay(180)
                    onResult(raw)
                }
            }
        }
        onDispose { qrManager.unbind() }
    }

    Box(Modifier.fillMaxSize()) {
        // Camera Preview
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoomDelta, _ ->
                        zoomRatio = (zoomRatio * zoomDelta).coerceIn(
                            QrScannerManager.MIN_ZOOM,
                            QrScannerManager.MAX_ZOOM
                        )
                        qrManager.setZoomRatio(zoomRatio)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { off -> focusTap = off }
                }
        )

        // Viewfinder with Downpay Purple Corner Brackets
        DownpayViewfinderOverlay(detected = detected)

        // Focus animation ring
        focusTap?.let { tap ->
            FocusRing(at = tap, key = tap.toString(), onDone = { focusTap = null })
        }

        // Top Bar Header (Downpay Screenshot Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_arrow_back_white),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Scan any QR",
                fontFamily = ClashGrotesk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onOpenFaq()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_help_white),
                    contentDescription = "Help",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom Controls: Upload QR & Torch (Downpay Screenshot Style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.7f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Upload QR (Gallery)
                DownpayScannerCircleButton(
                    label = "Upload QR",
                    iconResId = R.drawable.ic_photo_gallery,
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                // Torch
                DownpayScannerCircleButton(
                    label = "Torch",
                    iconResId = if (torchOn) R.drawable.ic_torch_on else R.drawable.ic_torch_off,
                    onClick = {
                        torchOn = !torchOn
                        qrManager.setTorch(torchOn)
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            // Bottom Brand Row with authentic Downpay UPI badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.upi_orange_border),
                    contentDescription = "Downpay UPI",
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Downpay  |  UPI · Offline Secure",
                    fontFamily = ClashGrotesk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Circular translucent button below the viewfinder (Downpay Style).
 */
@Composable
private fun DownpayScannerCircleButton(
    label: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    val view = LocalView.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

/**
 * Viewfinder with signature vibrant Downpay purple corner brackets.
 */
@Composable
private fun DownpayViewfinderOverlay(detected: Boolean) {
    val infinite = rememberInfiniteTransition(label = "scan_pulse")
    val sweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweep"
    )

    val purpleCorner = Color(0xFF8E24AA) // Vibrant Downpay corner purple
    val cornerColor by animateColorAsState(
        if (detected) Color.White else purpleCorner,
        label = "corner_color"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val boxSide = (w * 0.72f).coerceAtMost(h * 0.50f)
        val left = (w - boxSide) / 2f
        val top = (h - boxSide) / 2f - 40.dp.toPx()
        val right = left + boxSide
        val bottom = top + boxSide

        val cornerLength = 40.dp.toPx()
        val cornerThickness = 4.5.dp.toPx()
        val cornerRadius = 16.dp.toPx()

        // 1. Dark Scrim cutout (60% black outside viewfinder)
        val scrimPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    radiusX = cornerRadius,
                    radiusY = cornerRadius
                )
            )
        }
        drawPath(scrimPath, color = Color.Black.copy(alpha = 0.55f))

        // 2. Viewfinder subtle border
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(boxSide, boxSide),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 1.dp.toPx())
        )

        // 3. Downpay Vibrant Purple Corner Brackets
        // Top-Left Corner
        drawPath(
            path = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top + cornerRadius)
                quadraticTo(left, top, left + cornerRadius, top)
                lineTo(left + cornerLength, top)
            },
            color = cornerColor,
            style = Stroke(width = cornerThickness, cap = StrokeCap.Round)
        )

        // Top-Right Corner
        drawPath(
            path = Path().apply {
                moveTo(right - cornerLength, top)
                lineTo(right - cornerRadius, top)
                quadraticTo(right, top, right, top + cornerRadius)
                lineTo(right, top + cornerLength)
            },
            color = cornerColor,
            style = Stroke(width = cornerThickness, cap = StrokeCap.Round)
        )

        // Bottom-Left Corner
        drawPath(
            path = Path().apply {
                moveTo(left, bottom - cornerLength)
                lineTo(left, bottom - cornerRadius)
                quadraticTo(left, bottom, left + cornerRadius, bottom)
                lineTo(left + cornerLength, bottom)
            },
            color = cornerColor,
            style = Stroke(width = cornerThickness, cap = StrokeCap.Round)
        )

        // Bottom-Right Corner
        drawPath(
            path = Path().apply {
                moveTo(right - cornerLength, bottom)
                lineTo(right - cornerRadius, bottom)
                quadraticTo(right, bottom, right, bottom - cornerRadius)
                lineTo(right, bottom - cornerLength)
            },
            color = cornerColor,
            style = Stroke(width = cornerThickness, cap = StrokeCap.Round)
        )

        // 4. Subtle scanning line
        if (!detected) {
            val lineY = top + (boxSide * sweep)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        purpleCorner.copy(alpha = 0f),
                        purpleCorner.copy(alpha = 0.8f),
                        purpleCorner.copy(alpha = 0f)
                    ),
                    startX = left,
                    endX = right
                ),
                start = Offset(left + 8.dp.toPx(), lineY),
                end = Offset(right - 8.dp.toPx(), lineY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
private fun FocusRing(at: Offset, key: String, onDone: () -> Unit) {
    val anim = remember(key) { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(key) {
        anim.animateTo(1f, tween(400, easing = LinearEasing))
        onDone()
    }
    val scale = 1f + (1f - anim.value) * 0.5f
    val alpha = 1f - anim.value

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 32.dp.toPx() * scale,
            center = at,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
private fun ScannerPermissionDenied(
    permanentlyDenied: Boolean,
    onGrant: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DownpayColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = DownpayColors.Surface,
            shape = shape,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DownpayColors.Border, shape)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(DownpayColors.PurpleTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = DownpayColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Camera Permission Required",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DownpayColors.TextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Downpay needs camera access to scan QR codes for offline payments.",
                    fontSize = 13.sp,
                    color = DownpayColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DownpayColors.Primary)
                        .clickable { if (permanentlyDenied) onOpenSettings() else onGrant() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (permanentlyDenied) "Open Settings" else "Allow Camera",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClose() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DownpayColors.TextSecondary
                    )
                }
            }
        }
    }
}
