package com.thombretransport.vrms.presentation.insights

import com.thombretransport.vrms.data.local.model.ClientRevenueSummary
import com.thombretransport.vrms.data.local.model.RouteFrequency

data class VehicleRevenueItem(
    val vehicleId: Long,
    val registrationNumber: String,
    val vehicleType: String,
    val totalEarned: Double,
    val tripCount: Int,
    val totalKm: Double,
    val percentageOfFleet: Float
)

data class IdleVehicleItem(
    val vehicleId: Long,
    val registrationNumber: String,
    val vehicleType: String,
    val daysIdle: Int,
    val lastTripDate: String?
)

data class UnbilledTruckItem(
    val vehicleId: Long,
    val registrationNumber: String,
    val vehicleType: String,
    val tripCount: Int,
    val totalAmount: Double
)

data class InsightsUiState(
    // Top 3 Highlights ("At a Glance")
    val unbilledTotalAmount: Double = 0.0,
    val unbilledTripCount: Int = 0,
    val thisMonthBilledAmount: Double = 0.0,
    val thisMonthBillCount: Int = 0,
    val idleTrucksCount: Int = 0,

    // Tab 1: Money & Bills
    val lastMonthBilledAmount: Double = 0.0,
    val totalBilledAllTime: Double = 0.0,
    val unbilledTollAmount: Double = 0.0,
    val thisMonthDiscountAmount: Double = 0.0,
    val allTimeDiscountAmount: Double = 0.0,
    val unbilledTrucks: List<UnbilledTruckItem> = emptyList(),

    // Tab 2: Truck Performance
    val truckLeaderboard: List<VehicleRevenueItem> = emptyList(),
    val idleTrucks: List<IdleVehicleItem> = emptyList(),

    // Tab 3: Clients & Routes
    val topClients: List<ClientRevenueSummary> = emptyList(),
    val frequentRoutes: List<RouteFrequency> = emptyList(),
    val totalClientEarnings: Double = 0.0,

    // Month Labels
    val currentMonthName: String = "",
    val lastMonthName: String = ""
)
