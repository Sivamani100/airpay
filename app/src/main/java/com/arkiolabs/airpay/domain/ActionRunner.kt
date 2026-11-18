package com.arkiolabs.airpay.domain

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Holds the running state of an action execution.
 */
data class ActionRun(
    val events: SharedFlow<ActionEvent>,
    val cancel: suspend () -> Unit,
    val result: Deferred<ActionResult>
)

/**
 * Executes scripted USSD action flows against the [UssdEnginePort].
 * Pure domain logic with no Android framework dependencies (testable in isolation).
 *
 * Termination rules (priority order, mirrors the proven `UssdAction.ts` runner):
 *   1. Frame matches one of [UNIVERSAL_SUCCESS_PATTERNS] → success, even if
 *      the dialog still has an input field. Some carriers append a
 *      "1. Save contact 2. Exit" menu after the success message; we don't
 *      want that follow-up.
 *   2. Frame matches one of `action.failurePatterns` → fail with carrier text.
 *   3. Frame matches the current `done` step → success.
 *   4. Frame matches a non-`done` step → send the step's reply.
 *   5. Frame is `isTerminal` and didn't match any of the above → fail with
 *      carrier text. This catches "PSP not registered", "could not find your
 *      bank", etc., without us pre-listing every operator's wording.
 */
class ActionRunner(private val engine: UssdEnginePort) {

    companion object {
        const val DEFAULT_PACING_DELAY_MS = 250L

        /**
         * Universal success patterns. Checked BEFORE step matching on every
         * frame — money is sent, we're done, regardless of any follow-up
         * input the carrier appends.
         *
         * Why this exists: some carriers show the success message with a
         * follow-up input ("Your payment... is successful. Select option to
         * 1. Save contact 2. Exit") so isMenu is true. Without this
         * early-exit, the runner would wait for the next step and time out.
         */
        val UNIVERSAL_SUCCESS_PATTERNS: List<Regex> = listOf(
            Regex("\\bis\\s+successful\\b", RegexOption.IGNORE_CASE),
            Regex("payment\\s+(?:to\\s+\\S+\\s+)?(?:for\\s+)?(?:rs\\.?|inr|₹)?\\s*\\d.*successful", RegexOption.IGNORE_CASE),
            Regex("successfully\\s+(?:sent|paid|completed|debited|transferred)", RegexOption.IGNORE_CASE),
            Regex("transaction\\s+successful", RegexOption.IGNORE_CASE),
            Regex("txn\\s+successful", RegexOption.IGNORE_CASE),
            Regex("payment\\s+successful", RegexOption.IGNORE_CASE),
            // A real referenceId in the text is a strong success signal
            Regex("ref(?:erence)?\\s*(?:id|no|number|#)?\\s*[:\\-]?\\s*\\d{6,}", RegexOption.IGNORE_CASE)
        )
    }

    /**
     * Checks whether the AccessibilityService is currently running.
     */
    fun isServiceEnabled(): Boolean = engine.isServiceEnabled()

    /**
     * Starts executing an action flow.
     */
    fun runAction(
        action: Action,
        vars: Map<String, String>,
        scope: CoroutineScope
    ): ActionRun {
        val eventFlow = MutableSharedFlow<ActionEvent>(extraBufferCapacity = 32)
        val resultDeferred = CompletableDeferred<ActionResult>()
        var job: Job? = null

        job = scope.launch {
            var currentStepIndex = 0
            val completedSteps = mutableSetOf<Int>()
            val totalSteps = action.steps.size
            // Guard against the race where engine.cancel() — fired from
            // inside the collect lambda after we emit a terminal event —
            // synthesises its own "User cancelled" terminal frame into the
            // SharedFlow buffer. The buffered frame is delivered to the
            // very next collect tick (before cooperative cancellation
            // kicks in), causing a successful payment to be re-classified
            // as "Unexpected carrier response" by Priority 4.
            //
            // Once this flips to true we stop processing further frames
            // until cancellation actually takes effect on the next
            // suspension point.
            var terminated = false

            try {
                // 1. Dismiss any leftover dialog and dial the action code.
                engine.dismissDialog()
                engine.dial(action.code)

                // 2. Capture our session ID. Frames with a different
                //    sessionId are leftovers from a prior run and discarded.
                val mySessionId = engine.getSessionId()

                // 3. Walk frames as they arrive.
                engine.frames.collect { frame ->
                    if (terminated) return@collect
                    if (frame.sessionId != mySessionId) return@collect

                    val text = frame.text

                    // ── Priority 1: universal success short-circuit ────────
                    if (matchesUniversalSuccess(text)) {
                        terminated = true
                        eventFlow.emit(ActionEvent.Done(text))
                        resultDeferred.complete(ActionResult(success = true, resultText = text))
                        engine.dismissDialog()
                        engine.cancel()
                        job?.cancel()
                        return@collect
                    }

                    // ── Priority 2: action-specific failure patterns ───────
                    if (matchesFailurePattern(text, action.failurePatterns)) {
                        terminated = true
                        eventFlow.emit(ActionEvent.Error("Transaction failed", text))
                        resultDeferred.complete(ActionResult(success = false, resultText = text))
                        engine.dismissDialog()
                        engine.cancel()
                        job?.cancel()
                        return@collect
                    }

                    // ── Priority 3: step matching ──────────────────────────
                    var matchedIndex = matchStep(text, action.steps, currentStepIndex)
                    // If sequential match failed or was previously consumed, search any uncompleted step
                    if (matchedIndex < 0 || matchedIndex in completedSteps) {
                        matchedIndex = findUncompletedStep(text, action.steps, completedSteps)
                    }

                    if (matchedIndex >= 0) {
                        completedSteps.add(matchedIndex)
                        val step = action.steps[matchedIndex]
                        currentStepIndex = maxOf(currentStepIndex, matchedIndex + 1)

                        eventFlow.emit(ActionEvent.Progress(matchedIndex, totalSteps, step.label))
                        eventFlow.emit(ActionEvent.Frame(frame, matchedIndex))

                        if (step.done) {
                            terminated = true
                            eventFlow.emit(ActionEvent.Done(text))
                            resultDeferred.complete(ActionResult(success = true, resultText = text))
                            engine.dismissDialog()
                            job?.cancel()
                            return@collect
                        }

                        val reply = step.reply?.let { fillTemplate(it, vars) }
                        if (reply != null) {
                            // Verify the accessibility service is still alive
                            // before trying to reply. Samsung's battery
                            // optimization can kill it mid-session.
                            if (!engine.isServiceEnabled()) {
                                val errorMsg = "Accessibility service was killed — re-enable it in settings"
                                terminated = true
                                eventFlow.emit(ActionEvent.Error(errorMsg, text))
                                resultDeferred.complete(ActionResult(success = false, resultText = errorMsg))
                                job?.cancel()
                                return@collect
                            }

                            // Pacing delay: some carriers drop replies that
                            // arrive too fast; the user also gets a smoother
                            // progression instead of a slot-machine autofill.
                            delay(step.delayMs)

                            engine.sendReply(reply)
                            eventFlow.emit(ActionEvent.Reply(reply, matchedIndex))
                        }
                        return@collect
                    }

                    // ── Priority 4: terminal frame with no match ───────────
                    if (frame.isTerminal) {
                        terminated = true
                        eventFlow.emit(ActionEvent.Error("Unexpected carrier response", text))
                        resultDeferred.complete(ActionResult(success = false, resultText = text))
                        engine.dismissDialog()
                        engine.cancel()
                        job?.cancel()
                        return@collect
                    }
                    // Non-terminal, non-matched frames are ignored — we wait
                    // for the next frame.
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                if (!resultDeferred.isCompleted) {
                    resultDeferred.complete(ActionResult(success = false, resultText = "Cancelled"))
                }
            } catch (e: Exception) {
                if (!resultDeferred.isCompleted) {
                    val errorMsg = e.message ?: "Unknown error"
                    eventFlow.emit(ActionEvent.Error(errorMsg, ""))
                    resultDeferred.complete(ActionResult(success = false, resultText = errorMsg))
                }
            }
        }

        val cancelFn: suspend () -> Unit = {
            engine.cancel()
            if (!resultDeferred.isCompleted) {
                resultDeferred.complete(ActionResult(success = false, resultText = "Cancelled"))
            }
            job.cancel()
        }

        return ActionRun(
            events = eventFlow.asSharedFlow(),
            cancel = cancelFn,
            result = resultDeferred
        )
    }

    /**
     * Walks the step list starting from [fromIndex] and returns the index
     * of the first step whose regex matches [frameText], or -1 if no match.
     */
    fun matchStep(frameText: String, steps: List<ActionStep>, fromIndex: Int): Int {
        for (i in fromIndex until steps.size) {
            if (steps[i].match.containsMatchIn(frameText)) {
                return i
            }
        }
        return -1
    }

    /**
     * Resolves an uncompleted step when network frames arrive out-of-order
     * or when intermediate prompts (like optional remarks) are skipped by the bank.
     */
    fun findUncompletedStep(frameText: String, steps: List<ActionStep>, completed: Set<Int>): Int {
        // 1. Check for terminal/done step first
        val doneIdx = steps.indexOfFirst { it.done && it.match.containsMatchIn(frameText) }
        if (doneIdx >= 0) return doneIdx

        // 2. Disambiguate by prompt intent to prevent false matches:
        // Confirmation prompts ("Press 1 to confirm", "1. Confirm")
        val isConfirm = Regex("\\b(confirm|press\\s*1|1\\s*to\\s*confirm|1\\s*to\\s*proceed|are\\s*you\\s*sure|1\\s*[:.]\\s*confirm)\\b", RegexOption.IGNORE_CASE).containsMatchIn(frameText)
        if (isConfirm) {
            val confirmIdx = steps.indices.firstOrNull { i -> i !in completed && steps[i].reply == "1" && steps[i].match.containsMatchIn(frameText) }
            if (confirmIdx != null) return confirmIdx
        }

        // PIN prompts ("Enter UPI PIN", "6 digit PIN")
        val isPin = Regex("\\b(upi\\s*pin|mpin|\\d+\\s*digit.*pin|enter.*pin)\\b", RegexOption.IGNORE_CASE).containsMatchIn(frameText)
        if (isPin) {
            val pinIdx = steps.indices.firstOrNull { i -> i !in completed && steps[i].reply == "{pin}" && steps[i].match.containsMatchIn(frameText) }
            if (pinIdx != null) return pinIdx
        }

        // Amount prompts ("Enter amount", "Amount in Rs")
        val isAmount = Regex("(enter\\s*amount|amount\\s*in\\s*rs|\\bamount\\b)", RegexOption.IGNORE_CASE).containsMatchIn(frameText)
        if (isAmount && !isConfirm && !isPin) {
            val amountIdx = steps.indices.firstOrNull { i -> i !in completed && steps[i].reply == "{amount}" && steps[i].match.containsMatchIn(frameText) }
            if (amountIdx != null) return amountIdx
        }

        // Remark prompts ("Enter remark", "note")
        val isRemark = Regex("\\b(remark|comment|note)\\b", RegexOption.IGNORE_CASE).containsMatchIn(frameText)
        if (isRemark) {
            val remarkIdx = steps.indices.firstOrNull { i -> i !in completed && steps[i].reply == "{note}" && steps[i].match.containsMatchIn(frameText) }
            if (remarkIdx != null) return remarkIdx
        }

        // Recipient / Mobile / VPA prompts
        val isRecipient = Regex("(mobile|phone|beneficiary|receiver|recipient|payee|vpa|virtual.*payment|upi.*id)", RegexOption.IGNORE_CASE).containsMatchIn(frameText)
        if (isRecipient && !isConfirm && !isPin) {
            val recipientIdx = steps.indices.firstOrNull { i -> i !in completed && steps[i].reply == "{vpa}" && steps[i].match.containsMatchIn(frameText) }
            if (recipientIdx != null) return recipientIdx
        }

        // Fallback: any uncompleted step whose match pattern matches
        for (i in steps.indices) {
            if (i !in completed && steps[i].match.containsMatchIn(frameText)) {
                return i
            }
        }
        return -1
    }

    /**
     * Replaces `{key}` placeholders in [template] with values from [vars].
     */
    fun fillTemplate(template: String, vars: Map<String, String>): String {
        var result = template
        for ((key, value) in vars) {
            result = result.replace("{$key}", value)
        }
        return result
    }

    fun matchesUniversalSuccess(text: String): Boolean =
        UNIVERSAL_SUCCESS_PATTERNS.any { it.containsMatchIn(text) }

    fun matchesFailurePattern(text: String, patterns: List<Regex>): Boolean =
        patterns.any { it.containsMatchIn(text) }
}
