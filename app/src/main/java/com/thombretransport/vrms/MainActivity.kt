package com.thombretransport.vrms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thombretransport.vrms.presentation.MainViewModel
import com.thombretransport.vrms.presentation.billhistory.BillHistoryScreen
import com.thombretransport.vrms.presentation.billselector.BillCompilationScreen
import com.thombretransport.vrms.presentation.dashboard.FleetDashboardScreen
import com.thombretransport.vrms.presentation.insights.InsightsScreen
import com.thombretransport.vrms.presentation.navigation.Screen
import com.thombretransport.vrms.presentation.settings.SettingsScreen
import com.thombretransport.vrms.presentation.theme.VRMSTheme
import com.thombretransport.vrms.presentation.triplogger.TripLoggerScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VRMSTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route
                ) {
                    // 1. Dashboard Screen
                    composable(Screen.Dashboard.route) {
                        FleetDashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTripLogger = { vehicleId ->
                                navController.navigate(Screen.TripLogger.createRoute(vehicleId))
                            },
                            onNavigateToBillCompilation = { vehicleId ->
                                navController.navigate(Screen.BillCompilation.createRoute(vehicleId))
                            },
                            onNavigateToBillHistory = {
                                navController.navigate(Screen.BillHistory.route)
                            },
                            onNavigateToInsights = {
                                navController.navigate(Screen.Insights.route)
                            },
                            onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            }
                        )
                    }

                    // 2. Trip Logger Screen
                    composable(
                        route = Screen.TripLogger.route,
                        arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: 0L
                        TripLoggerScreen(
                            vehicleId = vehicleId,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 3. Bill Compilation Screen
                    composable(
                        route = Screen.BillCompilation.route,
                        arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: 0L
                        BillCompilationScreen(
                            vehicleId = vehicleId,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onBillGenerated = {
                                navController.navigate(Screen.BillHistory.route) {
                                    popUpTo(Screen.Dashboard.route)
                                }
                            }
                        )
                    }

                    // 4. Bill History Screen
                    composable(Screen.BillHistory.route) {
                        BillHistoryScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 5. Insights & Analytics Screen
                    composable(Screen.Insights.route) {
                        InsightsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToBillCompilation = { vehicleId ->
                                navController.navigate(Screen.BillCompilation.createRoute(vehicleId))
                            }
                        )
                    }

                    // 6. Settings Screen
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
