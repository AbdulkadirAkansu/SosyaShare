package com.akansu.sosyashare.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.akansu.sosyashare.R
import com.akansu.sosyashare.presentation.MainActivity

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Data payload varsa
        remoteMessage.data.isNotEmpty().let {
            val userId = remoteMessage.data["userId"] ?: ""
            val postId = remoteMessage.data["postId"] ?: ""
            val content = remoteMessage.data["content"] ?: "Yeni bir bildirim var"

            // Bildirimi göster
            sendNotification("Yeni Bildirim", content, userId, postId)
        }
    }



    private fun handleDataMessage(data: Map<String, String>) {
        val userId = data["userId"] ?: ""
        val postId = data["postId"] ?: ""

        showInAppNotification(userId, postId)
    }

    private fun showInAppNotification(userId: String, postId: String) {
        Log.d("FCM", "Uygulama içi bildirim gönderiliyor: UserId=$userId, PostId=$postId")
        val intent = Intent("com.akansu.sosyashare.NEW_NOTIFICATION")
        intent.putExtra("userId", userId)
        intent.putExtra("postId", postId)
        sendBroadcast(intent) // NotificationReceiver'ı tetikler
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Yeni Token Alındı: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        Log.d("FCM", "Token Sunucuya Gönderildi: $token")
        // Token'ı sunucuya gönderme işlemi burada yapılmalı.
    }

    private fun sendNotification(title: String, messageBody: String, userId: String, postId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("userId", userId)
            putExtra("postId", postId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
