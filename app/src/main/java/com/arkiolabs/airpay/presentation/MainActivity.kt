package com.arkiolabs.airpay.presentation

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.AirpayApplication
import com.arkiolabs.airpay.presentation.navigation.AirpayApp
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors
import com.arkiolabs.airpay.presentation.ui.theme.AirpayTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.arkiolabs.airpay.R.style.Theme_Airpay)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Block hardware Back button and back gestures while offline USSD codes are executing
        onBackPressedDispatcher.addCallback(this) {
            val app = application as? AirpayApplication
            val isUssdActive = app?.ussdEngine?.isSessionActive() == true
            if (isUssdActive) {
                // Ignore back press completely so the active session is never aborted
                return@addCallback
            }
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
            isEnabled = true
        }

        setContent {
            AirpayTheme {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(AirpayColors.Background)
                ) {
                    AirpayApp()
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val app = application as? AirpayApplication
        if (app?.ussdEngine?.isSessionActive() == true) {
            // Block all touches and drags inside the app window while USSD code is running
            return true
        }
        return super.dispatchTouchEvent(ev)
    }
}
