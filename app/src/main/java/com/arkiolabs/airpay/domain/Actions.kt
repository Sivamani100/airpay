package com.arkiolabs.airpay.domain

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
        Regex("re-?enter\\s+(a\\s+)?(valid|correct)", RegexOption.IGNORE_CASE),
        Regex("please\\s+re-?enter\\s+(correct|valid)", RegexOption.IGNORE_CASE),
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
     * Flow: (Optional Root/Account Menu) → UPI PIN → Balance display.
     * 25-second timeout.
     */
    val CheckBalance = Action(
        code = "*99*3#",
        steps = listOf(
            // Fallback 1: Main menu if carrier routed to *99# root
            ActionStep(
                match = Regex("(welcome|select\\s*option|send\\s*money|1\\s*[:.]\\s*send)", RegexOption.IGNORE_CASE),
                reply = "3",
                label = "Selecting Check Balance"
            ),
            // Fallback 2: Account selection if multiple accounts or account list appears
            ActionStep(
                match = Regex("\\b(select\\s*account|select\\s*bank|choose\\s*account|1\\s*[:.]\\s*\\S+)\\b", RegexOption.IGNORE_CASE),
                reply = "1",
                label = "Selecting Account"
            ),
            // Step: PIN prompt
            ActionStep(
                match = Regex("\\b(upi\\s*pin|mpin|\\d+\\s*digit.*pin|enter.*pin)\\b", RegexOption.IGNORE_CASE),
                reply = "{pin}",
                label = "Entering UPI PIN",
                delayMs = 300L
            ),
            // Final Step: Balance display
            ActionStep(
                match = Regex("balance|avail(able)?|rs\\.?\\s*\\d|inr\\s*\\d|₹\\s*\\d|amount\\s*(is|:)|ledger|a/c\\s*bal", RegexOption.IGNORE_CASE),
                done = true,
                label = "Balance fetched"
            )
        ),
        failurePatterns = COMMON_FAILURES.filterNot {
            it.pattern.contains("insufficient", ignoreCase = true) || it.pattern.contains("low\\s+balance", ignoreCase = true)
        } + listOf(
            Regex("pin\\s*(does\\s*not|doesn['']?t)\\s*match", RegexOption.IGNORE_CASE),
            Regex("pin\\s*mismatch", RegexOption.IGNORE_CASE),
            Regex("authentication\\s*failed", RegexOption.IGNORE_CASE),
            Regex("max(imum)?\\s*(attempts|tries|retries)", RegexOption.IGNORE_CASE),
            Regex("pin\\s*(blocked|locked|expired)", RegexOption.IGNORE_CASE)
        ),
        timeoutMs = 30_000L
    )

    /**
     * Fetch user's registered UPI ID from *99# menu via direct shortcode.
     * Code: *99*4*4# (direct fast-path) with fallback menu step handling.
     */
    val FetchUpiProfile = Action(
        code = "*99*4*4#",
        steps = listOf(
            // Direct match: carrier immediately returns "Your payment address\nPrimary: 9849497911@upi"
            ActionStep(
                match = Regex("(payment\\s*address|primary\\s*[:\\s]|upi\\s*id|vpa|@[a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
                done = true,
                label = "Capturing UPI ID"
            ),
            // Fallback step 1: main menu
            ActionStep(
                match = Regex("(welcome|select\\s*option|send\\s*money|1\\s*[:.]\\s*send)", RegexOption.IGNORE_CASE),
                reply = "4",
                label = "Opening My Profile"
            ),
            // Fallback step 2: profile submenu
            ActionStep(
                match = Regex("(my\\s*profile|profile|change\\s*bank|change\\s*language)", RegexOption.IGNORE_CASE),
                reply = "4",
                label = "Selecting UPI ID"
            ),
            // Fallback step 3: final address screen
            ActionStep(
                match = Regex("(payment\\s*address|primary\\s*[:\\s]|upi\\s*id|vpa|@[a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
                done = true,
                label = "Capturing UPI ID"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 18_000L
    )

    /**
     * Fetch user's registered UPI ID from SIM profile via carrier direct code.
     */
    val FetchUpiId = Action(
        code = "*99*4*4#",
        steps = listOf(
            ActionStep(
                match = Regex("(payment\\s*address|primary\\s*[:\\s]|upi\\s*id|vpa|@[a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
                done = true,
                label = "Capturing UPI ID from SIM"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 18_000L
    )

    /**
     * Send money to bank account using IFSC & Account Number (*99*1*5#).
     */
    val SendToBank = Action(
        code = "*99*1*5#",
        steps = listOf(
            ActionStep(
                match = Regex("(ifsc|branch|code)", RegexOption.IGNORE_CASE),
                reply = "{ifsc}",
                label = "Entering IFSC"
            ),
            ActionStep(
                match = Regex("(account|a/c|beneficiary.*acc)", RegexOption.IGNORE_CASE),
                reply = "{account}",
                label = "Entering Account Number"
            ),
            ActionStep(
                match = Regex("(enter\\s*amount|amount\\s*in\\s*rs|\\bamount\\b)", RegexOption.IGNORE_CASE),
                reply = "{amount}",
                label = "Entering Amount"
            ),
            ActionStep(
                match = Regex("\\b(remark|comment|note)\\b", RegexOption.IGNORE_CASE),
                reply = "{note}",
                label = "Adding Remarks"
            ),
            ActionStep(
                match = Regex("\\b(confirm|press\\s*1|1\\s*to\\s*confirm|1\\s*to\\s*proceed|are\\s*you\\s*sure|1\\s*[:.]\\s*confirm)\\b", RegexOption.IGNORE_CASE),
                reply = "1",
                label = "Confirming Transfer"
            ),
            ActionStep(
                match = Regex("\\b(upi\\s*pin|mpin|\\d+\\s*digit.*pin|enter.*pin)\\b", RegexOption.IGNORE_CASE),
                reply = "{pin}",
                label = "Entering UPI PIN"
            ),
            ActionStep(
                match = Regex("successful|transferred|debited|completed|ref(?:erence)?|payment\\s+done", RegexOption.IGNORE_CASE),
                done = true,
                label = "Transfer Complete"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 40_000L
    )

    /**
     * Request money using Mobile Number (*99*2*1#).
     */
    val RequestMoneyByPhone = Action(
        code = "*99*2*1#",
        steps = listOf(
            ActionStep(
                match = Regex("(mobile|phone|number)", RegexOption.IGNORE_CASE),
                reply = "{phone}",
                label = "Entering Mobile Number"
            ),
            ActionStep(
                match = Regex("(enter\\s*amount|amount\\s*in\\s*rs|\\bamount\\b)", RegexOption.IGNORE_CASE),
                reply = "{amount}",
                label = "Entering Amount"
            ),
            ActionStep(
                match = Regex("\\b(remark|comment|note)\\b", RegexOption.IGNORE_CASE),
                reply = "{note}",
                label = "Adding Remarks"
            ),
            ActionStep(
                match = Regex("\\b(confirm|press\\s*1|1\\s*to\\s*confirm|1\\s*to\\s*proceed|are\\s*you\\s*sure)\\b", RegexOption.IGNORE_CASE),
                reply = "1",
                label = "Confirming Request"
            ),
            ActionStep(
                match = Regex("request\\s*(sent|successful|completed)|ref(?:erence)?|successful", RegexOption.IGNORE_CASE),
                done = true,
                label = "Request Sent"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 30_000L
    )

    /**
     * Request money using UPI ID (*99*2*2#).
     */
    val RequestMoneyByVpa = Action(
        code = "*99*2*2#",
        steps = listOf(
            ActionStep(
                match = Regex("(vpa|upi\\s*id|address|virtual)", RegexOption.IGNORE_CASE),
                reply = "{vpa}",
                label = "Entering UPI ID"
            ),
            ActionStep(
                match = Regex("(enter\\s*amount|amount\\s*in\\s*rs|\\bamount\\b)", RegexOption.IGNORE_CASE),
                reply = "{amount}",
                label = "Entering Amount"
            ),
            ActionStep(
                match = Regex("\\b(remark|comment|note)\\b", RegexOption.IGNORE_CASE),
                reply = "{note}",
                label = "Adding Remarks"
            ),
            ActionStep(
                match = Regex("\\b(confirm|press\\s*1|1\\s*to\\s*confirm|1\\s*to\\s*proceed|are\\s*you\\s*sure)\\b", RegexOption.IGNORE_CASE),
                reply = "1",
                label = "Confirming Request"
            ),
            ActionStep(
                match = Regex("request\\s*(sent|successful|completed)|ref(?:erence)?|successful", RegexOption.IGNORE_CASE),
                done = true,
                label = "Request Sent"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 30_000L
    )

    /**
     * View recent transactions across the UPI network (*99*6*1#).
     */
    val ViewRecentTransactions = Action(
        code = "*99*6*1#",
        steps = listOf(
            ActionStep(
                match = Regex("txn|trans|statement|rs\\.?|inr|no\\s*txn|last|recent|ref|dr|cr", RegexOption.IGNORE_CASE),
                done = true,
                label = "Fetching Recent Transactions"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 25_000L
    )

    /**
     * View rewards information from carrier gateway (*99*6*2#).
     */
    val ViewRewards = Action(
        code = "*99*6*2#",
        steps = listOf(
            ActionStep(
                match = Regex("reward|point|coupon|offer|cashback|bonus|balance|no\\s*reward|info", RegexOption.IGNORE_CASE),
                done = true,
                label = "Fetching Rewards"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 25_000L
    )

    /**
     * Set or reset UPI PIN using Debit Card (*99*7*1#).
     */
    val ResetUpiPin = Action(
        code = "*99*7*1#",
        steps = listOf(
            ActionStep(
                match = Regex("(debit\\s*card|last\\s*6|card\\s*no|digits)", RegexOption.IGNORE_CASE),
                reply = "{debitDigits}",
                label = "Entering Card Digits"
            ),
            ActionStep(
                match = Regex("(expiry|valid|mm/yy|mmyy|valid\\s*thru)", RegexOption.IGNORE_CASE),
                reply = "{expiry}",
                label = "Entering Expiry Date"
            ),
            ActionStep(
                match = Regex("(new\\s*pin|set\\s*pin|enter\\s*pin|enter.*new)", RegexOption.IGNORE_CASE),
                reply = "{newPin}",
                label = "Entering New PIN"
            ),
            ActionStep(
                match = Regex("(confirm\\s*pin|re-?enter\\s*pin|confirm.*new)", RegexOption.IGNORE_CASE),
                reply = "{confirmPin}",
                label = "Confirming New PIN"
            ),
            ActionStep(
                match = Regex("successful|changed|set|completed|pin\\s*created", RegexOption.IGNORE_CASE),
                done = true,
                label = "PIN Updated"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 40_000L
    )

    /**
     * Change existing UPI PIN (*99*7*2#).
     */
    val ChangeUpiPin = Action(
        code = "*99*7*2#",
        steps = listOf(
            ActionStep(
                match = Regex("(old\\s*pin|existing\\s*pin|current\\s*pin)", RegexOption.IGNORE_CASE),
                reply = "{oldPin}",
                label = "Entering Old PIN"
            ),
            ActionStep(
                match = Regex("(new\\s*pin|enter\\s*pin|enter.*new)", RegexOption.IGNORE_CASE),
                reply = "{newPin}",
                label = "Entering New PIN"
            ),
            ActionStep(
                match = Regex("(confirm\\s*pin|re-?enter\\s*pin|confirm.*new)", RegexOption.IGNORE_CASE),
                reply = "{confirmPin}",
                label = "Confirming New PIN"
            ),
            ActionStep(
                match = Regex("successful|changed|updated|completed", RegexOption.IGNORE_CASE),
                done = true,
                label = "PIN Changed"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 35_000L
    )

    /**
     * Change linked bank account (*99*4*1#).
     */
    val ChangeLinkedBank = Action(
        code = "*99*4*1#",
        steps = listOf(
            ActionStep(
                match = Regex("(select\\s*bank|enter\\s*bank|name\\s*bank|3\\s*letter|ifsc|search)", RegexOption.IGNORE_CASE),
                reply = "{bankName}",
                label = "Selecting Bank"
            ),
            ActionStep(
                match = Regex("(select\\s*account|choose\\s*account|1\\s*[:.])", RegexOption.IGNORE_CASE),
                reply = "1",
                label = "Selecting Account"
            ),
            ActionStep(
                match = Regex("linked|changed|set\\s*as\\s*primary|successful|completed|updated", RegexOption.IGNORE_CASE),
                done = true,
                label = "Bank Account Updated"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 35_000L
    )

    /**
     * Fetch pending collect requests (*99*5#).
     */
    val PendingRequests = Action(
        code = "*99*5#",
        steps = listOf(
            ActionStep(
                match = Regex("pending|request|no\\s*pending|collect|approve|reject|decline", RegexOption.IGNORE_CASE),
                done = true,
                label = "Fetching Pending Requests"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 25_000L
    )

    /**
     * Fetch user profile details (*99*4*3# for My Details including Name, Bank, Account, UPI ID).
     */
    val FetchMyDetails = Action(
        code = "*99*4*3#",
        steps = listOf(
            // Direct response when carrier supports direct *99*4*3#
            ActionStep(
                match = Regex("(name|upi\\s*id|vpa|account|a/c|bank|primary|payment\\s*address|@[a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
                done = true,
                label = "Profile Details Fetched"
            ),
            // Fallback 1: Carrier lands on main menu
            ActionStep(
                match = Regex("(welcome|select\\s*option|send\\s*money|1\\s*[:.]\\s*send)", RegexOption.IGNORE_CASE),
                reply = "4",
                label = "Opening Profile"
            ),
            // Fallback 2: Carrier lands on Profile menu (Option 3 = My Details)
            ActionStep(
                match = Regex("(my\\s*profile|profile|change\\s*bank|change\\s*language|select\\s*option|1\\s*[:.]|3\\s*[:.]\\s*my\\s*details)", RegexOption.IGNORE_CASE),
                reply = "3",
                label = "Selecting My Details"
            ),
            // Fallback 3: Details frame
            ActionStep(
                match = Regex("(name|upi\\s*id|vpa|account|a/c|bank|primary|payment\\s*address|@[a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
                done = true,
                label = "Profile Details Fetched"
            )
        ),
        failurePatterns = COMMON_FAILURES,
        timeoutMs = 25_000L
    )
}
