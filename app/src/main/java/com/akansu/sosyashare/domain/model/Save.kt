package com.akansu.sosyashare.domain.model


data class Save(
    val postId: String,
    val userId: String,
    val savedAt: Long
)
