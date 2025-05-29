package com.example.blescan.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HistoryPoint::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
