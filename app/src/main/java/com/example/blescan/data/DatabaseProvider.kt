package com.example.blescan.data

import android.content.Context
import android.util.Log
import androidx.room.Room

object DatabaseProvider {

    private var database: AppDatabase? = null
    private const val TAG = "DatabaseProvider"

    fun initialize(context: Context) {
        if (database == null) {
            Log.d(TAG, "Initializing database...")
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sensor-history-db"
            ).build()
            Log.d(TAG, "Database initialized.")
        } else {
            Log.d(TAG, "Database already initialized.")
        }
    }

    fun getDatabase(): AppDatabase {
        Log.d(TAG, "Getting database instance.")
        return database ?: throw IllegalStateException("Database not initialized. Call initialize() first.")
    }

    fun getHistoryDao(): HistoryDao {
        Log.d(TAG, "Getting HistoryDao instance.")
        return getDatabase().historyDao()
    }
}
