package com.thombretransport.vrms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thombretransport.vrms.data.local.entity.BillEntity
import com.thombretransport.vrms.data.local.entity.BillItemEntity
import com.thombretransport.vrms.data.local.model.ClientRevenueSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Query("SELECT * FROM bills ORDER BY bill_number DESC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    suspend fun getBillById(id: Long): BillEntity?

    @Query("SELECT * FROM bills WHERE bill_number = :billNumber LIMIT 1")
    suspend fun getBillByNumber(billNumber: Long): BillEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBill(bill: BillEntity): Long

    @Query("UPDATE bills SET is_out_of_sync = 1 WHERE id = :billId")
    suspend fun markBillOutOfSync(billId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItem(item: BillItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItems(items: List<BillItemEntity>)

    @Query("SELECT * FROM bill_items WHERE bill_id = :billId ORDER BY sequence_number ASC")
    suspend fun getBillItems(billId: Long): List<BillItemEntity>

    @Query("SELECT COALESCE(SUM(final_payable_amount), 0.0) FROM bills WHERE generated_at >= :startEpochMillis")
    fun getBilledAmountSince(startEpochMillis: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM bills WHERE generated_at >= :startEpochMillis")
    fun getBillCountSince(startEpochMillis: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(final_payable_amount), 0.0) FROM bills WHERE generated_at >= :startEpochMillis AND generated_at < :endEpochMillis")
    fun getBilledAmountBetween(startEpochMillis: Long, endEpochMillis: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(total_discount_amount), 0.0) FROM bills WHERE generated_at >= :startEpochMillis")
    fun getDiscountAmountSince(startEpochMillis: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(total_discount_amount), 0.0) FROM bills")
    fun getTotalDiscountAllTime(): Flow<Double>

    @Query("SELECT COALESCE(SUM(final_payable_amount), 0.0) FROM bills")
    fun getTotalBilledAllTime(): Flow<Double>

    @Query("SELECT client_name AS clientName, COUNT(*) AS billCount, COALESCE(SUM(final_payable_amount), 0.0) AS totalAmount FROM bills GROUP BY client_name ORDER BY totalAmount DESC LIMIT :limit")
    fun getTopClients(limit: Int = 10): Flow<List<ClientRevenueSummary>>
}
