package com.akansu.sosyashare.data.model

import java.util.Date

data class MessageEntity(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false
)
