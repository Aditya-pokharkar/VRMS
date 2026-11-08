package com.thombretransport.vrms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thombretransport.vrms.data.local.entity.AppConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AppConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: AppConfigEntity)

    @Query("SELECT next_bill_number FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getNextBillNumber(): Long?

    @Query("UPDATE app_config SET next_bill_number = next_bill_number + 1 WHERE id = 1")
    suspend fun incrementBillNumber()

    @Query("UPDATE app_config SET pan_number = :pan, updated_at = :timestamp WHERE id = 1")
    suspend fun updatePanNumber(pan: String, timestamp: Long = System.currentTimeMillis())
}
