package com.example.socialbatterymanager.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utility class for monitoring network connectivity
 */
class NetworkConnectivityManager(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _isOnWifi = MutableStateFlow(false)
    val isOnWifi: StateFlow<Boolean> = _isOnWifi.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _isConnected.value = true
            updateConnectionType()
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            _isConnected.value = false
            _isOnWifi.value = false
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateConnectionType()
        }
    }
    
    init {
        // Initialize with current state
        updateConnectionState()
        
        // Register network callback
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }
    
    /**
     * Check if device is currently connected to the internet
     */
    fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
    
    /**
     * Check if device is connected to Wi-Fi
     */
    fun isCurrentlyOnWifi(): Boolean {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }
    
    /**
     * Check if device is connected to mobile data
     */
    fun isCurrentlyOnMobile(): Boolean {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }
    
    /**
     * Get current connection type
     */
    fun getCurrentConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        
        return when {
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> ConnectionType.WIFI
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> ConnectionType.MOBILE
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> ConnectionType.ETHERNET
            else -> ConnectionType.NONE
        }
    }
    
    private fun updateConnectionState() {
        _isConnected.value = isCurrentlyConnected()
        updateConnectionType()
    }
    
    private fun updateConnectionType() {
        _isOnWifi.value = isCurrentlyOnWifi()
    }
    
    /**
     * Unregister network callback to prevent memory leaks
     */
    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

/**
 * Enum representing connection types
 */
enum class ConnectionType {
    WIFI,
    MOBILE,
    ETHERNET,
    NONE
}

/**
 * Extension function to get user-friendly message for connection type
 */
fun ConnectionType.getMessage(context: Context): String {
    return when (this) {
        ConnectionType.WIFI -> "Connected via Wi-Fi"
        ConnectionType.MOBILE -> "Connected via Mobile Data"
        ConnectionType.ETHERNET -> "Connected via Ethernet"
        ConnectionType.NONE -> "Not connected"
    }
}