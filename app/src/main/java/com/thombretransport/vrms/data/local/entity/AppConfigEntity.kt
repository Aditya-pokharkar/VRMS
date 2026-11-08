package com.thombretransport.vrms.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey
    val id: Long = 1L,

    @ColumnInfo(name = "company_name")
    val companyName: String = "Thombre Transport",

    @ColumnInfo(name = "company_address")
    val companyAddress: String = "Baur Gaon, Taluka - Maval, Dist - Pune",

    @ColumnInfo(name = "proprietor_name")
    val proprietorName: String = "Mr. Thombre",

    @ColumnInfo(name = "pan_number")
    val panNumber: String = "AIRPT2980M", // Editable in Settings

    @ColumnInfo(name = "next_bill_number")
    val nextBillNumber: Long = 10001L, // Starts at 10001

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
