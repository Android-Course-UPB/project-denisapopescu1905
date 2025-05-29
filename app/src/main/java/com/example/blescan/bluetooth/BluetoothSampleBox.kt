/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.blescan.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.blescan.services.ConnectivityViewModel
import com.example.blescan.services.LocationObserver
import com.example.blescan.ui.theme.MainBlueTheme


@Composable
fun BluetoothSampleBox(
    connectivityViewModel: ConnectivityViewModel,
    extraPermissions: Set<String> = emptySet(),
    content: @Composable BoxScope.(BluetoothAdapter) -> Unit,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val bluetoothAdapter = context.getSystemService<BluetoothManager>()?.adapter


    val locationPermission = setOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    // For Android 12 and above we only need connect and scan
    val bluetoothPermissionSet = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
        // For Android versions 29 and lower
        setOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    } else {
        // For Android 30 (Q) and above
        setOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    PermissionBox(
        permissions = (bluetoothPermissionSet + locationPermission + extraPermissions).toList(),
        contentAlignment = Alignment.Center,
    ) {
        // Check to see if the Bluetooth classic feature is available.
        val hasBT = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        // Check to see if the BLE feature is available.
        val hasBLE = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        // Check if the adapter is enabled
        var isBTEnabled by remember {
            mutableStateOf(bluetoothAdapter?.isEnabled == true)
        }
        var bluetoothIndex by remember {
            mutableStateOf(0)
        }
        var locationIndex by remember {
            mutableStateOf(0)
        }

        if(connectivityViewModel.locationConnectivityStatus.value != LocationObserver.Status.LocationOn)
        {
            locationIndex = 1
        }

        if(!isBTEnabled)
        {
            bluetoothIndex = 1
        }

        Log.d(TAG, hasBLE.toString() + " " + hasBT.toString())

        when {
            bluetoothAdapter == null || !hasBT -> MissingFeatureText(text = "No bluetooth available")
            !hasBLE -> MissingFeatureText(text = "No bluetooth low energy available")
            !isBTEnabled || (locationIndex == 1) -> BluetoothDisabledScreen (bluetoothIndex, locationIndex)
            {
                isBTEnabled = true
                if(locationIndex == 1)
                {
                    locationIndex = 0
                }
                if(bluetoothIndex == 1)
                {
                    bluetoothIndex = 0
                }
            }
            else -> {
                // Request background location permission
                requestBackgroundLocationPermission()
                // Proceed with content
                content(bluetoothAdapter)
            }
        }
    }
}

@Composable
fun BluetoothDisabledScreen(
    bluetoothIndex: Int,
    locationIndex: Int,
    onEnabled: () -> Unit
) {
    val result =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult())
        { _ ->
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (locationIndex == 1) {
            Text(text = "Location is disabled")
            Button(
                colors = ButtonDefaults.buttonColors(MainBlueTheme),
                onClick = {
                    val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    result.launch(enableLocationIntent)
                },
            ) {
                Text(text = "Enable")
            }
        }
        if (bluetoothIndex == 1) {
            Text(text = "Bluetooth is disabled")
            Button(
                colors = ButtonDefaults.buttonColors(MainBlueTheme),
                onClick = {
                    result.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                },
            ) {
                Text(text = "Enable")
            }
        }
        Button(
            colors = ButtonDefaults.buttonColors(MainBlueTheme),
            onClick = {
                onEnabled()
            },
        ) {
            Text(text = "Done")
        }
    }
}


@Composable
private fun MissingFeatureText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.error,
    )
}
@Composable
fun requestBackgroundLocationPermission() {
    val context = LocalContext.current

    // Request background location permission
    val permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(permission),
            PERMISSION_REQUEST_BACKGROUND_LOCATION
        )
    }
}

const val PERMISSION_REQUEST_BACKGROUND_LOCATION = 123 // Any unique request code
