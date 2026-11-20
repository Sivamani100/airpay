package com.arkiolabs.airpay.presentation.navigation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arkiolabs.airpay.AirpayApplication
import com.arkiolabs.airpay.domain.SessionState
import com.arkiolabs.airpay.airpayApp
import com.arkiolabs.airpay.platform.ShakeDetector
import com.arkiolabs.airpay.presentation.BalanceViewModel
import com.arkiolabs.airpay.presentation.HistoryViewModel
import com.arkiolabs.airpay.presentation.PayViewModel
import com.arkiolabs.airpay.presentation.ServicesViewModel
import com.arkiolabs.airpay.presentation.permissions.rememberPermissionStatus
import com.arkiolabs.airpay.presentation.screens.AboutScreen
import com.arkiolabs.airpay.presentation.screens.BalanceScreen
import com.arkiolabs.airpay.presentation.screens.ChangeBankScreen
import com.arkiolabs.airpay.presentation.screens.ContactChatScreen
import com.arkiolabs.airpay.presentation.screens.HistoryScreen
import com.arkiolabs.airpay.presentation.screens.HomeScreen
import com.arkiolabs.airpay.presentation.screens.LegalScreen
import com.arkiolabs.airpay.presentation.screens.MyQrScreen
import com.arkiolabs.airpay.presentation.screens.NetworkTransactionsScreen
import com.arkiolabs.airpay.presentation.screens.PayScreen
import com.arkiolabs.airpay.presentation.screens.PendingRequestsScreen
import com.arkiolabs.airpay.presentation.screens.ProfileScreen
import com.arkiolabs.airpay.presentation.screens.RequestMoneyScreen
import com.arkiolabs.airpay.presentation.screens.RewardsScreen
import com.arkiolabs.airpay.presentation.screens.ScanScreen
import com.arkiolabs.airpay.presentation.screens.SettingsScreen
import com.arkiolabs.airpay.presentation.screens.SupportScreen
import com.arkiolabs.airpay.presentation.screens.ToBankScreen
import com.arkiolabs.airpay.presentation.screens.ToMobileScreen
import com.arkiolabs.airpay.presentation.screens.ToUpiScreen
import com.arkiolabs.airpay.presentation.screens.UpiPinManageScreen
import com.arkiolabs.airpay.presentation.ui.components.AirpayBottomNav
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

/**
 * Top-level scaffold for the Airpay-styled Offline UPI architecture.
 * Opens directly into the Home Screen without onboarding wizard or splash delay.
 * Permissions, SIM, and Bank configuration are requested via dedicated bottom sheets.
 */
@Composable
fun AirpayApp() {
    val app = LocalContext.current.airpayApp
    MainScaffold(app = app)
}

@Composable
private fun MainScaffold(app: AirpayApplication) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backstackEntry?.destination?.route ?: Screen.Home.route

    val permissions by rememberPermissionStatus()

    val payViewModel = rememberPayViewModel(app)
    val balanceViewModel = rememberBalanceViewModel(app)
    val historyViewModel = rememberHistoryViewModel(app)
    val servicesViewModel = rememberServicesViewModel(app)

    val payVmSession by payViewModel.sessionState.collectAsState()
    val balanceVmSession by balanceViewModel.sessionState.collectAsState()
    val servicesVmSession by servicesViewModel.sessionState.collectAsState()

    val sessionActive = payVmSession is SessionState.Running ||
        balanceVmSession is SessionState.Running ||
        servicesVmSession is SessionState.Running

    val shakeEnabled by app.prefsRepo.shakeToScanEnabled.collectAsState(initial = false)
    val openScannerOnLaunch by app.prefsRepo.openScannerOnLaunch.collectAsState(initial = false)

    // ── Shake to Scan Sensor Listener ───────────────────────────────
    DisposableEffect(shakeEnabled, sessionActive, currentRoute) {
        if (!shakeEnabled) return@DisposableEffect onDispose {}
        val detector = ShakeDetector(context) {
            if (!sessionActive && currentRoute != Screen.Scan.route) {
                navController.navigate(Screen.Scan.route) {
                    launchSingleTop = true
                }
            }
        }
        detector.start()
        onDispose {
            detector.stop()
        }
    }

    // ── Open Scanner on Launch ──────────────────────────────────────
    var hasLaunchedScannerOnStart by remember { mutableStateOf(false) }
    LaunchedEffect(openScannerOnLaunch) {
        if (openScannerOnLaunch && !hasLaunchedScannerOnStart && currentRoute == Screen.Home.route) {
            hasLaunchedScannerOnStart = true
            navController.navigate(Screen.Scan.route) {
                launchSingleTop = true
            }
        }
    }

    val hideBottomNav = sessionActive ||
        currentRoute == Screen.Scan.route ||
        currentRoute == Screen.Settings.route ||
        currentRoute == Screen.Support.route ||
        currentRoute == Screen.About.route ||
        currentRoute == Screen.Legal.route ||
        currentRoute == Screen.MyQr.route ||
        currentRoute == Screen.Pay.route ||
        currentRoute == Screen.ContactChat.route ||
        currentRoute == Screen.Balance.route ||
        currentRoute == Screen.ToMobile.route ||
        currentRoute == Screen.ToUpi.route ||
        currentRoute == Screen.ToBank.route ||
        currentRoute == Screen.RequestMoney.route ||
        currentRoute == Screen.NetworkTransactions.route ||
        currentRoute == Screen.Rewards.route ||
        currentRoute == Screen.UpiPinManage.route ||
        currentRoute == Screen.ChangeBank.route ||
        currentRoute == Screen.PendingRequests.route

    Column(
        Modifier
            .fillMaxSize()
            .background(AirpayColors.Background)
    ) {
        Box(Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // ── Tab 1: Home Dashboard (Airpay Style) ──────────────
                composable(Screen.Home.route) {
                    HomeScreen(
                        payViewModel = payViewModel,
                        historyViewModel = historyViewModel,
                        permissions = permissions,
                        onNavigatePay = {
                            navController.navigate(Screen.ToUpi.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateContactChat = {
                            navController.navigate(Screen.ContactChat.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToMobile = {
                            navController.navigate(Screen.ToMobile.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToUpi = {
                            navController.navigate(Screen.ToUpi.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToBank = {
                            navController.navigate(Screen.ToBank.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateRequestMoney = {
                            navController.navigate(Screen.RequestMoney.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateNetworkTxns = {
                            navController.navigate(Screen.NetworkTransactions.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateRewards = {
                            navController.navigate(Screen.Rewards.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateUpiPinManage = {
                            navController.navigate(Screen.UpiPinManage.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateChangeBank = {
                            navController.navigate(Screen.ChangeBank.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigatePendingRequests = {
                            navController.navigate(Screen.PendingRequests.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateProfile = {
                            navController.navigate(Screen.Profile.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateScan = {
                            navController.navigate(Screen.Scan.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateBalance = {
                            navController.navigate(Screen.Balance.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateMyQr = {
                            navController.navigate(Screen.MyQr.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateHistory = {
                            navController.navigate(Screen.History.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateFaq = {
                            navController.navigate(Screen.Settings.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateSettings = {
                            navController.navigate(Screen.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ── Tab 2: Center QR Scanner ────────────────────────────
                composable(Screen.Scan.route) {
                    ScanScreen(
                        qrManager = app.qrScannerManager,
                        onResult = { raw ->
                            payViewModel.onQrScanned(raw)
                            navController.navigate(Screen.Pay.route) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        onClose = { navController.popBackStack() },
                        onOpenFaq = {
                            navController.navigate(Screen.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ── Tab 3: Transaction History ──────────────────────────
                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onPay = {
                            navController.navigate(Screen.Pay.route) {
                                launchSingleTop = true
                            }
                        },
                        onPayAgain = { txn ->
                            payViewModel.prefillFromTransaction(
                                vpa = txn.vpa,
                                amount = txn.amount,
                                note = txn.note
                            )
                            val digits = txn.vpa.replace(Regex("[^0-9]"), "")
                            if (digits.length >= 10 && !txn.vpa.contains("@")) {
                                navController.navigate(Screen.ContactChat.route) {
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(Screen.Pay.route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        onClose = { navController.popBackStack() }
                    )
                }

                // ── Send Money / Pay Screen ─────────────────────────────
                composable(Screen.Pay.route) {
                    PayScreen(
                        viewModel = payViewModel,
                        onNavigateScan = {
                            navController.navigate(Screen.Scan.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateHistory = {
                            navController.navigate(Screen.History.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateFaq = {
                            navController.navigate(Screen.Settings.route) {
                                launchSingleTop = true
                            }
                        },
                        permissions = permissions
                    )
                }

                // ── To Mobile Number Screen ─────────────────────────────
                composable(Screen.ToMobile.route) {
                    ToMobileScreen(
                        onContactSelected = { phone, name ->
                            payViewModel.prefillFromTransaction(vpa = phone, amount = "", note = "")
                            navController.navigate(Screen.ContactChat.route) { launchSingleTop = true }
                        },
                        onBack = { navController.popBackStack() },
                        onOpenFaq = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        }
                    )
                }

                // ── Contact Payment Chat Screen (PhonePe Style) ─────────
                composable(Screen.ContactChat.route) {
                    ContactChatScreen(
                        viewModel = payViewModel,
                        historyViewModel = historyViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenFaq = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        },
                        permissions = permissions
                    )
                }

                // ── To Bank / UPI ID Screen ─────────────────────────────
                composable(Screen.ToUpi.route) {
                    ToUpiScreen(
                        historyViewModel = historyViewModel,
                        onUpiValidated = { vpa, payeeName ->
                            payViewModel.prefillFromTransaction(vpa = vpa, amount = "", note = "")
                            navController.navigate(Screen.Pay.route) { launchSingleTop = true }
                        },
                        onBack = { navController.popBackStack() },
                        onOpenFaq = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        }
                    )
                }

                // ── Check Balance Screen ────────────────────────────────
                composable(Screen.Balance.route) {
                    BalanceScreen(
                        viewModel = balanceViewModel,
                        historyViewModel = historyViewModel,
                        onClose = { navController.popBackStack() }
                    )
                }

                // ── My QR Screen ────────────────────────────────────────
                composable(Screen.MyQr.route) {
                    MyQrScreen(
                        onClose = { navController.popBackStack() },
                        onOpenFaq = {
                            navController.navigate(Screen.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ── Settings Screen (Only 6 Items) ──────────────────────
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        prefsRepo = app.prefsRepo,
                        historyViewModel = historyViewModel,
                        onOpenSupport = {
                            navController.navigate(Screen.Support.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenAbout = {
                            navController.navigate(Screen.About.route) {
                                launchSingleTop = true
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // ── Support Us Dedicated Screen ─────────────────────────
                composable(Screen.Support.route) {
                    SupportScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ── About Airpay Dedicated Screen ───────────────────────
                composable(Screen.About.route) {
                    AboutScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ── Legal / Terms ───────────────────────────────────────
                composable(Screen.Legal.route) {
                    LegalScreen(onClose = { navController.popBackStack() })
                }
                composable(Screen.Privacy.route) {
                    LegalScreen(onClose = { navController.popBackStack() })
                }
                composable(Screen.Terms.route) {
                    LegalScreen(onClose = { navController.popBackStack() })
                }

                // ── Send to Bank Account (IFSC) Screen ───────────────────
                composable(Screen.ToBank.route) {
                    ToBankScreen(
                        servicesViewModel = servicesViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenFaq = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
                    )
                }

                // ── Request Money Screen ────────────────────────────────
                composable(Screen.RequestMoney.route) {
                    RequestMoneyScreen(
                        servicesViewModel = servicesViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenFaq = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
                    )
                }

                // ── Network Transactions Screen (*99*6*1#) ───────────────
                composable(Screen.NetworkTransactions.route) {
                    NetworkTransactionsScreen(
                        servicesViewModel = servicesViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenFaq = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
                    )
                }

                // ── Rewards Screen (*99*6*2#) ───────────────────────────
                composable(Screen.Rewards.route) {
                    RewardsScreen(
                        servicesViewModel = servicesViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenFaq = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
                    )
                }

                // ── UPI PIN Settings Screen (*99*7#) ─────────────────────
                composable(Screen.UpiPinManage.route) {
                    UpiPinManageScreen(
                        servicesViewModel = servicesViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenFaq = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
                    )
                }

                // ── Change Linked Bank Screen (*99*4*1#) ─────────────────
                composable(Screen.ChangeBank.route) {
                    ChangeBankScreen(
                        servicesViewModel = servicesViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenFaq = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
                    )
                }

                // ── Pending Requests Screen (*99*5#) ─────────────────────
                composable(Screen.PendingRequests.route) {
                    PendingRequestsScreen(
                        servicesViewModel = servicesViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenFaq = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
                    )
                }

                // ── Profile / My Details Screen (*99*4#) ─────────────────
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        servicesViewModel = servicesViewModel,
                        onNavigateMyQr = { navController.navigate(Screen.MyQr.route) { launchSingleTop = true } },
                        onNavigateSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } },
                        onOpenFaq = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } }
                    )
                }
            }
        }

        // Airpay Bottom Navigation Bar
        if (!hideBottomNav) {
            AirpayBottomNav(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = false }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                }
            )
        }
    }
}

private fun writeToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", text))
}

private fun showSystemToast(context: Context, text: String) {
    Toast.makeText(context.applicationContext, text, Toast.LENGTH_LONG).show()
}

@Composable
private fun rememberPayViewModel(app: AirpayApplication): PayViewModel {
    val context = LocalContext.current
    return remember {
        PayViewModel(
            actionRunner = app.actionRunner,
            historyRepo = app.historyRepo,
            prefsRepo = app.prefsRepo,
            carrierDetector = app.carrierDetector,
            overlayController = app.overlayController,
            onDialerFallback = { code ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$code"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            clipboardWriter = { text -> writeToClipboard(context, text) },
            systemToast = { text -> showSystemToast(context, text) }
        )
    }
}

@Composable
private fun rememberBalanceViewModel(app: AirpayApplication): BalanceViewModel {
    return remember {
        BalanceViewModel(
            actionRunner = app.actionRunner,
            prefsRepo = app.prefsRepo,
            carrierDetector = app.carrierDetector,
            overlayController = app.overlayController
        )
    }
}

@Composable
private fun rememberHistoryViewModel(app: AirpayApplication): HistoryViewModel {
    return remember {
        HistoryViewModel(
            historyRepo = app.historyRepo
        )
    }
}

@Composable
private fun rememberServicesViewModel(app: AirpayApplication): ServicesViewModel {
    val context = LocalContext.current
    return remember {
        ServicesViewModel(
            actionRunner = app.actionRunner,
            prefsRepo = app.prefsRepo,
            historyRepo = app.historyRepo,
            overlayController = app.overlayController,
            context = context
        )
    }
}
