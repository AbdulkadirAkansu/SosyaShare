package com.akansu.sosyashare.domain.model


import java.util.Date

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val comments: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val bio: String = "",
    val lastUsernameChange: Date? = null,
)
