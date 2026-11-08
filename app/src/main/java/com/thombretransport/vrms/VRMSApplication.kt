package com.thombretransport.vrms

import android.app.Application
import com.thombretransport.vrms.data.local.database.AppDatabase

class VRMSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-warm database instance on application start
        AppDatabase.getDatabase(this)
    }
}
