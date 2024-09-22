package com.akansu.sosyashare.data.model

import java.util.Date
import java.util.UUID

data class ReplyEntity(
    val id: String = UUID.randomUUID().toString(),
    val commentId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val likes: List<String> = emptyList()
) {
    constructor() : this(
        id = "",
        commentId = "",
        userId = "",
        username = "",
        userProfileUrl = "",
        content = "",
        timestamp = Date(),
        likes = emptyList()
    )
}

