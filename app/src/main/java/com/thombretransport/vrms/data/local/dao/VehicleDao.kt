package com.thombretransport.vrms.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thombretransport.vrms.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicles WHERE status = 'ACTIVE' ORDER BY created_at DESC")
    fun getActiveVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY created_at DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleById(id: Long): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE registration_number = :regNumber AND status = 'ACTIVE' LIMIT 1")
    suspend fun findActiveByRegistration(regNumber: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Delete
    suspend fun delete(vehicle: VehicleEntity)

    @Query("SELECT COUNT(*) FROM trips WHERE vehicle_id = :vehicleId")
    suspend fun countTripsForVehicle(vehicleId: Long): Int

    @Query("SELECT COUNT(*) FROM bills WHERE vehicle_id = :vehicleId")
    suspend fun countBillsForVehicle(vehicleId: Long): Int
}
