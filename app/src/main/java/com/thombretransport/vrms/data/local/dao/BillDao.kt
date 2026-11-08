package com.thombretransport.vrms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thombretransport.vrms.data.local.entity.BillEntity
import com.thombretransport.vrms.data.local.entity.BillItemEntity
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
}
