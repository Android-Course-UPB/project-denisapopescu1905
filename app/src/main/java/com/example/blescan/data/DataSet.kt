package com.example.blescan.data

import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.blescan.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


const val DatasetTag = "DataSet"

data class RailNavigationItem(
    val title: String,
    val selectedIcon: Int,
    val size: Dp,
    val descriptionInfo: String,
    val measureUnit: String,
    var lineGraphPoints: List<Pair<Long, Long>>,
    var historyPoints: List<Pair<Long, Long>>,
    var uiState: UiState
)

val railItems = listOf(
    RailNavigationItem(
        title = "Temperature",
        selectedIcon = R.drawable.temperature_svgrepo_com,
        size = 39.dp,
        descriptionInfo = "Physical quantity that quantitatively expresses the attribute of hotness or coldness. Temperature is measured with a thermometer. It reflects the average kinetic energy of the vibrating and colliding atoms making up a substance.\n",
        measureUnit = "Celsius (°C) or Fahrenheit (°F)",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    ),
    RailNavigationItem(
        title = "Humidity",
        selectedIcon = R.drawable.humidity_svgrepo_com,
        size = 32.dp,
        descriptionInfo = "Measure of the amount of water vapor in the air. Relative humidity measures the amount of water in the air in relation to the maximum amount of water vapor (moisture).\n",
        measureUnit = "Percentage (%)",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    ),
    RailNavigationItem(
        title = "Pressure",
        selectedIcon = R.drawable.pressure_svgrepo_com__1_,
        size = 32.dp,
        descriptionInfo = "Air pressure, also known as atmospheric or barometric pressure, is the force applied by air molecules[1] as they press down on the earth and all other surfaces. This includes people, objects, and walls. \n",
        measureUnit = "Hectopascals (hPa)",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    ),
    RailNavigationItem(
        title = "Altitude",
        selectedIcon = R.drawable.altitude_svgrepo_com,
        size = 32.dp,
        descriptionInfo = "Altitude or elevation is distance above sea level.\n",
        measureUnit = "Meters (m)",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    ),
    RailNavigationItem(
        title = "Carbon dioxide   (CO2)",
        selectedIcon = R.drawable.co2_svgrepo_com,
        size = 32.dp,
        descriptionInfo = " CO2, ubiquitous natural greenhouse gas, used in photosynthesis. The chosen unit measure is Parts Per Million (PPM), which describes very small substance concentrations.",
        measureUnit = "Parts Per Million (PPM)",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    ),
    RailNavigationItem(
        title = "Particulate Matter   (PM1.0)",
        selectedIcon = R.drawable.pm2_svgrepo_com,
        size = 32.dp,
        descriptionInfo = "PM, particles omnipresent in air. Examples include smoke, dust or snog, very harmful for human lungs if inhaled. Depending on particle diameter, most widely used PM levels are :\n" +
                "– PM1.0\n" +
                "– PM2.5\n" +
                "– PM10",
        measureUnit = "ug/m3 - micrograms of pollutant per air cubic metre",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    ),
    RailNavigationItem(
        title = "Particulate Matter   (PM2.5)",
        selectedIcon = R.drawable.molecule_svgrepo_com,
        size = 32.dp,
        descriptionInfo = "PM, particles omnipresent in air. Examples include smoke, dust or snog, very harmful for human lungs if inhaled. Depending on particle diameter, most widely used PM levels are :\n" +
                "– PM1.0\n" +
                "– PM2.5\n" +
                "– PM10",
        measureUnit = "ug/m3 - micrograms of pollutant per air cubic metre",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    ),
    RailNavigationItem(
        title = "Particulate Matter   (PM10)",
        selectedIcon = R.drawable.wind_svgrepo_com,
        size = 32.dp,
        descriptionInfo = "PM, particles omnipresent in air. Examples include smoke, dust or snog, very harmful for human lungs if inhaled. Depending on particle diameter, most widely used PM levels are :\n" +
                "– PM1.0\n" +
                "– PM2.5\n" +
                "– PM10",
        measureUnit = "ug/m3 - micrograms of pollutant per air cubic metre",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    ),
    RailNavigationItem(
        title = "Air Quality Index (AQI)",
        selectedIcon = R.drawable.molecule_svgrepo_com,
        size = 32.dp,
        descriptionInfo = "The AQI is a standard value of pollution in a certain zone, calculated by combining a predefined model with different historical and real-time air quality parameters, called by scientist pollutants.",
        measureUnit = "-",
        lineGraphPoints = emptyList(),
        historyPoints = emptyList(),
        UiState(
            speed = "10",
            ping = "50 ms",
            maxSpeed = "100 Mbps",
            arcValue = 0.5f,
            inProgress = false
        ),
    )
)

val dropdownItems = listOf(
    "Temperature",
    "Humidity",
    "Pressure",
    "Altitude",
    "CO2",
    "PM1.0",
    "PM2.5",
    "PM10",
    "AIQ"
)


// Example data for the line chart (list of Pair(x, y))
val dataPoints = listOf(
    Pair(25L, 1718726181542L),
    Pair(44L, 1718726181599L)
)

@Composable
fun LineChartClassic(
    dataPoints: List<Pair<Long, Long>>,
    modifier: Modifier = Modifier,
    railSelectedItem: Int
) {
    Log.d("Graphs", "History " + dataPoints.toString())
    // val notification = MyNotification("aaa", "bbb")
    // NotificationViewModelProvider.getInstance().addNotification(notification)
    val minTimestamp = dataPoints.minOf { it.second }
    val maxTimestamp = dataPoints.maxOf { it.second }
    val minValue = dataPoints.minOf { it.first - 3 }
    val maxValue = dataPoints.maxOf { it.first + 3 }

    Log.d("Graphs", "History $minTimestamp $maxTimestamp $minValue $maxValue")

    Canvas(modifier = modifier.fillMaxSize()) {
        val startX = 50f
        val startY = size.height - 50f
        val endX = size.width - 50f
        val endY = 50f

        // Calculate scaling factors
        val scaleX = (endX - startX) / (maxTimestamp - minTimestamp).toFloat()
        val scaleY = (startY - endY) / (maxValue - minValue).toFloat()

        // Draw axis
        drawLine(
            start = Offset(startX, startY),
            end = Offset(endX, startY),
            color = Color.Black,
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawLine(
            start = Offset(startX, endY),
            end = Offset(startX, startY),
            color = Color.Black,
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // Draw x-axis labels (timestamps)
        dataPoints.forEach { point ->
            val x = startX + ((point.second - minTimestamp) * scaleX)
            drawLine(
                start = Offset(x, startY + 10f),
                end = Offset(x, startY),
                color = Color.Black,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Draw y-axis labels (values)
        val yLabels = listOf(minValue, maxValue)
        yLabels.forEach { value ->
            val y = startY - ((value - minValue) * scaleY)
            drawLine(
                start = Offset(startX - 10f, y),
                end = Offset(startX, y),
                color = Color.Black,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Draw last value on y-axis
        val lastValue = dataPoints.last().first
        val lastValueY = startY - ((lastValue - minValue) * scaleY)
        drawContext.canvas.nativeCanvas.drawText(
            lastValue.toString(),
            startX - 40f,
            lastValueY.toFloat(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 35f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        )
        drawContext.canvas.nativeCanvas.drawText(
            minValue.toString(),
            startX - 40f,
            startY,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 35f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        )
        drawContext.canvas.nativeCanvas.drawText(
            maxValue.toString(),
            startX - 40f,
            endY,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 35f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        )

        drawContext.canvas.nativeCanvas.drawText(
            "Timestamp (s)",
            endX - 180f, // Adjust this value to position the text correctly
            startY + 40f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 29f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)  // Set bold typeface
            }
        )

        // Draw line chart
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = startX + ((point.second - minTimestamp) * scaleX)
            val y = startY - ((point.first - minValue) * scaleY)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = when (railSelectedItem) {
                0 -> Color(83, 109, 254)  // Color for index 0
                1 -> Color(244, 67, 54)   // Color for index 1
                2 -> Color(38, 198, 218)
                3 -> Color(224, 64, 251)
                4 -> Color(0, 230, 118)
                5 -> Color(233, 30, 99)
                6 -> Color(255, 87, 34)
                7 -> Color(255, 152, 0, 255)
                // Add more cases for other index values if needed
                else -> Color.Red  // Default color
            },
            style = Stroke(width = 6f)
        )
    }
}


@Composable
fun LineChart(
    sensorDataViewModel: SensorDataViewModel,
    dataPoints: List<Pair<Long, Long>>,
    modifier: Modifier = Modifier,
    railSelectedItem: Int
) {
    val minTimestamp = dataPoints.minOf { it.second }
    val maxTimestamp = dataPoints.maxOf { it.second }
    val (minValue, maxValue) = sensorDataViewModel.getMinMax(railSelectedItem)
    Log.d("Graphs", "$minTimestamp $maxTimestamp $minValue $maxValue")

    Canvas(modifier = modifier.fillMaxSize()) {
        val startX = 80f
        val startY = size.height - 50f
        val endX = size.width - 20f
        val endY = 50f

        // Calculate scaling factors
        val scaleX = (endX - startX) / ((maxTimestamp - minTimestamp) * 2).toFloat()
        val scaleY = (startY - endY) / (maxValue - minValue).toFloat()

        // Draw axis
        drawLine(
            start = Offset(startX, startY),
            end = Offset(endX, startY),
            color = Color.Black,
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawLine(
            start = Offset(startX, endY),
            end = Offset(startX, startY),
            color = Color.Black,
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // Draw x-axis labels (timestamps)
        dataPoints.forEach { point ->
            val x = startX + ((point.second - minTimestamp) * scaleX)
            drawLine(
                start = Offset(x, startY + 10f),
                end = Offset(x, startY),
                color = Color.Black,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

        }

        // Draw y-axis labels (values)
        val yLabels = listOf(minValue, maxValue)
        yLabels.forEach { value ->
            val y = startY - ((value - minValue) * scaleY)
            drawLine(
                start = Offset(startX - 10f, y.toFloat()),
                end = Offset(startX, y.toFloat()),
                color = Color.Black,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                startX - 40f,
                y.toFloat(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f
                }
            )
        }

        // Draw last value on y-axis
        val lastValue = dataPoints.last().first
        val lastValueY = startY - ((lastValue - minValue) * scaleY)
        drawContext.canvas.nativeCanvas.drawText(
            lastValue.toString(),
            startX - 40f,
            lastValueY.toFloat(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 35f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        )

        drawContext.canvas.nativeCanvas.drawText(
            "Timestamp (s)",
            endX - 180f, // Adjust this value to position the text correctly
            startY + 40f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 29f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)  // Set bold typeface
            }
        )

        // Draw line chart
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = startX + ((point.second - minTimestamp) * scaleX)
            val y = startY - ((point.first - minValue) * scaleY)
            if (index == 0) {
                path.moveTo(x, y.toFloat())
            } else {
                path.lineTo(x, y.toFloat())
            }
        }
        drawPath(
            path = path,
            color = when (railSelectedItem) {
                0 -> Color(83, 109, 254)  // Color for index 0
                1 -> Color(244, 67, 54)   // Color for index 1
                2 -> Color(38, 198, 218)
                3 -> Color(224, 64, 251)
                4 -> Color(0, 230, 118)
                5 -> Color(233, 30, 99)
                6 -> Color(255, 87, 34)
                7 -> Color(255, 152, 0, 255)
                // Add more cases for other index values if needed
                else -> Color.Red  // Default color
            },
            style = Stroke(width = 6f)
        )
    }
}

// Helper function to format timestamp to readable time
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


@Preview(showBackground = true)
@Composable
fun PreviewLineChart() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "Line Chart Example")
            Spacer(modifier = Modifier.height(16.dp))
            //LineChart(dataPoints = dataPoints, 0)
        }
    }
}