// PrivateAccountMapper.kt
package com.akansu.sosyashare.data.mapper

import com.akansu.sosyashare.data.model.PrivateAccountEntity
import com.akansu.sosyashare.domain.model.PrivateAccount

fun PrivateAccountEntity.toDomainModel(): PrivateAccount {
    return PrivateAccount(
        userId = userId,
        isPrivate = isPrivate,
        allowedFollowers = allowedFollowers
    )
}
