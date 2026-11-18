package com.arkiolabs.downpay.domain

/**
 * Pre-built USSD action definitions for supported operations.
 * Each action defines the USSD code, step sequence with regex matchers,
 * failure patterns, and timeout configuration.
 *
 * Failure patterns are ported verbatim from the proven `UssdAction.ts`
 * reference. Tighten them with operator-specific phrases as you encounter
 * new wording — never loosen them past `\b` word boundaries or you will
 * false-positive on prompts that share words.
 */
object Actions {

    /**
     * Common failure patterns shared across all USSD actions, ported from the
     * `UssdAction.ts` reference. Run BEFORE step matching — if any of these
     * fire, the runner bails out with the carrier text as the error.
     *
     * Tip when reading these regexes:
     *   - `\s+` matches one or more spaces (so it bridges filler words like
     *     "is" only when paired with optional groups)
     *   - `\b` is a word-boundary so we don't match inside other words
     */
    val COMMON_FAILURES: List<Regex> = listOf(
        // Generic catch-all — handles "not a valid UPI ID", "is not registered",
        // "not found", "not a registered user", "not allowed", etc. This single
        // pattern covers the bulk of carrier rejections.
        Regex("\\bnot\\s+(a\\s+)?(valid|registered|recognised|recognized|allowed|found|active|enabled|known|supported)\\b", RegexOption.IGNORE_CASE),

        // Generic "didn't work" / decline
        Regex("\\bnot\\s+(debited|sent|processed|completed)\\b", RegexOption.IGNORE_CASE),
        Regex("transaction\\s+(declined|failed|cancelled|denied|aborted)", RegexOption.IGNORE_CASE),
        Regex("unable\\s+to\\s+process", RegexOption.IGNORE_CASE),

        // Wrong / invalid input — covers many phrasings
        Regex("\\b(invalid|incorrect|wrong|bad)\\s+(pin|upi|vpa|amount|input|entry|id|password|number|account)\\b", RegexOption.IGNORE_CASE),
        // Mirror: noun-then-adjective phrasing (carriers commonly say "UPI
        // PIN is incorrect or invalid"). The pattern above only catches
        // "incorrect PIN"; this catches "PIN is incorrect/invalid/wrong"
        // and "entered UPI PIN is incorrect/invalid" so AUTO mode actually
        // bails out on a wrong-PIN reply instead of falling through to a
        // raw carrier dialog.
        Regex("\\b(pin|upi\\s*pin|upi\\s*id|vpa|password)\\s+(is\\s+)?(incorrect|invalid|wrong|mismatch)\\b", RegexOption.IGNORE_CASE),
        Regex("entered\\s+(upi\\s+)?pin\\s+(is\\s+)?(incorrect|invalid|wrong)", RegexOption.IGNORE_CASE),
        Regex("enter\\s+(a\\s+)?(valid|correct)", RegexOption.IGNORE_CASE),
        Regex("please\\s+enter\\s+(correct|valid)", RegexOption.IGNORE_CASE),
        Regex("please\\s+check\\s+and\\s+try\\s+again", RegexOption.IGNORE_CASE),

        // Specific UPI / PSP phrases — kept separately for clarity even though
        // the generic catch-all above usually matches them too.
        Regex("psp\\s+(is\\s+)?not\\s+(registered|recognised|recognized)", RegexOption.IGNORE_CASE),
        Regex("vpa\\s+(does\\s+not\\s+exist|is\\s+not\\s+(registered|valid))", RegexOption.IGNORE_CASE),
        Regex("upi\\s*id\\s+(is\\s+)?(invalid|incorrect|wrong)", RegexOption.IGNORE_CASE),

        // Account / user not found
        Regex("(no|not\\s+a)\\s+(account|user|customer)\\s+(found|registered|exists)", RegexOption.IGNORE_CASE),
        Regex("account\\s+(not\\s+found|does\\s+not\\s+exist)", RegexOption.IGNORE_CASE),
        Regex("user\\s+not\\s+found", RegexOption.IGNORE_CASE),

        // Self-pay
        Regex("(sender|payer).*(receiver|payee).*same|(receiver|payee).*(sender|payer).*same", RegexOption.IGNORE_CASE),
        Regex("\\bsame\\s+(account|vpa|user)\\b", RegexOption.IGNORE_CASE),
        Regex("cannot\\s+(send|pay)\\s+(to\\s+)?(self|yourself|same)", RegexOption.IGNORE_CASE),

        // Funds / limit
        Regex("(insufficient\\s+(funds|balance)|(balance|funds)\\s+(is\\s+)?insufficient|low\\s+balance)", RegexOption.IGNORE_CASE),
        Regex("exceed(s|ed)?\\s+(limit|amount)|over\\s+limit", RegexOption.IGNORE_CASE),

        // Service availability & technical errors
        Regex("service\\s+(unavailable|not\\s+available|down)", RegexOption.IGNORE_CASE),
        Regex("try\\s+(again\\s+)?later|temporarily\\s+unavailable", RegexOption.IGNORE_CASE),
        Regex("technical\\s+(error|issue|problem)", RegexOption.IGNORE_CASE),
        Regex("session\\s+(timed\\s+out|expired|terminated)", RegexOption.IGNORE_CASE),

        // User-not-onboarded phrases — the carrier returns these when
        // the user's mobile number isn't linked to a bank account.
        // The UI routes these specifically to a "you're not registered" flow.
        Regex("could\\s+not\\s+find\\s+(your|ur)\\s+bank", RegexOption.IGNORE_CASE),
        Regex("is\\s+not\\s+a\\s+valid\\s+selection", RegexOption.IGNORE_CASE),
        Regex("please\\s+enter\\s+the\\s+correct\\s+no", RegexOption.IGNORE_CASE),
        Regex("bank\\s+not\\s+found|no\\s+bank\\s+(linked|found)", RegexOption.IGNORE_CASE)
    )

    /**
     * Send money via UPI using carrier gateway code.
     * Flow: VPA → Amount → Remark (optional) → Beneficiary Confirm (press 1) → UPI PIN → Success.
     * 35-second hard timeout.
     */
    val SendUpi = Action(
        code = "*99*1*3#",
        steps = listOf(
            ActionStep(
                match = Regex("(receiver|payee|recipient|vpa|virtual.*payment|upi.*id)", RegexOption.IGNORE_CASE),
                reply = "{vpa}",
                label = "Sending UPI ID"
            ),
            ActionStep(
                match = Regex("(enter\\s*amount|amount\\s*in\\s*rs|\\bamount\\b)", RegexOption.IGNORE_CASE),
                reply = "{amount}",
                label = "Sending amount"
            ),
            ActionStep(
                match = Regex("\\b(remark|comment|note)\\b", RegexOption.IGNORE_CASE),
                reply = "{note}",
                label = "Adding note"
            ),
            ActionStep(
                // Displays beneficiary name & amount and prompts "1 to confirm" or "Press 1 to proceed" BEFORE the UPI PIN
                match = Regex("\\b(confirm|press\\s*1|1\\s*to\\s*confirm|1\\s*to\\s*proceed|are\\s*you\\s*sure|1\\s*[:.]\\s*confirm)\\b", RegexOption.IGNORE_CASE),
                reply = "1",
                label = "Confirming"
            ),
            ActionStep(
                match = Regex("\\b(upi\\s*pin|mpin|\\d+\\s*digit.*pin|enter.*pin)\\b", RegexOption.IGNORE_CASE),
                reply = "{pin}",
                label = "Entering UPI PIN"
            ),
            ActionStep(
                match = Regex("successful|payment\\s+(?:sent|completed|done)|thank\\s*you\\s*for\\s*using|ref(?:erence)?\\s*(?:no|number|id)?\\s*[:\\-]?|debited", RegexOption.IGNORE_CASE),
                done = true,
                label = "Payment complete"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 35_000L
    )

    /**
     * Send money directly to a Mobile Number using carrier gateway code.
     * Flow: Mobile No → Amount → Remark (optional) → Beneficiary Confirm (press 1) → UPI PIN → Success.
     * 35-second hard timeout.
     */
    val SendToMobile = Action(
        code = "*99*1*1#",
        steps = listOf(
            ActionStep(
                // Do NOT include bare "enter" or "number" — they collide with "Enter amount" and "Enter PIN"
                match = Regex("(mobile|phone|beneficiary|receiver|recipient|payee)", RegexOption.IGNORE_CASE),
                reply = "{vpa}",
                label = "Sending Mobile Number"
            ),
            ActionStep(
                match = Regex("(enter\\s*amount|amount\\s*in\\s*rs|\\bamount\\b)", RegexOption.IGNORE_CASE),
                reply = "{amount}",
                label = "Sending amount"
            ),
            ActionStep(
                match = Regex("\\b(remark|comment|note)\\b", RegexOption.IGNORE_CASE),
                reply = "{note}",
                label = "Adding note"
            ),
            ActionStep(
                // Displays beneficiary name & amount and prompts "1 to confirm" BEFORE the UPI PIN
                match = Regex("\\b(confirm|press\\s*1|1\\s*to\\s*confirm|1\\s*to\\s*proceed|are\\s*you\\s*sure|1\\s*[:.]\\s*confirm)\\b", RegexOption.IGNORE_CASE),
                reply = "1",
                label = "Confirming"
            ),
            ActionStep(
                match = Regex("\\b(upi\\s*pin|mpin|\\d+\\s*digit.*pin|enter.*pin)\\b", RegexOption.IGNORE_CASE),
                reply = "{pin}",
                label = "Entering UPI PIN"
            ),
            ActionStep(
                match = Regex("successful|payment\\s+(?:sent|completed|done)|thank\\s*you|ref(?:erence)?|debited", RegexOption.IGNORE_CASE),
                done = true,
                label = "Payment complete"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 35_000L
    )

    /**
     * Check balance via carrier gateway code.
     * 2-step flow: PIN → Balance display.
     * 18-second hard timeout.
     */
    val CheckBalance = Action(
        code = "*99*3#",
        steps = listOf(
            ActionStep(
                match = Regex("upi\\s*pin|enter.*pin|6\\s*digit.*pin|4\\s*digit.*pin", RegexOption.IGNORE_CASE),
                reply = "{pin}",
                label = "Entering UPI PIN"
            ),
            ActionStep(
                match = Regex("balance|avail(able)?|rs\\.?\\s*\\d|inr\\s*\\d|₹\\s*\\d|amount\\s*(is|:)|ledger|a/c\\s*bal", RegexOption.IGNORE_CASE),
                done = true,
                label = "Balance fetched"
            )
        ),
        failurePatterns = COMMON_FAILURES + listOf(
            Regex("pin\\s*(does\\s*not|doesn['']?t)\\s*match", RegexOption.IGNORE_CASE),
            Regex("pin\\s*mismatch", RegexOption.IGNORE_CASE),
            Regex("authentication\\s*failed", RegexOption.IGNORE_CASE),
            Regex("max(imum)?\\s*(attempts|tries|retries)", RegexOption.IGNORE_CASE),
            Regex("pin\\s*(blocked|locked|expired)", RegexOption.IGNORE_CASE)
        ),
        timeoutMs = 18_000L
    )

    /**
     * Fetch user's registered UPI ID from SIM profile via carrier gateway code.
     */
    val FetchUpiId = Action(
        code = "*99*4*4#",
        steps = listOf(
            ActionStep(
                match = Regex(".*", RegexOption.IGNORE_CASE),
                done = true,
                label = "Fetching UPI ID from SIM"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 18_000L
    )
}
