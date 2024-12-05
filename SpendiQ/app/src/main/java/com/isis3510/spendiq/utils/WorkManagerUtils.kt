package com.isis3510.spendiq.utils

import android.content.Context
import androidx.work.*
import com.isis3510.spendiq.services.SyncLimitsWorker

fun scheduleSyncWork(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<SyncLimitsWorker>()
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "syncLimitsWork",
        ExistingWorkPolicy.REPLACE,
        syncWorkRequest
    )
}
