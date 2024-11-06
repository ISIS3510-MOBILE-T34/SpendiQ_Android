package com.isis3510.spendiq.model.repository

import android.util.Log
import com.isis3510.spendiq.services.AnomalyApiService

class AnomalyRepository {
    private val apiService = AnomalyApiService.create()

    suspend fun analyzeTransaction(userId: String, transactionId: String) {
        try {
            apiService.analyzeTransaction(userId, transactionId)
        } catch (e: Exception) {
            Log.e("AnomalyRepository", "Error analyzing transaction", e)
            // We're not handling the error since we just want to make the call
        }
    }
}