package com.thombretransport.vrms.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "client_history",
    indices = [Index(value = ["client_name"], unique = true)]
)
data class ClientHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "client_name")
    val clientName: String,

    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long = System.currentTimeMillis()
)
