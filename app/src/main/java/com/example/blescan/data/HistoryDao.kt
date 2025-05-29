package com.example.blescan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryPoint(historyPoint: HistoryPoint)

    //Room with parameterized queries
    @Query("SELECT * FROM HistoryPoint WHERE sensorType = :sensorType ORDER BY timestamp DESC")
    suspend fun getHistoryForSensor(sensorType: String): List<HistoryPoint>
}