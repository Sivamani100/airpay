package com.arkiolabs.airpay.platform

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.arkiolabs.airpay.domain.FrameFilter

/**
 * Listener interface for USSD frame events.
 * The UssdEngine registers itself to receive frames from this service.
 */
interface UssdFrameListener {
    /** Called when a new USSD frame is captured. */
    fun onFrame(text: String, isMenu: Boolean, isTerminal: Boolean)

    /** Called when the OS reports the carrier dialog window has gone away. */
    fun onSessionEnded(reason: String) {}
}

/**
 * AccessibilityService that watches known USSD dialog packages,
 * extracts visible text, classifies frames as menu or terminal,
 * and provides methods to drive the carrier dialog programmatically.
 *
 * Mirrors the proven implementation from `References/UssdTest`. Critical
 * differences from a naive port:
 *  - Uses `event.source` as the primary tree (works even when our overlay
 *    is on top — `rootInActiveWindow` would point at the overlay).
 *  - Handles `TYPE_VIEW_TEXT_CHANGED` and `TYPE_WINDOWS_CHANGED`. The
 *    latter is debounced (500ms) so we can detect when the carrier dialog
 *    truly goes away (session end).
 *  - `findUssdRoot()` walks `getWindows()` so dialog lookups succeed
 *    while the branded overlay is on top.
 *  - `sendReply()` clears `lastEmittedText` so the very next frame is
 *    delivered to the listener even if its text duplicates what was last
 *    shown.
 */
class UssdAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "UssdA11y"

        /** Debounce window before deciding the carrier dialog is gone. */
        private const val SESSION_END_DEBOUNCE_MS = 4000L

        @Volatile
        var instance: UssdAccessibilityService? = null
            private set

        val USSD_PACKAGES = setOf(
            "com.android.phone",
            "com.android.server.telecom",
            "com.samsung.android.app.telephonyui",
            "com.samsung.android.incallui",
            "com.google.android.dialer",
            "com.android.dialer",
            "com.android.incallui",
            "com.oplus.dialer",
            "com.coloros.dialer",
            "com.miui.telephonyui",
            "com.xiaomi.telephonyui",
            "com.vivo.telephonyui",
            "android"
        )

        fun isUssdPackage(pkg: String?): Boolean {
            if (pkg.isNullOrBlank()) return false
            if (pkg in USSD_PACKAGES) return true
            val lower = pkg.lowercase()
            return lower == "android" ||
                lower.contains("telecom") ||
                lower.contains("telephony") ||
                lower.contains("dialer") ||
                lower.contains("phone") ||
                lower.contains("incall")
        }
    }

    private val frameFilter = FrameFilter()
    private var lastEmittedText: String? = null
    private var hasSeenFirstDialog: Boolean = false
    @Volatile
    private var activeDialogRoot: AccessibilityNodeInfo? = null

    private val main = Handler(Looper.getMainLooper())
    private val sessionEndCheck = Runnable { verifySessionStillAlive() }

    @Volatile
    var frameListener: UssdFrameListener? = null

    @Volatile
    var sessionActive: Boolean = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 100
        }
        Log.d(TAG, "service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        main.removeCallbacks(sessionEndCheck)
        if (instance === this) instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        // Only act while a session is running — saves CPU and avoids leaking
        // accidental dialog reads back to the listener.
        if (!sessionActive) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                main.removeCallbacks(sessionEndCheck)
                main.postDelayed(sessionEndCheck, SESSION_END_DEBOUNCE_MS)
            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val pkg = event.packageName?.toString() ?: return
                if (!isUssdPackage(pkg)) return

                // Retrieve top-level window root so the entire dialog tree is captured
                val rawNode = event.source ?: findUssdRoot() ?: return
                val root: AccessibilityNodeInfo = getWindowRoot(rawNode) ?: rawNode
                if (!isRealUssdDialog(root)) return
                activeDialogRoot = root
                handleDialog(root)
            }
        }
    }

    override fun onInterrupt() {
        // No-op; required by AccessibilityService
    }

    // ─── Public API ────────────────────────────────────────────────────────────

    /**
     * Sends a reply to the active USSD dialog by setting text in the EditText
     * and clicking the Send/OK button.
     *
     * @return true if the reply was successfully sent
     */
    fun sendReply(reply: String): Boolean {
        val root = findUssdRoot() ?: run {
            Log.w(TAG, "sendReply: no USSD window found")
            return false
        }
        val edit = findEditText(root) ?: run {
            Log.w(TAG, "sendReply: no EditText in USSD window")
            return false
        }

        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                reply
            )
        }
        edit.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

        // Give Android binder/IPC a brief gap to commit text to the telephony process
        try {
            Thread.sleep(120)
        } catch (_: InterruptedException) {}

        // Clear the dedup and cached dialog state so the very next frame is re-emitted
        // to the listener even if its text duplicates what was last shown.
        activeDialogRoot = null
        lastEmittedText = null
        frameFilter.reset()

        val sendBtn = findClickableButton(root, SEND_LABELS)
        val clicked = if (sendBtn != null) performClick(sendBtn) else false
        if (!clicked) {
            // Some OEMs auto-submit when the EditText itself is clicked
            // (the IME action triggers a hidden Send button).
            edit.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        return true
    }

    private fun getWindowRoot(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var curr = node ?: return null
        while (curr.parent != null) {
            curr = curr.parent ?: break
        }
        return curr
    }

    /**
     * Dismisses the current USSD dialog by clicking any close-style button
     * (ok/cancel/close/dismiss/done). Never performs system back action to ensure
     * MainActivity is never accidentally closed.
     *
     * @return true if the dialog was dismissed or no dialog was present
     */
    fun dismissDialog(): Boolean {
        val root = findUssdRoot() ?: activeDialogRoot
        if (root == null) {
            lastEmittedText = null
            frameFilter.reset()
            activeDialogRoot = null
            return true
        }
        var ok = false
        val close = findClickableButton(root, DISMISS_LABELS)
        if (close != null) {
            ok = performClick(close)
        }
        if (!ok) {
            walk(root) { node ->
                if (!ok) {
                    val t = node.text?.toString()?.trim()?.lowercase().orEmpty()
                    val d = node.contentDescription?.toString()?.trim()?.lowercase().orEmpty()
                    if (t in DISMISS_LABELS || d in DISMISS_LABELS) {
                        ok = performClick(node)
                    }
                }
            }
        }
        lastEmittedText = null
        frameFilter.reset()
        activeDialogRoot = null
        return ok
    }

    private fun performClick(node: AccessibilityNodeInfo?): Boolean {
        var curr: AccessibilityNodeInfo? = node ?: return false
        while (curr != null) {
            if (curr.isClickable) {
                val res = curr.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (res) return true
            }
            curr = curr.parent
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun cancelDialog(): Boolean = dismissDialog()

    /**
     * Resets state for a new USSD session.
     * Clears the deduplication filter, seen flag, and last emitted text.
     */
    fun resetForNewSession() {
        activeDialogRoot = null
        hasSeenFirstDialog = false
        lastEmittedText = null
        frameFilter.reset()
        main.removeCallbacks(sessionEndCheck)
    }

    // ─── Internal ──────────────────────────────────────────────────────────────

    private fun handleDialog(root: AccessibilityNodeInfo) {
        val texts = collectTexts(root)
        if (texts.isEmpty()) return

        val joinedText = texts.joinToString("\n").trim()
        if (joinedText.isBlank()) return

        // Filter system placeholders
        if (frameFilter.isSystemPlaceholder(joinedText)) return

        // Mark that a legitimate USSD dialog frame has been received
        hasSeenFirstDialog = true

        // Dedup: don't re-emit identical frames
        if (joinedText == lastEmittedText) return

        // Classify the frame
        val isMenu = hasInput(root)
        val isTerminal = !isMenu && hasOnlyDismiss(root)

        lastEmittedText = joinedText
        Log.d(TAG, "frame: $joinedText (menu=$isMenu terminal=$isTerminal)")
        frameListener?.onFrame(joinedText, isMenu, isTerminal)
    }

    private fun isRealUssdDialog(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val pkg = node.packageName?.toString().orEmpty()
        if (!isUssdPackage(pkg)) return false
        if (pkg == "android") {
            return hasInput(node) || hasButtons(node) || collectTexts(node).isNotEmpty()
        }
        return true
    }

    private fun hasButtons(node: AccessibilityNodeInfo): Boolean {
        var found = false
        walk(node) { n ->
            if (found) return@walk
            val cls = n.className?.toString().orEmpty()
            val text = n.text?.toString()?.trim()?.lowercase().orEmpty()
            if (cls.contains("Button", true) || text in SEND_LABELS || text in DISMISS_LABELS) {
                found = true
            }
        }
        return found
    }

    /**
     * Find the currently-visible USSD dialog's root node. Prioritizes the active
     * focused window, then walks interactive windows, and falls back to refreshed cached root.
     */
    private fun findUssdRoot(): AccessibilityNodeInfo? {
        rootInActiveWindow?.let { active ->
            if (isRealUssdDialog(active)) return active
        }
        for (w: AccessibilityWindowInfo in windows.orEmpty()) {
            val r = w.root ?: continue
            if (isRealUssdDialog(r)) return r
        }
        activeDialogRoot?.let { cached ->
            if (cached.refresh() && isRealUssdDialog(cached)) return cached
        }
        return null
    }

    private fun verifySessionStillAlive() {
        if (!sessionActive || !hasSeenFirstDialog) return
        if (findUssdRoot() == null) {
            Log.d(TAG, "USSD window gone — session ended")
            hasSeenFirstDialog = false
            frameListener?.onSessionEnded("dialog_dismissed")
            lastEmittedText = null
            frameFilter.reset()
        }
    }

    private fun collectTexts(node: AccessibilityNodeInfo): List<String> {
        val out = mutableListOf<String>()
        walk(node) { n ->
            val cls = n.className?.toString().orEmpty()
            // Skip Buttons and EditTexts — they're affordances, not messages.
            if (cls.contains("Button", true) || cls.contains("EditText", true)) return@walk
            n.text?.toString()?.takeIf { it.isNotBlank() && !frameFilter.isSystemPlaceholder(it) }
                ?.let { out.add(it) }
        }
        return out
    }

    private fun hasInput(node: AccessibilityNodeInfo): Boolean {
        var found = false
        walk(node) { n ->
            if (n.className?.toString()?.contains("EditText", true) == true) found = true
        }
        return found
    }

    private fun hasOnlyDismiss(node: AccessibilityNodeInfo): Boolean {
        val labels = mutableListOf<String>()
        walk(node) { n ->
            if (n.className?.toString()?.contains("Button", true) == true) {
                n.text?.toString()?.let { labels.add(it.lowercase()) }
            }
        }
        if (labels.isEmpty()) return false
        return labels.all { l ->
            l.contains("ok") || l.contains("cancel") || l.contains("close") ||
                l.contains("dismiss") || l.contains("done")
        }
    }

    private fun findEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var found: AccessibilityNodeInfo? = null
        walk(node) { n ->
            if (found == null && n.className?.toString()?.contains("EditText", true) == true) {
                found = n
            }
        }
        return found
    }

    private fun findClickableButton(
        node: AccessibilityNodeInfo,
        labels: Set<String>
    ): AccessibilityNodeInfo? {
        var found: AccessibilityNodeInfo? = null
        val wantsSend = labels.contains("send") || labels.contains("submit")
        val wantsCancel = labels.contains("cancel") || labels.contains("close")

        // Priority 1: Exact / prefix match on button text or contentDescription
        walk(node) { n ->
            if (found != null) return@walk
            val cls = n.className?.toString().orEmpty()
            if (cls.contains("EditText", true)) return@walk

            val text = n.text?.toString()?.trim()?.lowercase().orEmpty()
            val desc = n.contentDescription?.toString()?.trim()?.lowercase().orEmpty()

            // If we want Send, NEVER match a Cancel/Dismiss button!
            if (wantsSend && (text in setOf("cancel", "close", "dismiss", "exit") || desc in setOf("cancel", "close", "dismiss", "exit"))) {
                return@walk
            }
            // If we want Cancel, NEVER match a Send button!
            if (wantsCancel && (text in setOf("send", "submit", "reply", "continue", "yes") || desc in setOf("send", "submit", "reply", "continue", "yes"))) {
                return@walk
            }

            val matchesLabel = labels.any { label ->
                text == label || desc == label ||
                    (text.startsWith(label) && text.length <= label.length + 2)
            }

            if (matchesLabel && (cls.contains("Button", true) || n.isClickable || n.parent?.isClickable == true)) {
                found = n
            }
        }

        // Priority 2: Fallback to standard button IDs (button1 / button2) only if text didn't match
        if (found == null) {
            walk(node) { n ->
                if (found != null) return@walk
                val cls = n.className?.toString().orEmpty()
                if (cls.contains("EditText", true)) return@walk

                val id = n.viewIdResourceName.orEmpty().lowercase()
                val text = n.text?.toString()?.trim()?.lowercase().orEmpty()
                val desc = n.contentDescription?.toString()?.trim()?.lowercase().orEmpty()

                if (wantsSend && (text in setOf("cancel", "close", "dismiss", "exit") || desc in setOf("cancel", "close", "dismiss", "exit"))) {
                    return@walk
                }
                if (wantsCancel && (text in setOf("send", "submit", "reply", "continue", "yes") || desc in setOf("send", "submit", "reply", "continue", "yes"))) {
                    return@walk
                }

                val isStandardSend = wantsSend && (id.endsWith(":id/button1") || id.endsWith("/button1"))
                val isStandardCancel = wantsCancel && (id.endsWith(":id/button2") || id.endsWith("/button2"))

                if (isStandardSend || isStandardCancel) {
                    if (cls.contains("Button", true) || n.isClickable || n.parent?.isClickable == true || isStandardSend || isStandardCancel) {
                        found = n
                    }
                }
            }
        }

        // Priority 3: Fallback for custom OEM dialogs — if wantsSend and no button matched yet,
        // pick any clickable button that is explicitly not a Cancel/Close/Dismiss action.
        if (found == null && wantsSend) {
            walk(node) { n ->
                if (found != null) return@walk
                val cls = n.className?.toString().orEmpty()
                if (cls.contains("EditText", true)) return@walk

                val text = n.text?.toString()?.trim()?.lowercase().orEmpty()
                val desc = n.contentDescription?.toString()?.trim()?.lowercase().orEmpty()

                if (text in setOf("cancel", "close", "dismiss", "exit") || desc in setOf("cancel", "close", "dismiss", "exit")) {
                    return@walk
                }

                if (cls.contains("Button", true) || (n.isClickable && !n.isScrollable)) {
                    found = n
                }
            }
        }

        return found
    }

    /** Depth-first walk over the node tree. */
    private fun walk(node: AccessibilityNodeInfo, visit: (AccessibilityNodeInfo) -> Unit) {
        visit(node)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { walk(it, visit) }
        }
    }
}

private val SEND_LABELS = setOf("send", "ok", "submit", "reply", "done", "continue", "yes")
private val DISMISS_LABELS = setOf("ok", "cancel", "close", "dismiss", "done", "exit")
