package com.arkiolabs.localpe.presentation.navigation

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arkiolabs.localpe.LocalPeApplication
import com.arkiolabs.localpe.domain.SessionState
import com.arkiolabs.localpe.localPeApp
import com.arkiolabs.localpe.presentation.BalanceViewModel
import com.arkiolabs.localpe.presentation.HistoryViewModel
import com.arkiolabs.localpe.presentation.PayViewModel
import com.arkiolabs.localpe.presentation.permissions.rememberPermissionStatus
import com.arkiolabs.localpe.presentation.screens.AlertsScreen
import com.arkiolabs.localpe.presentation.screens.BalanceScreen
import com.arkiolabs.localpe.presentation.screens.FaqScreen
import com.arkiolabs.localpe.presentation.screens.HistoryScreen
import com.arkiolabs.localpe.presentation.screens.HomeScreen
import com.arkiolabs.localpe.presentation.screens.LegalScreen
import com.arkiolabs.localpe.presentation.screens.MyQrScreen
import com.arkiolabs.localpe.presentation.screens.PayScreen
import com.arkiolabs.localpe.presentation.screens.ScanScreen
import com.arkiolabs.localpe.presentation.screens.SearchScreen
import com.arkiolabs.localpe.presentation.screens.SettingsScreen
import com.arkiolabs.localpe.presentation.screens.ToMobileScreen
import com.arkiolabs.localpe.presentation.screens.ToUpiScreen
import com.arkiolabs.localpe.presentation.screens.onboarding.OnboardingFlow
import com.arkiolabs.localpe.presentation.ui.components.LocalPeBottomNav
import com.arkiolabs.localpe.presentation.ui.theme.LocalPeColors
import kotlinx.coroutines.launch

/**
 * Top-level scaffold for the LocalPe-styled Offline UPI architecture.
 *
 *  - First-launch onboarding flow gates the app until complete.
 *  - Persistent LocalPe Bottom Navigation Bar with 5 destinations:
 *      Home, Search, Center Floating QR, Alerts, History.
 *  - Sub-destinations: Pay (Send Money), Balance, MyQr, Settings, Faq, Legal.
 */
@Composable
fun LocalPeApp() {
    val app = LocalContext.current.localPeApp
    val firstLaunchDone by app.prefsRepo.firstLaunchComplete.collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    if (!firstLaunchDone) {
        OnboardingFlow(onComplete = {
            scope.launch { app.prefsRepo.setFirstLaunchComplete(true) }
        })
    } else {
        MainScaffold(
            app = app,
            onReplayOnboarding = {
                scope.launch { app.prefsRepo.setFirstLaunchComplete(false) }
            }
        )
    }
}

@Composable
private fun MainScaffold(app: LocalPeApplication, onReplayOnboarding: () -> Unit) {
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backstackEntry?.destination?.route ?: Screen.Home.route

    val permissions by rememberPermissionStatus()

    val payViewModel = rememberPayViewModel(app)
    val balanceViewModel = rememberBalanceViewModel(app)
    val historyViewModel = rememberHistoryViewModel(app)

    val payVmSession by payViewModel.sessionState.collectAsState()
    val balanceVmSession by balanceViewModel.sessionState.collectAsState()

    val sessionActive = payVmSession is SessionState.Running ||
        balanceVmSession is SessionState.Running
    val hideBottomNav = sessionActive ||
        currentRoute == Screen.Scan.route ||
        currentRoute == Screen.Faq.route ||
        currentRoute == Screen.Legal.route ||
        currentRoute == Screen.MyQr.route ||
        currentRoute == Screen.Pay.route ||
        currentRoute == Screen.Balance.route ||
        currentRoute == Screen.ToMobile.route ||
        currentRoute == Screen.ToUpi.route

    Column(
        Modifier
            .fillMaxSize()
            .background(LocalPeColors.Background)
    ) {
        Box(Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // ── Tab 1: Home Dashboard (LocalPe Style) ──────────────
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
                            navController.navigate(Screen.Faq.route) {
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

                // ── Tab 2: Search Screen ────────────────────────────────
                composable(Screen.Search.route) {
                    SearchScreen(
                        historyViewModel = historyViewModel,
                        onNavigatePay = { vpa ->
                            if (vpa.isNotBlank()) {
                                payViewModel.prefillFromTransaction(vpa = vpa, amount = "", note = "")
                            }
                            navController.navigate(Screen.Pay.route) {
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
                        onOpenFaq = {
                            navController.navigate(Screen.Faq.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ── Tab 3: Center QR Scanner ────────────────────────────
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
                            navController.navigate(Screen.Faq.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ── Tab 4: Alerts Screen ────────────────────────────────
                composable(Screen.Alerts.route) {
                    AlertsScreen(
                        onOpenFaq = {
                            navController.navigate(Screen.Faq.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenSettings = {
                            navController.navigate(Screen.Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ── Tab 5: Transaction History ──────────────────────────
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
                            navController.navigate(Screen.Pay.route) {
                                launchSingleTop = true
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
                            navController.navigate(Screen.Faq.route) {
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
                            navController.navigate(Screen.Pay.route) { launchSingleTop = true }
                        },
                        onBack = { navController.popBackStack() },
                        onOpenFaq = {
                            navController.navigate(Screen.Faq.route) { launchSingleTop = true }
                        }
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
                            navController.navigate(Screen.Faq.route) { launchSingleTop = true }
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
                            navController.navigate(Screen.Faq.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ── Settings Screen ─────────────────────────────────────
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        prefsRepo = app.prefsRepo,
                        historyViewModel = historyViewModel,
                        permissions = permissions,
                        versionName = "1.0.1",
                        onClearAllData = { /* historyViewModel.clearHistory handled */ },
                        onOpenFaq = {
                            navController.navigate(Screen.Faq.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenHistory = {
                            navController.navigate(Screen.History.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenLegal = {
                            navController.navigate(Screen.Legal.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ── FAQ / Help Center ───────────────────────────────────
                composable(Screen.Faq.route) {
                    FaqScreen(
                        onClose = { navController.popBackStack() },
                        onReplayOnboarding = onReplayOnboarding
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
            }
        }

        // LocalPe Bottom Navigation Bar
        if (!hideBottomNav) {
            LocalPeBottomNav(
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
private fun rememberPayViewModel(app: LocalPeApplication): PayViewModel {
    val context = LocalContext.current
    return remember {
        PayViewModel(
            actionRunner = app.actionRunner,
            historyRepo = app.historyRepo,
            prefsRepo = app.prefsRepo,
            carrierDetector = app.carrierDetector,
            jioVoiceGateway = app.jioVoiceGateway,
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
private fun rememberBalanceViewModel(app: LocalPeApplication): BalanceViewModel {
    val context = LocalContext.current
    return remember {
        BalanceViewModel(
            actionRunner = app.actionRunner,
            prefsRepo = app.prefsRepo,
            carrierDetector = app.carrierDetector,
            jioVoiceGateway = app.jioVoiceGateway,
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
private fun rememberHistoryViewModel(app: LocalPeApplication): HistoryViewModel {
    return remember { HistoryViewModel(app.historyRepo) }
}
