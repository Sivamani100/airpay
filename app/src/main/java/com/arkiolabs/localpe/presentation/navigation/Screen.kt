package com.arkiolabs.localpe.presentation.navigation

/**
 * Navigation routes for the PhonePe-styled architecture.
 *
 * Bottom navigation tabs:
 *  - Home: Dashboard with profile avatar, banner, "Money Transfers" 4-circle grid, and offline status.
 *  - Search: Fast search for VPAs, contacts, and transactions.
 *  - Scan: Camera QR scanner (opened by the elevated center purple button).
 *  - Alerts: Service status & offline payment alerts.
 *  - History: Transaction records with search and statements.
 *
 * Additional screens:
 *  - Pay: Send Money form (to Mobile Number or UPI ID).
 *  - Balance: Check Balance via USSD.
 *  - MyQr: My QR Code & offline receiving address.
 *  - Settings: Operation mode, permissions, privacy.
 *  - Faq: Help Center.
 *  - Legal: Terms & Privacy.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Scan : Screen("scan")
    object Alerts : Screen("alerts")
    object History : Screen("history")

    object Pay : Screen("pay")
    object ToMobile : Screen("to_mobile")
    object ToUpi : Screen("to_upi")
    object Balance : Screen("balance")
    object MyQr : Screen("myqr")
    object Settings : Screen("settings")
    object Faq : Screen("faq")
    object Legal : Screen("legal")
    object Privacy : Screen("privacy")
    object Terms : Screen("terms")
}
