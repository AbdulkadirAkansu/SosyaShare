package com.akansu.sosyashare.domain.model

import java.util.Date
import java.util.UUID

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val userId: String,
    val content: String,
    val timestamp: Date = Date(),
    val likes: MutableList<String> = mutableListOf(),
    val parentCommentId: String? = null, // null ise ana yorum, değilse yanıt
    val replies: MutableList<Comment> = mutableListOf() // Yanıtları tutmak için
)



