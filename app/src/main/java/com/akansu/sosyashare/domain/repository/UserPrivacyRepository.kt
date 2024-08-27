package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.UserPrivacy
import com.google.firebase.firestore.ListenerRegistration

interface UserPrivacyRepository {
    suspend fun getUserPrivacy(userId: String): UserPrivacy?
    suspend fun updateUserPrivacy(userPrivacy: UserPrivacy)
    suspend fun updateUserPrivacySetting(userId: String, isPrivate: Boolean)
    fun addUserPrivacySettingListener(userId: String, onPrivacyChanged: (Boolean) -> Unit): ListenerRegistration
    suspend fun addAllowedFollower(userId: String, followerId: String)
    suspend fun removeAllowedFollower(userId: String, followerId: String)
}
