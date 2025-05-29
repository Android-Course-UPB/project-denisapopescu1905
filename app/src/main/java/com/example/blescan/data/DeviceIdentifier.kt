package com.example.blescan.data

import android.content.Context
import android.provider.Settings
import androidx.core.content.edit

object DeviceIdentifier {

    private const val PREFS_NAME = "device_identifier_prefs"
    private const val DEVICE_ID_KEY = "device_id"
    var deviceId : String = ""

    fun getDeviceId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if the ID is already stored in SharedPreferences
        var deviceId = sharedPreferences.getString(DEVICE_ID_KEY, null)
        if (deviceId == null) {
            // Retrieve the ANDROID_ID
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

            // Store the ID in SharedPreferences
            sharedPreferences.edit {
                putString(DEVICE_ID_KEY, deviceId)
            }
        }

        return deviceId
    }
}