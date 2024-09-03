package com.akansu.sosyashare.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class MessageEntity(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    @get:PropertyName("isRead") val isRead: Boolean = false,
    val replyToMessageId: String? = null, // Yanıtlanan mesajın ID'si, null olabilir
    val chatId: String = "" // Mesajın hangi sohbete ait olduğunu belirlemek için
)
