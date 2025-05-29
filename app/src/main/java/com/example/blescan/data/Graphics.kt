package com.example.blescan.data
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.lang.Float.max
import kotlin.math.floor
import kotlin.math.roundToInt
const val TAGGraph = "GRAPH"
class UiState(
    val speed: String = "",
    val ping: String = "-",
    val maxSpeed: String = "-",
    val arcValue: Float = 0f,
    val inProgress: Boolean = false,
    val actualVal: Float = 0f,
    val measureUnit: String = ""
)

fun getScaled(currentValue: Float, maxValue: Float): Float {
    /// regula de 3 simpla
    val firstScaled = (currentValue * 240) / maxValue
    Log.d(TAGGraph, "current: " + currentValue.toString())
    Log.d(TAGGraph, "max: " + maxValue.toString())
    Log.d(TAGGraph, "firstScaled: " + firstScaled.toString())
    Log.d(TAGGraph, "firstScaled/240: " + (firstScaled/240).toString())
    if(currentValue < 0)
    {
        return 0f
    }
    return firstScaled/240

}

suspend fun startAnimation(animation: Animatable<Float, AnimationVector1D>, currentValue: Float, maxValue: Float) {
    val scaledValue = getScaled(currentValue, maxValue)
    Log.d(TAGGraph, "scaledValue: " + scaledValue.toString())
    animation.animateTo(scaledValue, keyframes {
        durationMillis = 2000
        0f at 0 using LinearOutSlowInEasing
        /*0.2f at 1000 with CubicBezierEasing(0.2f, -1.5f, 0f, 1f)
        0.76f at 2000 with CubicBezierEasing(0.2f, -2f, 0f, 1f)
        0.78f at 3000 with CubicBezierEasing(0.2f, -1.5f, 0f, 1f)
        0.82f at 4000 with CubicBezierEasing(0.2f, -2f, 0f, 1f)
        0.85f at 5000 with CubicBezierEasing(0.2f, -2f, 0f, 1f)
        0.89f at 6000 with CubicBezierEasing(0.2f, -1.2f, 0f, 1f)
        0.82f at 7500 with LinearOutSlowInEasing*/
    })
}


fun Animatable<Float, AnimationVector1D>.toUiState(maxSpeed: Float, maxValue: Float, actualVal : Float, measureUnit: String) = UiState(
    arcValue = value * 240f,  // value is scaled between 0 and 1, multiply by 240 for the arc
    speed = "%.1f".format(value * maxValue),
    ping = if (value > 0.2f) "${(value * 15).roundToInt()} ms" else "-",
    maxSpeed = if (maxSpeed > 0f) "%.1f mbps".format(maxSpeed) else "-",
    inProgress = isRunning,
    actualVal = actualVal,
    measureUnit = measureUnit
)



@Composable
fun SpeedTestScreen(index: Int, sensorDataViewModel: SensorDataViewModel) {

    val animation = remember { Animatable(0f) }
    val maxSpeed = remember { mutableStateOf(0f) }

    val maxValue = sensorDataViewModel.getMinMax(index).second

    val color = when (index) {
        0 -> Color(83, 109, 254)  // Color for index 0
        1 -> Color(244, 67, 54)  // Color for index 1
        2 -> Color(38, 198, 218)
        3 -> Color(224, 64, 251)
        4 -> Color(0, 230, 118)
        5 -> Color(233, 30, 99)
        6 -> Color(255, 87, 34)
        7 -> Color(255, 255, 0)
        // Add more cases for other index values if needed
        else -> Color.Red  // Default color
    }

    maxSpeed.value = max(maxSpeed.value, (animation.value * maxValue).toFloat())
    if(maxValue != 0.0) {
        LaunchedEffect(index) {
            startAnimation(animation, sensorDataViewModel.getValueForIndex(index), maxValue.toFloat())
        }
    }
    SpeedTestScreen(animation.toUiState(maxSpeed.value, 1f, sensorDataViewModel.getValueForIndex(index), sensorDataViewModel.getMeasureUnit(index)), color) {}
}

@Composable
private fun SpeedTestScreen(state: UiState, color : Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth() // Occupy the full width
            .padding(16.dp), // Add padding
        verticalArrangement = Arrangement.spacedBy(8.dp) // Add vertical spacing between children
    ) {
        SpeedIndicator(state = state, color = color)
    }
}

@Composable
fun SpeedIndicator(state: UiState, color : Color) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        CircularSpeedIndicator(state.arcValue, 240f, color)
        SpeedValue(state.actualVal.toString(), state.measureUnit)
    }
}

@Composable
fun SpeedValue(value: String , unit: String) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 45.sp,
            color = Color(66, 66, 66),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = unit,
            fontSize = 20.sp,
            color = Color(66, 66, 66),
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun StartButton(isEnabled: Boolean,  color : Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.padding(bottom = 24.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(width = 2.dp, color = color),

        ) {
        Text(
            text = "START",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CircularSpeedIndicator(value: Float,
                           angle: Float,
                           color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {
        drawLines(value, angle)
        drawArcs(value, angle, color)
    }
}

fun DrawScope.drawArcs(progress: Float, maxValue: Float, color: Color) {
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * (progress / maxValue)

    val topLeft = Offset(50f, 50f)
    val size = Size(size.width - 100f, size.height - 100f)

    fun drawBlur() {
        for (i in 0..20) {
            drawArc(
                color = color.copy(alpha = i / 900f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = 80f + (20 - i) * 20, cap = StrokeCap.Round)
            )
        }
    }

    fun drawStroke() {
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 86f, cap = StrokeCap.Round)
        )
    }

    val gradientShader = Brush.radialGradient(
        colors = listOf(color, color),
        center = Offset(size.width / 2f, size.height / 2f),
        radius = size.width / 2f
    )

    fun drawGradient() {
        drawArc(
            brush = gradientShader,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 80f, cap = StrokeCap.Round)
        )
    }

    drawBlur()
    drawStroke()
    drawGradient()
}

fun DrawScope.drawLines(progress: Float, maxValue: Float, numberOfLines: Int = 40) {
    val oneRotation = maxValue / numberOfLines
    Log.d("GRAF", "oneRotation " + oneRotation.toString())
    Log.d("GRAF", "maxValue " + maxValue.toString())
    val startValue = if (progress == 0f) 0 else floor(progress / maxValue * numberOfLines).toInt() + 1

    for (i in startValue..numberOfLines) {
        rotate(i * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                Color(66, 66, 66),
                Offset(if (i % 5 == 0) 80f else 30f, size.height / 2),
                Offset(0f, size.height / 2),
                8f,
                StrokeCap.Round
            )
        }
    }
}

// Historical Chart
