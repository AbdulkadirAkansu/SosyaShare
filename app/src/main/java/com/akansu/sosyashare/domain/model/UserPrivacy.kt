
package com.akansu.sosyashare.domain.model

data class UserPrivacy(
    val userId: String = "",
    val isPrivate: Boolean = false,
    val allowedFollowers: List<String> = emptyList()
)
