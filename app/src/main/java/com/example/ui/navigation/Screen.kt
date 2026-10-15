package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Analyzers : Screen("analyzers")
    data object History : Screen("history")
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")
    data object AddResult : Screen("add_result?recordId={recordId}&gameType={gameType}") {
        fun createRoute(recordId: Long? = null, gameType: String? = null): String {
            val recPart = if (recordId != null) "recordId=$recordId" else "recordId=-1"
            val typePart = if (gameType != null) "gameType=$gameType" else "gameType=RED_GREEN"
            return "add_result?$recPart&$typePart"
        }
    }
}
