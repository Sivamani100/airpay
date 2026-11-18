package com.arkiolabs.airpay.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.airpay.presentation.navigation.AirpayApp
import com.arkiolabs.airpay.presentation.ui.theme.AirpayTheme
import com.arkiolabs.airpay.presentation.ui.theme.AirpayColors

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(com.arkiolabs.airpay.R.style.Theme_Airpay)
        enableEdgeToEdge()
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
}
