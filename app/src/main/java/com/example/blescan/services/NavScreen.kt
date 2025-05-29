package com.example.blescan.services

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.WindPower
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blescan.bluetooth.ScanScreen
import com.example.blescan.bluetooth.BluetoothSampleBox
import com.example.blescan.bluetooth.GATTClient
import com.example.blescan.data.AirQuality
import com.example.blescan.bluetooth.ProfileScreen
import com.example.blescan.data.SensorDataViewModel
import com.example.blescan.alerts.AlertScreen
import com.example.blescan.data.MeasureScreen
import com.example.blescan.map.MapScreen


data class BottomNavigationItem(
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)

val items = listOf(
    BottomNavigationItem(
        title = "Device",
        selectedIcon = Icons.Filled.DeveloperMode,
        unselectedIcon = Icons.Outlined.DeveloperMode,
    ),
    BottomNavigationItem(
        title = "Air Quality",
        selectedIcon = Icons.Filled.WindPower,
        unselectedIcon = Icons.Outlined.WindPower,
    ),
    BottomNavigationItem(
        title = "Statistics",
        selectedIcon = Icons.Filled.PieChart,
        unselectedIcon = Icons.Outlined.PieChart,
    ),
    BottomNavigationItem(
        title = "Map",
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
    ),
    BottomNavigationItem(
        title = "Alerts",
        selectedIcon = Icons.Filled.NotificationsActive,
        unselectedIcon = Icons.Outlined.NotificationsActive,
    ),
)

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("InlinedApi")
@RequiresPermission(
    anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun NavScreen(navController: NavHostController, navBackStackEntry: NavBackStackEntry?, initialState: Int, sensorDataViewModel: SensorDataViewModel, connectivityViewModel: ConnectivityViewModel) {
    // Bottom Navigation Bar
    var selectedItem by remember { mutableIntStateOf(initialState) }

    val connectionState by sensorDataViewModel.connectionState.collectAsState()


    LaunchedEffect(connectionState) {
        Log.d("NavBackStack", "UPDATEEE $connectionState")
        when (connectionState) {
            BluetoothGatt.STATE_CONNECTED ->
            {
                navController.navigate("profile")
                {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.findStartDestination().id)
                        {
                            saveState = true
                        }
                    }
            }
            BluetoothGatt.STATE_DISCONNECTED ->
            {
                navController.navigate("device")
                {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
            }
            else ->
            {
                navController.navigate("device")
                {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }

            }
            // Add other states if needed
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom

    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .shadow(elevation = 40.dp)
                        .padding(0.dp, 16.dp, 0.dp, 0.dp),
                    containerColor = White,
                    contentColor = White,
                    tonalElevation = 40.dp
                ) {
                    items.forEach { item ->
                        val isSelected = item.title.lowercase() ==
                                navBackStackEntry?.destination?.route
                        NavigationBarItem(
                            selected = isSelected,
                            label = {
                                androidx.compose.material3.Text(
                                    text = item.title,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = if(isSelected) {
                                        item.selectedIcon
                                    } else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            onClick = {
                                    navController.navigate(item.title.lowercase()) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                    }

                                selectedItem = items.indexOf(item)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = White
                            )
                        )
                    }
                }

            }
        ) { padding ->
            NavHost(navController = navController, startDestination = "device") {
                Log.d("NavBackStack", "Destination: ${navController.currentBackStack.value}")
                    composable("device") {
                        Log.d(MainTAG, sensorDataViewModel.getConnectionState().toString())
                            if(connectionState != BluetoothGatt.STATE_CONNECTED) {
                                Log.d(MainTAG, "Not connected")
                                ScanScreen(
                                    navController = navController
                                )
                            }
                            else
                            {
                                ProfileScreen(navController = navController, sensorDataViewModel = sensorDataViewModel )
                            }

                        }
                    composable("connect") {
                        GATTClient.ConnectGATT(navController,sensorDataViewModel = sensorDataViewModel, connectivityViewModel = connectivityViewModel)
                    }
                    composable("profile")
                    {
                        ProfileScreen(navController = navController, sensorDataViewModel = sensorDataViewModel )
                    }

                composable("alerts")
                {
                    AlertScreen()
                }
                composable("map")
                {
                    Log.d("TAG", "MAP")
                    BluetoothSampleBox (connectivityViewModel = connectivityViewModel) {
                        MapScreen(sensorDataViewModel)
                    }
                    //startActivity(LocalContext.current, navIntent, null)
                }
                composable("air quality")
                {
                    MeasureScreen(navController, sensorDataViewModel = sensorDataViewModel, connectivityViewModel = connectivityViewModel)
                }
                composable("statistics")
                {
                    AirQuality(sensorDataViewModel = sensorDataViewModel)
                }
            }
            Log.d(MainTAG, "****** ${connectivityViewModel.connectivityStatus.collectAsState().value}")
        }
    }
}