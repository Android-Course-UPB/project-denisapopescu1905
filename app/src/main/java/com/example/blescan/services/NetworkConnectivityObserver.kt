package com.example.blescan.services

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blescan.data.SensorDataViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NetworkConnectivityObserver (
    context: Context
) : ConnectivityObserver {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback()
            {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(ConnectivityObserver.Status.Available)}
                }

                override fun onLosing(network: Network, maxMsToLivenetwork: Int) {
                    super.onLosing(network, maxMsToLivenetwork)
                    launch { send(ConnectivityObserver.Status.Losing)}
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(ConnectivityObserver.Status.Lost)}
                }
                override fun onUnavailable () {
                    super.onUnavailable()
                    launch { send(ConnectivityObserver.Status.Unavailable)}
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)


            awaitClose{
                connectivityManager.unregisterNetworkCallback(callback)
            }

        }.distinctUntilChanged()
    }
}

class BluetoothConnectivityObserver : BluetoothObserver {
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun observe(): Flow<BluetoothObserver.Status> = callbackFlow {
        while (isActive) {
            val bluetoothStatus = bluetoothAdapter?.state ?: BluetoothAdapter.STATE_OFF
            val status = when (bluetoothStatus) {
                BluetoothAdapter.STATE_ON -> BluetoothObserver.Status.BluetoothOn
                BluetoothAdapter.STATE_OFF -> BluetoothObserver.Status.BluetoothOff
                else -> BluetoothObserver.Status.BluetoothUnknown
            }
            send(status)
            delay(1000) // Check Bluetooth state every second
        }
        awaitClose()
    }
}

class LocationConnectivityObserver (
    context: Context
) : LocationObserver {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun observe(): Flow<LocationObserver.Status>  = callbackFlow {
        while (isActive) {
            val locationStatus = if (locationManager.isLocationEnabled) LocationObserver.Status.LocationOn else LocationObserver.Status.LocationOff
            send(locationStatus)
            delay(1000) // Check Bluetooth state every second
        }
        awaitClose()
    }
}

class ConnectivityViewModel(
    private val context: Context,
    private val networkObserver: NetworkConnectivityObserver,
    sensorDataViewModel: SensorDataViewModel,
    enteredIp: String
) : ViewModel() {
    private val _connectivityStatus = MutableStateFlow(ConnectivityObserver.Status.Unavailable)
    val connectivityStatus: StateFlow<ConnectivityObserver.Status> = _connectivityStatus
    private var lastStatus: ConnectivityObserver.Status? = null

    private val bluetoothObserver = BluetoothConnectivityObserver() // Create Bluetooth observer here

    private val _bluetoothConnectivityStatus = MutableStateFlow(BluetoothObserver.Status.BluetoothUnknown)
    val bluetoothConnectivityStatus: StateFlow<BluetoothObserver.Status> = _bluetoothConnectivityStatus
    private var bluetoothLastStatus: BluetoothObserver.Status? = null

    private val locationObserver = LocationConnectivityObserver(context)
    private val _locationConnectivityStatus = MutableStateFlow(LocationObserver.Status.LocationUnknown)
    val locationConnectivityStatus: StateFlow<LocationObserver.Status> = _locationConnectivityStatus
    private var locationLastStatus : LocationObserver.Status? = null


    init {
        observeConnectivity(sensorDataViewModel, enteredIp = enteredIp)
        observeBluetooth()
        observerLocation()
    }

    fun observeConnectivity(sensorDataViewModel: SensorDataViewModel, enteredIp: String) {
        viewModelScope.launch {
            networkObserver.observe().collect { status ->
                if (lastStatus == null || lastStatus != status) {
                    showToast("Connectivity status changed: $status")
                }
                lastStatus = status
                _connectivityStatus.value = status
                if(status == ConnectivityObserver.Status.Available)
                {
                    sensorDataViewModel.startPeriodicAPICalls(enteredIp = enteredIp)
                }
                else {
                    sensorDataViewModel.stopPeriodicAPICalls()
                }
            }
        }
    }

    private fun observeBluetooth() {
        viewModelScope.launch {
            bluetoothObserver.observe().collect { status ->
                if (bluetoothLastStatus == null || bluetoothLastStatus != status) {
                    showToast("Bluetooth status changed: $status")
                }
                bluetoothLastStatus = status
                _bluetoothConnectivityStatus.value = status
            }
        }
    }

    fun observerLocation() {
        viewModelScope.launch {
            locationObserver.observe().collect { status ->
                if (locationLastStatus == null || locationLastStatus != status) {
                    showToast("Location status changed: $status")
                }
                locationLastStatus = status
                _locationConnectivityStatus.value = status
            }
        }
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}