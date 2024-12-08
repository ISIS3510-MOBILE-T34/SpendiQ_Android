package com.isis3510.spendiq.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log

class NetworkViewModel(application: Application) : AndroidViewModel(application) {

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val connectivityManager =
        application.getSystemService(ConnectivityManager::class.java)

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        monitorNetworkConnectivity()
    }

    private fun monitorNetworkConnectivity() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Red disponible
                _isConnected.value = true
                Log.d("NetworkViewModel", "Network Available")
            }

            override fun onLost(network: Network) {
                // Red perdida
                _isConnected.value = false
                Log.d("NetworkViewModel", "Network Lost")
            }

            override fun onUnavailable() {
                // Red no disponible
                _isConnected.value = false
                Log.d("NetworkViewModel", "Network Unavailable")
            }
        }

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            Log.d("NetworkViewModel", "NetworkCallback registrado correctamente")
        } catch (e: Exception) {
            Log.e("NetworkViewModel", "Error al registrar NetworkCallback: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkCallback?.let { callback ->
            try {
                connectivityManager.unregisterNetworkCallback(callback)
                Log.d("NetworkViewModel", "NetworkCallback desregistrado correctamente")
            } catch (e: IllegalArgumentException) {
                Log.w("NetworkViewModel", "Intento de desregistrar un NetworkCallback no registrado: ${e.message}")
            }
            networkCallback = null
        }
    }
}
