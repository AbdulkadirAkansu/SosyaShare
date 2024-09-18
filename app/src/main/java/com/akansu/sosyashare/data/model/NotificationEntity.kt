package com.akansu.sosyashare.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class NotificationEntity(
    val userId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val senderProfileUrl: String? = null,
    val type: String = "",
    val postId: String? = null,
    val content: String = "",
    @get:PropertyName("isRead") val isRead: Boolean = false,
    val timestamp: Date = Date(),
    var documentId: String = ""
)

