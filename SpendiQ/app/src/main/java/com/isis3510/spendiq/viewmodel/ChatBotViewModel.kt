package com.isis3510.spendiq.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.spendiq.services.ApiService
import kotlinx.coroutines.launch

data class ChatMessage(val content: String, val fromUser: Boolean)

class ChatbotViewModel : ViewModel() {
    // Lista para almacenar los mensajes del chat
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages

    // Estado del bot
    var isBotActive = mutableStateOf(true)

    // Mensaje de restricción
    private val restrictionMessage = "You are a financial assistant. You must only respond to: " +
            "1. Financial inquiries (e.g., budgeting, expenses, savings, or investments). " +
            "2. Greetings if the user greets you." +
            "3. Questions about your identity (e.g., 'Who are you?')." +
            "If the message contains anything else, respond with: 'Sorry, but I only answer financial inquiries.'"

    // Función para enviar un mensaje al backend
    fun sendMessage(message: String, firebaseAnalytics: FirebaseAnalytics, balance:Long) {
        // Agregar el mensaje del usuario a la lista
        Log.d("ChatbotViewModel", "message: $message")
        _messages.add(ChatMessage(content = message, fromUser = true))

        // Enviar evento a Firebase Analytics
        val bundle = Bundle().apply {
            putString("user_message", message) // Puedes agregar más información si lo deseas
        }
        firebaseAnalytics.logEvent("chatbot_message_sent", bundle) // Nombre del evento

        viewModelScope.launch {
            try {
                // Realiza la solicitud al backend
                val response = ApiService.create().sendChatMessage(mapOf("message" to "$restrictionMessage $message with balance: $balance"))
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

    // Función para verificar el estado del bot
    fun checkBotStatus() {
        viewModelScope.launch {
            try {
                // Enviar un mensaje de prueba al bot
                val response = ApiService.create().sendChatMessage(mapOf("message" to "¿Estás activo?"))
                // Si recibimos una respuesta, el bot está activo
                isBotActive.value = true
                Log.d("ChatbotViewModel", "Bot is active: ${response.response}")
            } catch (e: Exception) {
                // Si hay un error, asumimos que el bot no está activo
                isBotActive.value = false
                Log.d("ChatbotViewModel", "Error checking bot status: ${e.message}")
            }
        }
    }
}