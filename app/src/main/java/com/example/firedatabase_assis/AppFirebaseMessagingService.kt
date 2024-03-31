package com.example.firedatabase_assis

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firedatabase_assis.service.MessagingHandlerService

class AppFirebaseMessagingService : FirebaseMessagingService() {
    private val messagingHandlerService = MessagingHandlerService()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("Notification gaskeun", "Test123")
        remoteMessage.notification?.let {
            val notificationTitle = it.title ?: "Notification Title"
            val notificationBody = it.body ?: "Notification Body"

            // Log the notification
            println("Notification received: Title - $notificationTitle, Body - $notificationBody")

            // Display the notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "Default"
            val channelName = "Default Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(channel)
            }

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(notificationTitle)
                .setSmallIcon(R.drawable.ic_baseline_visibility_24)
                .setContentText(notificationBody)
                .setAutoCancel(true)

            notificationManager.notify(0, notificationBuilder.build())
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM Token", token)

        messagingHandlerService.updateFCMToken(token)

        // Handle token (send it to your server, etc.)
    }
}
