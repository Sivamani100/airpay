package com.arkiolabs.downpay

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.arkiolabs.downpay.data.AppDatabase
import com.arkiolabs.downpay.data.HistoryRepository
import com.arkiolabs.downpay.data.PreferencesRepository
import com.arkiolabs.downpay.domain.ActionRunner
import com.arkiolabs.downpay.platform.CarrierDetector
import com.arkiolabs.downpay.platform.OverlayControllerImpl
import com.arkiolabs.downpay.platform.QrScannerManager
import com.arkiolabs.downpay.platform.UssdEngine

/**
 * Top-level DataStore delegate (must be at file level per DataStore docs).
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Application class wiring all singleton dependencies via manual DI.
 *
 * Validates: Requirements 14.1, 14.2, 15.1
 */
class DownpayApplication : Application() {

    // ─── Data Layer ────────────────────────────────────────────────────────────

    lateinit var database: AppDatabase
        private set

    lateinit var historyRepo: HistoryRepository
        private set

    lateinit var prefsRepo: PreferencesRepository
        private set

    // ─── Platform Layer ────────────────────────────────────────────────────────

    lateinit var overlayController: OverlayControllerImpl
        private set

    lateinit var ussdEngine: UssdEngine
        private set

    lateinit var carrierDetector: CarrierDetector
        private set

    lateinit var qrScannerManager: QrScannerManager
        private set

    lateinit var jioVoiceGateway: com.arkiolabs.downpay.platform.JioVoiceGateway
        private set

    // ─── Domain Layer ──────────────────────────────────────────────────────────

    lateinit var actionRunner: ActionRunner
        private set

    override fun onCreate() {
        super.onCreate()

        // Data layer
        val passphrase = "downpay_secure_db".toByteArray() // In production, derive from secure source
        database = AppDatabase.create(this, passphrase)
        historyRepo = HistoryRepository(database.transactionDao())
        prefsRepo = PreferencesRepository(dataStore)

        // Platform layer
        overlayController = OverlayControllerImpl(this)
        ussdEngine = UssdEngine(this, overlayController)
        carrierDetector = CarrierDetector(this)
        qrScannerManager = QrScannerManager()
        jioVoiceGateway = com.arkiolabs.downpay.platform.JioVoiceGateway(this)
        com.arkiolabs.downpay.platform.BankSmsReceiver.init(this)

        // Domain layer
        actionRunner = ActionRunner(ussdEngine)
    }
}

/**
 * Extension property for convenient access to [DownpayApplication] from any Context.
 */
val Context.downpayApp: DownpayApplication
    get() = applicationContext as DownpayApplication
