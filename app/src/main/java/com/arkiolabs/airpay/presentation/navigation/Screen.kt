package com.arkiolabs.airpay.presentation.navigation

/**
 * Navigation routes for the Airpay-styled architecture.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scan : Screen("scan")
    object History : Screen("history")

    object Pay : Screen("pay")
    object ToMobile : Screen("to_mobile")
    object ContactChat : Screen("contact_chat")
    object ToUpi : Screen("to_upi")
    object ToBank : Screen("to_bank")
    object RequestMoney : Screen("request_money")
    object NetworkTransactions : Screen("network_transactions")
    object Rewards : Screen("rewards")
    object UpiPinManage : Screen("upi_pin_manage")
    object ChangeBank : Screen("change_bank")
    object PendingRequests : Screen("pending_requests")
    object Profile : Screen("profile")
    object Balance : Screen("balance")
    object MyQr : Screen("myqr")
    object Settings : Screen("settings")
    object Support : Screen("support")
    object About : Screen("about")
    object Faq : Screen("settings")
    object Legal : Screen("legal")
    object Privacy : Screen("privacy")
    object Terms : Screen("terms")
}
