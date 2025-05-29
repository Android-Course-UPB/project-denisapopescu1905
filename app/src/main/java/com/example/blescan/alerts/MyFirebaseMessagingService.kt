package com.example.blescan.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blescan.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch
import java.util.UUID

const val CHANNEL_ID = "notify"
const val CHANNEL_NAME = "com.example.blescan"
const val FireTag = "FIREBASE"

data class MyNotification(val id: String, var title: String, val body: String, var isFavorite: Boolean = false)

class NotificationViewModel : ViewModel() {

    private val _notificationHistory = MutableLiveData<List<MyNotification>>()
    val notificationHistory: LiveData<List<MyNotification>> get() = _notificationHistory

    private val notifications = mutableListOf<MyNotification>()

    fun addNotification(notification: MyNotification) {
        Log.d(FireTag, "Add " + notification.title)
        viewModelScope.launch {
            notifications.add(notification)
            _notificationHistory.postValue(notifications.toList())
        }
    }
}

object NotificationViewModelProvider {
    private var instance: NotificationViewModel? = null

    fun getInstance(): NotificationViewModel {
        synchronized(this) {
            if (instance == null) {
                instance = NotificationViewModel()
            }
            return instance!!
        }
    }
}

class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(FireTag, "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(FireTag, "onMessageReceived: ${message.data}")
        message.notification?.let { notification ->
            notification.title?.let { notification.body?.let { it1 -> getFireBaseMessage(it, it1) } }
        }

    }

    private fun  getFireBaseMessage(title: String, body: String)
    {

        Log.d(FireTag, "Title: ${title}")
        Log.d(FireTag, "Body: ${body}")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.temperature_svgrepo_com) // Set a valid small icon here
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            //.setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create the NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Firebase Cloud Messaging Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, builder.build())

        val notification = MyNotification(UUID.randomUUID().toString(), title, body)
        NotificationViewModelProvider.getInstance().addNotification(notification)
    }
}