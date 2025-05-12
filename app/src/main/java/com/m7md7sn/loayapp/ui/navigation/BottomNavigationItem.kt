package com.m7md7sn.loayapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.m7md7sn.loayapp.R
import com.m7md7sn.loayapp.ui.app.Screen

data class BottomNavigationItem(
    val label: String = "",
    val icon: ImageVector = Icons.Filled.Home,
    val iconDrawable: Int = 0,
    val route: String = ""
) {

    fun bottomNavigationItems(): List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = "Home",
                icon = Icons.Filled.Home,
                route = Screen.Home.route
            ),
            BottomNavigationItem(
                label = "Monitoring",
                iconDrawable = R.drawable.ic_monitor,
                route = Screen.Monitoring.route
            )
        )
    }
}