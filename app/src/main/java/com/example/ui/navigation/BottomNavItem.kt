package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    data object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, "nav_home")
    data object Analyzers : BottomNavItem(Screen.Analyzers.route, "Analyzers", Icons.Filled.QueryStats, "nav_analyzers")
    data object History : BottomNavItem(Screen.History.route, "History", Icons.Filled.History, "nav_history")
    data object Statistics : BottomNavItem(Screen.Statistics.route, "Stats", Icons.Filled.Analytics, "nav_statistics")
    data object Settings : BottomNavItem(Screen.Settings.route, "Settings", Icons.Filled.Settings, "nav_settings")

    companion object {
        val items = listOf(Home, Analyzers, History, Statistics, Settings)
    }
}
