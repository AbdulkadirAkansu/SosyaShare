package com.akansu.sosyashare.domain.model

import java.util.Date

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Date = Date()
)
