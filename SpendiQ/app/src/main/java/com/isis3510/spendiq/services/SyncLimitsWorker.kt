package com.isis3510.spendiq.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.local.database.DatabaseProvider
import com.isis3510.spendiq.model.local.database.LimitsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncLimitsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val limitsDao = DatabaseProvider.getDatabase(applicationContext).limitsDao()


    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()

        val limits = withContext(Dispatchers.IO) {
            limitsDao.getLimits(userId)
        } ?: return Result.success()

        return try {
            db.collection("Limits").document(userId).set(limits).await()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
