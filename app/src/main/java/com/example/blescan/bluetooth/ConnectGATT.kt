package com.example.blescan.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.blescan.services.BluetoothObserver
import com.example.blescan.services.ConnectivityViewModel
import com.example.blescan.data.SensorDataViewModel

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.Int.Companion.MAX_VALUE
import kotlin.Int.Companion.MIN_VALUE

const val TAG = "ConnectGATT"



object GATTClient { //singleton
    var disconnectReq by mutableStateOf<Int>(0)
    var startMeasure by mutableStateOf<Boolean?>(false)
    /// MODIFY SERVICE UUID
    val serviceUUID: UUID = UUID.fromString("0000181a-0000-1000-8000-00805f9b34fb")
    const val MTU = 190
    var globalDevice by mutableStateOf<BluetoothDevice?>(null)
    var isConnected = false

    val characteristicsUUIDs = hashMapOf(
        UUID.fromString("00002A6E-0000-1000-8000-00805F9B34FB") to 0, // Temperature
        UUID.fromString("00002A6F-0000-1000-8000-00805F9B34FB") to 1, // Humidity - !
        UUID.fromString("00002A6D-0000-1000-8000-00805F9B34FB") to 2, // Pressure - !
        UUID.fromString("00002AB3-0000-1000-8000-00805F9B34FB") to 3, // Altitude - !
        UUID.fromString("00002B8C-0000-1000-8000-00805F9B34FB") to 4, // CO2
        UUID.fromString("00002BD5-0000-1000-8000-00805F9B34FB") to 5, // PM1
        UUID.fromString("00002BD6-0000-1000-8000-00805F9B34FB") to 6, // PM2.5
        UUID.fromString("00002BD7-0000-1000-8000-00805F9B34FB") to 7, // PM10
        UUID.fromString("00002A76-0000-1000-8000-00805F9B34FB") to 8, // Battery
        UUID.fromString("00002A79-0000-1000-8000-00805F9B34FB") to 9, // Timestamp
    )

    fun getIndexForUid(uuid: UUID): Int {
        for ((currentUUID, index) in characteristicsUUIDs.entries) {
            if (currentUUID == uuid) {
                Log.d(TAG, "Update index $index")
                return index
            }
        }
        return -1
    }
    fun getCurrentTime(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        return calendar.time.toString()
    }


    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    @Composable
    fun ConnectGATT(navController: NavHostController, sensorDataViewModel: SensorDataViewModel, connectivityViewModel: ConnectivityViewModel) {
        var selectedDevice by remember {
            mutableStateOf<BluetoothDevice?>(null)
        }

        // Check that BT permissions and that BT is available and enabled
        BluetoothSampleBox (connectivityViewModel = connectivityViewModel){
            AnimatedContent(targetState = selectedDevice, label = "Selected device") { device ->
                if(connectivityViewModel.bluetoothConnectivityStatus.value != BluetoothObserver.Status.BluetoothOn)
                {
                    Log.i(TAG,"ConnectGATT - connectivityViewModel.bluetoothConnectivityStatus.value != BluetoothObserver.Status.BluetoothOn")
                }
                if (device == null)
                {
                    // Scans for BT devices and handles clicks
                    // if ServiceUUID doesn't match then the button turns red
                    FindDevicesScreen (connectivityViewModel = connectivityViewModel) {
                        selectedDevice = it
                        globalDevice = selectedDevice
                        Log.v(TAG, "startMeasure=$startMeasure")
                    }
                } else {
                    // Once a device is selected show the UI and try to connect device
                    if ((startMeasure == true) && (connectivityViewModel.bluetoothConnectivityStatus.value == BluetoothObserver.Status.BluetoothOn)) {
                        //navController.clearBackStack("connect")
                        ConnectDeviceScreen(navController = navController, device = device, sensorDataViewModel = sensorDataViewModel)
                    }
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(
        anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
    )
    @Composable
    fun ConnectDeviceScreen(
        navController: NavHostController,
        device: BluetoothDevice,
        sensorDataViewModel: SensorDataViewModel,
    ) {
        // Keeps track of the last connection state with the device
        var state by remember(device) {
            mutableStateOf<DeviceConnectionState?>(null)
        }

        val context = LocalContext.current

        // Old Location logic - send location of first BLE connection
        /*val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }



        LaunchedEffect(Unit) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                Log.d("DEBUG", "STARTING SERVICE")
                val intent = Intent(context, LocationService::class.java)
                ContextCompat.startForegroundService(context, intent)
            } else {
                Log.e("DEBUG", "PERMISSION NOT GRANTED")
            }
        }*/
        /*val locationClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }
        locationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                state?.location = "${location?.latitude},${location?.longitude}"
                Log.d(TAG, "Location: ${state?.location} ${sensorDataViewModel.getLocationData()}")
                sensorDataViewModel.updateLocationData(state?.location)
            }*/


        val activity = (LocalContext.current as? Activity)
        // This effect will handle the connection and notify when the state changes
        if (activity != null) {
            BLEConnectEffect(device = device, sensorDataViewModel = sensorDataViewModel) {
                // update our state to recompose the UI
                state = it
                if(disconnectReq == 0)
                {
                    isConnected = true
                }
                Log.d(TAG, "Update state & recompose the UI $disconnectReq")
            }
            ProfileScreen(navController = navController, sensorDataViewModel = sensorDataViewModel)

        }
        DisposableEffect(Unit) {
            val window = context.findActivity()?.window
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                Log.d(TAG, "DISPOSE")
            }
        }

        Log.d(TAG, "ConnectionState " + (state?.connectionState?.toConnectionStateString()))
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    private fun Int.toConnectionStateString() = when (this) {
        BluetoothProfile.STATE_CONNECTED -> "Connected"
        BluetoothProfile.STATE_CONNECTING -> "Connecting"
        BluetoothProfile.STATE_DISCONNECTED -> "Disconnected"
        BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting"
        else -> "N/A"
    }

    data class DeviceConnectionState(
        val gatt: BluetoothGatt?,
        val connectionState: Int,
        val mtu: Int,
        var location: String,
        val services: List<BluetoothGattService> = emptyList(),
        val messageSent: Boolean = false,
        val messageReceived: String = "",
    ) {
        companion object {
            val None = DeviceConnectionState(null, -1, -1, "")
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Composable
    private fun BLEConnectEffect(
        device: BluetoothDevice,
        sensorDataViewModel : SensorDataViewModel,
        lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
        onStateChange: (DeviceConnectionState) -> Unit,
    ) {
        val context = LocalContext.current
        val currentOnStateChange by rememberUpdatedState(onStateChange)

        // Keep the current connection state
        var state by remember {
            mutableStateOf(DeviceConnectionState.None)
        }


        DisposableEffect(lifecycleOwner, device) {
            // This callback will notify us when things change in the GATT connection so we can update
            // our state machine

            // Queue for characteristics read operations
            val charsQueue: MutableList<BluetoothGattCharacteristic> = ArrayList()
            var queueIndex by mutableStateOf<Int>(0)
            var historyValues by mutableStateOf(listOf<Pair<Int, Int>>())
            var historyValid by mutableStateOf<Int>(0)

            val callback = object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int,
                ) {
                    super.onConnectionStateChange(gatt, status, newState)
                    state = state.copy(gatt = gatt, connectionState = newState)
                    currentOnStateChange(state)
                    sensorDataViewModel.updateConnectionState(state.connectionState)

                    Log.d(TAG, "onConnectionStateChange $status $newState")
                    if(newState == BluetoothGatt.STATE_DISCONNECTED)
                    {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.e(TAG, "No permission to disconnect.")
                            //return
                        }
                        gatt.close()
                        charsQueue.clear()
                        queueIndex = 0
                        state = DeviceConnectionState.None
                        startMeasure = false
                        sensorDataViewModel.updateConnectionState(state.connectionState)
                        disconnectReq = 0
                        isConnected = false
                        globalDevice = null
                        historyValues = listOf()
                        historyValid = 0
                    }

                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        Log.e(TAG, "An error happened: $status")
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.e(TAG, "No permission.")
                        }
                        // Disconnect and empty queue
                        gatt.close()
                        charsQueue.clear()
                        queueIndex = 0
                        state = DeviceConnectionState.None
                        startMeasure = false
                        sensorDataViewModel.updateConnectionState(state.connectionState)
                        disconnectReq = 0
                        isConnected = false
                        globalDevice = null
                        historyValues = listOf()
                        historyValid = 0
                    }
                    else if (newState == BluetoothProfile.STATE_CONNECTED)
                    {
                        val discoveredStatus = gatt.discoverServices()
                        isConnected = true
                        sensorDataViewModel.updateConnectionState(state.connectionState)
                        sensorDataViewModel.updateConnectionTime(getCurrentTime())
                        Log.d(TAG, "Discovering services: $discoveredStatus for name "+ device.name)
                        sensorDataViewModel.updateDeviceName(device.name)
                    }
                }

                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    super.onServicesDiscovered(gatt, status)
                    if (gatt != null) {
                        state = state.copy(services = gatt.services)
                    }
                    currentOnStateChange(state)
                    gatt?.requestMtu(MTU)
                }

                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                    super.onMtuChanged(gatt, mtu, status)
                    state = state.copy(gatt = gatt, mtu = mtu)
                    currentOnStateChange(state)
                    Log.d(TAG, "MTU changed; Current State: $state")
                    for (service in gatt.services) {
                        Log.d(TAG, "Discovered service: ${service.uuid}")

                        for (characteristic in service.characteristics) {
                            Log.d(TAG, "Characteristic UUID: ${characteristic.uuid}" )
                            if (characteristic.uuid in characteristicsUUIDs.keys)
                            {
                                Log.d(TAG, "ASTA E" + characteristic.uuid.toString())

                                charsQueue.add(characteristic)  // add characteristics to read queue
                            }
                            else
                            {
                                Log.d(TAG, "NU E" + characteristic.uuid.toString())
                            }
                            Log.d(TAG, "Size ${Thread.currentThread().name} !!!" + charsQueue.size.toString())
                        }
                    }
                    requestCharacteristic(gatt) // start reading

                }

                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                fun requestCharacteristic(gatt: BluetoothGatt?)
                {
                    if (charsQueue.size == 0) {
                        Log.d(TAG, "Queue empty")
                        return
                    }
                    else if (state.gatt == null) {
                        Log.d(TAG, "State disconnected")
                    } else {
                        Log.d(TAG, "${charsQueue.size}")

                        if (queueIndex >= charsQueue.size) {
                            queueIndex = 0
                            Log.d(TAG, "HistoryValues: $historyValues")
                            if(historyValid == 0)
                            {
                                historyValues.forEachIndexed { index, pair ->
                                    if(pair.first == MIN_VALUE || pair.first == MAX_VALUE)
                                    {
                                        sensorDataViewModel.updateValueForIndex(pair.second, 0.0f)
                                    }
                                    else
                                    {
                                        if (pair.second == 8)
                                        {
                                            sensorDataViewModel.updateBattery((pair.first.toFloat()/100).toString() + " %")

                                        }
                                        else if (pair.second < 9)
                                        {
                                            Log.d(TAG, "UpdateIndex ${pair.second} value ${pair.first.toFloat()}")
                                            sensorDataViewModel.updateValueForIndex(pair.second, pair.first.toFloat())

                                        }
                                    }
                                }

                            }
                            sensorDataViewModel.updateHistory(historyValues)
                            historyValues = listOf()
                            Thread.sleep(15000)

                        }
                        Log.d(TAG, "Reading characteristic at index $queueIndex")
                        gatt?.readCharacteristic(charsQueue[queueIndex])
                    }

                }
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onCharacteristicRead(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    val currentUuid = characteristic.uuid
                    Log.d(TAG, "Callback for uuid $currentUuid")

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val data = characteristic.value
                        Log.d(TAG, data.toString())

                        val intValue: Int

                        try {
                            intValue = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).int
                            Log.d(TAG, "READING $data - ${data.size}-  $intValue for ${characteristic.uuid}")
                            for (byteValue in data) {
                                Log.d(TAG, "Byte: $byteValue ${byteValue.toInt()}")
                            }
                            if(queueIndex == 9)
                            {
                                // Assuming bytes is your byte array
                                val bytes: ByteArray = data.reversedArray()

                                // Convert byte array to long
                                val timestampLong = ByteBuffer.wrap(bytes).long

                                // Create Date object
                                val date = Date(timestampLong)

                                val sdf = SimpleDateFormat("HH:mm:ss:dd:MM:yyyy", Locale.getDefault())
                                val timestamp = sdf.format(date)
                                Log.d(TAG, "TimeStamp: " + timestamp.toString())
                                historyValid = 1
                            }

                            historyValues += Pair(intValue, getIndexForUid(characteristic.uuid))

                        } catch (e: Exception) {
                            Log.d(TAG,"Read error: ${e.message}")
                            // Handle the exception as needed
                            if(queueIndex == 9)
                            {
                                historyValid = 0
                            }
                        }
                        queueIndex += 1
                    }
                    else
                    {
                        Log.d(TAG, "READING FAILED")
                    }

                    if(sensorDataViewModel.getConnectionState() == 0)
                    {
                        gatt?.disconnect()
                    }
                    requestCharacteristic(gatt)
                }
            }

            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {Log.d(TAG, "ON_START" + state.gatt)
                    if (state.gatt != null) {
                        // If we previously had a GATT connection let's reestablish it
                        state.gatt?.connect()
                    } else {
                        // Otherwise create a new GATT connection
                        state = state.copy(gatt = device.connectGatt(context, false, callback))
                    }
                } else if (event == Lifecycle.Event.ON_STOP) {
                    Log.d(TAG, "ON_STOP" + state.gatt)
                }
            }

            // Add the observer to the lifecycle
            lifecycleOwner.lifecycle.addObserver(observer)

            // When the effect leaves the Composition, remove the observer and close the connection
            onDispose {
                Log.d(TAG, "DISPOSE")
            }
        }
    }

}

