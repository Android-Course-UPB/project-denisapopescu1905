package com.example.blescan

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.blescan.bluetooth.PERMISSION_REQUEST_BACKGROUND_LOCATION
import com.example.blescan.bluetooth.gradientBoxOffset
import com.example.blescan.services.items
import com.example.blescan.ui.theme.MainBlueTheme
import kotlinx.coroutines.delay

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testButtonAndText() {
        var scan by mutableStateOf(false)

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                        Text(
                            text = "You are connected",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("text")
                        )
                        Box(
                            modifier = Modifier
                                .padding(20.dp)
                                .background(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(15.dp)
                                )
                        ) {
                            FloatingActionButton(
                                onClick = { scan = !scan },
                                modifier = Modifier
                                    .size(90.dp)
                                    .border(
                                        width = 3.dp,
                                        color = Color.Blue,
                                        shape = RoundedCornerShape(15.dp)
                                    )
                                    .testTag("button")
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(15.dp)
                                    ),
                                containerColor = Color.White,
                                contentColor = Color.White
                            ) {
                                if (scan) {
                                    LaunchedEffect(true) {
                                        // Simulate scanning for 5 seconds
                                        delay(5000)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Verify the initial state
        composeTestRule.onNodeWithTag("text").assertTextEquals("You are connected")
        composeTestRule.onNodeWithTag("button").performClick()

        // Verify the state after button click
        composeTestRule.runOnIdle {
            assert(scan)
        }
    }
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.blescan", appContext.packageName)
    }


    @Test
    fun hasBleSupport() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = appContext.packageManager
        val hasBleFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

        assertFalse("Device does not have BLE support", !hasBleFeature)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testNavigation() = runComposeUiTest {
        var selectedItem = 0

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom

            )
            {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .shadow(elevation = 40.dp)
                                .padding(0.dp, 16.dp, 0.dp, 0.dp),
                            containerColor = Color.White,
                            contentColor = Color.White,
                            tonalElevation = 40.dp
                        ) {
                            items.forEach { item ->
                                val isSelected = selectedItem == items.indexOf(item)
                                NavigationBarItem(
                                    modifier = Modifier.testTag("nav_item"),
                                    selected =  isSelected,
                                    label = {
                                        Text(
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
                                        //composeTestRule.onAllNodes(hasTestTag("nav_item")).assertCountEquals(5)
                                        println(item.title.lowercase())
                                        Thread.sleep(5000)
                                        selectedItem = items.indexOf(item)
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color.White
                                    )
                                )
                            }

                        }
                    }
                ){
                        padding ->
                }
            }
        }
    }
}
