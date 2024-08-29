package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.MessageEntity
import com.akansu.sosyashare.domain.model.Message

fun MessageEntity.toDomainModel(): Message {
    return Message(
        id = this.id,
        senderId = this.senderId,
        receiverId = this.receiverId,
        content = this.content,
        timestamp = this.timestamp,
        isRead = this.isRead  // isRead alanını ekledik
    )
}

fun Message.toEntityModel(): MessageEntity {
    return MessageEntity(
        id = this.id,
        senderId = this.senderId,
        receiverId = this.receiverId,
        content = this.content,
        timestamp = this.timestamp,
        isRead = this.isRead  // isRead alanını ekledik
    )
}
