package com.isis3510.spendiq.services

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

// Service to monitor location and send notifications for nearby offers
class LocationBasedOfferService(private val context: Context) {
    private val TAG = "LocationBasedOfferService"

    // Starts monitoring location-based offers
    fun startMonitoring() {
        Log.d(TAG, "Starting location-based offer monitoring")
        setupPeriodicWork()
    }

    // Sets up periodic work using WorkManager
    private fun setupPeriodicWork() {
        // Define constraints for the work
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a periodic work request with a 15-minute interval
        val workRequest = PeriodicWorkRequestBuilder<LocationNotificationWorker>(
            15, TimeUnit.MINUTES, // Interval between work
            5, TimeUnit.MINUTES  // Flex interval
        )
            .setConstraints(constraints) // Apply constraints
            .addTag("location_notification_work") // Add a tag for reference
            .build()

        // Enqueue the work request as unique work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LocationNotificationWork", // Unique name for this work
            ExistingPeriodicWorkPolicy.UPDATE, // Replace existing work if any
            workRequest
        )

        Log.d(TAG, "Periodic work request set up")
    }

    // Stops monitoring by cancelling all work with the specified tag
    fun stopMonitoring() {
        Log.d(TAG, "Stopping location-based offer monitoring")
        WorkManager.getInstance(context).cancelAllWorkByTag("location_notification_work")
    }
}