package com.thombretransport.vrms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thombretransport.vrms.data.local.entity.ClientHistoryEntity
import com.thombretransport.vrms.data.local.entity.LocationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT client_name FROM client_history ORDER BY last_used_at DESC")
    fun getClientSuggestions(): Flow<List<String>>

    @Query("SELECT location_name FROM location_history ORDER BY last_used_at DESC")
    fun getLocationSuggestions(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClient(client: ClientHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocation(location: LocationHistoryEntity)
}
