package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.model.NotificationEntity
import com.akansu.sosyashare.util.getAccessToken
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import java.util.Date
import javax.inject.Inject

class FirebaseNotificationService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notificationsCollection = firestore.collection("notifications")

    suspend fun addNotification(notification: NotificationEntity) {
        notificationsCollection.add(notification).await()
    }

    suspend fun clearNotificationsByUserId(userId: String) {
        val batch = firestore.batch()
        val notificationsSnapshot = notificationsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()

        notificationsSnapshot.documents.forEach { document ->
            batch.delete(document.reference)
        }

        batch.commit().await()
    }

    suspend fun sendNotification(
        userId: String,
        postId: String?,
        senderId: String,
        senderUsername: String,
        senderProfileUrl: String?,
        notificationType: String
    ) {
        val query = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("senderId", senderId)
            .whereEqualTo("type", notificationType)

        if (postId != null) {
            query.whereEqualTo("postId", postId)
        }

        val existingNotifications = query.get().await()

        if (existingNotifications.isEmpty) {
            val content = when (notificationType) {
                "like" -> "$senderUsername liked your post"
                "comment" -> "$senderUsername commented on your post"
                "follow" -> "$senderUsername started following you"
                "unfollow" -> "$senderUsername stopped following you"
                else -> "$senderUsername performed an action"
            }

            val notification = NotificationEntity(
                userId = userId,
                senderId = senderId,
                senderUsername = senderUsername,
                senderProfileUrl = senderProfileUrl ?: "",
                type = notificationType,
                postId = postId,
                content = content,
                isRead = false,
                timestamp = Date()
            )

            addNotification(notification)
            Log.d("NotificationService", "Yeni bildirim gönderildi.")
        } else {
            Log.d("NotificationService", "Aynı bildirim zaten gönderilmiş.")
        }
    }


    suspend fun canUserLikeOrComment(
        userId: String,
        postId: String,
        notificationType: String
    ): Boolean {
        val now = Date()
        val timeLimit = 0
        val recentActions = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("postId", postId)
            .whereEqualTo("senderId", userId)
            .whereEqualTo("type", notificationType)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        if (recentActions.isEmpty) {
            return true
        }

        val lastActionTime =
            recentActions.documents.firstOrNull()?.getDate("timestamp") ?: return true
        val timeDifference = now.time - lastActionTime.time
        return timeDifference > timeLimit
    }


    // Kullanıcının bildirimlerini al
    suspend fun getNotificationsByUserId(userId: String): List<NotificationEntity> {
        val result = notificationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .get()
            .await()

        return result.documents.map { document ->
            val notification = document.toObject(NotificationEntity::class.java)!!
            notification.copy(documentId = document.id)
        }
    }

    suspend fun markNotificationAsRead(notificationDocumentId: String) {
        val document = notificationsCollection.document(notificationDocumentId)
        val snapshot = document.get().await()

        if (snapshot.exists()) {
            document.update("isRead", true).await()
        } else {
            Log.e("FirebaseNotificationService", "Belge bulunamadı: $notificationDocumentId")
        }
    }


    suspend fun deleteNotification(notificationDocumentId: String) {
        try {
            val document = notificationsCollection.document(notificationDocumentId)
            val snapshot = document.get().await()

            if (snapshot.exists()) {
                document.delete().await()
                Log.d("FirebaseNotificationService", "Bildirim silindi: $notificationDocumentId")
            } else {
                Log.d("FirebaseNotificationService", "Belge bulunamadı: $notificationDocumentId")
            }
        } catch (e: Exception) {
            Log.e("FirebaseNotificationService", "Silme işlemi başarısız: ${e.message}")
        }
    }

}
