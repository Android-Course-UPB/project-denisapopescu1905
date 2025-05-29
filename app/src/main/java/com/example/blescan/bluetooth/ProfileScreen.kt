package com.example.blescan.bluetooth

import android.bluetooth.BluetoothGatt
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.blescan.ui.theme.MainBlueTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.blescan.R
import com.example.blescan.data.SensorDataViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController, sensorDataViewModel: SensorDataViewModel)
{

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

    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .paint(
                // Replace with your image id
                painterResource(id = R.drawable.gradient_drawable),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Box(
            modifier = Modifier
                .height(500.dp)
                .width(300.dp)
                .align(Alignment.Center)
                //.padding(80.dp)
                .background(color = Color.White, shape = RoundedCornerShape(15.dp))
                .border(5.dp, color = MainBlueTheme, RoundedCornerShape(15.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Icon(
                            imageVector = Icons.Filled.DeveloperMode, // Replace with your icon resource
                            contentDescription = null, // Provide a content description if needed
                            modifier = Modifier.size(24.dp), // Adjust the size as needed.
                            tint = if(sensorDataViewModel.getConnectionState() != BluetoothGatt.STATE_CONNECTED) Color.Red else Color.Green
                        )
                        androidx.compose.material3.Text(
                            text = sensorDataViewModel.getDeviceName(),
                            modifier = Modifier
                                .padding(horizontal = 8.dp) // Adjust the padding as needed
                                .align(Alignment.CenterVertically)
                                .weight(2f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Button(
                            onClick = {
                                navController.navigate("air quality")
                                {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 2.dp) // Match padding of DISCONNECT button
                                .align(Alignment.CenterVertically)
                        ) {
                            androidx.compose.material3.Text("MEASURE")
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 5.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        androidx.compose.material3.Text(
                            text = "Location: " + sensorDataViewModel.getLocationData(),
                            modifier = Modifier
                                .padding(horizontal = 8.dp) // Adjust the padding as needed
                                .align(Alignment.CenterVertically)
                                .weight(2f)
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        androidx.compose.material3.Text(
                            text = "Connection Time: \n" + sensorDataViewModel.getConnectionTime(),
                            modifier = Modifier
                                .padding(horizontal = 3.dp) // Adjust the padding as needed
                                .align(Alignment.CenterVertically)
                                .weight(2f)
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 2.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        androidx.compose.material3.Text(
                            text = "Battery: \n" + sensorDataViewModel.getBattery(),
                            modifier = Modifier
                                .padding(horizontal = 3.dp) // Adjust the padding as needed
                                .align(Alignment.CenterVertically)
                                .weight(2f)
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 2.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )
                    Button(
                        onClick = {
                            isLoading = true
                            sensorDataViewModel.updateConnectionState(0)
                            coroutineScope.launch {
                                delay(3000)
                                isLoading = false
                                Toast.makeText(context, "Wait 5s before trying to reconnect to device ${sensorDataViewModel.getDeviceName()}" , Toast.LENGTH_SHORT).show()
                                navController.navigate("device")
                                {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 60.dp, vertical=15.dp),
                        colors = ButtonDefaults.buttonColors(Color.Red)
                    ) {

                        androidx.compose.material3.Text("DISCONNECT")
                    }
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                }
            }
        }
    }
}