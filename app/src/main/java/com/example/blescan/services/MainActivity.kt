package com.example.blescan.services

import BLEScanTheme
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.blescan.alerts.FireTag
import com.example.blescan.data.AppDatabase
import com.example.blescan.data.DatabaseProvider
import com.example.blescan.data.DeviceIdentifier
import com.example.blescan.ui.theme.MainBlueTheme
import com.example.blescan.data.SensorDataViewModel
import com.example.blescan.map.MapActivity
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale

const val MainTAG = "MainActivity"
class MainActivity : ComponentActivity(){
    private lateinit var firebaseMessaging: FirebaseMessaging
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        DeviceIdentifier.deviceId = DeviceIdentifier.getDeviceId(this).toString()
        Log.d(FireTag, "DeviceId " + DeviceIdentifier.deviceId.toString())
        TTSManager.initialize(this)
        firebaseMessaging = FirebaseMessaging.getInstance()

        firebaseMessaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(FireTag, "Token: $token")
                // Send token to server
            } else {
                Log.w(FireTag, "Failed to get token")
            }
        }

        firebaseMessaging.subscribeToTopic("topic").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Subscription successful
                Log.d(FireTag, "Subscribed to topic")
            } else {
                // Subscription failed
                Log.e(FireTag, "Failed to subscribe to topic", task.exception)
            }
        }

        DatabaseProvider.initialize(this)
        val historyDao = DatabaseProvider.getHistoryDao()

        setContent {
            BLEScanTheme {
                var showIpDialog by remember { mutableStateOf(true) }
                var enteredIp by remember { mutableStateOf("") }
                if (showIpDialog)
                {
                    IpInputDialog(
                        onDismiss = { },
                        onConfirm = { ip ->
                            enteredIp = ip
                            showIpDialog = false
                            Log.d(MainTAG, "IP Address Entered: $ip")
                        }
                    )
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val sensorDataViewModel = viewModel<SensorDataViewModel>()
                val networkObserver = NetworkConnectivityObserver(context = this)
                BluetoothConnectivityObserver()
                Intent(this@MainActivity, MapActivity::class.java)

                if(!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
                {
                    Log.d(MainTAG, "No BLE support -> exit")
                    AlertDialog(
                         onDismissRequest = { finish() },
                         onConfirmation = {
                             finish()
                         },
                         dialogTitle = "No BLE Support",
                         dialogText = "This app is not available for your device because it was made for a newer version of Android.",
                         icon = Icons.Default.Bluetooth,
                         confirmText = "Confirm",
                         dismissText = "Dismiss"
                    )
                }
                Log.d(APIService, "enteredIp (main) $enteredIp")
                val connectivityViewModel = ConnectivityViewModel(context = this, networkObserver, sensorDataViewModel, enteredIp)
                Surface(
                     modifier = Modifier.fillMaxSize(),
                     color = MaterialTheme.colorScheme.background
                 ) {
                     NavScreen(navController, navBackStackEntry, 0, sensorDataViewModel, connectivityViewModel)
                 }
            }
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "location_channel",
            "Location Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}


@Composable
fun IpInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var ipAddress by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }

    fun validateIp(ip: String): Boolean {
        /*val ipRegex = Regex(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )
        return ip.matches(ipRegex)*/
        return ip.isNotEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter IP Address") },
        text = {
            Column {
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = {
                        ipAddress = it
                        isValid = validateIp(it)
                    },
                    label = { Text("IP Address") },
                    singleLine = true
                )
                if (!isValid && ipAddress.isNotEmpty()) {
                    //Text("Invalid IP Address", color = MaterialTheme.colorScheme.error)
                    Text("Invalid URL", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(ipAddress) },
                enabled = isValid
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    confirmText: String,
    dismissText: String
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon",
                modifier = Modifier.size(43.dp), tint = MainBlueTheme)
        },
        title = {
            Text(text = dialogTitle, textAlign = TextAlign.Center)
        },
        text = {
            Text(text = dialogText, textAlign = TextAlign.Center)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(dismissText)
            }
        }
    )
}