package com.akansu.sosyashare.data.mapper

import android.util.Log
import com.akansu.sosyashare.data.model.UserPrivacyEntity
import com.akansu.sosyashare.domain.model.UserPrivacy


fun UserPrivacyEntity.toDomainModel(): UserPrivacy {
    return try {
        Log.d("UserPrivacyMapper", "Mapping UserPrivacyEntity to UserPrivacy. Data: $this")

        // Null ve veri tiplerini kontrol et
        val mappedUserPrivacy = UserPrivacy(
            userId = userId ?: run {
                Log.w("UserPrivacyMapper", "userId is null, defaulting to empty string")
                ""
            },
            isPrivate = isPrivate ?: run {
                Log.w("UserPrivacyMapper", "isPrivate is null, defaulting to false")
                false
            },
            allowedFollowers = allowedFollowers ?: run {
                Log.w("UserPrivacyMapper", "allowedFollowers is null, defaulting to empty list")
                emptyList()
            }
        )

        Log.d("UserPrivacyMapper", "Successfully mapped UserPrivacyEntity to UserPrivacy: $mappedUserPrivacy")
        mappedUserPrivacy
    } catch (e: Exception) {
        Log.e("UserPrivacyMapper", "Error mapping UserPrivacyEntity to UserPrivacy: ${e.message}", e)
        throw e // Veya null döndürebilirsiniz ama genelde bu tür hatalar throw edilir
    }
}



