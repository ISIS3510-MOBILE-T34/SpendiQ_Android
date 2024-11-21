package com.isis3510.spendiq.services

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

class LocationBasedOfferService(private val context: Context) {
    private val TAG = "LocationBasedOfferService"

    fun startMonitoring() {
        Log.d(TAG, "Starting location-based offer monitoring")
        scheduleLocationWork()
    }

    private fun scheduleLocationWork() {
        // Create work constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .build()

        // Create work request
        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationNotificationWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("location_notification_work")
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        // Schedule the work
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "LocationNotificationWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                locationWorkRequest
            )

        // Observe work status
        WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData("location_notification_work")
            .observeForever { workInfoList ->
                workInfoList?.forEach { workInfo ->
                    Log.d(TAG, "Work status: ${workInfo.state}")
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            Log.d(TAG, "Work completed successfully")
                        }
                        WorkInfo.State.FAILED -> {
                            Log.e(TAG, "Work failed: ${workInfo.outputData.getString("error_message")}")
                        }
                        else -> {
                            Log.d(TAG, "Work state: ${workInfo.state}")
                        }
                    }
                }
            }
    }

    fun stopMonitoring() {
        Log.d(TAG, "Stopping location-based offer monitoring")
        WorkManager.getInstance(context).cancelUniqueWork("LocationNotificationWork")
    }
}