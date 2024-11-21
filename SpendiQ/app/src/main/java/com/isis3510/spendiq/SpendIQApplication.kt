package com.isis3510.spendiq

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

class SpendIQApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        )
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
    }
}