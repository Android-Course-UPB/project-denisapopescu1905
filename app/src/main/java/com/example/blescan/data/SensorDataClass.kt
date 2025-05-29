package com.example.blescan.data

import LocationData
import MapViewModel
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blescan.alerts.MyNotification
import com.example.blescan.alerts.NotificationViewModelProvider
import com.example.blescan.bluetooth.TAG
import com.example.blescan.services.APIService
import com.example.blescan.services.LocationReadingJson
import com.example.blescan.services.OkHttpClientService
import com.example.blescan.services.SensorDataJson
import com.example.blescan.services.SensorReadingJson
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.expressions.dsl.generated.length
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import java.util.UUID


data class SensorData(
    var temperature: Float = 0.0f,
    var humidity: Float = 0.0f,
    var pressure: Float = 0.0f,
    var alt: Float = 0.0f,
    var air: Float = 0.0f,
    var co2: Float = 0.0f,
    var temperature2: Float = 0.0f,
    var pm1: Float = 0.0f,
    var pm25: Float = 0.0f,
    var pm10: Float = 0.0f,
    var n03: Float = 0.0f,
    var n05: Float = 0.0f,
    var n1: Float = 0.0f,
    var n25: Float = 0.0f,
    var n5: Float = 0.0f,
    var n10: Float = 0.0f,
    var locationData: String = "",
    var deviceName: String = "",
    var connectionState: Int = 0,
    var connectionTime: String = "",
    var longitude: Float = 26.1025f,
    var latitude: Float = 44.4268f,
    var battery : String = ""
)

fun getRandomPair(pairs: List<Pair<String, String>>): Pair<String, String> {
    val randomIndex = Random.nextInt(pairs.size)
    return pairs[randomIndex]
}


data class SensorInfo(var value: Float, val name: String)

class SensorDataViewModel : ViewModel() {

    // MutableStateFlow to hold the current sensor data
    val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData

    // MutableStateFlow to hold the hashmap of index and SensorInfo
    val _sensorInfoMap = MutableStateFlow(initializeSensorDataMap())
    val sensorInfoMap: MutableStateFlow<MutableMap<Int, SensorInfo>> = _sensorInfoMap

    private val _connectionState = MutableStateFlow(BluetoothGatt.STATE_DISCONNECTED)
    val connectionState: StateFlow<Int> get() = _connectionState

    val pairs = listOf(
        Pair("Stay Hydrated!", "Don't forget to drink plenty of water to stay hydrated"),
        Pair("Healthy Breathing!", "Air quality is unhealthy today. Use an air purifier and avoid outdoor pollution sources."),
        Pair("Stay Cool!", "Stay indoors or in the shade to avoid heat exhaustion. Drink water regularly and take cool showers."),
        Pair("Sun Safety Tip!", "It's sunny and hot! Don't forget to apply sunscreen with SPF 30.")
    )




    private fun initializeSensorDataMap(): MutableMap<Int, SensorInfo> {
        val defaultValues = SensorData()
        return hashMapOf(
            0 to SensorInfo(defaultValues.temperature, "Temperature"),
            1 to SensorInfo(defaultValues.humidity, "Humidity"),
            2 to SensorInfo(defaultValues.pressure, "Pressure"),
            3 to SensorInfo(defaultValues.alt, "Altitude"),
            //3 to SensorInfo(defaultValues.air, "Air Quality"),
            4 to SensorInfo(defaultValues.co2, "CO2"),
            //6 to SensorInfo(defaultValues.temperature2, "Temperature"),
            5 to SensorInfo(defaultValues.pm1, "PM1.0"),
            6 to SensorInfo(defaultValues.pm25, "PM2.5"),
            7 to SensorInfo(defaultValues.pm10, "PM10"),
            /*10 to SensorInfo(defaultValues.n03, "N0.3"),
            11 to SensorInfo(defaultValues.n05, "N0.5"),
            12 to SensorInfo(defaultValues.n1, "N1.0"),
            13 to SensorInfo(defaultValues.n25, "N2.5"),
            14 to SensorInfo(defaultValues.n5, "N5.0"),
            15 to SensorInfo(defaultValues.n10, "N10")*/
        )
    }



    // Function to update sensor data
    fun updateSensorData(updateFunction: (SensorData) -> Unit) {
        viewModelScope.launch {
            val newData = _sensorData.value.copy()
            updateFunction(newData)
            _sensorData.emit(newData)
        }
    }

    // Function to update sensor info map
    fun updateSensorInfoMap(updatedMap: MutableMap<Int, SensorInfo>) {
        viewModelScope.launch {
            _sensorInfoMap.emit(updatedMap)
        }
    }

    fun updateLocationSensorData(latitude: Float, longitude: Float) {
        updateSensorData {
                sensorData ->
                sensorData.latitude = latitude
                sensorData.longitude = longitude
        }
    }
    fun getLocationData(): String {
        return _sensorData.value.locationData
    }
    fun getLatitudeData(): Float {
        return _sensorData.value.latitude
    }
    fun getLongitudeData(): Float {
        return _sensorData.value.longitude
    }

    fun updateDeviceName(newName: String) {
        updateSensorData {
                sensorData ->
            sensorData.deviceName = newName
        }
    }

    fun getDeviceName() : String {
        return _sensorData.value.deviceName
    }

    fun updateConnectionState(newConnectionState: Int) {
        Log.d(TAG, "updateConnectionState $newConnectionState")
        updateSensorData {
                sensorData ->
                sensorData.connectionState = newConnectionState
        }

        viewModelScope.launch {
            _connectionState.value = newConnectionState
        }
    }

    fun getConnectionState() : Int {
        return _sensorData.value.connectionState
    }

    fun updateConnectionTime(newConnectionTime: String) {
        updateSensorData {
                sensorData ->
            sensorData.connectionTime = newConnectionTime
        }
    }

    fun updateBattery(newBat: String) {
        updateSensorData {
                sensorData ->
            sensorData.battery = newBat
        }
    }

    fun getConnectionTime() : String {
        return _sensorData.value.connectionTime
    }

    fun getBattery() : String {
        return _sensorData.value.battery
    }

    fun printSensorDataMap() {
        val map = _sensorInfoMap.value
        for ((index, sensorInfo) in map) {
            println("Index: $index, Name: ${sensorInfo.name}, Value: ${sensorInfo.value}")
        }
    }

    fun getMeasureUnit(index: Int): String {
        val unit = when (index) {
            in 0..0 -> "°C"
            in 1..1 -> "%"
            in 2..2 -> "hPa"
            in 3..3 -> "m"
            in 4..4 -> "ppm"
            in 5..5 -> "ug/m3"
            in 6..6 -> "ug/m3"
            in 7..9 -> "ug/m3"
            else -> "#/100cc"
        }
        return unit
    }

    fun updateValueForIndex(index: Int, newValue: Float) {
        val updatedMap = _sensorInfoMap.value
        updatedMap[index]?.value = newValue
        Log.d(MeasureTAG, "$index $updatedMap[index]?.value $newValue")
        updateSensorInfoMap(updatedMap)
    }

    fun getValueForIndex(index: Int) : Float {
        val updatedMap = _sensorInfoMap.value
        return updatedMap[index]?.value ?: 0f
    }


    fun getMinMax(index: Int): Pair<Double, Double> {
        return when (index) {
            0 -> Pair(-15.0, 57.0) // Temperature min and max
            1 -> Pair(0.0, 100.0) // Humidity min and max
            2 -> Pair(950.0, 1047.25) // Pressure min and max
            3 -> Pair(-420.0, 8848.0) // Altitude min and max
            4 -> Pair(400.0, 2100.0) // CO2 min and max
            5 -> Pair(0.0, 250.0) // PM1.0 min and max
            6 -> Pair(0.0, 250.0) // PM2.5 min and max
            7 -> Pair(0.0, 430.0) // PM10 min and max
            else -> Pair(0.0, 450.0) // AIQ
        }
    }

    data class FourTuple(
        val first: String,
        val second: String,
        val third: String,
        val fourth: String
    )

    fun getRangeMeaning(index: Int): FourTuple {
        return when (index) {
            0 -> FourTuple("Low", "Medium", "High", "V. High") // Temperature min and max
            1 -> FourTuple("Low", "Medium", "High", "V. High") // Humidity min and max
            2 -> FourTuple("Low", "Medium", "High", "V. High") // Pressure min and max
            3 -> FourTuple("Low", "Medium", "High", "V. High") // Altitude min and max
            4 -> FourTuple("Good", "Satisfactory", "Poor", "V. Poor") // CO2 min and max
            5 -> FourTuple("Good", "Satisfactory", "Poor", "V. Poor") // PM1.0 min and max
            6 -> FourTuple("Good", "Satisfactory", "Poor", "V. Poor") // PM2.5 min and max
            7 -> FourTuple("Good", "Satisfactory", "Poor", "V. Poor") // PM10 min and max
            else -> FourTuple("Good", "Satisfactory", "Poor", "V. Poor") // AIQ
        }
    }

    fun getRangeHealth(index: Int): FourTuple {
        return when (index) {
            0 -> FourTuple("Hypothermia", "", "Dehydration", "Sunburn, H. Exhaustion") // Temperature min and max
            1 -> FourTuple("Dry skin", "", "Mold, Allergies", "Asthma") // Humidity min and max
            2 -> FourTuple("Dizziness, Nausea", "Weather-Related Effects", "Sinus and Ear Discomfort", "Decompression Sickness") // Pressure min and max
            3 -> FourTuple("", "", "Reduced Oxygen Saturation", "High-Altitude Pulmonary Edema") // Altitude min and max
            4 -> FourTuple("Hyperventilation", "", "Shortness of Breath", "Hypercapnia") // CO2 min and max
            5 -> FourTuple("", "Respiratory Irritation", "Respiratory Discomfort", "Respiratory Infection") // PM1.0 min and max
            6 -> FourTuple("", "Respiratory Irritation", "Respiratory Discomfort", "Respiratory Infection") // PM2.5 min and max
            7 -> FourTuple("", "Respiratory Irritation", "Respiratory Discomfort", "Respiratory Infection") // PM10 min and max
            else -> FourTuple("", "Minor Breathing Discomfort", "Heart Disease", "Serious impact on Lungs/Heart") // AIQ
        }
    }

    fun getNameForIndex(index: Int) : String {
        val updatedMap = _sensorInfoMap.value
        return updatedMap[index]?.name ?: ""
    }

    private var isPeriodicAPICallsRunning = false
    private var timerJob: Job? = null
    private val retroService = OkHttpClientService<Any?>()

    fun startPeriodicAPICalls(enteredIp: String) {
        Log.d(APIService, "enteredIp (startPeriodicAPI) " + enteredIp)
        if (!isPeriodicAPICallsRunning && enteredIp.isNotEmpty()) {
            isPeriodicAPICallsRunning = true
            timerJob = viewModelScope.launch {
                while (isPeriodicAPICallsRunning) {
                    Log.d(APIService, "Periodic call ${getConnectionState()}")
                    if (getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
                        val sensorDataJson = SensorDataJson(
                            clientId = "clientIdMa" + "-" + DeviceIdentifier.deviceId,
                            timestamp = System.currentTimeMillis(),
                            location = parseLocationToApi(),
                            data = parseDataToApi())
                        Log.d(APIService, sensorDataJson.toString())
                        try {
                            // Call the sendDataToServer function
                            val response = withContext(Dispatchers.IO) {
                                Log.d(APIService, "In call " + enteredIp)
                                retroService.sendDataToServer(sensorDataJson, enteredIp)
                            }
                            // Handle the response if needed
                        } catch (e: Exception) {
                            // Handle exceptions
                            Log.d(APIService, "EXCETPTIE $e")
                        }
                    }
                    // Periodic Get for Map and Graph - Commented for now until I get relevant responses from API
                    try {
                        val measurementResponse = withContext(Dispatchers.IO) {
                            retroService.getDataFromServer(this@SensorDataViewModel)
                        }
                        Log.d(APIService, "Received data: $measurementResponse")
                    } catch (e: Exception) {
                        Log.d(APIService, "Exception on resp: $e")
                    }
                    delay(15000) // 15 seconds delay
                }
            }
        }
    }

    private fun parseDataToApi(): List<SensorReadingJson> {
        return sensorInfoMap.value.map { (key, value) ->
            SensorReadingJson(
                value.name,
                value.value
            )
        }
    }

    private fun parseLocationToApi(): LocationReadingJson {
        return LocationReadingJson(LocationData.latitude, LocationData.longitude)
    }

    fun stopPeriodicAPICalls() {
        Log.d(DatasetTag, "Periodic call stop!")
        isPeriodicAPICallsRunning = false
        timerJob?.cancel()
    }

    data class Tuple(val x: Double, val y: Double, var z: Double)

    val listOfLists: MutableList<MutableList<Tuple>> = MutableList(9) { mutableListOf<Tuple>() }
    private val _listOfListsIndex = MutableStateFlow(0)

    private val lock = Any()

    fun isListEmpty(): Boolean {
        val allEmpty = this.listOfLists.all { it.isEmpty() }
        return allEmpty
    }


    // Function to get a list by index
    fun getList(index: Int): List<Tuple>? {
        return if (index in 0 until listOfLists.size) {
            listOfLists[index]
        } else {
            null
        }
    }

// val notification = MyNotification("aaa", "bbb")
    // NotificationViewModelProvider.getInstance().addNotification(notification)

    fun averageOfZValuesAtIndex(index: Int): Double {
        // Check if the index is within the bounds of the listOfLists
        if (index < 0 || index >= listOfLists.size) {
            throw IndexOutOfBoundsException("Index $index is out of bounds for the list of lists")
        }

        val zValues = listOfLists[index].map { it.z }
        if(zValues.sum() / zValues.size < getValueForIndex(index))
        {
            val notification = MyNotification(UUID.randomUUID().toString(),"0" + railItems[index].title, "Value ${getValueForIndex(index)} higher than average ${String.format("%.1f", zValues.sum() / zValues.size)}! Avoid outdoor activities!")
            NotificationViewModelProvider.getInstance().addNotification(notification)
        }
        else if (getValueForIndex(index).toInt() != 0)
        {
            val notification = MyNotification(UUID.randomUUID().toString(),"1" + railItems[index].title, "${railItems[index].title} is lower than usual average ${String.format("%.1f", zValues.sum() / zValues.size)}.")
            NotificationViewModelProvider.getInstance().addNotification(notification)
        }
        return if (zValues.isNotEmpty()) {
            zValues.sum() / zValues.size
        } else {
            0.0
        }
    }

    // Function to add a tuple to a specific list
    fun addTupleToList(index: Int, tuple: Tuple) {
        if (index in 0 until listOfLists.size) {
            synchronized(lock) {
                listOfLists[index].add(tuple)
            }
        }
    }

    fun updateTupleToList(index: Int, newTuple: Tuple) {
        if (index in 0 until listOfLists.size) {
            synchronized(lock) {
                listOfLists[index].forEach { tuple ->
                    // Check if x and y match the tuple's x and y values
                    if (tuple.x == newTuple.x && tuple.y == newTuple.y) {
                        // Update z if match is found
                        tuple.z = newTuple.z
                    }
                }
            }
        }
    }


    fun avgNestedList() {
            for (i in 0..8)
            {
                val avg = averageOfZValuesAtIndex(i)
                val pair: Pair<Long, Long> = Pair(avg.toLong(), System.currentTimeMillis())
                if(pair.first != 0L)
                {
                    railItems[i].lineGraphPoints += pair
                }
                Log.d(APIService, "Avg $avg, Index $i")
            }
    }


    fun replaceList(index: Int, newList: MutableList<Tuple>) {
        if (index in 0 until listOfLists.size) {
            listOfLists[index] = newList
        }
    }

    fun convertTuplesToGeoJson(mapViewModel: MapViewModel): FeatureCollection? {

        val features = getList(mapViewModel.selectedIndex)?.map { tuple ->
            val point = Point.fromLngLat(tuple.y, tuple.x)
            Feature.fromGeometry(point).also { feature ->
                feature.addNumberProperty("intensity", tuple.z)
            }
        }
        return features?.let { FeatureCollection.fromFeatures(it) }
    }

    fun updateHistory(historyValues: List<Pair<Int, Int>>)
    {

        val dao = DatabaseProvider.getHistoryDao()

        historyValues.forEach { (value, index) ->
            val sensorType = railItems.getOrNull(index)?.title ?: return@forEach

            val timestamp = System.currentTimeMillis()
            val historyPoint = HistoryPoint(
                sensorType = sensorType,
                value = value.toLong(),
                timestamp = timestamp
            )

            // Insert into DB on IO thread
            CoroutineScope(Dispatchers.IO).launch {
                dao.insertHistoryPoint(historyPoint)
                Log.d("DatabaseProvider", "Inserted HistoryPoint: sensorType=$sensorType, value=$value, timestamp=$timestamp")
            }

            // Also update in-memory list
            val pair = Pair(value.toLong(), timestamp)
            railItems[index].historyPoints += pair

            Log.d("DatabaseProvider", "Updated in-memory historyPoints for $sensorType: ${railItems[index].historyPoints}")
        }

    }
}