
package com.akansu.sosyashare.domain.model

data class PrivateAccount(
    val userId: String,
    val isPrivate: Boolean,
    val allowedFollowers: List<String>
)
