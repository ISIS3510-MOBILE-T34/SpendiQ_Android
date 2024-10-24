package com.isis3510.spendiq.Services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Path
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface AnomalyApiService {
    @POST("api/analyze-transaction-complete/{user_id}/{transaction_id}")
    suspend fun analyzeTransaction(
        @Path("user_id") userId: String,
        @Path("transaction_id") transactionId: String
    )

    companion object {
        private const val BASE_URL = "http://148.113.204.223:8000/"

        fun create(): AnomalyApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AnomalyApiService::class.java)
        }
    }
}