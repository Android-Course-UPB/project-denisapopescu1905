package com.example.blescan

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.blescan.bluetooth.GATTClient.characteristicsUUIDs
import com.example.blescan.bluetooth.GATTClient.getCurrentTime
import com.example.blescan.data.SensorDataViewModel

import com.example.blescan.data.railItems
import com.example.blescan.map.distanceBetweenPoints
import com.example.blescan.map.totalDistanceTraveled
import com.example.blescan.services.*
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.Calendar
import java.util.UUID

class FunTest {


    @Test
    fun `sendDataToServer should return success response`() {
        val service = OkHttpClientService<Any?>()
        val requestData = SensorDataJson(
            clientId = "123",
            timestamp = System.currentTimeMillis(),
            location = LocationReadingJson(0.0f, 0.0f),
            data = listOf(SensorReadingJson("temperature", 25.0f))
        )

        val response = runBlocking {
            service.sendDataToServer(requestData, "127.0.0.1")
        }
        print(response)

        assertEquals(200, response.code())
        return
    }

    @Test
    fun `getCurrentTime returns formatted time string`() {
        val fixedTimeInMillis = 1719475379000
        val calendar = Calendar.getInstance().apply {
            timeInMillis = fixedTimeInMillis
        }

        val currentTime = getCurrentTime()
        assertTrue(currentTime.startsWith("Tue May 28"))
    }

    private val originalSize = characteristicsUUIDs.size

    @Test
    fun testRailItemsCount() {
        val expectedCount = 8
        val actualCount = railItems.size
        assertEquals(expectedCount, actualCount)
    }
}


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ConnectivityViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var networkObserver: NetworkConnectivityObserver

    @Mock
    lateinit var sensorDataViewModel: SensorDataViewModel

    private lateinit var connectivityViewModel: ConnectivityViewModel

    private val connectivityFlow = MutableSharedFlow<ConnectivityObserver.Status>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Stub observer flow
        `when`(networkObserver.observe()).thenReturn(connectivityFlow)

        connectivityViewModel = ConnectivityViewModel(
            context = context,
            networkObserver = networkObserver,
            sensorDataViewModel = sensorDataViewModel,
            enteredIp = "192.168.0.1"
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits connectivity status and triggers API call`() = runTest {
        connectivityFlow.emit(ConnectivityObserver.Status.Available)

        Assert.assertEquals(
            ConnectivityObserver.Status.Available,
            connectivityViewModel.connectivityStatus.value
        )

        verify(sensorDataViewModel).startPeriodicAPICalls("192.168.0.1")
    }

    @Test
    fun `stops API call on lost connectivity`() = runTest {
        connectivityFlow.emit(ConnectivityObserver.Status.Lost)

        Assert.assertEquals(
            ConnectivityObserver.Status.Lost,
            connectivityViewModel.connectivityStatus.value
        )

        verify(sensorDataViewModel).stopPeriodicAPICalls()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    class SensorDataViewModelTest {

        private lateinit var viewModel: SensorDataViewModel

        private val testDispatcher = StandardTestDispatcher()

        @Before
        fun setup() {
            Dispatchers.setMain(testDispatcher)
            viewModel = SensorDataViewModel()
        }

        @After
        fun tearDown() {
            Dispatchers.resetMain()
        }

        @Test
        fun `updateSensorData should change temperature`() = runTest {
            val newTemperature = 36.5f
            viewModel.updateSensorData { it.temperature = newTemperature }

            testDispatcher.scheduler.advanceUntilIdle()
            val updated = viewModel.sensorData.value

            assertEquals(newTemperature, updated.temperature)
        }

        @Test
        fun `updateDeviceName should correctly set device name`() = runTest {
            val name = "MyTestDevice"
            viewModel.updateDeviceName(name)

            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(name, viewModel.getDeviceName())
        }

        @Test
        fun `updateConnectionState should correctly emit new value`() = runTest {
            val state = 2
            viewModel.updateConnectionState(state)

            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(state, viewModel.connectionState.value)
        }

        @Test
        fun `getValueForIndex returns correct default value`() {
            val value = viewModel.getValueForIndex(0) // Temperature
            assertEquals(0.0f, value)
        }

        @Test
        fun `updateValueForIndex updates sensorInfoMap value`() = runTest {
            val index = 1
            val value = 85.0f

            viewModel.updateValueForIndex(index, value)

            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(value, viewModel.getValueForIndex(index))
        }

        @Test
        fun `getMinMax returns expected range for pressure`() {
            val minMax = viewModel.getMinMax(2)
            assertEquals(950.0, minMax.first, 0.001)
            assertEquals(1047.25, minMax.second, 0.001)
        }

        @Test
        fun `getRangeMeaning returns correct labels for PM25`() {
            val meanings = viewModel.getRangeMeaning(6)
            assertEquals("Good", meanings.first)
            assertEquals("Satisfactory", meanings.second)
            assertEquals("Poor", meanings.third)
            assertEquals("V. Poor", meanings.fourth)
        }
    }
}