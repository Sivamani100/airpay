package com.arkiolabs.airpay.presentation.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.arkiolabs.airpay.R
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.domain.ActionEvent
import com.arkiolabs.airpay.domain.Actions
import com.arkiolabs.airpay.domain.UpiParser
import com.arkiolabs.airpay.presentation.permissions.openAccessibilitySettingsWithGuide
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.ClashGrotesk
import com.arkiolabs.airpay.presentation.ui.theme.Iconsax
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Authentic Airpay "MY QR" screen.
 * Displays user's smooth rounded scannable UPI QR code with center square-typed logo.
 * Supports blurred unactivated state with 1-tap "Activate QR" SIM fetch,
 * manual customizer, copy, download, and share.
 */
@Composable
fun MyQrScreen(
    onClose: () -> Unit,
    onOpenFaq: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val app = context.airpayApp
    val prefsRepo = app.prefsRepo
    val actionRunner = app.actionRunner
    val scope = rememberCoroutineScope()
    val cachedPrefs = remember { context.getSharedPreferences("airpay_fast_cache", Context.MODE_PRIVATE) }
    val overlayController = app.overlayController

    val savedUpiIdFlow by prefsRepo.userUpiId.collectAsState(initial = null)
    val cachedUpiId = remember { cachedPrefs.getString("user_upi_id", null) }
    var activeUpiId by remember { mutableStateOf(cachedUpiId?.takeIf { it.isNotBlank() && it.contains("@") }) }

    // Sync state if DataStore emits an update
    LaunchedEffect(savedUpiIdFlow) {
        if (!savedUpiIdFlow.isNullOrBlank() && savedUpiIdFlow!!.contains("@")) {
            activeUpiId = savedUpiIdFlow
            cachedPrefs.edit().putString("user_upi_id", savedUpiIdFlow).commit()
        }
    }

    val isQrActivated = !activeUpiId.isNullOrBlank() && activeUpiId!!.contains("@")
    val effectiveUpiId = if (isQrActivated) activeUpiId!! else "sample@upi"

    var isActivating by remember { mutableStateOf(false) }
    var activationStatus by remember { mutableStateOf("Connecting to SIM…") }

    // Pre-load square app logo bitmap for center of QR
    val logoBitmap = remember(context) {
        drawableToBitmap(context, R.drawable.ic_splash_logo)
    }

    // Generate smooth rounded QR code bitmap
    val qrBitmap = remember(effectiveUpiId, logoBitmap) {
        val upiUri = "upi://pay?pa=$effectiveUpiId&pn=Airpay%20User&cu=INR"
        generateSmoothQrBitmap(content = upiUri, size = 600, logoBitmap = logoBitmap)
    }

    // Function to run the offline SIM activation
    fun startQrActivation() {
        val phoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        if (!phoneGranted) {
            Toast.makeText(context, "Phone permission is required to detect SIM", Toast.LENGTH_SHORT).show()
            return
        }

        if (!actionRunner.isServiceEnabled()) {
            Toast.makeText(context, "Please enable Fast Payment Automation in Accessibility settings", Toast.LENGTH_LONG).show()
            openAccessibilitySettingsWithGuide(context)
            return
        }

        isActivating = true
        activationStatus = "Connecting to SIM channel…"

        // Show branded opaque overlay to completely cover carrier dialogs
        overlayController.show(
            title = "Activating Offline QR",
            subtitle = "Airpay is querying your bank…",
            stepLabel = "CONNECTING"
        )
        overlayController.onCancel = {
            overlayController.hide()
            isActivating = false
        }

        val run = actionRunner.runAction(
            action = Actions.FetchUpiProfile,
            vars = emptyMap(),
            scope = scope
        )

        scope.launch {
            run.events.collect { event ->
                when (event) {
                    is ActionEvent.Progress -> {
                        activationStatus = event.label ?: "Fetching UPI ID…"
                        overlayController.update(
                            title = "Activating Offline QR",
                            subtitle = "Airpay is querying your bank…",
                            stepLabel = (event.label ?: "FETCHING").uppercase()
                        )
                    }
                    is ActionEvent.Done -> {
                        val raw = event.resultText
                        val extracted = UpiParser.extractVpaFromText(raw)
                            ?: Regex("([a-zA-Z0-9.\\-_]{3,}@[a-zA-Z0-9.\\-_]{2,})").find(raw)?.value
                            ?: Regex("(\\d{10}@[a-zA-Z0-9.\\-_]+)").find(raw)?.value

                        if (extracted != null) {
                            // Persist permanently in BOTH DataStore and SharedPreferences
                            prefsRepo.setUserUpiId(extracted)
                            cachedPrefs.edit().putString("user_upi_id", extracted).commit()
                            activeUpiId = extracted
                            activationStatus = "QR Activated!"
                            Toast.makeText(context, "QR Activated: $extracted", Toast.LENGTH_SHORT).show()
                        } else {
                            activationStatus = "Unable to read UPI ID from reply"
                        }

                        // Ensure carrier dialog is dismissed completely while overlay is still covering it
                        com.arkiolabs.airpay.platform.UssdAccessibilityService.instance?.dismissDialog()
                        kotlinx.coroutines.delay(400)
                        com.arkiolabs.airpay.platform.UssdAccessibilityService.instance?.dismissDialog()
                        overlayController.hide()
                        isActivating = false
                    }
                    is ActionEvent.Error -> {
                        com.arkiolabs.airpay.platform.UssdAccessibilityService.instance?.dismissDialog()
                        kotlinx.coroutines.delay(400)
                        com.arkiolabs.airpay.platform.UssdAccessibilityService.instance?.dismissDialog()
                        overlayController.hide()
                        isActivating = false
                        activationStatus = "Activation failed"
                        Toast.makeText(context, "Activation error: ${event.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AirpayColors.Background)
            .statusBarsPadding()
    ) {
        // ── Top App Bar ─────────────────────────────────────────────
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F5F7))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onClose()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Iconsax.ArrowLeft,
                        contentDescription = "Back",
                        tint = AirpayColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "My QR Code",
                    fontSize = 18.sp,
                    fontFamily = ClashGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = AirpayColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Balanced spacer to match left navigation button
                Spacer(Modifier.size(38.dp))
            }
        }

        // ── Main Scrollable Body ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Main Elevated QR Card ───────────────────────────────
            val cardShape = RoundedCornerShape(24.dp)
            Surface(
                color = Color.White,
                shape = cardShape,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEEEEEE), cardShape)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Status Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isQrActivated) AirpayColors.SuccessTint else AirpayColors.SaffronTint)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isQrActivated) AirpayColors.SuccessGreen else AirpayColors.Saffron)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isQrActivated) "Active for Offline Receiving" else "QR Not Activated",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isQrActivated) Color(0xFF1B5E20) else AirpayColors.SaffronDark
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── QR Code Viewport (Smooth Rounded QR with center square logo) ──
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(20.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Offline UPI QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (!isQrActivated || isActivating) {
                                            Modifier.blur(18.dp)
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                        }

                        // Overlay for Unactivated or Activating State
                        if (!isQrActivated || isActivating) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.82f))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isActivating) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = AirpayColors.Primary,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(38.dp)
                                        )
                                        Spacer(Modifier.height(14.dp))
                                        Text(
                                            text = activationStatus,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AirpayColors.TextPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(AirpayColors.Primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = AirpayColors.Saffron,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        Text(
                                            text = "Activate Your QR",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AirpayColors.TextPrimary
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = "Fetch your official bank UPI ID from your SIM card",
                                            fontSize = 11.sp,
                                            color = AirpayColors.TextSecondary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 15.sp
                                        )

                                        Spacer(Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(AirpayColors.Primary)
                                                .clickable {
                                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                    startQrActivation()
                                                }
                                                .padding(horizontal = 20.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Activate QR",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // UPI ID display badge
                    if (isQrActivated) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF6F7F9))
                                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    copyToClipboard(context, effectiveUpiId)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = effectiveUpiId,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AirpayColors.TextPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                            Icon(
                                painter = painterResource(R.drawable.ic_copy),
                                contentDescription = "Copy",
                                tint = AirpayColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "UPI ID: Not Activated",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AirpayColors.TextSecondary
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Download & Share Action Pills ──────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Download Pill
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF9FAFB))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    if (qrBitmap != null) {
                                        saveQrToGallery(context, qrBitmap)
                                    }
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.download_icon),
                                contentDescription = null,
                                tint = AirpayColors.TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Download",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AirpayColors.TextPrimary
                            )
                        }

                        // Share Pill
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF9FAFB))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    shareUpiDetails(context, effectiveUpiId, qrBitmap)
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = null,
                                tint = AirpayColors.TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Share",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AirpayColors.TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Secondary Management Actions ────────────────────────────
            if (isQrActivated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                startQrActivation()
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = AirpayColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Re-sync from SIM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AirpayColors.Primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draws a smooth, modern QR code with rounded modules and a center square logo cutout.
 */
private fun generateSmoothQrBitmap(
    content: String,
    size: Int = 600,
    logoBitmap: Bitmap? = null
): Bitmap? {
    return try {
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        hints[EncodeHintType.MARGIN] = 2

        val bitMatrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )
        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val modulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#08101D") // Deep Dark Navy
            style = Paint.Style.FILL
        }

        val moduleWidth = size.toFloat() / matrixWidth
        val moduleHeight = size.toFloat() / matrixHeight
        val radius = moduleWidth * 0.35f

        // Center cutout box size (approx 22% of QR code)
        val logoBoxSize = (size * 0.22f).toInt()
        val logoLeft = (size - logoBoxSize) / 2
        val logoTop = (size - logoBoxSize) / 2
        val logoRight = logoLeft + logoBoxSize
        val logoBottom = logoTop + logoBoxSize

        val rect = RectF()
        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                if (bitMatrix.get(x, y)) {
                    val px = x * moduleWidth
                    val py = y * moduleHeight

                    // Exclude modules inside the center logo cutout area
                    if (px + moduleWidth > logoLeft && px < logoRight &&
                        py + moduleHeight > logoTop && py < logoBottom
                    ) {
                        continue
                    }

                    rect.set(px, py, px + moduleWidth, py + moduleHeight)
                    canvas.drawRoundRect(rect, radius, radius, modulePaint)
                }
            }
        }

        // Draw center logo background with rounded corners
        val logoBgRect = RectF(
            logoLeft.toFloat() - 4f,
            logoTop.toFloat() - 4f,
            logoRight.toFloat() + 4f,
            logoBottom.toFloat() + 4f
        )
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#EEEEEE")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val logoRadius = 16f
        canvas.drawRoundRect(logoBgRect, logoRadius, logoRadius, bgPaint)
        canvas.drawRoundRect(logoBgRect, logoRadius, logoRadius, borderPaint)

        // Draw the square-typed logo in the center
        if (logoBitmap != null) {
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoBoxSize, logoBoxSize, true)
            canvas.drawBitmap(scaledLogo, logoLeft.toFloat(), logoTop.toFloat(), null)
        }

        bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Converts any drawable resource into a clean software Bitmap.
 */
private fun drawableToBitmap(context: Context, @DrawableRes drawableId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.coerceAtLeast(140),
        drawable.intrinsicHeight.coerceAtLeast(140),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", text))
    Toast.makeText(context, "UPI ID copied: $text", Toast.LENGTH_SHORT).show()
}

private fun saveQrToGallery(context: Context, bitmap: Bitmap) {
    try {
        val filename = "Airpay_QR_${System.currentTimeMillis()}.png"
        val outputStream = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Airpay")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Airpay")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file)
        }

        outputStream?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            Toast.makeText(context, "QR saved to Pictures/Airpay", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(context, "Unable to save QR code", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save QR: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareUpiDetails(context: Context, upiId: String, bitmap: Bitmap?) {
    try {
        if (bitmap != null) {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "airpay_qr.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val fileUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, "Scan my QR or pay me directly via UPI ID: $upiId")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share UPI QR"))
        } else {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Pay me directly via UPI ID: $upiId on Airpay Offline UPI")
            }
            context.startActivity(Intent.createChooser(intent, "Share UPI ID"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
