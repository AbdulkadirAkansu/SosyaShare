package com.akansu.sosyashare.presentation.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.akansu.sosyashare.data.repository.NotificationRepositoryImpl
import com.akansu.sosyashare.domain.model.Notification
import com.akansu.sosyashare.data.remote.FirebaseNotificationService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotificationReceiver", "Bildirim Alındı")
        val userId = intent?.getStringExtra("userId") ?: return
        val postId = intent?.getStringExtra("postId") ?: return

        Log.d("NotificationReceiver", "UserId=$userId, PostId=$postId")

        // FirebaseFirestore instance alıyoruz
        val firestore = FirebaseFirestore.getInstance()
        val notificationService = FirebaseNotificationService(firestore)
        val notificationRepository = NotificationRepositoryImpl(notificationService)

        val notification = Notification(
            userId = userId,
            postId = postId,
            content = "Yeni bir bildirim var.",
            isRead = false
        )

        // Coroutine içinde asenkron işlemleri başlatıyoruz
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationRepository.addNotification(notification)
                Log.d("NotificationReceiver", "Yeni bildirim eklendi: ${notification.documentId}")
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Bildirim eklenirken hata oluştu: ${e.message}")
            }
        }
    }
}