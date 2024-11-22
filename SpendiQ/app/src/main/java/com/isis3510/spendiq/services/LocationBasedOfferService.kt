package com.isis3510.spendiq.services

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

class LocationBasedOfferService(private val context: Context) {
    private val TAG = "LocationBasedOfferService"

    fun startMonitoring() {
        Log.d(TAG, "Starting location-based offer monitoring")
        setupPeriodicWork()
    }

    private fun setupPeriodicWork() {
        // Define work constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create periodic work request that runs every 15 minutes
        val workRequest = PeriodicWorkRequestBuilder<LocationNotificationWorker>(
            15, TimeUnit.MINUTES, // Minimum interval allowed by WorkManager
            5, TimeUnit.MINUTES  // Flex interval
        )
            .setConstraints(constraints)
            .addTag("location_notification_work")
            .build()

        // Enqueue the work request
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LocationNotificationWork",
            ExistingPeriodicWorkPolicy.UPDATE, // Replace existing if any
            workRequest
        )

        Log.d(TAG, "Periodic work request set up")
    }

    fun stopMonitoring() {
        Log.d(TAG, "Stopping location-based offer monitoring")
        WorkManager.getInstance(context).cancelAllWorkByTag("location_notification_work")
    }
}