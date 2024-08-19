package com.akansu.sosyashare.data.model

data class SaveEntity(
    val postId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)
