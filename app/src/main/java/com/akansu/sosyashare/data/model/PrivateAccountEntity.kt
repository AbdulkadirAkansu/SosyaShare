package com.akansu.sosyashare.data.model

data class PrivateAccountEntity(
    val userId: String = "",
    val isPrivate: Boolean = false,
    val allowedFollowers: List<String> = emptyList()
)
