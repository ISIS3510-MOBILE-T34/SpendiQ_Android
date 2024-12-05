package com.isis3510.spendiq.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Define la interfaz ApiService
interface ApiService {

    @POST("/api/chat")
    suspend fun sendChatMessage(@Body message: Map<String, String>): ResponseData

    companion object {
        private const val BASE_URL = "http://192.168.2.27:5000"

        // Crea una instancia de ApiService
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}

// Clase de datos para la respuesta
data class ResponseData(val response: String)
