package com.akansu.sosyashare.data.model

import java.util.Date
import java.util.UUID

data class CommentEntity(
    val id: String = UUID.randomUUID().toString(),
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val likes: List<String> = emptyList(),
    val replies: List<CommentEntity> = emptyList(), // Yanıtlar artık CommentEntity türünde
    val parentCommentId: String? = null
)
