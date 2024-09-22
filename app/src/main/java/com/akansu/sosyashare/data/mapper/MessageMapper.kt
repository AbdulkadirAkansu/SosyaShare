package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.MessageEntity
import com.akansu.sosyashare.domain.model.Message
import android.util.Log

fun MessageEntity.toDomainModel(): Message {
    Log.d("Mapper", "toDomainModel - Converting MessageEntity to Message: $this")
    return Message(
        id = this.id,
        senderId = this.senderId,
        receiverId = this.receiverId,
        content = this.content,
        timestamp = this.timestamp,
        isRead = this.isRead,
        replyToMessageId = this.replyToMessageId,
        chatId = this.chatId,
    ).also {
        Log.d("Mapper", "toDomainModel - Resulting Message: $it")
    }
}

fun Message.toEntityModel(): MessageEntity {
    Log.d("Mapper", "toEntityModel - Converting Message to MessageEntity: $this")
    return MessageEntity(
        id = this.id,
        senderId = this.senderId,
        receiverId = this.receiverId,
        content = this.content,
        timestamp = this.timestamp,
        isRead = this.isRead,
        replyToMessageId = this.replyToMessageId,
        chatId = this.chatId,
    ).also {
        Log.d("Mapper", "toEntityModel - Resulting MessageEntity: $it")
    }
}
