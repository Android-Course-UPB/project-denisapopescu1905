package com.example.blescan.alerts
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blescan.ui.theme.GreenAlert
import com.example.blescan.ui.theme.MainBlueTheme
import com.example.blescan.ui.theme.RedAlert


@Composable
fun AlertScreen()
{
    val context = LocalContext.current
    var alertsEnabled by rememberSaveable { mutableStateOf(false) }


    Log.d(FireTag, NotificationViewModelProvider.getInstance().notificationHistory.value?.size.toString())

    val notificationList by produceState(initialValue = emptyList<MyNotification>()) {
        NotificationViewModelProvider.getInstance().notificationHistory.observeForever {
            val seenTitles = HashSet<String>()
            val uniqueNotifications = it?.filter { notification ->
                seenTitles.add(notification.title)
            } ?: emptyList()
            value = uniqueNotifications
        }
    }

    var notifications by remember { mutableStateOf(notificationList) }

    Log.d(FireTag, notificationList.size.toString())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
                //.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Alerts & Notifications",
                        color = Color.Unspecified,
                        textAlign = TextAlign.Center,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (alertsEnabled) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                            contentDescription = null,
                            tint = MainBlueTheme,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    alertsEnabled = !alertsEnabled
                                    if (alertsEnabled) {
                                        showToast(context, "Alerts Enabled!")
                                    } else {
                                        showToast(context, "Alerts Disabled!")
                                    }
                                }
                        )
                    }
                }
            }
            if(alertsEnabled) {
                items(notificationList) { notification ->
                    SwipeableCard(
                        notification = notification,
                        onDeleted = {
                            notifications = notifications.filterNot { it.id == notification.id }
                            var title = if (notification.title.startsWith("0") || notification.title.startsWith("1")) {
                                notification.title.substring(1)}
                            else
                            {
                                notification.title
                            }
                            showToast(context, "${title} notification deleted!")
                            notification.title = "AAAAAAAAAAAAAAAAA"
                        }
                    )

                }
            }
        }
    }

}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}


@Composable
fun SwipeableCard(
    notification: MyNotification,
    onDeleted: () -> Unit,
) {

    if (!notification.isFavorite) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(Color.White)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = when {
                    notification.title.startsWith("0") -> CardDefaults.cardColors(containerColor = RedAlert)
                    notification.title.startsWith("1") -> CardDefaults.cardColors(containerColor = GreenAlert)
                    else -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (notification.title.startsWith("0") || notification.title.startsWith("1")) {
                                notification.title.substring(1)
                            } else {
                                notification.title
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = notification.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Outlined.RestoreFromTrash,
                            contentDescription = null,
                            tint = Color.White,
                            modifier =
                            Modifier.size(24.dp).
                            clickable{
                                notification.isFavorite = ! notification.isFavorite
                                onDeleted()
                            }
                        )
                    }
                }
            }
        }
    }
}
