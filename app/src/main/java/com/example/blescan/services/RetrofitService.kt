package com.example.blescan.services

import android.util.Log
import com.example.blescan.data.SensorDataViewModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.UUID

const val APIService = "APIService"

data class RawMeasurementResponse(
    val data: List<List<Any>>
)

data class MeasurementEntry(
    val clientId: String,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val temp: Double,
    val hum: Double,
    val press: Double,
    val alt: Double,
    val co2: Double,
    val pm1: Double,
    val pm2_5: Double,
    val pm10: Double
)

fun List<List<Any>>.toMeasurementEntries(): List<MeasurementEntry> {
    return this.map { item ->
        MeasurementEntry(
            clientId = item[0] as String,
            timestamp = item[1] as String,
            latitude = (item[2] as Number).toDouble(),
            longitude = (item[3] as Number).toDouble(),
            temp = (item[4] as Number).toDouble(),
            hum = (item[5] as Number).toDouble(),
            press = (item[6] as Number).toDouble(),
            alt = (item[7] as Number).toDouble(),
            co2 = (item[8] as Number).toDouble(),
            pm1 = (item[9] as Number).toDouble(),
            pm2_5 = (item[10] as Number).toDouble(),
            pm10 = (item[11] as Number).toDouble()
        )
    }
}

data class Metadata(
    val runId: String,
    val dataTimestamp: Long
)

@JsonClass(generateAdapter = true)
data class SensorDataJson(
    @Json(name = "clientId") val clientId: String,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "location") val location: LocationReadingJson,
    @Json(name = "data") val data: List<SensorReadingJson>
)

@JsonClass(generateAdapter = true)
data class SensorDataJsonGet(
    @Json(name = "latitude") val latitude: Float,
    @Json(name = "longitude") val longitude: Float,
    @Json(name = "radius") val radius: Int,
    @Json(name = "layers") val layers: List<String>
)

@JsonClass(generateAdapter = true)
data class SensorReadingJson(
    @Json(name = "dimension") val dimension: String,
    @Json(name = "value") val value: Float
)

@JsonClass(generateAdapter = true)
data class LocationReadingJson(
    @Json(name = "latitude") val latitude: Float,
    @Json(name = "longitude") val longitude: Float
)

interface RetrofitMoshiApi {
    @POST("http://34.116.229.188:3000/ingest")  // Old IP, it is now configured by user at app startup
    suspend fun sendData(@Body requestData: SensorDataJson): Response<SensorDataJson>
}

class OkHttpClientService<ResponseBody : Any?> {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(false)  // Enable following redirects
        .followSslRedirects(false)  // Enable following SSL redirects
        .build()

    suspend fun sendDataToServer(requestData: SensorDataJson, enteredIp: String): Response<SensorDataJson> {
        return withContext(Dispatchers.IO) {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val jsonAdapter = moshi.adapter(SensorDataJson::class.java)
            val json = jsonAdapter.toJson(requestData)

            val requestBody = json.toRequestBody("application/json".toMediaType())

            //var url = "http://$enteredIp:3000/ingest" // Initial URL
            var url = "https://kong-fea27b0248eua7mms.kongcloud.dev/ingest/"
            Log.d(APIService, "enteredIp $enteredIp")
            Log.d(APIService, "url $url")

            var redirectCount = 0
            var response: okhttp3.Response

            do {
                val request = Request.Builder()
                    //.addHeader("X-Auth-Token", "aaaaa")
                    .url(url)
                    .post(requestBody)
                    .build()


                Log.d("APIService", "Sending request to URL: $url")
                Log.d("APIService", "Request Method: ${request.method}")
                Log.d("APIService", "Request Headers:")
                request.headers.forEach { header ->
                    Log.d("APIService", "${header.first}: ${header.second}")
                }

                Log.d(APIService, "Request: $requestBody, $request $requestData.")
                for ((name, value) in request.headers) {
                    Log.d(APIService, "Header: $name = $value")
                }
                response = client.newCall(request).execute()

                if (response.isSuccessful || response.code != 302) {
                    break // If the request was successful or not a redirect, break the loop
                }

                val newLocation = response.header("Location")
                Log.d(APIService,"Location: " + newLocation.toString())
                if (newLocation != null) {
                    url = newLocation // Update URL with the new location
                    redirectCount++
                } else {
                    throw IllegalStateException("Redirect without location header")
                }
            } while (redirectCount < 100) // Maximum number of redirects to follow

            val responseBody = response.body?.string() ?: ""
            Log.d(APIService,"$response $redirectCount")

            if (response.isSuccessful) {
                Response.success(jsonAdapter.fromJson(responseBody))
            } else {
                if(redirectCount == 0) {
                    Response.error(
                        response.code,
                        responseBody.toResponseBody("application/json".toMediaType())
                    )
                }
                else {
                    Response.success(jsonAdapter.fromJson(responseBody))
                }
            }
        }
    }

    suspend fun getDataFromServer(sensorDataViewModel: SensorDataViewModel) {
        return withContext(Dispatchers.IO) {
            //var url = "http://$enteredIp:3000/ingest"//"https://spm-output-api-fake.preda.dev/measurements?location=44.435022,26.046144&radius=2000&layers=TEMP,HUM,PRESS,ALT,CO2,PM1,PM2.5,PM10,AQ"
            var url = "https://kong-fea27b0248eua7mms.kongcloud.dev/results/"

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val jsonAdapter = moshi.adapter(SensorDataJsonGet::class.java)


            val bodyObject = SensorDataJsonGet(
                latitude = sensorDataViewModel.getLatitudeData(),
                longitude = sensorDataViewModel.getLongitudeData(),
                radius = 1000,
                layers = listOf("Temperature", "Humidity", "Pressure", "Altitude", "CO2", "PM1_0", "PM2_5", "PM10")
            )

            val json = jsonAdapter.toJson(bodyObject)
            Log.d("APIService", "Actual JSON string being sent: $json")

            val requestBody = json.toRequestBody("application/json".toMediaType())

            var redirectCount = 0
            var response: okhttp3.Response
            val measurementResponseAdapter = moshi.adapter(RawMeasurementResponse::class.java)
            Log.d("APIService", "Req Body: $requestBody")

            do {
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
                Log.d("APIService", "Sending GET(POST) request to URL: $url")
                Log.d("APIService", "Request Method: ${request.method}")
                Log.d("APIService", "Request Body: ${request.body}")
                Log.d("APIService", "Request Headers:")
                request.headers.forEach { header ->
                    Log.d("APIService", "${header.first}: ${header.second}")
                }
                Log.d(APIService, request.toString())
                println(request.toString())

                response = client.newCall(request).execute()

                if (response.isSuccessful || response.code != 302) {
                    Log.d(APIService, response.toString())
                    break // If the request was successful or not a redirect, break the loop
                }

                val newLocation = response.header("Location")
                Log.d(APIService,newLocation.toString())
                if (newLocation != null) {
                    url = newLocation // Update URL with the new location
                    redirectCount++
                } else {
                    throw IllegalStateException("Redirect without location header")
                }
            } while (redirectCount < 100) // Maximum number of redirects to follow
            if (response.isSuccessful) {
                response.body?.string()?.let { responseBody ->
                    Log.d("APIService", "Raw response body: $responseBody")
                    try {
                        val rawResponse = measurementResponseAdapter.fromJson(responseBody)
                        val entries = rawResponse?.data?.toMeasurementEntries()
                        Log.d("APIService", "Parsed entries: $entries")
                        parseResponse(entries, sensorDataViewModel)
                    } catch (e: Exception) {
                        Log.e("APIService", "Failed to parse response", e)
                    }
                }
            }
        }
    }
    fun parseResponse(resp: List<MeasurementEntry>?, sensorDataViewModel: SensorDataViewModel) {
        if (resp == null) return

        for (entry in resp) {
            try {
                val coords = listOf(entry.latitude, entry.longitude)
                val values = listOf(
                    entry.temp, entry.hum, entry.press, entry.alt,
                    entry.co2, entry.pm1, entry.pm2_5, entry.pm10
                )

                if (sensorDataViewModel.isListEmpty()) {
                    createTuples(coords, values, sensorDataViewModel)
                } else {
                    updateTuples(coords, values, sensorDataViewModel)
                }
            } catch (e: Exception) {
                Log.e("ParseResponse", "Skipping malformed entry: $entry", e)
            }
        }

        if (!sensorDataViewModel.isListEmpty()) {
            sensorDataViewModel.avgNestedList()
        }
    }



    fun convertToDoubleList(list: List<Any>): List<Double> {
        val result = mutableListOf<Double>()
        for (item in list) {
            val doubleValue = when (item) {
                is Double -> item
                is String -> item.toDoubleOrNull()
                else -> null
            }
            if (doubleValue != null) {
                result.add(doubleValue)
            }
        }
        return result
    }


    fun createTuples(
        coordList: List<Double>,
        valueList: List<Double>,
        sensorDataViewModel: SensorDataViewModel
    ) {
        for ((index, value) in valueList.withIndex()) {
            val tuple = SensorDataViewModel.Tuple(coordList[0], coordList[1], value)
            sensorDataViewModel.addTupleToList(index, tuple)
        }
    }

    fun updateTuples(
        coordList: List<Double>,
        valueList: List<Double>,
        sensorDataViewModel: SensorDataViewModel
    ) {
        for ((index, value) in valueList.withIndex()) {
            val tuple = SensorDataViewModel.Tuple(coordList[0], coordList[1], value)
            sensorDataViewModel.updateTupleToList(index, tuple)
        }
    }

}
