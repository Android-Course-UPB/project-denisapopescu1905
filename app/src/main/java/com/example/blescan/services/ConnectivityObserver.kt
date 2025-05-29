package com.example.blescan.services

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.common.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface ConnectivityObserver {
    fun observe(): Flow<Status>

    enum class Status
    {
        Available, Unavailable, Losing, Lost
    }
}

interface BluetoothObserver{
    fun observe(): Flow<Status>

    enum class Status
    {
        BluetoothOn, BluetoothOff, BluetoothUnknown
    }
}

interface LocationObserver{
    fun observe(): Flow<Status>

    data class LocationStatus(
        val latitude: Double?,
        val logitude: Double?,
        val status: Status,
    )

    enum class Status
    {
        LocationOn, LocationOff, LocationUnknown
    }
}

