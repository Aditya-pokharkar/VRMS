package com.thombretransport.vrms.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicle_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["bill_number"], unique = true),
        Index(value = ["vehicle_id"]),
        Index(value = ["bill_date"])
    ]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "bill_number")
    val billNumber: Long, // Monotonic sequential integer starting at 10001

    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,

    @ColumnInfo(name = "vehicle_reg_snapshot")
    val vehicleRegSnapshot: String,

    @ColumnInfo(name = "client_name")
    val clientName: String,

    @ColumnInfo(name = "bill_date")
    val billDate: String, // DD/MM/YYYY

    @ColumnInfo(name = "billing_period_text")
    val billingPeriodText: String,

    @ColumnInfo(name = "vehicle_type")
    val vehicleType: String,

    @ColumnInfo(name = "total_primary_metric")
    val totalPrimaryMetric: Double, // Total Toll (AC) or Total KM (Non-AC)

    @ColumnInfo(name = "total_gross_amount")
    val totalGrossAmount: Double,

    @ColumnInfo(name = "total_discount_amount")
    val totalDiscountAmount: Double,

    @ColumnInfo(name = "final_payable_amount")
    val finalPayableAmount: Double,

    @ColumnInfo(name = "amount_in_words")
    val amountInWords: String,

    @ColumnInfo(name = "pdf_relative_path")
    val pdfRelativePath: String,

    @ColumnInfo(name = "is_out_of_sync")
    val isOutOfSync: Boolean = false, // Set true if linked trips are edited/deleted

    @ColumnInfo(name = "generated_at")
    val generatedAt: Long = System.currentTimeMillis()
)
