package com.thombretransport.vrms.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object TripLogger : Screen("trip_logger/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "trip_logger/$vehicleId"
    }
    object BillCompilation : Screen("bill_compilation/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "bill_compilation/$vehicleId"
    }
    object BillHistory : Screen("bill_history")
    object Insights : Screen("insights")
    object Settings : Screen("settings")
}
