package com.akansu.sosyashare.data.model

import java.util.Date

data class PostEntity(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val createdAt: Date = Date()
)
