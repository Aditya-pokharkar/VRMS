package com.thombretransport.vrms.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location_history",
    indices = [Index(value = ["location_name"], unique = true)]
)
data class LocationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "location_name")
    val locationName: String,

    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long = System.currentTimeMillis()
)
