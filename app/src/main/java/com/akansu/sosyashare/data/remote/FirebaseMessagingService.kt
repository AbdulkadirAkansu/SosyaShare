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
import com.akansu.sosyashare.util.getAccessToken
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request


class FirebaseMessagingService : FirebaseMessagingService() {
    private val firestore = FirebaseFirestore.getInstance()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Yeni token alındı: $token")
        sendTokenToServer(token)
    }

    suspend fun getFCMTokenByUserId(userId: String): String? {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val token = userDoc.getString("fcmToken")
            Log.d("FCM", "FCM Token alındı: $token")
            token
        } catch (e: Exception) {
            Log.e("MessagingService", "FCM token alınırken hata: ${e.message}")
            null
        }
    }

    fun sendFCMNotification(
        context: Context,
        fcmToken: String,
        title: String,
        commentContent: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val url = "https://fcm.googleapis.com/v1/projects/sosyashare/messages:send"
            val accessToken = getAccessToken(context)

            val notificationJson = """
        {
            "message": {
                "token": "$fcmToken",
                "notification": {
                    "title": "$title",
                    "body": "$commentContent" 
                }
            }
        }
        """

            val request = Request.Builder()
                .url(url)
                .post(notificationJson.toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("FCM", "Bildirim başarıyla gönderildi")
                    } else {
                        Log.e("FCM", "Bildirim gönderim hatası: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM", "Ağ hatası: ${e.message}")
            }
        }
    }


    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getAccessToken(this@FirebaseMessagingService)
                val url = "https://fcm.googleapis.com/v1/projects/sosyashare/messages:send"

                val tokenJson = """
            {
                "token": "$token"
            }
            """

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .post(tokenJson.toRequestBody("application/json".toMediaType()))
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                // Ağ işlemi
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("FCM", "Token başarıyla sunucuya gönderildi")
                    } else {
                        Log.e(
                            "FCM",
                            "Token gönderiminde hata: ${response.code} - ${response.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM", "Token gönderim hatası: ${e.message}")
            }
        }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Bildirim"
        val body = remoteMessage.notification?.body ?: "Yeni mesajınız var"
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
