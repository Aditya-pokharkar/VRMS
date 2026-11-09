package com.thombretransport.vrms.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thombretransport.vrms.data.local.entity.TripEntity
import com.thombretransport.vrms.data.local.model.RouteFrequency
import com.thombretransport.vrms.data.local.model.UnbilledVehicleSummary
import com.thombretransport.vrms.data.local.model.VehicleLastTrip
import com.thombretransport.vrms.data.local.model.VehicleTripStats
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

    @Query("SELECT COALESCE(SUM(final_amount), 0.0) FROM trips WHERE billing_status = 'UNBILLED'")
    fun getUnbilledTotalAmount(): Flow<Double>

    @Query("SELECT COUNT(*) FROM trips WHERE billing_status = 'UNBILLED'")
    fun getUnbilledTripCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(escort_toll_charges), 0.0) FROM trips WHERE billing_status = 'UNBILLED'")
    fun getUnbilledTollTotal(): Flow<Double>

    @Query("SELECT vehicle_id AS vehicleId, COUNT(*) AS tripCount, COALESCE(SUM(final_amount), 0.0) AS totalAmount FROM trips WHERE billing_status = 'UNBILLED' GROUP BY vehicle_id ORDER BY totalAmount DESC")
    fun getUnbilledVehicleSummaries(): Flow<List<UnbilledVehicleSummary>>

    @Query("SELECT source_location AS sourceLocation, destination_location AS destinationLocation, COUNT(*) AS tripCount FROM trips WHERE source_location != '' OR destination_location != '' GROUP BY source_location, destination_location ORDER BY tripCount DESC LIMIT :limit")
    fun getFrequentRoutes(limit: Int = 10): Flow<List<RouteFrequency>>

    @Query("SELECT vehicle_id AS vehicleId, COUNT(*) AS tripCount, COALESCE(SUM(final_amount), 0.0) AS totalEarned, COALESCE(SUM(km_traveled), 0.0) AS totalKm FROM trips GROUP BY vehicle_id")
    fun getAllVehicleTripStats(): Flow<List<VehicleTripStats>>

    @Query("SELECT vehicle_id AS vehicleId, MAX(trip_date) AS lastTripDate, MAX(created_at) AS lastTripCreatedAt FROM trips GROUP BY vehicle_id")
    fun getVehicleLastTrips(): Flow<List<VehicleLastTrip>>
}
