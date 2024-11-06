package com.isis3510.spendiq.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * ConnectivityViewModel class
 *
 * This ViewModel monitors the network connectivity status of the application.
 * It utilizes the ConnectivityManager to detect network changes and provides
 * a LiveData object that can be observed to determine the current connection status.
 *
 * Key Features:
 * - Connectivity Monitoring: Uses a NetworkCallback to listen for network availability changes.
 * - LiveData Exposure: Provides a LiveData<Boolean> that indicates whether the device is connected to the internet.
 *
 * State Management:
 * - `_isConnected`: MutableLiveData<Boolean> that holds the current connectivity status.
 * - `isConnected`: Public LiveData<Boolean> for external observers to check the connection status.
 *
 * Initialization:
 * - On initialization, it checks the current network status and registers a default network callback
 *   to listen for changes in connectivity.
 */
class ConnectivityViewModel(application: Application) : AndroidViewModel(application) {
    // MutableLiveData to hold the connectivity status
    private val _isConnected = MutableLiveData<Boolean>()

    // Public LiveData for observing connectivity status
    val isConnected: LiveData<Boolean> get() = _isConnected

    // ConnectivityManager instance for network operations
    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        // Check the initial connectivity status
        _isConnected.value = isNetworkAvailable()

        // Register the NetworkCallback to monitor connectivity changes
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isConnected.postValue(true) // Connection available
            }

            override fun onLost(network: Network) {
                _isConnected.postValue(false) // Connection lost
            }
        })
    }

    // Check if the network is available
    private fun isNetworkAvailable(): Boolean {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
