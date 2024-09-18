package com.akansu.sosyashare.domain.model

import java.util.Date

data class Notification(
    val userId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val senderProfileUrl: String? = null,
    val type: String = "",
    val postId: String? = null,
    val content: String = "",
    val isRead: Boolean = false,
    val timestamp: Date = Date(),
    val documentId: String = ""  // Burada documentId'yi ekliyoruz
)


