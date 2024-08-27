package com.akansu.sosyashare.data.mapper

import android.util.Log
import com.akansu.sosyashare.data.model.UserPrivacyEntity
import com.akansu.sosyashare.domain.model.UserPrivacy

fun UserPrivacyEntity.toDomainModel(): UserPrivacy {
    Log.d("UserPrivacyMapper", "Mapping UserPrivacyEntity to UserPrivacy. isPrivate: $isPrivate")
    return UserPrivacy(
        userId = userId,
        isPrivate = isPrivate,
        allowedFollowers = allowedFollowers
    )
}
