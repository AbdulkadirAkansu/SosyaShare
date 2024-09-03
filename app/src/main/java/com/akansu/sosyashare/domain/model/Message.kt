package com.akansu.sosyashare.domain.model

import java.util.Date

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    val replyToMessageId: String? = null,
    val chatId: String = "",
)
