package com.arkiolabs.localpe.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.arkiolabs.localpe.presentation.navigation.LocalPeApp
import com.arkiolabs.localpe.presentation.ui.theme.LocalPeTheme
import com.arkiolabs.localpe.presentation.ui.theme.LocalPeColors

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(com.arkiolabs.localpe.R.style.Theme_LocalPe)
        enableEdgeToEdge()
        setContent {
            LocalPeTheme {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(LocalPeColors.Background)
                ) {
                    LocalPeApp()
                }
            }
        }
    }
}
