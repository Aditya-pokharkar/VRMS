package com.thombretransport.vrms.data.local.model

data class UnbilledVehicleSummary(
    val vehicleId: Long,
    val tripCount: Int,
    val totalAmount: Double
)

data class RouteFrequency(
    val sourceLocation: String,
    val destinationLocation: String,
    val tripCount: Int
)

data class VehicleTripStats(
    val vehicleId: Long,
    val tripCount: Int,
    val totalEarned: Double,
    val totalKm: Double
)

data class VehicleLastTrip(
    val vehicleId: Long,
    val lastTripDate: String?,
    val lastTripCreatedAt: Long?
)

data class ClientRevenueSummary(
    val clientName: String,
    val billCount: Int,
    val totalAmount: Double
)
