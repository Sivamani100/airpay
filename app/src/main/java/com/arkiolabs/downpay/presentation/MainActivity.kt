package com.arkiolabs.downpay.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.downpay.presentation.navigation.DownpayApp
import com.arkiolabs.downpay.presentation.ui.theme.DownpayTheme
import com.arkiolabs.downpay.presentation.ui.theme.DownpayColors

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(com.arkiolabs.downpay.R.style.Theme_Downpay)
        enableEdgeToEdge()
        setContent {
            DownpayTheme {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(DownpayColors.Background)
                ) {
                    DownpayApp()
                }
            }
        }
    }
}
