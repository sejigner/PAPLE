package com.gievenbeck.paple

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData

class NetworkConnection(
    private val context: Context
) : LiveData<Boolean>() {
    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onActive() {
        super.onActive()
        updateConnection()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback())
            }
            else -> {
                val request = NetworkRequest.Builder()
                    .build()
                connectivityManager.registerNetworkCallback (request, connectivityManagerCallback())
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback (networkCallback)
    }

    private fun connectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                postValue (false)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                postValue (false)
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue (true)
            }
        }
        return networkCallback
    }

    private fun updateConnection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            postValue(connectivityManager.isDefaultNetworkActive)
        } else {
            val activeNetwork: NetworkInfo? =
                connectivityManager.activeNetworkInfo
            postValue (activeNetwork?.isConnected)
        }
    }

}