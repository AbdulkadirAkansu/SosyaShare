package com.akansu.sosyashare.data.model


data class UserPrivacyEntity(
    var userId: String = "",
    val isPrivate: Boolean = false,
    val allowedFollowers: List<String> = emptyList()
)


