package com.arkiolabs.downpay.platform

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

/**
 * TYPE_APPLICATION_OVERLAY window painted ABOVE the carrier USSD dialog so
 * the user only ever sees branded UI while a session runs. Mirrors the
 * proven implementation from `References/UssdTest`.
 *
 * Two modes:
 *  - **Full overlay** (`show`/`update`): covers the carrier dialog entirely.
 *  - **Minimal floating bar** (`showMinimal`/`updateMinimal`): a small bar
 *    pinned below the status bar; carrier dialog stays visible underneath.
 *
 * Critical flag on both windows: `FLAG_NOT_FOCUSABLE`. If the overlay grabs
 * keyboard focus, the underlying carrier dialog can't reliably receive
 * AccessibilityService setText/click actions.
 */
class OverlayControllerImpl(private val context: Context) : OverlayController {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val handler = Handler(Looper.getMainLooper())

    // ── Full overlay state ─────────────────────────────────────────────────────
    private var overlayView: FrameLayout? = null
    private var titleView: TextView? = null
    private var subtitleView: TextView? = null
    private var stepLabelView: TextView? = null
    private var spinnerView: ProgressBar? = null

    // ── Jio Auto-Inject Stepper state ──────────────────────────────────────────
    private var jioStep1Status: TextView? = null
    private var jioStep2Status: TextView? = null
    private var jioStep3Status: TextView? = null
    private var jioStep4Status: TextView? = null
    private var jioStep5Status: TextView? = null
    private var jioProgressText: TextView? = null

    // ── Minimal floating bar state ─────────────────────────────────────────────
    private var minimalView: LinearLayout? = null
    private var minimalLabel: TextView? = null
    private var minimalStepCount: TextView? = null
    private var minimalProgressFill: View? = null
    private var minimalProgressTrack: FrameLayout? = null

    override var onCancel: (() -> Unit)? = null
    override var onSpeakerToggle: (() -> Boolean)? = null
    override var onMinimalTapped: (() -> Unit)? = null

    private fun fullParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun minimalParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(48)
        }
    }

    override fun canShow(): Boolean = Settings.canDrawOverlays(context)

    // ─── Full overlay ──────────────────────────────────────────────────────────

    override fun show(title: String, subtitle: String, stepLabel: String) {
        if (!canShow()) return
        // If the minimal bar is up, dismiss it before opening the full overlay.
        removeMinimalView()

        handler.post {
            if (overlayView != null) {
                update(title, subtitle, stepLabel)
                return@post
            }
            try {
                val v = buildFullView(title, subtitle, stepLabel)
                windowManager.addView(v, fullParams())
                overlayView = v
            } catch (_: Exception) {
                overlayView = null
            }
        }
    }

    override fun update(title: String, subtitle: String, stepLabel: String) {
        handler.post {
            if (overlayView == null) {
                show(title, subtitle, stepLabel)
                return@post
            }
            spinnerView?.visibility = View.VISIBLE
            titleView?.text = title
            subtitleView?.text = subtitle
            stepLabelView?.text = stepLabel.uppercase()
        }
    }

    // ─── Minimal bar ───────────────────────────────────────────────────────────

    override fun showMinimal(progress: Int, total: Int, label: String) {
        if (!canShow()) return
        // If the full overlay is up, dismiss it before opening the minimal bar.
        removeFullView()

        handler.post {
            if (minimalView != null) {
                updateMinimal(progress, total, label)
                return@post
            }
            try {
                val v = buildMinimalView(progress, total, label)
                windowManager.addView(v, minimalParams())
                minimalView = v
            } catch (_: Exception) {
                minimalView = null
            }
        }
    }

    override fun updateMinimal(progress: Int, total: Int, label: String) {
        handler.post {
            minimalLabel?.text = label.uppercase() + "…"
            minimalLabel?.setTextColor(OVERLAY_WHITE)
            val safeTotal = total.coerceAtLeast(1)
            val displayedStep = (progress + 1).coerceIn(1, safeTotal)
            minimalStepCount?.text = "STEP $displayedStep / $safeTotal"
            // Update progress fill width using weights/layout params.
            minimalProgressTrack?.let { track ->
                track.post {
                    val fraction = (progress.toFloat() / safeTotal).coerceIn(0f, 1f)
                    val newWidth = (track.width * fraction).toInt()
                    minimalProgressFill?.layoutParams =
                        FrameLayout.LayoutParams(
                            newWidth,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                }
            }
        }
    }

    override fun showError(title: String, message: String, holdMs: Long) {
        handler.post {
            // Prefer minimal bar feedback if it's already up.
            if (minimalView != null) {
                minimalLabel?.text = title.uppercase()
                minimalLabel?.setTextColor(OVERLAY_DANGER)
                minimalStepCount?.text = message.take(40)
                handler.postDelayed({ hide() }, holdMs)
                return@post
            }
            if (overlayView == null) {
                show(title, message, "")
            }
            spinnerView?.visibility = View.GONE
            titleView?.setTextColor(OVERLAY_DANGER)
            titleView?.text = title
            subtitleView?.text = message
            stepLabelView?.text = "TAP CANCEL TO DISMISS"
            handler.postDelayed({ hide() }, holdMs)
        }
    }

    override fun hide() {
        handler.post {
            removeFullView()
            removeMinimalView()
        }
    }

    private fun removeFullView() {
        overlayView?.let { view ->
            try {
                if (view.parent != null) windowManager.removeView(view)
            } catch (_: Exception) {
            }
        }
        overlayView = null
        titleView = null
        subtitleView = null
        stepLabelView = null
        spinnerView = null
        jioStep1Status = null
        jioStep2Status = null
        jioStep3Status = null
        jioStep4Status = null
        jioStep5Status = null
        jioProgressText = null
    }

    private fun removeMinimalView() {
        minimalView?.let { view ->
            try {
                if (view.parent != null) windowManager.removeView(view)
            } catch (_: Exception) {
            }
        }
        minimalView = null
        minimalLabel = null
        minimalStepCount = null
        minimalProgressFill = null
        minimalProgressTrack = null
    }

    // ─── Jio Auto-Inject Overlay ──────────────────────────────────────────────

    override fun showJioAutoInject(
        recipient: String,
        amount: String,
        stepNumber: Int,
        stepTitle: String,
        stepSubtitle: String
    ) {
        if (!canShow()) return
        removeMinimalView()

        handler.post {
            if (overlayView != null) {
                updateJioAutoInject(stepNumber, stepTitle, stepSubtitle)
                return@post
            }
            try {
                val v = buildJioAutoInjectView(recipient, amount, stepNumber, stepTitle, stepSubtitle)
                windowManager.addView(v, fullParams())
                overlayView = v
            } catch (_: Exception) {
                overlayView = null
            }
        }
    }

    override fun updateJioAutoInject(
        stepNumber: Int,
        stepTitle: String,
        stepSubtitle: String
    ) {
        handler.post {
            val green = DOWNPAY_SUCCESS
            val purple = DOWNPAY_PRIMARY
            val gray = DOWNPAY_TEXT_MUTED

            when (stepNumber) {
                1 -> {
                    jioStep1Status?.text = "INJECTING..."
                    jioStep1Status?.setTextColor(purple)
                    jioStep2Status?.text = "WAITING"
                    jioStep2Status?.setTextColor(gray)
                    jioStep3Status?.text = "WAITING"
                    jioStep3Status?.setTextColor(gray)
                    jioStep4Status?.text = "WAITING"
                    jioStep4Status?.setTextColor(gray)
                    jioStep5Status?.text = "WAITING"
                    jioStep5Status?.setTextColor(gray)
                }
                2 -> {
                    jioStep1Status?.text = "SENT ✓"
                    jioStep1Status?.setTextColor(green)
                    jioStep2Status?.text = "INJECTING..."
                    jioStep2Status?.setTextColor(purple)
                    jioStep3Status?.text = "WAITING"
                    jioStep3Status?.setTextColor(gray)
                    jioStep4Status?.text = "WAITING"
                    jioStep4Status?.setTextColor(gray)
                    jioStep5Status?.text = "WAITING"
                    jioStep5Status?.setTextColor(gray)
                }
                3 -> {
                    jioStep1Status?.text = "SENT ✓"
                    jioStep1Status?.setTextColor(green)
                    jioStep2Status?.text = "SENT ✓"
                    jioStep2Status?.setTextColor(green)
                    jioStep3Status?.text = "INJECTING..."
                    jioStep3Status?.setTextColor(purple)
                    jioStep4Status?.text = "WAITING"
                    jioStep4Status?.setTextColor(gray)
                    jioStep5Status?.text = "WAITING"
                    jioStep5Status?.setTextColor(gray)
                }
                4 -> {
                    jioStep1Status?.text = "SENT ✓"
                    jioStep1Status?.setTextColor(green)
                    jioStep2Status?.text = "SENT ✓"
                    jioStep2Status?.setTextColor(green)
                    jioStep3Status?.text = "SENT ✓"
                    jioStep3Status?.setTextColor(green)
                    jioStep4Status?.text = "INJECTING..."
                    jioStep4Status?.setTextColor(purple)
                    jioStep5Status?.text = "WAITING"
                    jioStep5Status?.setTextColor(gray)
                }
                5 -> {
                    jioStep1Status?.text = "SENT ✓"
                    jioStep1Status?.setTextColor(green)
                    jioStep2Status?.text = "SENT ✓"
                    jioStep2Status?.setTextColor(green)
                    jioStep3Status?.text = "SENT ✓"
                    jioStep3Status?.setTextColor(green)
                    jioStep4Status?.text = "SENT ✓"
                    jioStep4Status?.setTextColor(green)
                    jioStep5Status?.text = "AUTHORIZING..."
                    jioStep5Status?.setTextColor(purple)
                }
                else -> {
                    jioStep1Status?.text = "SENT ✓"
                    jioStep1Status?.setTextColor(green)
                    jioStep2Status?.text = "SENT ✓"
                    jioStep2Status?.setTextColor(green)
                    jioStep3Status?.text = "SENT ✓"
                    jioStep3Status?.setTextColor(green)
                    jioStep4Status?.text = "SENT ✓"
                    jioStep4Status?.setTextColor(green)
                    jioStep5Status?.text = "SENT ✓"
                    jioStep5Status?.setTextColor(green)
                }
            }
            jioProgressText?.text = "$stepTitle: $stepSubtitle"
        }
    }

    private fun buildJioAutoInjectView(
        recipient: String,
        amount: String,
        stepNumber: Int,
        stepTitle: String,
        stepSubtitle: String
    ): FrameLayout {
        // 100% OPAQUE background covering the entire display so call dialer is completely invisible!
        val root = FrameLayout(context).apply {
            setBackgroundColor(DOWNPAY_BG)
            isClickable = true
        }

        val isBalanceCheck = amount.isBlank()

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), Color.parseColor("#E0E0E0"))
            }
            setPadding(dp(20), dp(22), dp(20), dp(22))
        }

        val badge = TextView(context).apply {
            text = if (isBalanceCheck) "JIO VoLTE BALANCE CHECK" else "JIO VoLTE OFFLINE GATEWAY"
            setTextColor(DOWNPAY_PRIMARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            letterSpacing = 0.12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EDE7F6"))
                cornerRadius = dp(6).toFloat()
            }
            setPadding(dp(8), dp(4), dp(8), dp(4))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = lp
        }

        val title = TextView(context).apply {
            text = if (isBalanceCheck) "Checking Bank Balance" else "Paying ₹$amount"
            setTextColor(DOWNPAY_TEXT_PRIMARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(12)
            layoutParams = lp
        }

        val subtitle = TextView(context).apply {
            text = if (isBalanceCheck) "via Jio VoLTE Gateway (08045163666)" else "to +91 $recipient"
            setTextColor(DOWNPAY_TEXT_MUTED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(4)
            layoutParams = lp
        }

        val progress = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            indeterminateTintList = android.content.res.ColorStateList.valueOf(DOWNPAY_PRIMARY)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(16)
            layoutParams = lp
        }

        val stepperContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F5F3FB"))
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(14), dp(12), dp(14), dp(12))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(16)
            layoutParams = lp
        }

        fun makeStepRow(name: String): Pair<LinearLayout, TextView> {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(6), 0, dp(6))
            }
            val label = TextView(context).apply {
                text = name
                setTextColor(DOWNPAY_TEXT_PRIMARY)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = lp
            }
            val status = TextView(context).apply {
                text = "WAITING"
                setTextColor(DOWNPAY_TEXT_MUTED)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                letterSpacing = 0.08f
            }
            row.addView(label)
            row.addView(status)
            return Pair(row, status)
        }

        if (isBalanceCheck) {
            val (row1, stat1) = makeStepRow("1. Option: Balance Check (2)")
            val (row2, stat2) = makeStepRow("2. Bank UPI PIN: ••••")
            val (row3, stat3) = makeStepRow("3. Bank SMS Verification")
            jioStep1Status = stat1
            jioStep2Status = stat2
            jioStep3Status = stat3
            jioStep4Status = null
            jioStep5Status = null
            stepperContainer.addView(row1)
            stepperContainer.addView(row2)
            stepperContainer.addView(row3)
        } else {
            val (row1, stat1) = makeStepRow("1. Option: Money Transfer (1)")
            val (row2, stat2) = makeStepRow("2. Recipient: +91 $recipient")
            val (row3, stat3) = makeStepRow("3. Amount: ₹$amount")
            val (row4, stat4) = makeStepRow("4. Confirm Details (1)")
            val (row5, stat5) = makeStepRow("5. Bank UPI PIN: ••••")
            jioStep1Status = stat1
            jioStep2Status = stat2
            jioStep3Status = stat3
            jioStep4Status = stat4
            jioStep5Status = stat5
            stepperContainer.addView(row1)
            stepperContainer.addView(row2)
            stepperContainer.addView(row3)
            stepperContainer.addView(row4)
            stepperContainer.addView(row5)
        }

        jioProgressText = TextView(context).apply {
            text = "$stepTitle\n$stepSubtitle"
            setTextColor(DOWNPAY_TEXT_MUTED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(14)
            layoutParams = lp
        }

        val cancelBtn = Button(context).apply {
            text = if (isBalanceCheck) "DONE / HANG UP" else "CANCEL PAYMENT"
            setTextColor(Color.parseColor("#D32F2F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(8).toFloat()
                setStroke(dp(1), Color.parseColor("#FFCDD2"))
            }
            setPadding(dp(16), dp(10), dp(16), dp(10))
            setOnClickListener {
                onCancel?.invoke()
                hide()
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(14)
            layoutParams = lp
        }

        card.addView(badge)
        card.addView(title)
        card.addView(subtitle)
        card.addView(progress)
        card.addView(stepperContainer)
        card.addView(jioProgressText)
        card.addView(cancelBtn)

        val cardLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            leftMargin = dp(20)
            rightMargin = dp(20)
        }
        root.addView(card, cardLp)

        return root
    }

    // ─── Full overlay view construction ────────────────────────────────────────

    private fun buildFullView(title: String, subtitle: String, stepLabel: String): FrameLayout {
        // 100% OPAQUE background covering the entire display so carrier dialogs are 100% invisible!
        val dim = FrameLayout(context).apply {
            setBackgroundColor(DOWNPAY_BG)
            isClickable = false
            isFocusable = false
        }

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), Color.parseColor("#E0E0E0"))
            }
            setPadding(dp(20), dp(22), dp(20), dp(22))
        }

        val brand = TextView(context).apply {
            text = "DOWNPAY OFFLINE UPI"
            setTextColor(DOWNPAY_PRIMARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            letterSpacing = 0.12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EDE7F6"))
                cornerRadius = dp(6).toFloat()
            }
            setPadding(dp(8), dp(4), dp(8), dp(4))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = lp
        }

        titleView = TextView(context).apply {
            text = title
            setTextColor(DOWNPAY_TEXT_PRIMARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(12)
            layoutParams = lp
        }

        subtitleView = TextView(context).apply {
            text = subtitle
            setTextColor(DOWNPAY_TEXT_MUTED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(4)
            layoutParams = lp
        }

        spinnerView = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            indeterminateTintList = android.content.res.ColorStateList.valueOf(DOWNPAY_PRIMARY)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(16)
            layoutParams = lp
        }

        val stepCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F5F3FB"))
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(14), dp(12), dp(14), dp(12))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(16)
            layoutParams = lp
        }

        stepLabelView = TextView(context).apply {
            text = stepLabel.uppercase()
            setTextColor(DOWNPAY_PRIMARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            letterSpacing = 0.08f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val helperText = TextView(context).apply {
            text = "Offline banking session is active. Background dialogs are hidden."
            setTextColor(DOWNPAY_TEXT_MUTED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(4)
            layoutParams = lp
        }

        stepCard.addView(stepLabelView)
        stepCard.addView(helperText)

        val cancel = Button(context).apply {
            text = "CANCEL PAYMENT"
            setTextColor(Color.parseColor("#D32F2F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(8).toFloat()
                setStroke(dp(1), Color.parseColor("#FFCDD2"))
            }
            setPadding(dp(16), dp(10), dp(16), dp(10))
            setOnClickListener {
                onCancel?.invoke()
                hide()
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(18)
            layoutParams = lp
        }

        card.addView(brand)
        card.addView(titleView)
        card.addView(subtitleView)
        card.addView(spinnerView)
        card.addView(stepCard)
        card.addView(cancel)

        val cardLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            leftMargin = dp(20)
            rightMargin = dp(20)
        }
        dim.addView(card, cardLp)
        return dim
    }

    // ─── Minimal floating bar view construction ────────────────────────────────

    private fun buildMinimalView(progress: Int, total: Int, label: String): LinearLayout {
        val safeTotal = total.coerceAtLeast(1)
        val displayedStep = (progress + 1).coerceIn(1, safeTotal)

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = minimalBarBg()
            setPadding(dp(16), dp(12), dp(16), dp(0))
            // 90% screen width — the WindowManager.LayoutParams.MATCH_PARENT
            // along with horizontal margin handles this approximately.
            val sideMargin = dp(20)
            val lp = WindowManager.LayoutParams()
            // Real margins are baked into params at attach time; here we just
            // pad the inner view to leave a gutter.
            setPadding(dp(16) + sideMargin, dp(12), dp(16) + sideMargin, dp(0))
            isClickable = true
            setOnClickListener {
                onMinimalTapped?.invoke()
            }
        }

        // Top row: spinner + label + step count
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = lp
        }

        val spinner = ProgressBar(context).apply {
            indeterminateTintList = android.content.res.ColorStateList.valueOf(OVERLAY_ACCENT)
            val lp = LinearLayout.LayoutParams(dp(18), dp(18))
            lp.rightMargin = dp(10)
            layoutParams = lp
        }

        minimalLabel = TextView(context).apply {
            text = label.uppercase() + "…"
            setTextColor(OVERLAY_WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            letterSpacing = 0.12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val lp = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            layoutParams = lp
        }

        minimalStepCount = TextView(context).apply {
            text = "STEP $displayedStep / $safeTotal"
            setTextColor(OVERLAY_TEXT_SECONDARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            letterSpacing = 0.1f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        topRow.addView(spinner)
        topRow.addView(minimalLabel)
        topRow.addView(minimalStepCount)

        // Bottom: 2dp progress track with lime fill
        minimalProgressTrack = FrameLayout(context).apply {
            setBackgroundColor(OVERLAY_BORDER)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(2)
            )
            lp.topMargin = dp(10)
            lp.bottomMargin = dp(10)
            layoutParams = lp
        }
        minimalProgressFill = View(context).apply {
            setBackgroundColor(OVERLAY_ACCENT)
            val fraction = (progress.toFloat() / safeTotal).coerceIn(0f, 1f)
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            // Initial pixel-width is a fudge; updateMinimal() recomputes it
            // once the track has measured.
            lp.width = 0
            layoutParams = lp
        }
        minimalProgressTrack?.addView(minimalProgressFill)

        container.addView(topRow)
        container.addView(minimalProgressTrack)
        return container
    }

    private fun minimalBarBg(): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(OVERLAY_MINIMAL_FILL)
            setStroke(dp(1), OVERLAY_ACCENT)
        }
    }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    companion object {
        // Downpay palette
        private const val DOWNPAY_PRIMARY = 0xFF5F259E.toInt()
        private const val DOWNPAY_BG = 0xFFF9F9FB.toInt()
        private const val DOWNPAY_TEXT_PRIMARY = 0xFF1C1C1E.toInt()
        private const val DOWNPAY_TEXT_MUTED = 0xFF757575.toInt()
        private const val DOWNPAY_SUCCESS = 0xFF00875A.toInt()

        // Overlay status bar palette
        private const val OVERLAY_DIM = 0xFF000000.toInt() // 100% Opaque black so dialer is completely invisible
        private const val OVERLAY_SURFACE_HIGH = 0xFF16181D.toInt()
        private const val OVERLAY_BORDER = 0xFF2A2D34.toInt()
        private const val OVERLAY_WHITE = 0xFFFFFFFF.toInt()
        private const val OVERLAY_TEXT_SECONDARY = 0xFF9BA1A8.toInt()
        private const val OVERLAY_ACCENT = 0xFFC5F542.toInt()
        private const val OVERLAY_DANGER = 0xFFFF4D4D.toInt()
        private const val OVERLAY_MINIMAL_FILL = 0xFF000000.toInt() // 100% opaque
    }
}
