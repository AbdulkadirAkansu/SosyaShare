
package com.akansu.sosyashare.data.repository

import com.akansu.sosyashare.data.mapper.toDomainModel
import com.akansu.sosyashare.data.model.PrivateAccountEntity
import com.akansu.sosyashare.data.remote.FirebasePrivateAccountService
import com.akansu.sosyashare.domain.model.PrivateAccount
import com.akansu.sosyashare.domain.repository.PrivateAccountRepository
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject

class PrivateAccountRepositoryImpl @Inject constructor(
    private val service: FirebasePrivateAccountService
) : PrivateAccountRepository {

    override suspend fun getPrivateAccount(userId: String): PrivateAccount? {
        return service.getPrivateAccount(userId)?.toDomainModel()
    }

    override suspend fun updatePrivateAccount(privateAccount: PrivateAccount) {
        val entity = PrivateAccountEntity(
            userId = privateAccount.userId,
            isPrivate = privateAccount.isPrivate,
            allowedFollowers = privateAccount.allowedFollowers
        )
        service.updatePrivateAccount(entity)
    }

    override suspend fun updatePrivateAccountWithTransaction(privateAccount: PrivateAccount): Boolean {
        val entity = PrivateAccountEntity(
            userId = privateAccount.userId,
            isPrivate = privateAccount.isPrivate,
            allowedFollowers = privateAccount.allowedFollowers
        )
        return service.updatePrivateAccountWithTransaction(entity)
    }

    override suspend fun updateUserPrivacySetting(userId: String, isPrivate: Boolean) {
        service.updateUserPrivacySetting(userId, isPrivate)
    }

    override fun addUserPrivacySettingListener(userId: String, onPrivacyChanged: (Boolean) -> Unit): ListenerRegistration {
        return service.addUserPrivacySettingListener(userId, onPrivacyChanged)
    }

    override suspend fun addAllowedFollower(userId: String, followerId: String) {
        service.addAllowedFollower(userId, followerId)
    }

    override suspend fun removeAllowedFollower(userId: String, followerId: String) {
        service.removeAllowedFollower(userId, followerId)
    }
}
