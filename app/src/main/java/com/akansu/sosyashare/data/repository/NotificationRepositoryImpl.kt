package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.NotificationMapper
import com.akansu.sosyashare.data.remote.FirebaseNotificationService
import com.akansu.sosyashare.domain.model.Notification
import com.akansu.sosyashare.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationService: FirebaseNotificationService
) : NotificationRepository {

    override suspend fun addNotification(notification: Notification) {
        val notificationEntity = NotificationMapper.toEntity(notification)
        notificationService.addNotification(notificationEntity)
    }

    override suspend fun clearNotificationsByUserId(userId: String) {
        notificationService.clearNotificationsByUserId(userId)
    }

    override suspend fun sendNotification(
        userId: String,
        postId: String,
        senderId: String,
        senderUsername: String,
        senderProfileUrl: String?,
        notificationType: String
    ) {
        notificationService.sendNotification(userId, postId, senderId, senderUsername, senderProfileUrl, notificationType)
    }

    override suspend fun canUserLikeOrComment(userId: String, postId: String, notificationType: String): Boolean {
        return notificationService.canUserLikeOrComment(userId, postId, notificationType)
    }

    override suspend fun getNotificationsByUserId(userId: String): List<Notification> {
        return notificationService.getNotificationsByUserId(userId).map { NotificationMapper.fromEntity(it) }
    }

    override suspend fun markNotificationAsRead(notificationId: String) {
        notificationService.markNotificationAsRead(notificationId)
    }

    override suspend fun deleteNotification(notificationId: String) {
        notificationService.deleteNotification(notificationId)
    }
}
