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
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.blescan.services.ConnectivityViewModel
import com.example.blescan.R
import com.example.blescan.services.BluetoothObserver
import com.example.blescan.ui.theme.MainBlueTheme
import kotlinx.coroutines.delay

@SuppressLint("InlinedApi", "MissingPermission")
@RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
@Composable
fun FindDevicesScreen(connectivityViewModel: ConnectivityViewModel, onConnect: (BluetoothDevice) -> Unit) {
    val context = LocalContext.current
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(key1 = onBackPressedDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing when back button is pressed
            }
        }
        onBackPressedDispatcher?.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }
    var clickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val adapter = checkNotNull(context.getSystemService<BluetoothManager>()?.adapter)
    var scanning by remember {
        mutableStateOf(true)
    }
    val devices = remember {
        mutableStateListOf<BluetoothDevice>()
    }
    val pollutionTrackingDevices = remember {
        mutableStateListOf<BluetoothDevice>()
    }
    val scanSettings: ScanSettings = ScanSettings.Builder()
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    // This effect will start scanning for devices when the screen is visible
    // If scanning is stop removing the effect will stop the scanning.
    if (scanning) {
        BluetoothScanEffect(
            scanSettings = scanSettings,
            onScanFailed = {
                scanning = false
                Log.w(TAG, "Scan failed with error: $it")
            },
            onDeviceFound = { scanResult ->
                if (!devices.contains(scanResult.device)) {
                    devices.add(scanResult.device)

                }
                Log.d(TAG, "kkkkk " + devices.size)
                for (item in devices) {
                    Log.d(TAG, "Array item: $item ${item.uuids} ${item.name}")
                }

                //Finding the pollution tracker - based on service UuID
                val serviceUuids = scanResult.scanRecord?.serviceUuids.orEmpty()
                if(serviceUuids.isNotEmpty())
                {
                Log.d(TAG, "aaaaa$serviceUuids nnnnn ${serviceUuids.size}")}

                if (serviceUuids.contains(ParcelUuid(GATTClient.serviceUUID))) {
                    if (!pollutionTrackingDevices.contains(scanResult.device)) {
                        pollutionTrackingDevices.add(scanResult.device)
                    }
                }
            },
        )
        // Stop scanning after a while
        LaunchedEffect(true) {
            delay(1000)
            scanning = false
           // devices.clear()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .paint(
                // Replace with your image id
                painterResource(id = R.drawable.gradient_drawable),
                contentScale = ContentScale.FillBounds)


    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 15.dp,
                    end = 15.dp,
                    top = 45.dp,
                    bottom = 55.dp)
                .background(color = Color.White, shape = RoundedCornerShape(15.dp))
                .border(5.dp, color = MainBlueTheme, RoundedCornerShape(15.dp))
                .align(Alignment.Center)

        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                item {
                    Text(text = "Available pollution tracking devices")
                }

                item {
                    if (scanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = {
                                val currentTime = System.currentTimeMillis()
                                if ((currentTime - lastClickTime < 3000) || (lastClickTime.toInt() == 0)) { // Check if the second click is within 5 seconds
                                    Toast.makeText(context, "Scanning available again in 3 seconds!", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    devices.clear()
                                    pollutionTrackingDevices.clear()
                                    /////////
                                    if(connectivityViewModel.bluetoothConnectivityStatus.value == BluetoothObserver.Status.BluetoothOn)
                                    {
                                        scanning = true

                                    }
                                    else
                                    {
                                        val status = connectivityViewModel.bluetoothConnectivityStatus.value
                                        Toast.makeText(context, "$status: Enable Bluetooth!", Toast.LENGTH_SHORT).show()

                                    }
                                }
                                lastClickTime = currentTime
                                clickCount ++
                            },
                        ) {
                            Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
                        }
                    }
                }

                if (devices.isEmpty()) {
                    item {
                        Text(text = "No pollution tracking devices found")
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 8.dp),
                            thickness = 1.dp,
                            color = Color.Gray
                        )
                    }
                } else {
                    items(devices) { item ->
                        BluetoothDeviceItem(
                            bluetoothDevice = item,
                            isPollutionTracker = pollutionTrackingDevices.contains(item),
                            onConnect = onConnect,
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("MissingPermission")
@Composable
internal fun BluetoothDeviceItem(
    bluetoothDevice: BluetoothDevice,
    isPollutionTracker: Boolean = false,
    onConnect: (BluetoothDevice) -> Unit,
) {
    var buttonMessage by remember { mutableStateOf("CONNECT") }
    var buttonColor by remember { mutableStateOf(MainBlueTheme) }
    Log.v(TAG, bluetoothDevice.alias.toString() + " " + bluetoothDevice.bluetoothClass + " " + bluetoothDevice.bondState + " " + bluetoothDevice.type + " " + bluetoothDevice.uuids)
    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = bluetoothDevice.name ?: bluetoothDevice.address ?: "N/A",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterVertically)
                .weight(1f)
        )
        Button(
            onClick = {
                // Handle button click
                if(isPollutionTracker)
                {
                  onConnect(bluetoothDevice)
                    GATTClient.startMeasure = true
                }
                else
                {
                    buttonMessage = "UNAVAILABLE"
                    buttonColor = Color.Red
                }
            },
            colors = ButtonDefaults.buttonColors(buttonColor),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(buttonMessage, fontSize = 11.sp)
        }

    }
    HorizontalDivider(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        thickness = 1.dp,
        color = Color.Gray
    )
}

@SuppressLint("InlinedApi")
@RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
@Composable
private fun BluetoothScanEffect(
    scanSettings: ScanSettings,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onScanFailed: (Int) -> Unit,
    onDeviceFound: (device: ScanResult) -> Unit,
) {
    val context = LocalContext.current
    val adapter = context.getSystemService<BluetoothManager>()?.adapter

    if (adapter == null) {
        onScanFailed(ScanCallback.SCAN_FAILED_INTERNAL_ERROR)
        return
    }

    val currentOnDeviceFound by rememberUpdatedState(onDeviceFound)

    DisposableEffect(lifecycleOwner, scanSettings) {
        val leScanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                currentOnDeviceFound(result)
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                onScanFailed(errorCode)
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            // Start scanning once the app is in foreground and stop when in background
            if (event == Lifecycle.Event.ON_START) {
                Log.d(TAG, "Adapter ON_START" )
                adapter.bluetoothLeScanner.startScan(null, scanSettings, leScanCallback)
            } else if (event == Lifecycle.Event.ON_STOP) {
                adapter.bluetoothLeScanner.stopScan(leScanCallback)
                Log.d(TAG, "Adapter ON_STOP" )
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer and stop scanning
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adapter.bluetoothLeScanner.stopScan(leScanCallback)
        }
    }
}
