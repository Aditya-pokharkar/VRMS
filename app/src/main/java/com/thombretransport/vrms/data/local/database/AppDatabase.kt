package com.thombretransport.vrms.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thombretransport.vrms.data.local.dao.AppConfigDao
import com.thombretransport.vrms.data.local.dao.BillDao
import com.thombretransport.vrms.data.local.dao.HistoryDao
import com.thombretransport.vrms.data.local.dao.TripDao
import com.thombretransport.vrms.data.local.dao.VehicleDao
import com.thombretransport.vrms.data.local.entity.AppConfigEntity
import com.thombretransport.vrms.data.local.entity.BillEntity
import com.thombretransport.vrms.data.local.entity.BillItemEntity
import com.thombretransport.vrms.data.local.entity.ClientHistoryEntity
import com.thombretransport.vrms.data.local.entity.LocationHistoryEntity
import com.thombretransport.vrms.data.local.entity.TripEntity
import com.thombretransport.vrms.data.local.entity.VehicleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VehicleEntity::class,
        TripEntity::class,
        BillEntity::class,
        BillItemEntity::class,
        AppConfigEntity::class,
        ClientHistoryEntity::class,
        LocationHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao
    abstract fun billDao(): BillDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vrms_database.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Initialize default app config with starting bill number 10001 and PAN
                            CoroutineScope(Dispatchers.IO).launch {
                                val configDao = getDatabase(context).appConfigDao()
                                configDao.insertOrUpdate(
                                    AppConfigEntity(
                                        id = 1L,
                                        companyName = "Thombre Transport",
                                        companyAddress = "Baur Gaon, Taluka - Maval, Dist - Pune",
                                        proprietorName = "Mr. Thombre",
                                        panNumber = "AIRPT2980M",
                                        nextBillNumber = 10001L
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
