package com.thombretransport.vrms.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thombretransport.vrms.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips WHERE vehicle_id = :vehicleId ORDER BY trip_date DESC, created_at DESC")
    fun getTripsForVehicle(vehicleId: Long): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE vehicle_id = :vehicleId AND billing_status = 'UNBILLED' ORDER BY trip_date DESC")
    fun getUnbilledTripsForVehicle(vehicleId: Long): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id IN (:tripIds) ORDER BY trip_date ASC")
    suspend fun getTripsByIds(tripIds: List<Long>): List<TripEntity>

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: Long): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity): Long

    @Update
    suspend fun update(trip: TripEntity)

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("UPDATE trips SET billing_status = 'BILLED', linked_bill_id = :billId, linked_bill_number = :billNumber WHERE id IN (:tripIds)")
    suspend fun markTripsAsBilled(tripIds: List<Long>, billId: Long, billNumber: Long)
}
