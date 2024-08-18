package com.akansu.sosyashare.domain.model

import java.util.Date

interface BaseComment {
    val id: String
    val userId: String
    val username: String
    val userProfileUrl: String
    val content: String
    val timestamp: Date
    val likes: MutableList<String>
}