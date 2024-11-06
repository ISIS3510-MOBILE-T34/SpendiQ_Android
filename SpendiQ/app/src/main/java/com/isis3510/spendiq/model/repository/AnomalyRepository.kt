package com.isis3510.spendiq.model.repository

import android.util.Log
import com.isis3510.spendiq.services.AnomalyApiService

/**
 * Repository class for managing anomaly detection requests related to transactions.
 * This class provides a method to analyze transactions for anomalies by interacting
 * with the Anomaly API service.
 */
class AnomalyRepository {
    // Instance of the AnomalyApiService to make network calls
    private val apiService = AnomalyApiService.create()

    /**
     * Analyzes a transaction for anomalies using the provided user ID and transaction ID.
     *
     * @param userId The ID of the user associated with the transaction.
     * @param transactionId The ID of the transaction to analyze.
     */
    suspend fun analyzeTransaction(userId: String, transactionId: String) {
        try {
            // Make a network call to analyze the specified transaction
            apiService.analyzeTransaction(userId, transactionId)
        } catch (e: Exception) {
            // Log any exceptions encountered during the network call
            Log.e("AnomalyRepository", "Error analyzing transaction", e)
            // Errors are logged but not propagated as we don't need to handle them here
        }
    }
}
