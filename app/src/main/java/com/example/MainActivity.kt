package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.preferences.AppThemeMode
import com.example.domain.model.GameType
import com.example.ui.addresult.AddEditResultScreen
import com.example.ui.addresult.AddEditResultViewModel
import com.example.ui.analyzers.AnalyzersScreen
import com.example.ui.analyzers.AnalyzersViewModel
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.navigation.BottomNavItem
import com.example.ui.navigation.Screen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.statistics.StatisticsScreen
import com.example.ui.statistics.StatisticsViewModel
import com.example.ui.theme.ColorPredictorTheme
import com.example.ui.theme.NeonCyan

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ColorPredictorApp
        val container = app.container

        setContent {
            val themeMode by container.userPreferencesRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)
            val hapticEnabled by container.userPreferencesRepository.hapticEnabled
                .collectAsStateWithLifecycle(initialValue = true)

            ColorPredictorTheme(themeMode = themeMode) {
                MainAppScaffold(
                    container = container,
                    hapticEnabled = hapticEnabled
                )
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    container: AppContainer,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if bottom bar should be visible (hidden on Add/Edit screen)
    val showBottomBar = currentRoute?.startsWith("add_result") != true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = NeonCyan,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    BottomNavItem.items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Home
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.provideFactory(container.predictionRepository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToAddResult = { gameType ->
                        navController.navigate(Screen.AddResult.createRoute(gameType = gameType?.name))
                    },
                    onNavigateToAnalyzerModule = { moduleIndex ->
                        navController.navigate(Screen.Analyzers.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    }
                )
            }

            // 2. Analyzers
            composable(Screen.Analyzers.route) {
                val analyzersViewModel: AnalyzersViewModel = viewModel(
                    factory = AnalyzersViewModel.provideFactory(container.predictionRepository)
                )
                AnalyzersScreen(
                    viewModel = analyzersViewModel,
                    onNavigateToAddResult = { gameType ->
                        navController.navigate(Screen.AddResult.createRoute(gameType = gameType.name))
                    }
                )
            }

            // 3. History
            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.provideFactory(container.predictionRepository)
                )
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateToEditRecord = { recordId ->
                        navController.navigate(Screen.AddResult.createRoute(recordId = recordId))
                    },
                    onNavigateToAddResult = {
                        navController.navigate(Screen.AddResult.createRoute())
                    }
                )
            }

            // 4. Statistics
            composable(Screen.Statistics.route) {
                val statsViewModel: StatisticsViewModel = viewModel(
                    factory = StatisticsViewModel.provideFactory(container.predictionRepository)
                )
                StatisticsScreen(viewModel = statsViewModel)
            }

            // 5. Settings
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.provideFactory(
                        container.predictionRepository,
                        container.userPreferencesRepository
                    )
                )
                SettingsScreen(viewModel = settingsViewModel)
            }

            // 6. Add / Edit Result
            composable(
                route = Screen.AddResult.route,
                arguments = listOf(
                    navArgument("recordId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("gameType") {
                        type = NavType.StringType
                        defaultValue = "RED_GREEN"
                    }
                )
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: -1L
                val gameTypeStr = backStackEntry.arguments?.getString("gameType") ?: "RED_GREEN"
                val initialGameType = GameType.fromString(gameTypeStr)

                val addEditViewModel: AddEditResultViewModel = viewModel(
                    factory = AddEditResultViewModel.provideFactory(
                        container.predictionRepository,
                        if (recordId > 0) recordId else null,
                        initialGameType
                    )
                )

                AddEditResultScreen(
                    viewModel = addEditViewModel,
                    hapticEnabled = hapticEnabled,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
