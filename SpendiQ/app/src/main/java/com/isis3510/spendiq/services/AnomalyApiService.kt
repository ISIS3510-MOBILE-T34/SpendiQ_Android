package com.isis3510.spendiq.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Path
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// Interface defining the API service for analyzing transactions
interface AnomalyApiService {
    /**
     * Sends a request to analyze a completed transaction for anomalies.
     *
     * @param userId The ID of the user for whom the transaction is being analyzed.
     * @param transactionId The ID of the transaction that is being analyzed.
     */
    @POST("api/analyze-transaction-complete/{user_id}/{transaction_id}")
    suspend fun analyzeTransaction(
        @Path("user_id") userId: String,
        @Path("transaction_id") transactionId: String
    )

    companion object {
        // Base URL of the API
        private const val BASE_URL = "http://148.113.204.223:8000/"

        /**
         * Creates an instance of the AnomalyApiService with the necessary configurations.
         *
         * @return An instance of AnomalyApiService for making API calls.
         */
        fun create(): AnomalyApiService {
            // Build the OkHttpClient with custom timeout settings
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
                .build()

            // Create a Retrofit instance with the specified base URL and client
            return Retrofit.Builder()
                .baseUrl(BASE_URL) // Set the base URL for the API
                .client(client) // Attach the OkHttpClient instance
                .addConverterFactory(GsonConverterFactory.create()) // Set the converter for JSON
                .build() // Build the Retrofit instance
                .create(AnomalyApiService::class.java) // Create the API service interface
        }
    }
}
