package com.example.blescan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HistoryPoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sensorType: String, // e.g., "Temperature"
    val value: Long,
    val timestamp: Long
)
