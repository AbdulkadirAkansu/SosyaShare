package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.Notification

interface NotificationRepository {
    suspend fun addNotification(notification: Notification)
    suspend fun getNotificationsByUserId(userId: String): List<Notification>
    suspend fun markNotificationAsRead(notificationId: String)
    suspend fun deleteNotification(notificationId: String)
    suspend fun sendNotification(userId: String, postId: String, senderId: String, senderUsername: String, senderProfileUrl: String?, notificationType: String)
    suspend fun canUserLikeOrComment(userId: String, postId: String, notificationType: String): Boolean
    suspend fun clearNotificationsByUserId(userId: String)

}

