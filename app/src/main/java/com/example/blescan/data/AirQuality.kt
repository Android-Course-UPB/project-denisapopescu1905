package com.example.blescan.data

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blescan.bluetooth.GenericIcon
import com.example.blescan.ui.theme.Blue50
import com.example.blescan.ui.theme.MainBlueTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirQuality(sensorDataViewModel: SensorDataViewModel) {
    var railSelectedItem by rememberSaveable { mutableIntStateOf(0) }
    val bottomSheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isHistoryOpen by rememberSaveable { mutableStateOf(false) }

    Surface(color = Color.White) {
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            NavigationRail(
                containerColor = MainBlueTheme,
                modifier = Modifier.width(56.dp),
            ) {
                railItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        icon = {
                            GenericIcon(
                                color = Color.White,
                                resourceId = item.selectedIcon,
                                size = item.size
                            )
                        },
                        selected = railSelectedItem == index,
                        onClick = { railSelectedItem = index },
                        colors = NavigationRailItemDefaults.colors(
                            indicatorColor = Blue50
                        )
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = railItems[railSelectedItem].title,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .padding(vertical = 5.dp)
                            .weight(2.5f),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainBlueTheme,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 3.dp)
                            .padding(vertical = 5.dp)
                            .clickable(onClick = { isSheetOpen = true }),
                        tint = MainBlueTheme
                    )
                }

                Row(Modifier.fillMaxWidth()) {
                    if(railSelectedItem != 8) {
                        SpeedTestScreen(railSelectedItem, sensorDataViewModel)
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if(!isHistoryOpen)"Predictions" else "History",
                        modifier = Modifier
                            .padding(start = 15.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainBlueTheme,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 3.dp)
                            .padding(vertical = 5.dp)
                            .clickable(onClick = { isHistoryOpen = !isHistoryOpen }),
                        tint = MainBlueTheme
                    )
                }
                Row(Modifier.fillMaxWidth())
                {
                    val interval = sensorDataViewModel.getMinMax(railSelectedItem)
                    Log.d("Graphs", railItems[railSelectedItem].lineGraphPoints.toString())
                    if(!isHistoryOpen) {
                        if(railItems[railSelectedItem].lineGraphPoints.isNotEmpty()) {

                            LineChart(
                                sensorDataViewModel = sensorDataViewModel,
                                dataPoints = railItems[railSelectedItem].lineGraphPoints,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                railSelectedItem
                            )
                        }
                    }
                    else
                    {
                        if(railItems[railSelectedItem].historyPoints.isNotEmpty()) {
                            LineChartClassic(
                                dataPoints = railItems[railSelectedItem].historyPoints,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                railSelectedItem
                            )
                        }
                    }

                    //drawLineChart(railSelectedItem)
                }
               // Spacer(modifier = Modifier.height(36.dp))

                if (isSheetOpen) {
                    ModalBottomSheet(
                        sheetState = bottomSheetState,
                        onDismissRequest = { isSheetOpen = false }
                    ) {
                        Text(
                            text = railItems[railSelectedItem].title,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .padding(vertical = 5.dp),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = MainBlueTheme,
                        )
                        Text(
                            text = railItems[railSelectedItem].descriptionInfo,
                            modifier = Modifier
                                .padding(start = 16.dp),
                                //.weight(4f),
                            fontSize = 20.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Measure Unit: ")
                                }
                                append(railItems[railSelectedItem].measureUnit)
                            },
                            modifier = Modifier
                                .padding(start = 16.dp),
                            fontSize = 20.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        //Spacer(modifier = Modifier.height(32.dp)) // Adjust the height as needed
                        val interval: Pair<Double, Double> = sensorDataViewModel.getMinMax(railSelectedItem)
                        val (start: Double, end: Double) = interval
                        val firstQuarter: Double = start + (end - start) / 4
                        val secondQuarter: Double = start + 2 * (end - start) / 4
                        val thirdQuarter: Double = start + 3 * (end - start) / 4
                        val fourthQuarter: Double = end

                        val column1Weight = .4f // 50%
                        val column2Weight = .3f // 50%
                        val column3Weight = .3f // 50%
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .padding(26.dp)) {
                            // Here is the header
                            item {
                                Row(Modifier.fillMaxWidth()) {
                                    TableCell(text = "Range", weight = column1Weight, fontSize = 16)
                                    TableCell(text = "Meaning", weight = column2Weight, fontSize = 16)
                                    TableCell(text = "Health", weight = column3Weight, fontSize = 16)
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    TableCell(text = "[$start, $firstQuarter]" , weight = column1Weight, fontSize = 11, fontWeight = FontWeight.Bold)
                                    TableCell(text = sensorDataViewModel.getRangeMeaning(railSelectedItem).first, weight = column2Weight, fontSize = 10, color =  Color(52, 235, 153))
                                    TableCell(text = sensorDataViewModel.getRangeHealth(railSelectedItem).first, weight = column3Weight, fontSize = 10)
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    TableCell(text = "[${firstQuarter + 1}, $secondQuarter]" , weight = column1Weight, fontSize = 11, fontWeight = FontWeight.Bold)
                                    TableCell(text = sensorDataViewModel.getRangeMeaning(railSelectedItem).second, weight = column2Weight, fontSize = 10, color = Color(255, 255, 0))
                                    TableCell(text = sensorDataViewModel.getRangeHealth(railSelectedItem).second, weight = column3Weight, fontSize = 10)
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    TableCell(text = "[${secondQuarter + 1}, $thirdQuarter]" , weight = column1Weight, fontSize = 11, fontWeight = FontWeight.Bold)
                                    TableCell(text = sensorDataViewModel.getRangeMeaning(railSelectedItem).third, weight = column2Weight, fontSize = 10, color = Color(235, 171, 52))
                                    TableCell(text = sensorDataViewModel.getRangeHealth(railSelectedItem).third, weight = column3Weight, fontSize = 10)
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    TableCell(text = "[${thirdQuarter + 1}, $fourthQuarter]" , weight = column1Weight, fontSize = 11, fontWeight = FontWeight.Bold)
                                    TableCell(text = sensorDataViewModel.getRangeMeaning(railSelectedItem).fourth, weight = column2Weight, fontSize = 10, color = Color(235, 61, 52))
                                    TableCell(text = sensorDataViewModel.getRangeHealth(railSelectedItem).fourth, weight = column3Weight, fontSize = 10)
                                }
                            }
                            // Here are all the lines of your table.



                        }

                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    fontSize: Int,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Box(
        Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .background(color)
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),)
    {
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}