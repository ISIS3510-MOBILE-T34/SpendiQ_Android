package com.isis3510.spendiq.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkViewModel(application: Application) : AndroidViewModel(application) {

    // MutableStateFlow for network status
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val connectivityManager =
        application.getSystemService(ConnectivityManager::class.java)

    init {
        monitorNetworkConnectivity()
    }

    private fun monitorNetworkConnectivity() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // Network is available
                    _isConnected.value = true
                }

                override fun onLost(network: Network) {
                    // Network is lost
                    _isConnected.value = false
                }

                override fun onUnavailable() {
                    // Network is unavailable
                    _isConnected.value = false
                }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
    }
}
