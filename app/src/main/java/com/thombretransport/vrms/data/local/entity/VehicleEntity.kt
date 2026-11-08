package com.thombretransport.vrms.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    indices = [
        Index(value = ["registration_number"]),
        Index(value = ["status"])
    ]
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "registration_number")
    val registrationNumber: String, // Normalized uppercase without spaces/hyphens

    @ColumnInfo(name = "vehicle_type")
    val vehicleType: String, // "AC", "NON_AC_CAGE", "OTHER" (Immutable once created)

    @ColumnInfo(name = "fuel_type")
    val fuelType: String = "Diesel", // "Petrol", "Diesel", "EV"

    @ColumnInfo(name = "other_details")
    val otherDetails: String = "",

    @ColumnInfo(name = "status")
    val status: String = "ACTIVE", // "ACTIVE", "DEACTIVATED"

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
