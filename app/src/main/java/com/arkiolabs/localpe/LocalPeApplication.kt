package com.arkiolabs.localpe

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.arkiolabs.localpe.data.AppDatabase
import com.arkiolabs.localpe.data.HistoryRepository
import com.arkiolabs.localpe.data.PreferencesRepository
import com.arkiolabs.localpe.domain.ActionRunner
import com.arkiolabs.localpe.platform.CarrierDetector
import com.arkiolabs.localpe.platform.OverlayControllerImpl
import com.arkiolabs.localpe.platform.QrScannerManager
import com.arkiolabs.localpe.platform.UssdEngine

/**
 * Top-level DataStore delegate (must be at file level per DataStore docs).
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Application class wiring all singleton dependencies via manual DI.
 *
 * Validates: Requirements 14.1, 14.2, 15.1
 */
class LocalPeApplication : Application() {

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

    lateinit var jioVoiceGateway: com.arkiolabs.localpe.platform.JioVoiceGateway
        private set

    // ─── Domain Layer ──────────────────────────────────────────────────────────

    lateinit var actionRunner: ActionRunner
        private set

    override fun onCreate() {
        super.onCreate()

        // Data layer
        val passphrase = "localpe_secure_db".toByteArray() // In production, derive from secure source
        database = AppDatabase.create(this, passphrase)
        historyRepo = HistoryRepository(database.transactionDao())
        prefsRepo = PreferencesRepository(dataStore)

        // Platform layer
        overlayController = OverlayControllerImpl(this)
        ussdEngine = UssdEngine(this, overlayController)
        carrierDetector = CarrierDetector(this)
        qrScannerManager = QrScannerManager()
        jioVoiceGateway = com.arkiolabs.localpe.platform.JioVoiceGateway(this)
        com.arkiolabs.localpe.platform.BankSmsReceiver.init(this)

        // Domain layer
        actionRunner = ActionRunner(ussdEngine)
    }
}

/**
 * Extension property for convenient access to [LocalPeApplication] from any Context.
 */
val Context.localPeApp: LocalPeApplication
    get() = applicationContext as LocalPeApplication
