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

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Gelen mesajı işleyen method
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Mesaj Alındı: ${remoteMessage.from}")

        // Mesajın bildirim kısmı varsa, bildirimi göster
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "Yeni Bildirim", it.body ?: "")
        }
    }

    // Token yenilendiğinde çağrılan method
    override fun onNewToken(token: String) {
        Log.d("FCM", "Yeni FCM Token Alındı: $token")
        // Burada token'ı sunucunuza kaydedebilirsiniz
        sendTokenToServer(token)
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE eklendi
        )

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification) // Bildirim simgesi
            .setContentTitle(title) // Bildirim başlığı
            .setContentText(messageBody) // Bildirim içeriği
            .setAutoCancel(true) // Tıklanınca otomatik kapanacak
            .setContentIntent(pendingIntent) // Bildirime tıklandığında açılacak intent

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (Oreo) ve üstü için Notification Channel oluştur
        val channel = NotificationChannel(
            channelId,
            "Default Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        // Bildirimi göster
        notificationManager.notify(0, notificationBuilder.build())
    }


    // Token'ı sunucuya kaydetme methodu (Opsiyonel)
    private fun sendTokenToServer(token: String) {
        // Token'ı sunucuya veya veritabanına kaydedin
        Log.d("FCM", "FCM Token sunucuya kaydedildi: $token")
    }
}
