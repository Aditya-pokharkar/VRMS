package com.thombretransport.vrms.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicle_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["vehicle_id"]),
        Index(value = ["trip_date"]),
        Index(value = ["billing_status"]),
        Index(value = ["linked_bill_id"])
    ]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,

    @ColumnInfo(name = "trip_date")
    val tripDate: String, // ISO YYYY-MM-DD

    @ColumnInfo(name = "vehicle_type_at_trip")
    val vehicleTypeAtTrip: String, // "AC", "NON_AC_CAGE", "OTHER"

    @ColumnInfo(name = "billing_status")
    val billingStatus: String = "UNBILLED", // "UNBILLED", "BILLED"

    // AC Vehicle Form Fields
    @ColumnInfo(name = "source_location")
    val sourceLocation: String = "",

    @ColumnInfo(name = "destination_location")
    val destinationLocation: String = "",

    @ColumnInfo(name = "client_invoice_ref")
    val clientInvoiceRef: String = "",

    @ColumnInfo(name = "escort_toll_charges")
    val escortTollCharges: Double = 0.0,

    @ColumnInfo(name = "base_amount")
    val baseAmount: Double = 0.0,

    // Non-AC / Other Vehicle Form Fields
    @ColumnInfo(name = "km_traveled")
    val kmTraveled: Double = 0.0,

    @ColumnInfo(name = "rate_per_km")
    val ratePerKm: Double = 0.0,

    @ColumnInfo(name = "original_amount")
    val originalAmount: Double = 0.0,

    @ColumnInfo(name = "discount_percentage")
    val discountPercentage: Double = 0.0,

    // Common Final Metric
    @ColumnInfo(name = "final_amount")
    val finalAmount: Double = 0.0,

    @ColumnInfo(name = "linked_bill_id")
    val linkedBillId: Long? = null,

    @ColumnInfo(name = "linked_bill_number")
    val linkedBillNumber: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
