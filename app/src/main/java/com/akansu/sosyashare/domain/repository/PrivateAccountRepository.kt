package com.akansu.sosyashare.domain.repository

import com.akansu.sosyashare.domain.model.PrivateAccount
import com.google.firebase.firestore.ListenerRegistration

interface PrivateAccountRepository {
    suspend fun getPrivateAccount(userId: String): PrivateAccount?
    suspend fun updatePrivateAccount(privateAccount: PrivateAccount)
    suspend fun updatePrivateAccountWithTransaction(privateAccount: PrivateAccount): Boolean
    suspend fun addAllowedFollower(userId: String, followerId: String)
    suspend fun removeAllowedFollower(userId: String, followerId: String)
    suspend fun updateUserPrivacySetting(userId: String, isPrivate: Boolean)
    fun addUserPrivacySettingListener(userId: String, onPrivacyChanged: (Boolean) -> Unit): ListenerRegistration
}


