package com.arkiolabs.downpay.presentation.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkiolabs.downpay.R
import com.arkiolabs.downpay.presentation.navigation.Screen
import com.arkiolabs.downpay.presentation.ui.theme.DownpayColors

/**
 * Authentic Downpay Bottom Navigation Bar:
 *  - 5 tabs: Home, Search, [Center QR Scanner], Alerts, History.
 *  - Center QR button is an authentic vibrant purple circle, vertically centered and NEVER cut off.
 *  - Clean white surface with subtle top hairline divider matching Downpay screenshots.
 *  - Active tab is solid black (Downpay style), inactive is crisp gray.
 */
@Composable
fun DownpayBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Surface(
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color(0xFFEEEEEE),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home
            NavTabItem(
                label = "Home",
                isActive = currentRoute == Screen.Home.route,
                activeResId = R.drawable.bottom_nav_home_selected,
                inactiveResId = R.drawable.bottom_nav_home_unselected,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onNavigate(Screen.Home.route)
                },
                modifier = Modifier.weight(1f)
            )

            // Tab 2: Search
            NavTabItem(
                label = "Search",
                isActive = currentRoute == Screen.Search.route,
                activeResId = R.drawable.bottom_nav_search_selected,
                inactiveResId = R.drawable.bottom_nav_search_unselected,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onNavigate(Screen.Search.route)
                },
                modifier = Modifier.weight(1f)
            )

            // Tab 3: Center QR Scanner Button (Authentic Downpay Purple Circle - Perfectly Centered, Never Cut Off)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(elevation = 4.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(DownpayColors.PurpleGradient)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onNavigate(Screen.Scan.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_scan),
                        contentDescription = "Scan any QR",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Tab 4: Alerts
            NavTabItem(
                label = "Alerts",
                isActive = currentRoute == Screen.Alerts.route,
                activeResId = R.drawable.bottom_nav_alerts_selected,
                inactiveResId = R.drawable.bottom_nav_alerts_unselected,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onNavigate(Screen.Alerts.route)
                },
                modifier = Modifier.weight(1f)
            )

            // Tab 5: History
            NavTabItem(
                label = "History",
                isActive = currentRoute == Screen.History.route,
                activeResId = R.drawable.bottom_nav_history_selected,
                inactiveResId = R.drawable.bottom_nav_history_unselected,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onNavigate(Screen.History.route)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    isActive: Boolean,
    activeResId: Int,
    inactiveResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint by animateColorAsState(
        targetValue = if (isActive) Color(0xFF1C1C1E) else Color(0xFF757575),
        label = "nav_tint_$label"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = if (isActive) activeResId else inactiveResId),
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = tint
        )
    }
}
