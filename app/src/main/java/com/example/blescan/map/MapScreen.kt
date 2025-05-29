package com.example.blescan.map

import LocationData
import MapTAG
import MapViewModel
import android.Manifest
import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.blescan.data.SensorDataViewModel
import com.example.blescan.data.dropdownItems
import com.example.blescan.services.LocationService
import com.example.blescan.ui.theme.MainBlueTheme
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.MapDebugOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.MapboxStandardStyle
import com.mapbox.maps.extension.compose.style.layers.generated.CircleColor
import com.mapbox.maps.extension.compose.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.compose.style.layers.generated.CircleOpacity
import com.mapbox.maps.extension.compose.style.layers.generated.CircleRadius
import com.mapbox.maps.extension.compose.style.layers.generated.Filter
import com.mapbox.maps.extension.compose.style.layers.generated.HeatmapColor
import com.mapbox.maps.extension.compose.style.layers.generated.HeatmapIntensity
import com.mapbox.maps.extension.compose.style.layers.generated.HeatmapLayer
import com.mapbox.maps.extension.compose.style.layers.generated.HeatmapOpacity
import com.mapbox.maps.extension.compose.style.layers.generated.HeatmapRadius
import com.mapbox.maps.extension.compose.style.layers.generated.HeatmapWeight
import com.mapbox.maps.extension.compose.style.layers.generated.LineCap
import com.mapbox.maps.extension.compose.style.layers.generated.LineGradient
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoin
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.layers.generated.LineWidth
import com.mapbox.maps.extension.compose.style.layers.generated.Visibility
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.LineMetrics
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.gt
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import locationChannel
import maxX
import maxY
import minX
import minY
import java.lang.Math.atan2
import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


fun generateRoute(longitude: Double, latitude: Double): LineString? {
    // Create a list of points representing the route
    val routePoints = mutableListOf<Point>()

    // Add the current location as the starting point of the route
    val startPoint = Point.fromLngLat(longitude, latitude)
    routePoints.add(startPoint)

    // Create a LineString from the list of points
    return LineString.fromLngLats(routePoints)
}

fun startLocationService(context: Context) {
    val intent = Intent(context, LocationService::class.java)
    context.startForegroundService(intent)
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1

@Composable
fun LocationComponent(content: @Composable () -> Unit)
{
    // Location service logic
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var hasLocationPermission by rememberSaveable { mutableStateOf(false) }
    val locationRequest = LocationRequest.create().apply {
        interval = 3000 // Update interval in milliseconds (e.g., 10 seconds)
        fastestInterval = 3000 // Fastest update interval in milliseconds (e.g., 5 seconds)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Request high accuracy location updates
    }

    fun checkLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    if (!hasLocationPermission) {
        if (checkLocationPermissions()) {
            hasLocationPermission = true
        } else {
            Log.d(MapTAG, "No LOCATION permissions. Requesting permissions.")
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    if (hasLocationPermission) {
        // Create a location callback to handle location updates
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    coroutineScope.launch {
                        // Handle location updates here
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // Update your map or perform other actions based on the new location
                        Log.d(MapTAG, "MapLocation: $latitude $longitude")
                        // updateLocation(longitude, latitude)
                        val locationPair: Pair<Double, Double> = Pair(latitude, longitude)
                        locationChannel.send(locationPair)
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    Log.d(MapTAG, "Location services are not available.")
                }
            }
        }


        // Start location updates
        LaunchedEffect(Unit) {
            try {
                startLocationService(context)
                locationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.d(MapTAG, "SecurityException: ${e.message}")
            } catch (e: Exception) {
                Log.d(MapTAG, "Exception: ${e.message}")
            }
        }

        // Stop location updates when the composable is disposed
        DisposableEffect(Unit) {
            onDispose {
                locationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}





fun updateLocation(longitude: Double, latitude: Double)
{
    LocationData.longitude = longitude.toFloat()
    LocationData.latitude = latitude.toFloat()
    Log.d(MapTAG,  "UpdateLocation " + LocationData.longitude.toString() + " " + LocationData.latitude.toString())
}



@Composable
fun ExposedDropdownMenuSample(mapViewModel: MapViewModel) {

        var expanded by remember { mutableStateOf(false) }
        var dropdownWidth by remember { mutableStateOf(0.dp) }

        FloatingActionButton(
            containerColor = Color.White,
            onClick = { expanded = !expanded },
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Locatebutton",
                tint = MainBlueTheme
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    dropdownWidth = 30.dp
                }
                .background(Color.White)
        ) {
            dropdownItems.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, color = if(mapViewModel.selectedIndex == index) MainBlueTheme else Color.Black) },
                    onClick = {
                        expanded = false
                        mapViewModel.selectedIndex = index
                    }
                )
            }
        }
}

@Composable
fun startRoute(mapViewModel: MapViewModel, isRoute: (Boolean) -> Unit)
{
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomStart,
    ) {

        Button(
            colors = ButtonDefaults.buttonColors(MainBlueTheme),
            onClick = {
                mapViewModel.toggleRoute()
                isRoute(mapViewModel.startedRoute)
            },
        ) {
            if(!mapViewModel.startedRoute)
            {
                Text(text = "START")
            }
            else
            {
                Text(text = "STOP")
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun NavigationStyle(sensorDataViewModel: SensorDataViewModel, routeLine: LineString?, mapViewModel: MapViewModel) {
    val geoJsonSource = rememberGeoJsonSourceState {
        lineMetrics = LineMetrics(true)
    }

    val interval: Pair<Double, Double> = sensorDataViewModel.getMinMax(mapViewModel.selectedIndex)
    val (start: Double, end: Double) = interval
    val firstQuarter: Double = start + (end - start) / 4
    val secondQuarter: Double = start + 2 * (end - start) / 4
    val thirdQuarter: Double = start + 3 * (end - start) / 4
    val fourthQuarter: Double = end

    val geoJsonSourceHeat = rememberGeoJsonSourceState {
        GeoJsonSource.Builder("geoJsonSource")
            .lineMetrics(true)
            .build()
    }


    LaunchedEffect(routeLine) {
        routeLine?.let {
            geoJsonSource.data = GeoJSONData(it)
        }
        if(sensorDataViewModel.listOfLists[mapViewModel.selectedIndex].isNotEmpty())
        {
            val featureCollection = sensorDataViewModel.convertTuplesToGeoJson(mapViewModel)
            geoJsonSourceHeat.data = GeoJSONData(featureCollection?.features() ?: emptyList())
        }
    }
    Log.d(MapTAG, "geoJsonSource - Ruta:" + geoJsonSource.data.toString())
    Log.d(MapTAG, "geoJsonSourceHeat - HeatMap:" + geoJsonSourceHeat.data.toString())


    MapboxStandardStyle(
        topSlot = {
            if(mapViewModel.selectedMenu == "HeatMap" && LocationData.zoom <= 12.4 )//&& LocationData.zoom >= 11.4)
            {
                HeatmapLayer(
                    sourceState = geoJsonSourceHeat,
                    filter = Filter(
                        gt(get { literal("intensity") }, literal(secondQuarter))
                    ),
                    heatmapWeight = HeatmapWeight(
                        interpolate {
                            linear()
                            get { literal("intensity") }
                            stop {
                                literal(secondQuarter)
                                literal(0.5) // Weight for intensity 50
                            }
                            stop {
                                literal(thirdQuarter)
                                literal(0.7) // Weight for intensity 100
                            }
                            stop {
                                literal(fourthQuarter)
                                literal(1) // Weight for intensity 100
                            }
                        }
                    ),
                    heatmapIntensity = HeatmapIntensity(
                        interpolate {
                            linear()
                            get { literal("intensity") }
                            stop {
                                literal(secondQuarter)
                                literal(0.5) // Weight for intensity 50
                            }
                            stop {
                                literal(thirdQuarter)
                                literal(0.7) // Weight for intensity 100
                            }
                            stop {
                                literal(fourthQuarter)
                                literal(1) // Weight for intensity 100
                            }
                        }
                    ),
                    heatmapOpacity = HeatmapOpacity(
                        interpolate {
                            linear()
                            zoom()
                            /*stop{
                                literal(11.4)
                                literal(0.0) // Opacity for zoom level 12.5
                            }*/
                            stop{
                                literal(11.9)
                                literal(0.7) // Opacity for zoom level 12.5
                            }
                            stop {
                                literal(12.2)
                                literal(0.85) // Opacity for zoom level 12.5
                            }
                            stop {
                                literal(12.3)
                                literal(0.55) // Opacity for zoom level 12.0
                            }
                            stop {
                                literal(12.45)
                                literal(0.0) // Opacity for zoom level 11.8
                            }

                        }
                    ),
                    heatmapColor = HeatmapColor(
                        interpolate {
                            linear()
                            heatmapDensity()
                            stop {
                                literal(0.0)
                                rgba(0.0, 0.0, 0.0, 0.0) // Transparent
                            }
                            stop {
                                literal(0.25)
                                rgb(0.0, 255.0, 0.0) // Green
                            }
                            stop {
                                literal(0.5)
                                rgb(255.0, 255.0, 0.0) // Yellow
                            }
                            stop {
                                literal(0.75)
                                rgb(255.0, 165.0, 0.0) // Orange
                            }
                            stop {
                                literal(0.99)
                                rgb(255.0, 0.0, 0.0) // Red
                            }
                        }
                    ),
                    heatmapRadius = HeatmapRadius(
                        interpolate {
                            linear()
                            zoom()
                            stop {
                                literal(2.4) // At zoom level 19
                                literal(0.3) // Large radius
                            }
                            stop {
                                literal(5.4) // At zoom level 19
                                literal(1.5) // Large radius
                            }
                            stop {
                                literal(8.4) // At zoom level 19
                                literal(3.0) // Large radius
                            }
                            stop {
                                literal(9.4) // At zoom level 19
                                literal(4.0) // Large radius
                            }
                            stop {
                                literal(9.5) // At zoom level 19
                                literal(5.9) // Large radius
                            }
                            stop {
                                literal(10.4) // At zoom level 19
                                literal(8.9) // Large radius
                            }
                            stop {
                                literal(11.4) // At zoom level 19
                                literal(15.9) // Large radius
                            }
                            stop {
                                literal(11.8) // At zoom level 19
                                literal(20.0) // Large radius
                            }
                            stop {
                                literal(12.0) // At zoom level 10
                                literal(25.0) // Small radius
                            }
                        }
                    )
                )


            }
        }
            ,
        middleSlot = {
            if (routeLine != null && mapViewModel.startedRoute) {
                LineLayer(
                    sourceState = geoJsonSource,
                    lineWidth = LineWidth(
                        interpolate {
                            exponential {
                                literal(1.5)
                            }
                            zoom()
                            stop {
                                literal(10)
                                product(7.0, 1.0)
                            }
                            stop {
                                literal(14.0)
                                product(10.5, 1.0)
                            }
                            stop {
                                literal(16.5)
                                product(15.5, 1.0)
                            }
                            stop {
                                literal(19.0)
                                product(24.0, 1.0)
                            }
                            stop {
                                literal(22.0)
                                product(29.0, 1.0)
                            }
                        }
                    ),
                    lineCap = LineCap.ROUND,
                    lineJoin = LineJoin.ROUND,
                    lineGradient = LineGradient(
                        interpolate {
                            linear()
                            lineProgress()
                            stop {
                                literal(0)
                                rgb(128.0, 164.0, 237.0) // Lighter version of MainBlueTheme
                            }
                            stop {
                                literal(0.5)
                                rgb(96.0, 142.0, 233.0) // Original MainBlueTheme
                            }
                            stop {
                                literal(1.0)
                                rgb(77.0, 114.0, 186.0) // Darker version of MainBlueTheme
                            }
                        }
                    )
                )
                LineLayer(
                    sourceState = geoJsonSource,
                    lineWidth = LineWidth(
                        interpolate {
                            exponential {
                                literal(1.5)
                            }
                            zoom()
                            stop {
                                literal(4.0)
                                product(3.0, 1.0)
                            }
                            stop {
                                literal(10.0)
                                product(4.0, 1.0)
                            }
                            stop {
                                literal(13.0)
                                product(6.0, 1.0)
                            }
                            stop {
                                literal(16.0)
                                product(10.0, 1.0)
                            }
                            stop {
                                literal(19.0)
                                product(14.0, 1.0)
                            }
                            stop {
                                literal(22.0)
                                product(18.0, 1.0)
                            }
                        }
                    ),
                    lineCap = LineCap.ROUND,
                    lineJoin = LineJoin.ROUND,
                    /*
                    lineGradient = LineGradient(
                        interpolate {
                            linear()
                            lineProgress()
                            // blue
                            stop { literal(0.0); rgb { literal(6); literal(1); literal(255) } }
                            // royal blue
                            stop { literal(0.1); rgb { literal(59); literal(118); literal(227) } }
                            // cyan
                            stop { literal(0.3); rgb { literal(7); literal(238); literal(251) } }
                            // lime
                            stop { literal(0.5); rgb { literal(0); literal(255); literal(42) } }
                            // yellow
                            stop { literal(0.7); rgb { literal(255); literal(252); literal(0) } }
                            // red
                            stop { literal(1.0); rgb { literal(255); literal(30); literal(0) } }
                        }
                    )*/
                    lineGradient = LineGradient(
                        interpolate {
                            linear()
                            lineProgress()
                            stop {
                                literal(0)
                                rgb(128.0, 164.0, 237.0) // Lighter version of MainBlueTheme
                            }
                            stop {
                                literal(0.5)
                                rgb(96.0, 142.0, 233.0) // Original MainBlueTheme
                            }
                            stop {
                                literal(1.0)
                                rgb(77.0, 114.0, 186.0) // Darker version of MainBlueTheme
                            }
                        }
                    )
                )
            }
        },
        bottomSlot = {
            if(mapViewModel.selectedMenu == "HeatMap" && LocationData.zoom >= 11) {
                CircleLayer(
                    sourceState = geoJsonSourceHeat,
                    visibility = Visibility.VISIBLE,
                    circleColor = CircleColor(
                        interpolate {
                            linear()
                            get { literal("intensity") }
                            stop {
                                literal(firstQuarter.toDouble())
                                rgb(0.0, 255.0, 0.0) // Green
                            }
                            stop {
                                literal(secondQuarter.toDouble())
                                rgb(255.0, 255.0, 0.0) // Yellow
                            }
                            stop {
                                literal(thirdQuarter.toDouble())
                                rgb(255.0, 165.0, 0.0) // Orange
                            }
                            stop {
                                literal(fourthQuarter.toDouble())
                                rgb(255.0, 0.0, 0.0) // Red
                            }
                        }
                    ),
                    circleRadius = CircleRadius(
                        interpolate {
                            linear()
                            zoom()
                            stop {
                                literal(12.4)  // At zoom level 10
                                literal(5)  // Medium radius
                            }
                            stop {
                                literal(12.5)  // At zoom level 10
                                literal(7)  // Medium radius
                            }
                            stop {
                                literal(13)  // At zoom level 20
                                literal(10)  // Large radius
                            }
                            stop {
                                literal(14)  // At zoom level 20
                                literal(15)  // Large radius
                            }
                            stop {
                                literal(15)  // At zoom level 20
                                literal(18)  // Large radius
                            }
                            stop {
                                literal(16)  // At zoom level 20
                                literal(20)  // Large radius
                            }
                        }
                    ),
                    circleOpacity = CircleOpacity(
                        interpolate {
                            linear()
                            zoom()
                            stop {
                                literal(11.5)  // At zoom level 10
                                literal(0.0)  // Medium radius
                            }
                            stop {
                                literal(12)  // At zoom level 10
                                literal(0.2)  // Medium radius
                            }
                            stop {
                                literal(12.5)  // At zoom level 10
                                literal(0.4)  // Medium radius
                            }
                            stop {
                                literal(13)  // At zoom level 20
                                literal(0.7)  // Large radius
                            }
                            stop {
                                literal(14)  // At zoom level 20
                                literal(0.8)  // Large radius
                            }
                            stop {
                                literal(15)  // At zoom level 20
                                literal(0.9)  // Large radius
                            }
                            stop {
                                literal(16)  // At zoom level 20
                                literal(1.0)  // Large radius
                            }
                        }
                    )
                )
            }
        }
    )
}

fun distanceBetweenPoints(point1: Point, point2: Point): Double {
    val lat1 = Math.toRadians(point1.latitude())
    val lon1 = Math.toRadians(point1.longitude())
    val lat2 = Math.toRadians(point2.latitude())
    val lon2 = Math.toRadians(point2.longitude())

    val dLon = lon2 - lon1
    val dLat = lat2 - lat1

    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    // Radius of the Earth in kilometers
    val radius = 6371.0

    return radius * c
}

fun totalDistanceTraveled(lineString: LineString): Double {
    var totalDistance = 0.0
    val coordinates = lineString.coordinates()
    Log.d(MapTAG, coordinates.size.toString())
    for (i in 1 until coordinates.size) {
        val distance = distanceBetweenPoints(coordinates[i - 1], coordinates[i])
        Log.d(MapTAG, "Coord " + coordinates[i - 1].toString())
        totalDistance += distance
    }
    return totalDistance
}

@OptIn(MapboxExperimental::class)
@Composable
fun MapScreen(sensorDataViewModel: SensorDataViewModel, mapViewModel: MapViewModel = viewModel())
{
    var isFirstTime by remember { mutableStateOf(true) }
    LocationComponent {}
    val locationUpdate = locationChannel.receiveAsFlow().collectAsState(initial = null)
    Log.d(MapTAG, "LocationChannel ${locationUpdate.value}")

    val mapViewportState = rememberMapViewportState{
        setCameraOptions{
            zoom(LocationData.zoom)
            center(Point.fromLngLat(LocationData.longitude.toDouble(),
                LocationData.latitude.toDouble()
            ))
            pitch(0.0)
            bearing(0.0)
        }
    }


    //Log.d(MapTAG,mapViewportState.mapViewportStatus.toString())

    LaunchedEffect(locationUpdate.value) {
        Log.d(MapTAG, "LocationChannel ${locationUpdate.value} ${isFirstTime}")
        if (isFirstTime && (locationUpdate.value != null)) {
            mapViewportState.flyTo(
                cameraOptions{
                    Log.d(
                        MapTAG,
                        "mapViewportState.easeTo OK " + LocationData.longitude + " " + LocationData.latitude
                    )
                    center(Point.fromLngLat(LocationData.longitude.toDouble(), LocationData.latitude.toDouble()))
                    zoom(LocationData.zoom)
                    pitch(0.0)
                    bearing(0.0)
                },
                MapAnimationOptions.mapAnimationOptions{duration(5000)}
            )
            isFirstTime = false
        }
        Log.d(MapTAG, "???? " + "startedRoute " + mapViewModel.startedRoute)
        if(mapViewModel.startedRoute) {
            val routeSegment = generateRoute(
                LocationData.longitude.toDouble(),
                LocationData.latitude.toDouble(),
            )
            if (mapViewModel.routeLine == null) {
                Log.d(MapTAG, "routeLine NULL")
                val combinedCoordinates = routeSegment?.coordinates() ?: emptyList()
                mapViewModel.updateRoute(LineString.fromLngLats(combinedCoordinates))
                mapViewModel.printRoute()
            }
            else {
                mapViewModel.routeLine?.let { existingRouteLine ->
                    val combinedCoordinates =
                        existingRouteLine.coordinates() + (routeSegment?.coordinates()
                            ?: emptyList())
                    mapViewModel.updateRoute(LineString.fromLngLats(combinedCoordinates))
                    Log.d(MapTAG, "Route update " + mapViewModel.routeLine.toString())
                }

                mapViewModel.routeLine?.let {
                    mapViewModel.updateDistance(totalDistanceTraveled(it))
                    Log.d(MapTAG, "Total Distance: ${mapViewModel.totalDistance}")
                }
                mapViewModel.printRoute()
            }


        }
    }
    Log.d(MapTAG,"Zoom level: " + mapViewportState.cameraState.zoom.toString())
    LocationData.zoom = mapViewportState.cameraState.zoom


    val mapBoxMap = MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState= mapViewportState,
        style={
            MapStyle(style=Style.STANDARD)
            NavigationStyle(sensorDataViewModel = sensorDataViewModel, routeLine =  mapViewModel.routeLine, mapViewModel = mapViewModel)
        }
    )
    {
        // Add a HeatmapLayer with the given geoJsonSource
        MapEffect(Unit){mapView ->
            mapView.mapboxMap.setDebug(
                listOf(
                    MapDebugOptions.TILE_BORDERS,
                    MapDebugOptions.PARSE_STATUS,
                    MapDebugOptions.TIMESTAMPS,
                    MapDebugOptions.COLLISION,
                    MapDebugOptions.STENCIL_CLIP,
                    MapDebugOptions.DEPTH_BUFFER,
                    MapDebugOptions.MODEL_BOUNDS,
                    MapDebugOptions.TERRAIN_WIREFRAME,
                ),
                false
            )
            mapView.location.updateSettings{
                locationPuck=createDefault2DPuck(withBearing=true)
                puckBearingEnabled=true
                puckBearing=PuckBearing.HEADING
                enabled=true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp, end = 16.dp, top = 36.dp, bottom = 106.dp),
        contentAlignment = Alignment.TopStart,
    ) {

            val formatPatterns = listOf("#.###", "#,###")
            fun formatNumber(number: Double): String? {
                for (pattern in formatPatterns) {
                    try {
                        val df = DecimalFormat(pattern)
                        return df.format(number)
                    } catch (e: NumberFormatException) {
                        // Continue to next pattern if parsing fails
                        Log.d(MapTAG, "XXX")

                    }
                }
                return null
            }

            val truncatedValue = formatNumber(mapViewModel.totalDistance) ?: "0"
            Log.d(MapTAG, "TruncatedValue " + truncatedValue)
            val modifiedString = truncatedValue.replace(",", ".")
            val progressValue = modifiedString.toFloat()
            LinearProgressIndicator(
                progress = {
                    progressValue // Set a static progress value between 0.0 and 1.0
                },
                modifier = Modifier
                    .height(8.dp)
                    .width(180.dp),
                color = MainBlueTheme, // Customize the progress color if needed
                trackColor = Color.White, // Customize the track color if needed
                strokeCap = StrokeCap.Round, // Customize the stroke cap if needed
            )
            Text(
                text = "$truncatedValue km", // Replace 'distance' with your actual distance value
                modifier = Modifier.padding(start = 8.dp, top = 16.dp),
                color = MainBlueTheme,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )

    }

    if(mapViewModel.selectedMenu != "Route") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 4.dp, end = 16.dp, top = 36.dp, bottom = 106.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            TransparentBoxWithText(
                mapViewModel.selectedIndex,
                sensorDataViewModel,
                borderWidth = 1f,
                cornerRadius = 16f,
                textSize = 16f
            )


        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 106.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        FloatingActionButton(
            containerColor = Color.White,
            onClick = {
                mapViewportState.flyTo(
                    cameraOptions{
                        Log.d(
                            MapTAG,
                            LocationData.latitude.toString()+""+ LocationData.longitude.toString()
                        )
                        center(Point.fromLngLat(LocationData.longitude.toDouble(), LocationData.latitude.toDouble()))
                        zoom(LocationData.zoom)
                        pitch(0.0)
                        bearing(0.0)
                    },
                    MapAnimationOptions.mapAnimationOptions{duration(5000)}
                )
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_menu_mylocation),
                contentDescription = "Locatebutton",
                colorFilter = ColorFilter.tint(MainBlueTheme)
            )
        }




        Box(
            modifier = Modifier
                .fillMaxSize(),
                //.padding(start = 1.dp, end = 1.dp, top =1.dp, bottom = 1.dp),
            contentAlignment = Alignment.CenterStart,
        )
        {
            ExpandableButtonMenu(
                mapViewModel = mapViewModel,
                onMenuItemClicked = { menuItem ->
                    // Handle menu item clicked event
                    mapViewModel.updateMenu(menuItem)
                   Log.d(MapTAG, "Menu item clicked: ${mapViewModel.selectedMenu}")
                },
                ischecked = { checked ->
                    // Handle the checked state
                    Log.d(MapTAG, "Checked state: ${mapViewModel.isChecked}")
                }
            )
        }
        startRoute(
            mapViewModel = mapViewModel,
            isRoute = {
                value -> Log.d(MapTAG, "Route state: $value")
            }
        )
    }

}

@Composable
fun TransparentBoxWithText(
    index: Int,
    sensorDataViewModel: SensorDataViewModel,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.7f), // 20% opacity
    borderColor: Color = Color.Black,
    borderWidth: Float = 1f,
    cornerRadius: Float = 16f,
    textColor: Color = Color.Black,
    textSize: Float = 16f
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(16.dp)
            .border(
                border = BorderStroke(borderWidth.dp, borderColor),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val interval: Pair<Double, Double> = sensorDataViewModel.getMinMax(index)
        val (start: Double, end: Double) = interval
        val firstQuarter: Double = start + (end - start) / 4
        val secondQuarter: Double = start + 2 * (end - start) / 4
        val thirdQuarter: Double = start + 3 * (end - start) / 4
        val fourthQuarter: Double = end
        Column {
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Dot(color = Color(52, 235, 153), size = 10)
                Spacer(modifier = Modifier.width(1.dp))
                Text(
                    text = sensorDataViewModel.getRangeMeaning(index).first,
                    style = TextStyle(
                        color = textColor,
                        fontSize = textSize.sp
                    ),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "[$start, $firstQuarter]",
                    style = TextStyle(
                        color = textColor,
                        fontSize = 11f.sp
                    )
                )
            }
            Row {
                Dot(color = Color(255, 255, 0), size = 10)
                Spacer(modifier = Modifier.width(1.dp))
                Text(
                    text = sensorDataViewModel.getRangeMeaning(index).second,
                    style = TextStyle(
                        color = textColor,
                        fontSize = textSize.sp
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "[$firstQuarter, $secondQuarter]",
                    style = TextStyle(
                        color = textColor,
                        fontSize = 11f.sp
                    )
                )
            }
            Row {
                Dot(color = Color(235, 171, 52), size = 10)
                Spacer(modifier = Modifier.width(1.dp))
                Text(
                    text = sensorDataViewModel.getRangeMeaning(index).third,
                    style = TextStyle(
                        color = textColor,
                        fontSize = textSize.sp
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "[$secondQuarter, $thirdQuarter]",
                    style = TextStyle(
                        color = textColor,
                        fontSize = 11f.sp
                    )
                )
            }
            Row {
                Dot(color = Color(235, 61, 52), size = 10)
                Spacer(modifier = Modifier.width(1.dp))
                Text(
                    text = sensorDataViewModel.getRangeMeaning(index).fourth,
                    style = TextStyle(
                        color = textColor,
                        fontSize = textSize.sp
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "[$thirdQuarter, $fourthQuarter]",
                    style = TextStyle(
                        color = textColor,
                        fontSize = 11f.sp
                    )
                )

            }
        }
    }
}



@Composable
fun ExpandableButtonMenu( mapViewModel: MapViewModel, onMenuItemClicked: (String) -> Unit, ischecked: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var buttonOffsetX by remember { mutableStateOf(0f) }
    var buttonOffsetY by remember { mutableStateOf(0f) }

    val buttonSize = animateDpAsState(if (expanded) 115.dp else 55.dp, spring(stiffness = 400f),
        label = ""
    )

    Box(
        modifier = Modifier
            .padding(16.dp)
            .size(buttonSize.value)
            .offset { IntOffset(buttonOffsetX.roundToInt(), buttonOffsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    Log.d(MapTAG, buttonOffsetY.toString() + " " + buttonOffsetX.toString())
                    if ((buttonOffsetX + dragAmount.x < maxX) && (buttonOffsetX + dragAmount.x > minX)) {
                        buttonOffsetX += dragAmount.x
                    }
                    if ((buttonOffsetY + dragAmount.y < maxY) && (buttonOffsetY + dragAmount.y > minY)) {
                        buttonOffsetY += dragAmount.y
                    }
                }
            },
        contentAlignment = Alignment.TopStart
    ) {

        FloatingActionButton(
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .size(buttonSize.value),
            onClick = { expanded = !expanded},
            containerColor = Color.White
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {

                if(!expanded)
                {
                    Text("Menu")
                }
                else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                mapViewModel.updateMenu("HeatMap")
                                onMenuItemClicked("HeatMap")
                            }
                        ) {
                            val iconTint = if (mapViewModel.selectedMenu == "HeatMap") MainBlueTheme else LocalContentColor.current
                            CompositionLocalProvider(LocalContentColor provides iconTint) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null
                                )
                                Text("HMap")
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .height(40.dp)
                                .width(2.dp),
                            color = Color.Black
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                mapViewModel.updateMenu("Route")
                                onMenuItemClicked("Route")
                            }
                        ) {
                            val iconTint = if (mapViewModel.selectedMenu == "Route") MainBlueTheme else LocalContentColor.current
                            CompositionLocalProvider(LocalContentColor provides iconTint) {
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = null
                                )
                                Text("Route")
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        ExposedDropdownMenuSample(mapViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun Dot(
    color: Color,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(color, CircleShape)
    )
}
