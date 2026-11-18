package com.arkiolabs.localpe.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class NeoPopNavItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun NeoPopBottomNav(
    items: List<NeoPopNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LocalPeBottomNav(
        currentRoute = selectedKey,
        onNavigate = onSelect,
        modifier = modifier
    )
}
