package com.thombretransport.vrms.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["bill_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bill_id"]),
        Index(value = ["original_trip_id"])
    ]
)
data class BillItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "bill_id")
    val billId: Long,

    @ColumnInfo(name = "original_trip_id")
    val originalTripId: Long,

    @ColumnInfo(name = "sequence_number")
    val sequenceNumber: Int, // 1-based index on PDF table

    @ColumnInfo(name = "frozen_trip_date")
    val frozenTripDate: String,

    @ColumnInfo(name = "frozen_details_json")
    val frozenDetailsJson: String, // Frozen representation of row details

    @ColumnInfo(name = "frozen_gross_amount")
    val frozenGrossAmount: Double,

    @ColumnInfo(name = "frozen_deduction_or_discount")
    val frozenDeductionOrDiscount: Double,

    @ColumnInfo(name = "frozen_net_amount")
    val frozenNetAmount: Double
)
