package com.example.blescan.data

import android.bluetooth.BluetoothGatt
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.blescan.services.ConnectivityObserver
import com.example.blescan.services.ConnectivityViewModel
import com.example.blescan.services.TTSManager
import com.example.blescan.ui.theme.MainBlueTheme
import com.google.common.io.Files.append
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

const val MeasureTAG = "MeasureTAG"



@Composable
fun MeasureScreen(navController: NavHostController, sensorDataViewModel: SensorDataViewModel, connectivityViewModel: ConnectivityViewModel)
{
    var isReading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var sensorString by remember { mutableStateOf("Sensor Data: ") }

    val sensorInfoMap by sensorDataViewModel.sensorInfoMap.collectAsState()
    val mapHash = sensorInfoMap.values.joinToString { it.toString() }.hashCode()

    LaunchedEffect(isReading) {
        if(isReading) {
            if(sensorDataViewModel.getConnectionState() != BluetoothGatt.STATE_CONNECTED)
            {
                TTSManager.speak("No Device Connected")
            }
            else {
                sensorInfoMap.forEach { (key, sensorInfo) ->
                    sensorString += "${sensorInfo.name}: ${sensorInfo.value} ${
                        sensorDataViewModel.getMeasureUnit(
                            key
                        )
                    }\n"
                }
                TTSManager.speak(sensorString)
            }
        }

    }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("%.2f°N",sensorDataViewModel.getLatitudeData()) + " " + String.format("%.2f°E",sensorDataViewModel.getLongitudeData()),
                            color = Color.Unspecified,
                            textAlign = TextAlign.Center,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Wifi, // Replace with your icon resource
                                contentDescription = null, // Provide a content description if needed
                                tint = if(connectivityViewModel.connectivityStatus.value != ConnectivityObserver.Status.Available) Color.Red else Color.Green,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            Icon(
                                imageVector = Icons.Outlined.DeveloperMode, // Replace with your icon resource
                                contentDescription = null, // Provide a content description if needed
                                tint = if(sensorDataViewModel.getConnectionState() != BluetoothGatt.STATE_CONNECTED) Color.Red else Color.Green,
                                modifier = Modifier
                                    .size(24.dp)

                                    .clickable {
                                        if (sensorDataViewModel.getConnectionState() != BluetoothGatt.STATE_CONNECTED) {
                                            navController.navigate("device")
                                            {
                                                launchSingleTop = true
                                                restoreState = true
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                            }
                                        } else {
                                            navController.navigate("profile")
                                            {
                                                launchSingleTop = true
                                                restoreState = true
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()) // Enable horizontal scrolling
                            .padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = sensorDataViewModel.getDeviceName(),
                            color = Color.Unspecified,
                            textAlign = TextAlign.Left,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1, // Ensure the text is only one row
                            overflow = TextOverflow.Clip, // Clip the text, since it will be scrollable
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                val items = (0..2).toList()
                itemsIndexed(items) { index, _ ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        if(index == 0) {
                            MeasureElement(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxWidth(), index * 3, sensorDataViewModel
                            )
                            MeasureElement(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxWidth(), index * 3 + 1, sensorDataViewModel
                            )
                        }
                        else
                        {
                            MeasureElement(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxWidth(), index * 3 - 1, sensorDataViewModel
                            )
                            MeasureElement(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxWidth(), index * 3, sensorDataViewModel
                            )
                            MeasureElement(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxWidth(), index * 3 + 1, sensorDataViewModel
                            )
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(bottom = 10.dp, top = 20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SpatialAudioOff,
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp),
                            tint = MainBlueTheme
                        )
                        Button(
                            onClick = {
                                isReading = !isReading
                                if(!isReading)
                                {
                                    sensorString = "Sensor Data: "
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 2.dp) // Match padding of DISCONNECT button
                        ) {
                            Text("AUDIO")
                        }
                    }
                }
            }
        }
}


@Composable
fun MeasureElement(modifier: Modifier = Modifier, index: Int,  sensorDataViewModel: SensorDataViewModel) {
    val borderColor = MainBlueTheme

    //val sensorInfo = sensorDataViewModel.getSensorInfo(index)
    val sensorDataState by sensorDataViewModel.sensorData.collectAsState()
    var text by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var text3 by remember { mutableStateOf("") }
    text = sensorDataViewModel.getNameForIndex(index)
    text2 = sensorDataViewModel.getValueForIndex(index).toString()
    text3 = " " + sensorDataViewModel.getMeasureUnit(index).toString()
    val coroutineScope = rememberCoroutineScope()
    var recompositionTrigger by remember { mutableStateOf(true) }
    if(sensorDataViewModel.getConnectionState() == BluetoothGatt.STATE_CONNECTED) {
        LaunchedEffect(recompositionTrigger) {
            withContext(Dispatchers.IO) {
                // Launch a coroutine on a different thread
                val updateValue = sensorDataViewModel.getValueForIndex(index)
                    .toString()
                withContext(Dispatchers.Main) {
                    text2 = updateValue
                }

                Log.d(
                    MeasureTAG,
                    "Reexecution Triggered: Executed on thread: ${Thread.currentThread().name}"
                )

                delay(5000)
                recompositionTrigger = !recompositionTrigger
            }
        }
    }

    Box(
        modifier = modifier
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(25.dp)
            )
            .aspectRatio(1f)
    ) {
        FloatingActionButton(
            onClick = {
                val speakText = "$text: $text2 $text3"
                TTSManager.speak(speakText)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .border(
                    width = 4.dp,
                    color = borderColor,  // Adjust color as needed
                    shape = RoundedCornerShape(25.dp)
                )
                .shadow(
                    elevation = 6.dp,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(25.dp)
                )
                .clickable()
                {

                }
                .background(color = Color.White, shape = RoundedCornerShape(25.dp)),
            containerColor = Color.White,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                    Text(
                        text = text,
                        color = MainBlueTheme,
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                Row {
                    Text(
                        text = text2,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = text3,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = if(index!= 4) 15.sp else 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
