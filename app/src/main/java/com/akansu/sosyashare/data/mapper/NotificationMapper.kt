package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.NotificationEntity
import com.akansu.sosyashare.domain.model.Notification

object NotificationMapper {
    fun fromEntity(entity: NotificationEntity): Notification {
        return Notification(
            userId = entity.userId,
            senderId = entity.senderId,
            senderUsername = entity.senderUsername,
            senderProfileUrl = entity.senderProfileUrl,
            type = entity.type,
            postId = entity.postId,
            content = entity.content,
            isRead = entity.isRead,
            timestamp = entity.timestamp,
            documentId = entity.documentId  // documentId'yi buraya ekliyoruz
        )
    }

    fun toEntity(notification: Notification): NotificationEntity {
        return NotificationEntity(
            userId = notification.userId,
            senderId = notification.senderId,
            senderUsername = notification.senderUsername,
            senderProfileUrl = notification.senderProfileUrl,
            type = notification.type,
            postId = notification.postId,
            content = notification.content,
            isRead = notification.isRead,
            timestamp = notification.timestamp,
            documentId = notification.documentId  // documentId'yi buraya ekliyoruz
        )
    }
}


