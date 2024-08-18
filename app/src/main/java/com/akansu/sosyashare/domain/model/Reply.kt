package com.akansu.sosyashare.domain.model

import java.util.Date
import java.util.UUID

data class Reply(
    override val id: String = UUID.randomUUID().toString(),
    val commentId: String,
    override val userId: String,
    override val username: String,
    override val userProfileUrl: String,
    override val content: String,
    override val timestamp: Date = Date(),
    override val likes: MutableList<String> = mutableListOf()
) : BaseComment
