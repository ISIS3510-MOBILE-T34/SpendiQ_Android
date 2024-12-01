package com.isis3510.spendiq.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.spendiq.services.ApiService
import kotlinx.coroutines.launch

data class ChatMessage(val content: String, val fromUser: Boolean)

class ChatbotViewModel : ViewModel() {
    // Lista para almacenar los mensajes del chat
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages

    // Función para enviar un mensaje al backend
    fun sendMessage(message: String) {
        // Agregar el mensaje del usuario a la lista
        Log.d("ChatbotViewModel", "message: $message")
        _messages.add(ChatMessage(content = message, fromUser = true))

        viewModelScope.launch {
            try {
                // Realiza la solicitud al backend
                val response = ApiService.create().sendChatMessage(mapOf("message" to message))
                Log.d("ChatbotViewModel", "Response: $response")
                // Agregar la respuesta del bot a la lista
                _messages.add(ChatMessage(content = response.response, fromUser = false))
            } catch (e: Exception) {
                // Manejo de errores
                Log.d("ChatbotViewModel", "Error: ${e.message}")
                _messages.add(ChatMessage(content = "Error: ${e.message}", fromUser = false))
            }
        }
    }
}