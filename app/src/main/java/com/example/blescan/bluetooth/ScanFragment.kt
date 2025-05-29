package com.example.blescan.bluetooth

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.blescan.R
import com.example.blescan.ui.theme.MainBlueTheme
import kotlinx.coroutines.delay
var gradientBoxOffset = 0.dp


@Composable
fun ScanButton(onClick: () -> Unit, scanning: Boolean, navController: NavHostController) {
    var devicesFound by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(20.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        FloatingActionButton(
            onClick = { onClick() },
            modifier = Modifier
                .size(90.dp)
                .padding(0.dp)
                .border(
                    width = 3.dp,
                    color = MainBlueTheme,
                    shape = RoundedCornerShape(15.dp)
                )
                .background(color = Color.White, shape = RoundedCornerShape(15.dp)),
            containerColor = Color.White,
            contentColor = Color.White
        ) {
            if (scanning) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp),
                    color = MainBlueTheme
                )
                LaunchedEffect(true) {
                    // Simulate scanning for 5 seconds
                    delay(5000)
                    gradientBoxOffset += 10.dp
                    // After 2 seconds, set devicesFound to true
                    //navController.navigate("connect")
                    devicesFound = true
                }
                if (devicesFound) {
                    // Use GATT functions to get the list of devices
                    navController.navigate("connect")
                    {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = MainBlueTheme
                    )
                }
            } else {
                BluetoothIcon(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun GenericIcon(
    color: Color,
    resourceId: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = resourceId),
        contentDescription = null, // Add content description if needed
        modifier = modifier.size(size),
        tint = color
    )
}

@Composable
fun BluetoothIcon(
    modifier: Modifier = Modifier
        .background(Color.White)
) {
    Image(
        modifier = modifier
            .size(1.dp) // Adjust the size as needed
            .padding(19.dp), // Adjust the padding as needed
        painter = painterResource(id = R.drawable.bluetooth_svgrepo_com),
        contentDescription = null,
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ScanScreen(navController: NavHostController) {
    var scan by remember { mutableStateOf(false) }



    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.White)
    ) {
        // Calculate heights based on screen size
        val maxHeight = 830.dp
        val topHeight: Dp = maxHeight * 2 / 3

        // Draw the gradient at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topHeight)
                .shadow(
                    elevation = 6.dp,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(bottomStart = 75.dp, bottomEnd = 75.dp)
                )
        ) {
            Image(
                painter = painterResource(R.drawable.gradient_drawable),
                contentDescription = "",
                modifier = Modifier.fillMaxSize().height(topHeight)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = "Scan Now!",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                ScanButton(onClick = {
                    scan = !scan }, scanning = scan, navController = navController)
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ConnectedScreen(navController: NavHostController) {
    var scan by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.White)
    ) {
        // Calculate heights based on screen size
        val maxHeight = 2830.dp
        val topHeight: Dp = maxHeight * 2 / 3

        // Draw the gradient at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topHeight)
                .shadow(
                    elevation = 6.dp,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(topStart = 75.dp, topEnd = 75.dp)
                )
        ) {
            Image(
                painter = painterResource(R.drawable.gradient_drawable),
                contentDescription = "",
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = "You are connected",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                ScanButton(onClick = {
                    scan = !scan
                }, scanning = scan, navController = navController)
            }
        }
    }
}


