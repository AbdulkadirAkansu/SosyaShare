package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.MessageEntity
import com.akansu.sosyashare.domain.model.Message
import android.util.Log

fun MessageEntity.toDomainModel(): Message {
    Log.d("Mapper", "toDomainModel - Converting MessageEntity to Message: $this")
    return Message(
        id = this.id, // ID'nin boş olmadığından emin olun
        senderId = this.senderId,
        receiverId = this.receiverId,
        content = this.content,
        timestamp = this.timestamp,
        isRead = this.isRead
    ).also {
        Log.d("Mapper", "toDomainModel - Resulting Message: $it")
    }
}

fun Message.toEntityModel(): MessageEntity {
    Log.d("Mapper", "toEntityModel - Converting Message to MessageEntity: $this")
    return MessageEntity(
        id = this.id, // Eğer `id` veritabanında üretiliyorsa burada set edilmemeli.
        senderId = this.senderId,
        receiverId = this.receiverId,
        content = this.content,
        timestamp = this.timestamp,
        isRead = this.isRead
    ).also {
        Log.d("Mapper", "toEntityModel - Resulting MessageEntity: $it")
    }
}
