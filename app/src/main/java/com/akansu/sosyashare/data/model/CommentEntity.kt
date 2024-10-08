package com.akansu.sosyashare.data.model

import java.util.Date
import java.util.UUID

data class CommentEntity(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String? = null,
    val content: String = "",
    val timestamp: Date = Date(),
    val likes: MutableList<String> = mutableListOf()
) {
    constructor() : this(
        id = "",
        postId = "",
        userId = "",
        username = "",
        userProfileUrl = null,
        content = "",
        timestamp = Date(),
        likes = mutableListOf()
    )
}
